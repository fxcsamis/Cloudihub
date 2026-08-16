package com.example.ui

import android.app.Application

// Search-history persistence (recent search chips shown on the Home screen).
// Only touches the public `recentSearches` state + SharedPreferences, so this
// is a pure file-organization split - same behaviour, smaller main file.

fun CloudihubViewModel.loadSearchHistory() {
    val spSearch = getApplication<Application>().getSharedPreferences("search_prefs", android.content.Context.MODE_PRIVATE)
    val savedSet = spSearch.getStringSet("history", null)
    recentSearches = if (savedSet != null) {
        savedSet.toList()
    } else {
        listOf("Rainclouds", "Storm tracker", "Space timelapse", "Sky view", "Thunderstorm", "Rainbow")
    }
}

fun CloudihubViewModel.addSearchQueryToHistory(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    val updated = (listOf(trimmed) + recentSearches.filter { !it.equals(trimmed, ignoreCase = true) }).take(20)
    recentSearches = updated
    persistSearchHistory(updated)
}

fun CloudihubViewModel.removeSearchQueryFromHistory(query: String) {
    val updated = recentSearches.filter { !it.equals(query, ignoreCase = true) }
    recentSearches = updated
    persistSearchHistory(updated)
}

fun CloudihubViewModel.clearSearchHistory() {
    recentSearches = emptyList()
    persistSearchHistory(emptyList())
}

fun CloudihubViewModel.persistSearchHistory(list: List<String>) {
    val spSearch = getApplication<Application>().getSharedPreferences("search_prefs", android.content.Context.MODE_PRIVATE)
    spSearch.edit().putStringSet("history", list.toSet()).apply()
}
