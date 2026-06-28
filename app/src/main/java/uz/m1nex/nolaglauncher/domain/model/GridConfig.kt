// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.domain.model

data class GridConfig(
    val columns: Int,
    val rows: Int
) {
    val perPage: Int get() = columns * rows
}
