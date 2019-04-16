package me.kenzierocks.visisort

data class VisiArray(
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
            get() = oldArray.data[index]

        override fun toString() = "<$oldArray[$index] (=${value.value})>"
    }

    fun ref(index: Int) = Ref(this, index)

    override fun toString() = id

}
