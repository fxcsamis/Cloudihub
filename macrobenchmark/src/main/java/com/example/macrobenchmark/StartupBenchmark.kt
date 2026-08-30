package com.example.macrobenchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Automated, on-device performance measurements that run on every CI build
 * (see .github/workflows/android.yml's `benchmark` job). This replaces having
 * to manually screen-record the app and eyeball the debug overlay for every
 * change - these numbers come out as a JSON artifact on each run instead.
 *
 * Must match the app's actual applicationId (app/build.gradle.kts).
 */
private const val TARGET_PACKAGE = "com.cloudihub.app"

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Cold-start timing: how long from tapping the icon to the first frame
     * being drawn. Directly measures whether startup work (like the old
     * dead-API retry loop we removed) is adding delay before the app is even
     * visible.
     */
    @Test
    fun coldStartup() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
    }

    /**
     * Frame timing while flinging the Home video feed - this is the direct,
     * automated equivalent of the manual "scroll Home and watch the debug
     * overlay's FPS/jank numbers" test. Reports frame duration percentiles
     * (P50/P90/P95/P99) so a regression (e.g. a re-introduced expensive
     * per-item shadow) shows up as a number going up in CI, not just a "it
     * feels laggy" report.
     */
    @Test
    fun scrollHomeFeed() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 3,
        startupMode = StartupMode.WARM
    ) {
        startActivityAndWait()

        // Give the (now-instant, local-only) home feed a moment to compose,
        // then find the scrollable video list and fling it a few times -
        // mirroring what a person scrolling the feed by hand actually does.
        // Kept short: a longer scrolling session produces a much bigger
        // Perfetto trace, and on Firebase Test Lab that was taking so long
        // to process that frame-duration metrics silently timed out,
        // leaving only a bare frameCount in the results.
        device.waitForIdle()
        val list = device.wait(Until.findObject(By.scrollable(true)), 5_000)
        if (list != null) {
            repeat(3) {
                list.fling(Direction.DOWN)
                device.waitForIdle()
            }
            repeat(2) {
                list.fling(Direction.UP)
                device.waitForIdle()
            }
        }
    }
}
