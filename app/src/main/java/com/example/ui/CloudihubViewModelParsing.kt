package com.example.ui

// Pure parsing/formatting helpers used by CloudihubViewModel's video-loading
// functions (loadHybridFeed, performPipedSearch, extractStreamAndPreparePlayer,
// prepareRelatedVideosFromApis). Moved out into extension functions on
// CloudihubViewModel since none of them touch the class's private state - they
// only take parameters and return values, so this is purely a file-size/
// organization split with no behaviour change. Call sites inside the ViewModel
// are unaffected: Kotlin resolves e.g. formatDuration(x) called from inside the
// class to this extension the same way it resolved the old private member.

fun CloudihubViewModel.parsePipedSearchJson(jsonStr: String): List<CloudVideo> {
    val list = mutableListOf<CloudVideo>()
    try {
        val json = org.json.JSONObject(jsonStr)
        val itemsArray = json.optJSONArray("items") ?: return list
        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            if (item.optString("type", "") != "stream") continue
            
            val url = item.optString("url", "")
            val videoId = item.optString("videoId", url.substringAfter("v=", ""))
            if (videoId.isEmpty()) continue
            
            val title = item.optString("title", "YouTube Video")
            val durationSecs = item.optInt("duration", 0)
            val durationStr = formatDuration(durationSecs)
            val creator = item.optString("uploaderName", item.optString("uploader", "Unknown Creator"))
            val thumbnail = item.optString("thumbnail", item.optString("thumbnailUrl", "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600"))
            val viewsCount = item.optLong("views", 0)
            val viewsStr = formatViews(viewsCount)
            
            list.add(
                CloudVideo(
                    id = videoId,
                    title = title,
                    duration = durationStr,
                    creator = creator,
                    imageUrl = thumbnail,
                    views = viewsStr,
                    fileUrl = "https://www.youtube.com/watch?v=$videoId",
                    sizeMb = 35.0 + (i % 10) * 4.5
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun CloudihubViewModel.parsePipedTrendingJson(jsonStr: String): List<CloudVideo> {
    val list = mutableListOf<CloudVideo>()
    try {
        val jsonArray = org.json.JSONArray(jsonStr)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val url = item.optString("url", "")
            val videoId = item.optString("videoId", url.substringAfter("v=", ""))
            if (videoId.isEmpty()) continue
            
            val title = item.optString("title", "YouTube Video")
            val durationSecs = item.optInt("duration", 0)
            val durationStr = formatDuration(durationSecs)
            val creator = item.optString("uploaderName", item.optString("uploader", "Unknown Creator"))
            val thumbnail = item.optString("thumbnail", item.optString("thumbnailUrl", "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600"))
            val viewsCount = item.optLong("views", 0)
            val viewsStr = formatViews(viewsCount)
            
            list.add(
                CloudVideo(
                    id = videoId,
                    title = title,
                    duration = durationStr,
                    creator = creator,
                    imageUrl = thumbnail,
                    views = viewsStr,
                    fileUrl = "https://www.youtube.com/watch?v=$videoId",
                    sizeMb = 35.0 + (i % 10) * 4.5
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun CloudihubViewModel.formatDuration(seconds: Int): String {
    if (seconds <= 0) return "0:00"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}

fun CloudihubViewModel.formatViews(views: Long): String {
    return when {
        views >= 1_000_000 -> String.format("%.1fM views", views / 1_000_000.0)
        views >= 1_000 -> String.format("%.1fK views", views / 1_000.0)
        else -> "$views views"
    }
}

fun CloudihubViewModel.getLocalFallbackVideos(): List<CloudVideo> {
    return listOf(
        CloudVideo(
            id = "ocean_clip",
            title = "Deep Ocean Scenic Exploration [Aesthetics]",
            duration = "0:30",
            creator = "VideoJS Ocean Labs",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
            views = "24.5M views",
            fileUrl = "https://vjs.zencdn.net/v/oceans.mp4",
            sizeMb = 21.9
        ),
        CloudVideo(
            id = "sintel_trailer",
            title = "Sintel Movie Official HD Trailer [Edge Gaming]",
            duration = "0:52",
            creator = "Blender Foundation",
            imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            views = "118.2M views",
            fileUrl = "https://media.w3.org/2010/05/sintel/trailer_hd.mp4",
            sizeMb = 13.9
        ),
        CloudVideo(
            id = "bunny_trailer",
            title = "Big Buck Bunny Official HD Trailer [Aesthetics]",
            duration = "0:32",
            creator = "Peach Open Movie",
            imageUrl = "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600",
            views = "89.4M views",
            fileUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4",
            sizeMb = 10.5
        ),
        CloudVideo(
            id = "w3_bunny",
            title = "Peach Project - Big Buck Bunny 10s Clip [Infrastructure]",
            duration = "0:10",
            creator = "W3Schools Media",
            imageUrl = "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=600",
            views = "45.1M views",
            fileUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            sizeMb = 0.8
        ),
        CloudVideo(
            id = "sample_mp4",
            title = "Learning Container Sample Demonstration [Sky Timelapse]",
            duration = "0:25",
            creator = "Container Corp",
            imageUrl = "https://images.unsplash.com/photo-1557672172-298e090bd0f1?w=600",
            views = "12.3M views",
            fileUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            sizeMb = 10.1
        ),
        CloudVideo(
            id = "w3_bear",
            title = "Bear Fishing Wild Stream Clip [Rainclouds]",
            duration = "0:04",
            creator = "W3Schools Wild",
            imageUrl = "https://images.unsplash.com/photo-1530595467537-0b5996c41f2d?w=600",
            views = "31.2M views",
            fileUrl = "https://www.w3schools.com/html/movie.mp4",
            sizeMb = 0.5
        )
    )
}

fun CloudihubViewModel.extractYoutubeVideoId(url: String): String {
    return try {
        if (url.contains("youtu.be/")) {
            url.substringAfter("youtu.be/").substringBefore("?").substringBefore("/")
        } else if (url.contains("v=")) {
            url.substringAfter("v=").substringBefore("&").substringBefore("/")
        } else if (url.contains("embed/")) {
            url.substringAfter("embed/").substringBefore("?").substringBefore("/")
        } else if (url.contains("shorts/")) {
            url.substringAfter("shorts/").substringBefore("?").substringBefore("/")
        } else ""
    } catch (e: Exception) {
        ""
    }
}

fun CloudihubViewModel.parsePipedStreamUrl(jsonStr: String): String {
    try {
        val json = org.json.JSONObject(jsonStr)
        val videoStreams = json.optJSONArray("videoStreams")
        if (videoStreams != null && videoStreams.length() > 0) {
            for (i in 0 until videoStreams.length()) {
                val stream = videoStreams.getJSONObject(i)
                if (!stream.optBoolean("videoOnly", false)) {
                    val url = stream.optString("url", "")
                    if (url.isNotEmpty()) return url
                }
            }
            val firstUrl = videoStreams.getJSONObject(0).optString("url", "")
            if (firstUrl.isNotEmpty()) return firstUrl
        }
        val hlsUrl = json.optString("hls", "")
        if (hlsUrl.isNotEmpty()) return hlsUrl
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun CloudihubViewModel.parsePipedRelatedStreams(jsonStr: String): List<CloudVideo> {
    val list = mutableListOf<CloudVideo>()
    try {
        val json = org.json.JSONObject(jsonStr)
        val relatedArray = json.optJSONArray("relatedStreams")
        if (relatedArray != null) {
            for (i in 0 until relatedArray.length()) {
                val item = relatedArray.getJSONObject(i)
                val url = item.optString("url", "")
                val videoId = item.optString("videoId", url.substringAfter("v=", ""))
                if (videoId.isEmpty()) continue
                
                val title = item.optString("title", "Related Video")
                val durationSecs = item.optInt("duration", 0)
                val durationStr = formatDuration(durationSecs)
                val creator = item.optString("uploaderName", item.optString("uploader", "Unknown Creator"))
                val thumbnail = item.optString("thumbnail", item.optString("thumbnailUrl", "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=400"))
                val viewsCount = item.optLong("views", 0)
                val viewsStr = formatViews(viewsCount)
                
                list.add(
                    CloudVideo(
                        id = videoId,
                        title = title,
                        duration = durationStr,
                        creator = creator,
                        imageUrl = thumbnail,
                        views = viewsStr,
                        fileUrl = "https://www.youtube.com/watch?v=$videoId",
                        sizeMb = 25.0 + (i % 5) * 5.5
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

