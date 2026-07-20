# CloudeHub Personal Media Metadata Resolver API

A lightweight FastAPI backend for Vercel that resolves metadata for user-owned, public-domain, Creative Commons, and explicitly downloadable media links.

## Files

- `requirements.txt`
- `vercel.json`
- `api/index.py`

## Local run

```bash
pip install -r requirements.txt
uvicorn api.index:app --reload --host 0.0.0.0 --port 8000
```

Test:

```bash
curl "http://localhost:8000/api/health"
curl "http://localhost:8000/api/extract?url=https%3A%2F%2Farchive.org%2Fdetails%2Fexample"
```

## Vercel environment variables

Optional:

```text
ALLOWED_MEDIA_DOMAINS=archive.org,commons.wikimedia.org,cdn.yourdomain.com
BLOCKED_MEDIA_DOMAINS=example-blocked.com
CORS_ALLOW_ORIGINS=*
YTDLP_SOCKET_TIMEOUT=10
YTDLP_RETRIES=1
```

## Compliance note

This backend intentionally does not support direct extraction from platforms where doing so could bypass ads, watermarking, DRM, access controls, or platform Terms of Service. It does not download, proxy, store, or process media bytes.
