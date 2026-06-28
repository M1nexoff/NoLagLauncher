// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.domain.model

object HomeGrid {
    const val MIN = 2
    const val MAX = 12
    const val MAX_FAVOURITES = 5

    val DEFAULT = GridConfig(columns = 4, rows = 5)

    val PRESETS = listOf(
        GridConfig(columns = 4, rows = 5),
        GridConfig(columns = 4, rows = 6),
        GridConfig(columns = 5, rows = 9)
    )
}
