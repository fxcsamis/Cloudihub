package com.example.debug

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lightweight in-memory + persisted debug log for the on-screen performance
 * overlay. Two jobs:
 *
 * 1. Any part of the app can call [DebugLog.log] to append a line (e.g. from
 *    a catch block) - the overlay shows the most recent entries live, so you
 *    can see exactly what's happening when the stutter occurs instead of
 *    guessing from a phone screenshot after the fact.
 * 2. [installCrashHandler] wraps the app's crash handler so that if the app
 *    DOES crash, the exception + a short stack trace gets written to
 *    SharedPreferences before the crash finishes - which survives the
 *    process dying and restarting, so the overlay can show "last crash was:
 *    ..." the next time you open the app, even though the crash itself
 *    closed the app before you could screenshot anything.
 */
object DebugLog {
    private const val PREFS_NAME = "cloudihub_debug_log"
    private const val KEY_LAST_CRASH = "last_crash"
    private const val KEY_LAST_CRASH_TIME = "last_crash_time"

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    private val _entries = MutableStateFlow<List<String>>(emptyList())
    val entries: StateFlow<List<String>> = _entries.asStateFlow()

    private val _lastCrash = MutableStateFlow<String?>(null)
    val lastCrash: StateFlow<String?> = _lastCrash.asStateFlow()

    /** Append a line to the live debug log (kept to the most recent 60 entries). */
    fun log(message: String) {
        val stamped = "${timeFormat.format(Date())}  $message"
        _entries.value = (_entries.value + stamped).takeLast(60)
    }

    /** Reads any crash saved from a previous run and clears the live entry list. */
    fun loadPersistedCrash(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LAST_CRASH, null)
        val savedTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0L)
        if (saved != null && savedTime > 0) {
            val whenStr = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US).format(Date(savedTime))
            _lastCrash.value = "Crashed at $whenStr:\n$saved"
        }
    }

    /** Clears the persisted crash record (call from the overlay's "Dismiss" button). */
    fun clearPersistedCrash(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_CRASH)
            .remove(KEY_LAST_CRASH_TIME)
            .apply()
        _lastCrash.value = null
    }

    /**
     * Installs a wrapper around the app's existing uncaught-exception handler.
     * Persists the crash to SharedPreferences (survives the process restart)
     * then hands off to the original handler so normal crash behaviour
     * (app closing, any existing crash reporting) still happens unchanged.
     */
    fun installCrashHandler(context: Context) {
        val appContext = context.applicationContext
        val existingHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val trace = throwable.stackTraceToString().take(2000)
                val message = "${throwable::class.java.simpleName}: ${throwable.message}\n$trace"
                appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LAST_CRASH, message)
                    .putLong(KEY_LAST_CRASH_TIME, System.currentTimeMillis())
                    .apply()
            } catch (_: Exception) {
                // Never let crash-logging itself throw and mask the real crash.
            }
            existingHandler?.uncaughtException(thread, throwable)
        }
    }
}
