package chen.you.stickytest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import chen.you.stickytest.R


class FooterAdapter : RecyclerView.Adapter<TestViewHolder>() {

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer, parent, false))
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