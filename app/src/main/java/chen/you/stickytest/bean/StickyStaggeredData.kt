package chen.you.stickytest.bean

import kotlin.random.Random

class StickyStaggeredData(val name: String) {

    val childs = ArrayList<Child>()

    constructor(name: String, testSize: Int) : this(name) {
        for (i in 0 until testSize) {
            childs.add(Child("$name test $i"));
        }
    }

    class Child(var name: String) {
        val scale: Float = 1 + Random.nextInt(10) / 5f
    }

    companion object {

        fun test(): ArrayList<StickyStaggeredData> {
            val testData = ArrayList<StickyStaggeredData>()
            testData.add(StickyStaggeredData("A", 1))
            testData.add(StickyStaggeredData("B", 2))
            testData.add(StickyStaggeredData("C", 5))
            testData.add(StickyStaggeredData("D", 0))
            testData.add(StickyStaggeredData("E", 4))
            testData.add(StickyStaggeredData("F", 6))
            testData.add(StickyStaggeredData("G", 2))
            testData.add(StickyStaggeredData("H", 9))
            testData.add(StickyStaggeredData("I", 10))
            testData.add(StickyStaggeredData("J", 5))
            testData.add(StickyStaggeredData("K", 4))
            testData.add(StickyStaggeredData("L", 3))
            testData.add(StickyStaggeredData("M", 1))
            testData.add(StickyStaggeredData("N", 1))
            testData.add(StickyStaggeredData("O", 3))
            testData.add(StickyStaggeredData("P", 12))
            testData.add(StickyStaggeredData("Q", 16))
            testData.add(StickyStaggeredData("R", 8))
            testData.add(StickyStaggeredData("S", 9))
            testData.add(StickyStaggeredData("T", 10))
            testData.add(StickyStaggeredData("U", 5))
            testData.add(StickyStaggeredData("V", 14))
            testData.add(StickyStaggeredData("W", 3))
            testData.add(StickyStaggeredData("X", 11))
            testData.add(StickyStaggeredData("Y", 10))
            testData.add(StickyStaggeredData("Z", 3))
            return testData
        }
    }
}