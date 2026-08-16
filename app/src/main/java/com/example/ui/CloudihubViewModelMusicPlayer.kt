package com.example.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay

// Music mini-player controls (play/pause/next/previous + the 1s progress ticker
// coroutine). musicTickerJob was changed from `private` to `internal` on the main
// class so these extension functions can still cancel/reassign it - same
// visibility as before from every other file in this app module, just not from
// outside the module (which doesn't apply here). No behaviour change.

fun CloudihubViewModel.togglePlayPause() {
    isPlaying = !isPlaying
    if (isPlaying) {
        startMusicTicker()
    } else {
        stopMusicTicker()
    }
}

fun CloudihubViewModel.playTrack(track: CloudMusicTrack) {
    currentTrack = track
    currentTrackProgressSec = 0
    isPlaying = true
    startMusicTicker()
    addHistoryItem("Music", track.title, track.artist)
}

fun CloudihubViewModel.nextTrack() {
    val index = musicTracks.indexOf(currentTrack)
    val nextIndex = (index + 1) % musicTracks.size
    playTrack(musicTracks[nextIndex])
}

fun CloudihubViewModel.previousTrack() {
    val index = musicTracks.indexOf(currentTrack)
    val prevIndex = if (index - 1 < 0) musicTracks.size - 1 else index - 1
    playTrack(musicTracks[prevIndex])
}

fun CloudihubViewModel.startMusicTicker() {
    musicTickerJob?.cancel()
    musicTickerJob = viewModelScope.launch {
        while (isPlaying) {
            delay(1000)
            if (currentTrackProgressSec >= currentTrack.durationSec) {
                nextTrack()
            } else {
                currentTrackProgressSec++
            }
        }
    }
}

fun CloudihubViewModel.stopMusicTicker() {
    musicTickerJob?.cancel()
}
