package chen.you.stickytest.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import chen.you.stickytest.R
import chen.you.stickytest.TestActivity


class HeaderAdapter : RecyclerView.Adapter<TestViewHolder>() {

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_header_v, parent, false)
        itemView.setOnClickListener {
            parent.context.startActivity(Intent(parent.context, TestActivity::class.java))
        }
        return TestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
    }

    override fun onViewAttachedToWindow(holder: TestViewHolder) {
        val params = holder.itemView.layoutParams
        if (params is StaggeredGridLayoutManager.LayoutParams) {
            params.isFullSpan = true
        }
    }
}