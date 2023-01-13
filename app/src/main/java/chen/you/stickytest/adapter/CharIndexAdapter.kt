package chen.you.stickytest.adapter

import chen.you.stickytest.bean.StickyData
import chen.you.stickyview.CharIndexView

class CharIndexAdapter : CharIndexView.Adapter() {

    private val data: ArrayList<StickyData> = StickyData.test()

    override fun getItemCount(): Int = data.size

    override fun getItemChar(position: Int): Char = data[position].name[0]
}