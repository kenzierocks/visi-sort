package me.kenzierocks.visisort

class IdTracker(private val base: String) {

    private val names = listOf("Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf",
            "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa",
            "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee",
            "Zulu")
    private val nameIndex = ArrayList<Int>()

    @Synchronized
    private fun nextNameList(): List<Int> {
        var index = 0
        while (true) {
            if (index >= nameIndex.size) {
                nameIndex.add(0)
            }
            val value = nameIndex[index] + 1
            if (value >= names.size) {
                for (i in 0..index) {
                    nameIndex[i] = 0
                }
                index++
                continue
            }
            nameIndex[index] = value
            break
        }
        return nameIndex.toList()
    }

    fun newId(): String {
        return base + ":" + nextNameList().joinToString("+", transform = names::get)
    }
}