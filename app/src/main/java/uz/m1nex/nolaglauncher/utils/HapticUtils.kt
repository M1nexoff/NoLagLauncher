// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.utils

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Utility functions for haptic feedback during launcher interactions.
 *
 * @author A'zamxo'ja Iskandarxo'jayev
 */
object HapticUtils {
    /**
     * Performs a light haptic feedback for drag start.
     */
    fun performDragStart(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Performs a subtle haptic feedback when hovering over a drop zone.
     */
    fun performHoverFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Performs a confirmation haptic feedback when successfully dropping an item.
     */
    fun performDropSuccess(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONFIRM,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Performs a rejection haptic feedback when dropping fails or is cancelled.
     */
    fun performDropCancel(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.REJECT,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}
