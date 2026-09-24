package com.arkitak.app
// IMPORTANT: replace the package above with your actual applicationId
// (must match android/app/build.gradle's `applicationId`, and this file's
// path under android/app/src/main/kotlin/... must match that package).
// Then merge this class into your real MainActivity.kt.

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlin.concurrent.thread
import kotlin.math.sqrt

/**
 * Always-on, echo-cancelled mic session — the "real phone call" audio mode
 * the Dart side (lib/audio/duplex_audio_session.dart) talks to. The two
 * switches that actually matter here:
 *   1. AudioManager.MODE_IN_COMMUNICATION + AudioSource.VOICE_COMMUNICATION
 *      — routes through the voice-call audio path instead of plain media
 *      recording.
 *   2. AcousticEchoCanceler/NoiseSuppressor/AutomaticGainControl attached
 *      to that record session — so the mic doesn't pick up the phone's own
 *      TTS output and feed back or force an artificial mute-while-speaking.
 * Both are what stop this from being the "Google Assistant click on/off"
 * feel and make it behave like an actual call.
 */
class MainActivity : FlutterActivity() {
    private val methodChannelName = "arkitak/audio_session"
    private val eventChannelName = "arkitak/audio_level"

    private var audioRecord: AudioRecord? = null
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var agc: AutomaticGainControl? = null
    private var recordingThread: Thread? = null
    @Volatile private var keepRecording = false
    private var eventSink: EventChannel.EventSink? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, methodChannelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "start" -> try {
                        startDuplexSession()
                        result.success(null)
                    } catch (e: Exception) {
                        result.error("START_FAILED", e.message, null)
                    }
                    "stop" -> {
                        stopDuplexSession()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, eventChannelName)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, sink: EventChannel.EventSink?) {
                    eventSink = sink
                }
                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            })
    }

    private fun startDuplexSession() {
        if (audioRecord != null) return // already running, no-op

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = if (minBufferSize > 0) minBufferSize * 2 else sampleRate

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate, channelConfig, audioFormat, bufferSize
        )
        audioRecord = record

        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(record.audioSessionId)?.apply { enabled = true }
        }
        if (NoiseSuppressor.isAvailable()) {
            ns = NoiseSuppressor.create(record.audioSessionId)?.apply { enabled = true }
        }
        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(record.audioSessionId)?.apply { enabled = true }
        }

        record.startRecording()
        keepRecording = true
        recordingThread = thread(start = true, name = "ArkitakDuplexAudio") {
            val buffer = ShortArray(bufferSize / 2)
            while (keepRecording) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    var sum = 0.0
                    for (i in 0 until read) {
                        val sample = buffer[i].toDouble()
                        sum += sample * sample
                    }
                    val rms = sqrt(sum / read)
                    // 16-bit PCM full scale is 32767; speech rarely gets
                    // close to that, so ×6 keeps normal talking in a
                    // usable 0..1 range. Tune on real hardware if it reads
                    // too quiet/hot.
                    val level = (rms / 32767.0 * 6.0).coerceIn(0.0, 1.0)
                    mainHandler.post { eventSink?.success(level) }
                }
            }
        }
    }

    private fun stopDuplexSession() {
        keepRecording = false
        recordingThread?.join(200)
        recordingThread = null
        aec?.release(); aec = null
        ns?.release(); ns = null
        agc?.release(); agc = null
        audioRecord?.apply { stop(); release() }
        audioRecord = null
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    override fun onDestroy() {
        stopDuplexSession()
        super.onDestroy()
    }
}
