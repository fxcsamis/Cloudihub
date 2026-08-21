package com.example.ui.components

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlinx.coroutines.CancellationException

/**
 * A [FlingBehavior] that replicates the exponential-decay fling curve iOS uses
 * (and that the FluidRecyclerView / fluid-scroll library ports to Android),
 * instead of the platform-default spline-based deceleration. This is the same
 * formula and DECELERATION_RATE_NORMAL constant (0.998) as that library's
 * `FluidScroller` class - re-implemented here natively for Compose's
 * `LazyColumn`/`LazyRow`, since the original library is a fork of the View-
 * system's `RecyclerView` and can't be dropped into a Compose screen directly.
 *
 * velocity(t) = v0 * rate^t   (t in milliseconds)
 * offset(t)   = v0 * (1 / ln(rate)) * (rate^t - 1)
 *
 * The lower `rate` is (closer to 1 = slower decay), the longer and "glidier"
 * the fling feels - 0.998 is what gives iOS/this library its long, gentle
 * coast compared to Android's default, snappier fling.
 */
class IosStyleFlingBehavior(
    private val decelerationRate: Float = DECELERATION_RATE_NORMAL,
    private val velocityThresholdPxPerMs: Float = 0.01f
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        // Compose gives velocity in px/second; the ported formula (and the
        // reference library) operates in px/millisecond.
        val v0 = initialVelocity / 1000f
        if (abs(v0) < velocityThresholdPxPerMs) {
            return initialVelocity
        }

        var startTimeNanos = -1L
        var previousOffset = 0f
        var currentVelocity = v0
        val invLnRate = 1f / ln(decelerationRate)

        try {
            while (abs(currentVelocity) > velocityThresholdPxPerMs) {
                withFrameNanos { frameTimeNanos ->
                    if (startTimeNanos < 0L) startTimeNanos = frameTimeNanos
                    val elapsedMs = (frameTimeNanos - startTimeNanos) / 1_000_000f

                    val offset = v0 * invLnRate * (decelerationRate.pow(elapsedMs) - 1f)
                    val delta = offset - previousOffset
                    previousOffset = offset
                    currentVelocity = v0 * decelerationRate.pow(elapsedMs)

                    val consumed = scrollBy(delta)
                    // If we've hit the start/end of the list, the scroll can't
                    // consume the full delta any more - stop the fling instead
                    // of continuing to compute offsets against a wall.
                    if (abs(consumed) < abs(delta) - 0.5f) {
                        currentVelocity = 0f
                    }
                }
            }
        } catch (e: CancellationException) {
            // Fling was interrupted by a new touch/drag - that's normal,
            // just stop here rather than propagating.
        }

        return currentVelocity * 1000f
    }

    companion object {
        const val DECELERATION_RATE_NORMAL = 0.998f
        const val DECELERATION_RATE_FAST = 0.99f
    }
}

/**
 * Drop-in replacement for `ScrollableDefaults.flingBehavior()` on a
 * `LazyColumn`/`LazyRow` - e.g. `LazyColumn(flingBehavior = rememberIosStyleFlingBehavior())`.
 */
@Composable
fun rememberIosStyleFlingBehavior(
    decelerationRate: Float = IosStyleFlingBehavior.DECELERATION_RATE_NORMAL
): FlingBehavior {
    return remember(decelerationRate) { IosStyleFlingBehavior(decelerationRate) }
}
