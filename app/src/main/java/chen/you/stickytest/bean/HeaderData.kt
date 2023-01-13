package chen.you.stickytest.bean

import chen.you.stickytest.R
import kotlin.random.Random


class HeaderData(val name: String) {

    val strs = ArrayList<String>()

    var header = randomDrawable()

    constructor(name: String, testSize: Int): this(name) {
        for (i in 0 until testSize) {
            strs.add("$name test $i")
        }
    }

    companion object {

        fun randomDrawable(): Int {
            val randomNum: Int = Random.nextInt(5)
            try {
                return R.drawable::class.java.getField("head$randomNum")[null] as Int
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return R.drawable.head0
        }

        fun test0(): ArrayList<HeaderData> {
            val headerData = ArrayList<HeaderData>()
            headerData.add(HeaderData("A", 1))
            headerData.add(HeaderData("B", 0))
            headerData.add(HeaderData("C", 3))
            headerData.add(HeaderData("E", 2))
            return headerData
        }

        fun test1(): ArrayList<HeaderData> {
            val headerData = ArrayList<HeaderData>()
            headerData.add(HeaderData("A", 1))
            headerData.add(HeaderData("B", 2))
            headerData.add(HeaderData("C", 5))
            headerData.add(HeaderData("D", 0))
            headerData.add(HeaderData("E", 4))
            headerData.add(HeaderData("F", 6))
            headerData.add(HeaderData("G", 2))
            headerData.add(HeaderData("H", 9))
            headerData.add(HeaderData("I", 10))
            headerData.add(HeaderData("J", 5))
            headerData.add(HeaderData("K", 4))
            headerData.add(HeaderData("L", 3))
            headerData.add(HeaderData("M", 1))
            headerData.add(HeaderData("N", 1))
            headerData.add(HeaderData("O", 3))
            headerData.add(HeaderData("p", 13))
            headerData.add(HeaderData("q", 11))
            headerData.add(HeaderData("r", 30))
            headerData.add(HeaderData("s", 9))
            headerData.add(HeaderData("t", 12))
            headerData.add(HeaderData("u", 5))
            headerData.add(HeaderData("v", 3))
            headerData.add(HeaderData("w", 8))
            headerData.add(HeaderData("x", 2))
            headerData.add(HeaderData("y", 7))
            headerData.add(HeaderData("z", 22))
            headerData.add(HeaderData("#", 15))
            return headerData
        }
    }
}