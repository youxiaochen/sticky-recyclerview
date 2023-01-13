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
import chen.you.stickytest.bean.HeaderData
import chen.you.stickyview.StickyLayoutView


class StickyLayoutAdapter(context: Context)
    : StickyLayoutView.Adapter<StickyLayoutAdapter.GroupViewHolder, StickyLayoutAdapter.ChildViewHolder>() {

    val data: ArrayList<HeaderData> = HeaderData.test1()
    private var inflater = LayoutInflater.from(context)

    override fun getGroupCount(): Int = data.size

    override fun getChildCount(groupPos: Int): Int = data[groupPos].strs.size

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(inflater.inflate(R.layout.item_layout_group, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(inflater.inflate(R.layout.item_child, parent, false)).apply {
            itemView.setOnClickListener {
                childInfo?.apply {
                    Toast.makeText(inflater.context, "GroupPos=${group.index},ChildPos=$index", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBindGroupViewHolder(vh: GroupViewHolder, groupPos: Int, isExpanded: Boolean) {
        val headerData = data[groupPos]
        vh.tv.text = headerData.name
        vh.iv.setImageResource(headerData.header)
    }

    override fun onBindChildViewHolder(vh: ChildViewHolder, groupPos: Int, childPos: Int, isLastChild: Boolean) {
        vh.tv.text = data[groupPos].strs[childPos]
    }

    override fun onCreateStickyViewHolder(parent: ViewGroup): GroupViewHolder {
        return GroupViewHolder(inflater.inflate(R.layout.item_layout_group, parent, false))
    }

    override fun onBindStickyViewHolder(vh: GroupViewHolder, groupPos: Int) {
        val headerData = data[groupPos]
        vh.tv.text = headerData.name
        vh.iv.setImageResource(headerData.header)
    }

    class GroupViewHolder(itemView: View) : ExpandableRecyclerView.GroupViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
        val iv: ImageView = itemView.findViewById(R.id.iv)
    }

    class ChildViewHolder(itemView: View) : ExpandableRecyclerView.ChildViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
        val iv: ImageView = itemView.findViewById(R.id.iv)
    }
}