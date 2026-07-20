"""
CloudeHub Personal Media Metadata Resolver API

Compliant serverless resolver for user-owned, public-domain, Creative Commons,
and otherwise explicitly downloadable media sources.

Important safety rules implemented here:
- This API never downloads, transcodes, stores, or proxies video/audio bytes.
- It only performs metadata extraction and returns stream URLs for the mobile app.
- Major social/video platforms known for ads, DRM, watermarking, and restrictive
  Terms of Service are blocked by default.
- There is no allowlist requirement; compliant public/user-owned media sources
  can be resolved without pre-registering their domains.
"""

from __future__ import annotations

import os
import re
from typing import Any, Dict, Iterable, List, Optional
from urllib.parse import urlparse

import yt_dlp
from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

APP_NAME = "CloudeHub Metadata Resolver"
APP_VERSION = "1.0.0"

# These are intentionally blocked by default because direct stream extraction from
# these platforms can bypass ads, watermarking, access controls, or platform ToS.
DEFAULT_BLOCKED_DOMAINS = {
    "youtube.com",
    "www.youtube.com",
    "m.youtube.com",
    "youtu.be",
    "music.youtube.com",
    "tiktok.com",
    "www.tiktok.com",
    "vm.tiktok.com",
    "instagram.com",
    "www.instagram.com",
    "facebook.com",
    "www.facebook.com",
    "fb.watch",
    "x.com",
    "twitter.com",
    "vimeo.com",
    "dailymotion.com",
}


def _csv_env_set(name: str) -> set[str]:
    raw = os.getenv(name, "")
    return {item.strip().lower() for item in raw.split(",") if item.strip()}


BLOCKED_DOMAINS = DEFAULT_BLOCKED_DOMAINS | _csv_env_set("BLOCKED_MEDIA_DOMAINS")

app = FastAPI(
    title=APP_NAME,
    version=APP_VERSION,
    description=(
        "Metadata-only resolver for personal, public-domain, Creative Commons, "
        "and explicitly downloadable media. No video/audio proxying. "
        "No allowlist is required, but high-risk domains remain blocked."
    ),
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[origin.strip() for origin in os.getenv("CORS_ALLOW_ORIGINS", "*").split(",")],
    allow_credentials=False,
    allow_methods=["GET", "OPTIONS"],
    allow_headers=["*"],
)


class ResolutionItem(BaseModel):
    resolution_name: str
    download_url: str


class ExtractResponse(BaseModel):
    id: Optional[str] = Field(default=None)
    title: Optional[str] = Field(default=None)
    thumbnail: Optional[str] = Field(default=None)
    duration: Optional[str] = Field(default=None, description="Duration in seconds as a string")
    views: Optional[str] = Field(default=None, description="Formatted view count")
    video_stream_url: Optional[str] = Field(default=None)
    audio_stream_url: Optional[str] = Field(default=None)
    resolutions: List[ResolutionItem] = Field(default_factory=list)


class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None


@app.exception_handler(HTTPException)
async def http_exception_handler(_: Request, exc: HTTPException) -> JSONResponse:
    detail = exc.detail if isinstance(exc.detail, str) else "Request failed"
    return JSONResponse(
        status_code=exc.status_code,
        content=ErrorResponse(error=detail).model_dump(),
    )


@app.get("/")
def root() -> Dict[str, str]:
    return {
        "name": APP_NAME,
        "version": APP_VERSION,
        "health": "/api/health",
        "extract": "/api/extract?url=https://example.com/media.mp4",
    }


@app.get("/api/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "service": "cloudehub-resolver"}


@app.get("/api/sites")
def supported_sites() -> Dict[str, Any]:
    """
    Return a lightweight directory of yt-dlp extractor display names.

    This endpoint returns names only, not extraction permission. The /api/extract
    safety policy still applies when resolving a specific URL. The Android app
    uses this list for searchable UI cards and online favicon/logo loading.
    """
    try:
        extractors = yt_dlp.extractor.gen_extractors()
        names = sorted({getattr(extractor, "IE_NAME", "").strip() for extractor in extractors if getattr(extractor, "IE_NAME", "").strip()})
    except Exception:
        names = []

    sites = []
    for idx, name in enumerate(names):
        clean_domain_hint = _site_name_to_domain_hint(name)
        sites.append({
            "id": idx + 1,
            "name": name,
            "domain_hint": clean_domain_hint,
            "logo_url": f"https://www.google.com/s2/favicons?domain={clean_domain_hint}&sz=128",
            "category": "yt-dlp extractor",
            "supported_formats": "Metadata / Stream URL",
        })

    return {
        "count": len(sites),
        "sites": sites,
        "note": "Directory only. Extraction is allowed only for personal, public-domain, Creative Commons, or explicitly downloadable media.",
    }


def _site_name_to_domain_hint(name: str) -> str:
    value = re.sub(r"[^a-zA-Z0-9]+", "", name).lower()
    if not value:
        return "example.com"
    if value in {"generic", "commonmistakes"}:
        return "example.com"
    return f"{value}.com"


@app.get(
    "/api/extract",
    response_model=ExtractResponse,
    responses={400: {"model": ErrorResponse}, 403: {"model": ErrorResponse}, 422: {"model": ErrorResponse}, 429: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
)
def extract(
    url: str = Query(..., min_length=8, description="Public URL to user-owned or legally downloadable media"),
) -> ExtractResponse:
    """
    Resolve a legally downloadable media URL into direct stream metadata.

    This endpoint does not download or proxy media. yt-dlp is used in simulate /
    skip-download mode only, and domains are gated by an allowlist.
    """
    normalized_url = _validate_and_normalize_url(url)
    _enforce_domain_policy(normalized_url)

    info = _extract_metadata(normalized_url)
    return _map_info_to_response(info)


def _validate_and_normalize_url(url: str) -> str:
    url = url.strip()

    parsed = urlparse(url)
    if parsed.scheme not in {"http", "https"}:
        raise HTTPException(status_code=400, detail="Only http and https URLs are supported.")
    if not parsed.netloc:
        raise HTTPException(status_code=400, detail="Invalid URL: missing host.")

    return url


def _hostname(url: str) -> str:
    parsed = urlparse(url)
    return (parsed.hostname or "").lower().strip(".")


def _domain_matches(host: str, configured_domain: str) -> bool:
    domain = configured_domain.lower().strip(".")
    return host == domain or host.endswith(f".{domain}")


def _matches_any(host: str, domains: Iterable[str]) -> bool:
    return any(_domain_matches(host, domain) for domain in domains)


def _enforce_domain_policy(url: str) -> None:
    host = _hostname(url)

    if not host:
        raise HTTPException(status_code=400, detail="Invalid URL host.")

    if _matches_any(host, BLOCKED_DOMAINS):
        raise HTTPException(
            status_code=403,
            detail=(
                "This source is blocked by default. CloudeHub supports only user-owned, "
                "public-domain, Creative Commons, or explicitly downloadable sources."
            ),
        )

    # No allowlist check is performed. The caller remains responsible for only
    # submitting user-owned, public-domain, Creative Commons, or explicitly
    # downloadable media URLs.
    return


def _extract_metadata(url: str) -> Dict[str, Any]:
    ydl_opts: Dict[str, Any] = {
        "simulate": True,
        "skip_download": True,
        "quiet": True,
        "no_warnings": True,
        "format": "bestvideo*+bestaudio/best",
        "noplaylist": True,
        "extract_flat": False,
        "socket_timeout": int(os.getenv("YTDLP_SOCKET_TIMEOUT", "10")),
        "retries": int(os.getenv("YTDLP_RETRIES", "1")),
        # Avoid writing cache/files in serverless environments.
        "cachedir": False,
    }

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
    except yt_dlp.utils.DownloadError as exc:
        message = _clean_error(str(exc))
        status = 429 if _looks_rate_limited(message) else 422
        raise HTTPException(status_code=status, detail=message)
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Metadata extraction failed: {_clean_error(str(exc))}")

    if not isinstance(info, dict):
        raise HTTPException(status_code=422, detail="Unable to extract media metadata from this URL.")

    # If a playlist slipped through, choose the first concrete entry.
    if info.get("_type") == "playlist" and info.get("entries"):
        first = next((entry for entry in info["entries"] if isinstance(entry, dict)), None)
        if first:
            info = first

    return info


def _map_info_to_response(info: Dict[str, Any]) -> ExtractResponse:
    formats = [fmt for fmt in info.get("formats", []) if isinstance(fmt, dict)]

    progressive = _best_progressive_format(formats)
    video_only = _best_video_only_format(formats)
    audio_only = _best_audio_only_format(formats)

    video_url: Optional[str] = None
    audio_url: Optional[str] = None

    # Prefer separate high-quality video/audio if both exist, otherwise use a
    # progressive stream for sources that provide a complete MP4/WebM file.
    if video_only and audio_only:
        video_url = video_only.get("url")
        audio_url = audio_only.get("url")
    elif progressive:
        video_url = progressive.get("url")
        audio_url = None
    else:
        direct_url = info.get("url") if isinstance(info.get("url"), str) else None
        video_url = direct_url
        audio_url = None

    if not video_url:
        raise HTTPException(status_code=422, detail="No direct playable stream URL was found.")

    return ExtractResponse(
        id=_string_or_none(info.get("id")),
        title=_string_or_none(info.get("title")),
        thumbnail=_pick_thumbnail(info),
        duration=_format_duration(info.get("duration")),
        views=_format_views(info.get("view_count")),
        video_stream_url=video_url,
        audio_stream_url=audio_url,
        resolutions=_available_resolutions(formats, video_url),
    )


def _available_resolutions(formats: List[Dict[str, Any]], fallback_url: Optional[str]) -> List[ResolutionItem]:
    """Return clean progressive download choices for the Android selector.

    We prefer complete audio+video formats because a single `download_url` can be
    downloaded/played directly by Android DownloadManager/Media3. If a source
    only exposes one direct URL, include that as `Best`. No media bytes are
    downloaded or proxied here.
    """
    seen: set[str] = set()
    candidates: List[Dict[str, Any]] = []

    for fmt in formats:
        url = fmt.get("url")
        if (
            _is_http_url(url)
            and fmt.get("vcodec") not in {None, "none"}
            and fmt.get("acodec") not in {None, "none"}
        ):
            candidates.append(fmt)

    candidates = sorted(
        candidates,
        key=lambda fmt: (
            _safe_number(fmt.get("height")),
            _safe_number(fmt.get("width")),
            _safe_number(fmt.get("tbr")),
        ),
        reverse=True,
    )

    output: List[ResolutionItem] = []
    for fmt in candidates:
        url = fmt.get("url")
        if not isinstance(url, str) or url in seen:
            continue
        seen.add(url)
        height = int(_safe_number(fmt.get("height")))
        width = int(_safe_number(fmt.get("width")))
        ext = _string_or_none(fmt.get("ext")) or "media"
        if height > 0:
            label = f"{height}p"
        elif width > 0:
            label = f"{width}w"
        else:
            label = "Best"
        output.append(ResolutionItem(resolution_name=f"{label} • {ext.upper()}", download_url=url))

    if not output and fallback_url:
        output.append(ResolutionItem(resolution_name="Best available", download_url=fallback_url))

    return output[:12]


def _best_progressive_format(formats: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    candidates = [
        fmt
        for fmt in formats
        if fmt.get("url")
        and fmt.get("vcodec") not in {None, "none"}
        and fmt.get("acodec") not in {None, "none"}
        and _is_http_url(fmt.get("url"))
    ]
    return _max_by_quality(candidates)


def _best_video_only_format(formats: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    candidates = [
        fmt
        for fmt in formats
        if fmt.get("url")
        and fmt.get("vcodec") not in {None, "none"}
        and fmt.get("acodec") in {None, "none"}
        and _is_http_url(fmt.get("url"))
    ]
    return _max_by_quality(candidates)


def _best_audio_only_format(formats: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    candidates = [
        fmt
        for fmt in formats
        if fmt.get("url")
        and fmt.get("vcodec") in {None, "none"}
        and fmt.get("acodec") not in {None, "none"}
        and _is_http_url(fmt.get("url"))
    ]
    return max(
        candidates,
        key=lambda fmt: (
            _safe_number(fmt.get("abr")),
            _safe_number(fmt.get("tbr")),
            _safe_number(fmt.get("filesize")),
            _safe_number(fmt.get("filesize_approx")),
        ),
        default=None,
    )


def _max_by_quality(candidates: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    return max(
        candidates,
        key=lambda fmt: (
            _safe_number(fmt.get("height")),
            _safe_number(fmt.get("width")),
            _safe_number(fmt.get("fps")),
            _safe_number(fmt.get("tbr")),
            _safe_number(fmt.get("filesize")),
            _safe_number(fmt.get("filesize_approx")),
        ),
        default=None,
    )


def _pick_thumbnail(info: Dict[str, Any]) -> Optional[str]:
    thumbnail = info.get("thumbnail")
    if isinstance(thumbnail, str) and thumbnail:
        return thumbnail

    thumbnails = info.get("thumbnails")
    if isinstance(thumbnails, list):
        valid = [thumb for thumb in thumbnails if isinstance(thumb, dict) and thumb.get("url")]
        best = max(valid, key=lambda thumb: _safe_number(thumb.get("height")) * _safe_number(thumb.get("width")), default=None)
        if best:
            return best.get("url")

    return None


def _format_duration(value: Any) -> Optional[str]:
    if value is None:
        return None
    try:
        seconds = int(float(value))
        return str(seconds)
    except (TypeError, ValueError):
        return str(value)


def _format_views(value: Any) -> Optional[str]:
    if value is None:
        return None
    try:
        return f"{int(value):,}"
    except (TypeError, ValueError):
        return str(value)


def _string_or_none(value: Any) -> Optional[str]:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def _safe_number(value: Any) -> float:
    try:
        if value is None:
            return 0.0
        return float(value)
    except (TypeError, ValueError):
        return 0.0


def _is_http_url(value: Any) -> bool:
    return isinstance(value, str) and value.startswith(("http://", "https://"))


def _looks_rate_limited(message: str) -> bool:
    return bool(re.search(r"rate.?limit|too many requests|http error 429|captcha", message, re.I))


def _clean_error(message: str) -> str:
    message = re.sub(r"\x1b\[[0-9;]*m", "", message)
    message = message.replace("ERROR:", "").strip()
    return message[:500] if message else "Unknown extraction error"
