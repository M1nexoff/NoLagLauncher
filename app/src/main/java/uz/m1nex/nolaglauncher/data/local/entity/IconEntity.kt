package uz.m1nex.nolaglauncher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "icons")
data class IconEntity(
    @PrimaryKey val componentKey: String,
    val bytes: ByteArray,
    val generatedForUpdateTime: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IconEntity) return false
        return componentKey == other.componentKey &&
                generatedForUpdateTime == other.generatedForUpdateTime &&
                bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = componentKey.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + generatedForUpdateTime.hashCode()
        return result
    }
}
