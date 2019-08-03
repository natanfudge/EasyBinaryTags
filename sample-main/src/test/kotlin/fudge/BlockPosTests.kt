package fudge

import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BlockPosTests {
    @Test
    fun testToFromLong() {
        val pos = BlockPos(1,2,3)
        val long = pos.toLong()
        val back = BlockPos.fromLong(long)
        assertEquals(pos,back)
    }
}