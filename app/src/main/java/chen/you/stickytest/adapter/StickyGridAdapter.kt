package chen.you.stickytest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import chen.you.expandable.ExpandableRecyclerView
import chen.you.stickytest.R
import chen.you.stickytest.ScaleImageView
import chen.you.stickytest.bean.StickyStaggeredData
import chen.you.stickyview.StickyRecyclerView.StickyAdapter


class StickyGridAdapter(context: Context, private val isStaggered: Boolean = false)
    : StickyAdapter<StickyGridAdapter.GroupViewHolder, StickyGridAdapter.ChildViewHolder>() {

    private val data: ArrayList<StickyStaggeredData> = StickyStaggeredData.test()
    private var inflater = LayoutInflater.from(context)

    override fun getGroupCount(): Int = data.size

    override fun getChildCount(groupPos: Int): Int = data[groupPos].childs.size

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(inflater.inflate(R.layout.item_group_left, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(inflater.inflate(R.layout.item_grid_child, parent, false)).apply {
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
        val child = data[groupPos].childs[childPos]
        vh.tv.text = child.name
        if (isStaggered) {
            vh.iv.setWidthScale(child.scale)
        }
    }

    override fun getStickerItem(groupPos: Int): String = data[groupPos].name

    class GroupViewHolder(itemView: View) : ExpandableRecyclerView.GroupViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
    }

    class ChildViewHolder(itemView: View) : ExpandableRecyclerView.ChildViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
        val iv: ScaleImageView = itemView.findViewById(R.id.iv)
    }
}