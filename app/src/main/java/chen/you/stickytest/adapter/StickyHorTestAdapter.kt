package chen.you.stickytest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import chen.you.expandable.ExpandableRecyclerView
import chen.you.stickytest.R
import chen.you.stickytest.bean.StickyData
import chen.you.stickyview.StickyRecyclerView.StickyAdapter
import chen.you.stickyview.StickyRecyclerView.StickyParams


class StickyHorTestAdapter(context: Context, private val gravity: Int)
    : StickyAdapter<StickyHorTestAdapter.GroupViewHolder, StickyHorTestAdapter.ChildViewHolder>() {

    private val data: ArrayList<StickyData> = StickyData.test()
    private var inflater = LayoutInflater.from(context)

    override fun getGroupCount(): Int = data.size

    override fun getChildCount(groupPos: Int): Int = data[groupPos].strs.size

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val layoutResId = when (gravity) {
            StickyParams.CENTER -> R.layout.item_group_hor_center
            StickyParams.END -> R.layout.item_group_hor_right
            else -> R.layout.item_group_hor_left
        }
        return GroupViewHolder(inflater.inflate(layoutResId, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(inflater.inflate(R.layout.item_child_hor, parent, false)).apply {
            itemView.setOnClickListener {
                childInfo?.apply {
                    Toast.makeText(inflater.context, "GroupPos=${group.index},ChildPos=$index", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBindGroupViewHolder(vh: GroupViewHolder, groupPos: Int, isExpanded: Boolean) {
        vh.tv.text = data[groupPos].name
    }

    override fun onBindChildViewHolder(vh: ChildViewHolder, groupPos: Int, childPos: Int, isLastChild: Boolean) {
        vh.tv.text = data[groupPos].strs[childPos]
    }

    override fun getStickerItem(groupPos: Int): String = data[groupPos].name

    class GroupViewHolder(itemView: View) : ExpandableRecyclerView.GroupViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
    }

    class ChildViewHolder(itemView: View) : ExpandableRecyclerView.ChildViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
        val iv: ImageView = itemView.findViewById(R.id.iv)
    }
}