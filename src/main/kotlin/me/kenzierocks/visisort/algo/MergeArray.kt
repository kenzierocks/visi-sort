package me.kenzierocks.visisort.algo

import me.kenzierocks.visisort.Data
import me.kenzierocks.visisort.OpChannel
import me.kenzierocks.visisort.VisiArray

abstract class MergeArray constructor(val data: VisiArray) {

    protected abstract fun newArray(data: VisiArray): MergeArray

    suspend fun init(opChannel: OpChannel): Pair<MergeArray, MergeArray>? {
        return with(opChannel) {
            when {
                data.size > 1 -> {
                    val leftAmt = data.size / 2
                    val leftSlice = data.slice(0 until leftAmt)
                    val rightSlice = data.slice(leftAmt until data.size)
                    newArray(leftSlice) to newArray(rightSlice)
                }
                else -> null
            }
        }
    }

    private val size = data.size

    protected abstract suspend fun performSubMerge(opChannel: OpChannel, left: MergeArray, right: MergeArray)

    suspend fun mergeLR(opChannel: OpChannel) {
        val (left, right) = init(opChannel) ?: return
        performSubMerge(opChannel, left, right)

        with(opChannel) {
            var index = 0
            var indexLeft = 0
            var indexRight = 0
            while (indexLeft < left.size && indexRight < right.size) {

                val append: VisiArray.Ref
                if (compare(left.data.ref(indexLeft), right.data.ref(indexRight)) <= 0) {
                    append = left.data.ref(indexLeft)
                    indexLeft++
                } else {
                    append = right.data.ref(indexRight)
                    indexRight++
                }
                copy(append, data.ref(index))
                index++
            }

            while (indexLeft < left.size) {
                copy(left.data.ref(indexLeft), data.ref(index))
                indexLeft++
                index++
            }

            while (indexRight < right.size) {
                copy(right.data.ref(indexRight), data.ref(index))
                indexRight++
                index++
            }
        }
    }

}
