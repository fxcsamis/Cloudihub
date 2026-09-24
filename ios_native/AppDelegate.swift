import UIKit
import Flutter
import AVFoundation

// Companion to android_native/MainActivity.kt — same two channel names,
// same idea: one continuous, echo-cancelled mic tap in real voice-call
// mode, so speaking doesn't require muting the mic (no "click on/off").
// Merge the relevant pieces into your actual AppDelegate.swift.
@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    private let methodChannelName = "arkitak/audio_session"
    private let eventChannelName = "arkitak/audio_level"

    private var audioEngine: AVAudioEngine?
    private var eventSink: FlutterEventSink?

    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)

        let controller = window?.rootViewController as! FlutterViewController

        let methodChannel = FlutterMethodChannel(name: methodChannelName, binaryMessenger: controller.binaryMessenger)
        methodChannel.setMethodCallHandler { [weak self] call, result in
            switch call.method {
            case "start":
                do {
                    try self?.startDuplexSession()
                    result(nil)
                } catch {
                    result(FlutterError(code: "START_FAILED", message: error.localizedDescription, details: nil))
                }
            case "stop":
                self?.stopDuplexSession()
                result(nil)
            default:
                result(FlutterMethodNotImplemented)
            }
        }

        let eventChannel = FlutterEventChannel(name: eventChannelName, binaryMessenger: controller.binaryMessenger)
        eventChannel.setStreamHandler(DuplexAudioStreamHandler(delegate: self))

        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }

    func startDuplexSession() throws {
        if audioEngine != nil { return } // already running, no-op

        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playAndRecord, mode: .voiceChat, options: [.allowBluetooth, .defaultToSpeaker])
        try session.setActive(true)

        let engine = AVAudioEngine()
        let input = engine.inputNode
        // Engages the built-in voice-processing unit (AEC/AGC/NS) — the
        // actual "real call" switch on iOS, same role as
        // AudioSource.VOICE_COMMUNICATION + AcousticEchoCanceler on Android.
        try input.setVoiceProcessingEnabled(true)

        let format = input.outputFormat(forBus: 0)
        input.installTap(onBus: 0, bufferSize: 1024, format: format) { [weak self] buffer, _ in
            guard let channelData = buffer.floatChannelData?[0] else { return }
            let frameLength = Int(buffer.frameLength)
            var sum: Float = 0
            for i in 0..<frameLength {
                let sample = channelData[i]
                sum += sample * sample
            }
            let rms = sqrt(sum / Float(max(frameLength, 1)))
            let level = min(max(rms * 6.0, 0.0), 1.0) // same rough scaling as Android
            DispatchQueue.main.async {
                self?.eventSink?(Double(level))
            }
        }

        try engine.start()
        audioEngine = engine
    }

    func stopDuplexSession() {
        audioEngine?.inputNode.removeTap(onBus: 0)
        audioEngine?.stop()
        audioEngine = nil
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }

    func setEventSink(_ sink: FlutterEventSink?) {
        eventSink = sink
    }
}

private class DuplexAudioStreamHandler: NSObject, FlutterStreamHandler {
    weak var delegate: AppDelegate?
    init(delegate: AppDelegate) { self.delegate = delegate }

    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        delegate?.setEventSink(events)
        return nil
    }

    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        delegate?.setEventSink(nil)
        return nil
    }
}
