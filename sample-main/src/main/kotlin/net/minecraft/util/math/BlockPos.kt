package net.minecraft.util.math

data class BlockPos(val x: Int, val y: Int, val z: Int) {
    fun toLong(): Long {
        return x + (y.toLong() shl 20) + (z.toLong() shl 40)
    }

    companion object {
        fun fromLong(long: Long): BlockPos {
            val z = long shr 40
            val withoutZ = long - (z shl 40)
            val y = withoutZ shr 20
            val x = long - (z shl 40) - (y shl 20)
            return BlockPos(x.toInt(), y.toInt(), z.toInt())
        }
    }
}