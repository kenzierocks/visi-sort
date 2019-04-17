package me.kenzierocks.visisort

import java.util.Objects

class VisiArray(
        /**
         * Level of the array. Starts at 0, higher is further down.
         */
        val level: Int,
        /**
         * Offset into the whole dataset. Used for visual positioning.
         */
        val offset: Int,
        val data: MutableList<Data>,
        val id: String = IDS.newId()
) {
    companion object {
        private val IDS = IdTracker("Array")
    }

    val size = data.size

    data class Ref(val array: VisiArray, val index: Int) {
        private val oldArray = array.copy()
        val value: Data
            get() = array.data[index]

        override fun toString() = "<$array[$index] (=${value.value})>"

        fun asOldArrayRef() = copy(array = oldArray)

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            return when (other) {
                is Ref -> array.id == other.array.id && index == other.index
                else -> false
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(array.id, index)
        }
    }

    fun ref(index: Int) = Ref(this, index)

    fun copy() = VisiArray(
            level, offset, data.toMutableList(), id
    )

    override fun toString() = id

}
