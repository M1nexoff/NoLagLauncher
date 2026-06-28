package uz.m1nex.nolaglauncher.domain.model

data class GridConfig(
    val columns: Int,
    val rows: Int
) {
    val perPage: Int get() = columns * rows
}
