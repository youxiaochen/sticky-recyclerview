package chen.you.stickytest.bean

class StickyData(val name: String) {

    val strs = ArrayList<String>()

    constructor(name: String, testSize: Int) : this(name) {
        for (i in 0 until testSize) {
            strs.add("$name test $i")
        }
    }

    companion object {

        fun test0(): ArrayList<StickyData> {
            val testData = ArrayList<StickyData>()
            testData.add(StickyData("A", 1))
            testData.add(StickyData("B", 2))
            testData.add(StickyData("C", 5))
            testData.add(StickyData("D", 0))
            testData.add(StickyData("E", 4))
            testData.add(StickyData("F", 6))
            testData.add(StickyData("G", 2))
            testData.add(StickyData("H", 9))
            testData.add(StickyData("I", 10))
            return testData
        }

        fun test(): ArrayList<StickyData> {
            val testData = ArrayList<StickyData>()
            testData.add(StickyData("A", 1))
            testData.add(StickyData("B", 2))
            testData.add(StickyData("C", 5))
            testData.add(StickyData("D", 0))
            testData.add(StickyData("E", 4))
            testData.add(StickyData("F", 6))
            testData.add(StickyData("G", 2))
            testData.add(StickyData("H", 9))
            testData.add(StickyData("I", 10))
            testData.add(StickyData("J", 5))
            testData.add(StickyData("K", 4))
            testData.add(StickyData("L", 3))
            testData.add(StickyData("M", 1))
            testData.add(StickyData("N", 1))
            testData.add(StickyData("O", 3))
            testData.add(StickyData("P", 12))
            testData.add(StickyData("Q", 16))
            testData.add(StickyData("R", 8))
            testData.add(StickyData("S", 9))
            testData.add(StickyData("T", 10))
            testData.add(StickyData("U", 5))
            testData.add(StickyData("V", 14))
            testData.add(StickyData("W", 3))
            testData.add(StickyData("X", 11))
            testData.add(StickyData("Y", 2))
            testData.add(StickyData("Z", 3))
            return testData
        }

    }
}