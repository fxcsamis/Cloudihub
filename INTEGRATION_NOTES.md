# CloudeHub UI + Metadata Resolver Integration

This merged package contains:

- Android Jetpack Compose UI from `fxcsamis/Ui`
- Updated Media Download Hub with:
  - same card/thumbnail visual style
  - search bar for supported source directory
  - refresh button
  - online logos/favicons loaded by Coil
  - API-driven extraction flow for compliant media links
- Vercel FastAPI backend in `/backend`

## Backend deploy

Deploy the `backend/` folder to Vercel. Endpoint used by Android:

```text
GET /api/extract?url=...
GET /api/sites
```

## Android config

Open:

```text
app/src/main/java/com/example/ui/CloudihubViewModel.kt
```

Change:

```kotlin
var mediaResolverBaseUrl by mutableStateOf("https://your-cloudehub-resolver.vercel.app")
```

to your deployed Vercel URL.

## Compliance

The resolver is designed for personal media, public-domain, Creative Commons, and explicitly downloadable media. It keeps a safety blocklist for high-risk ad/DRM/watermark/access-control platforms.
