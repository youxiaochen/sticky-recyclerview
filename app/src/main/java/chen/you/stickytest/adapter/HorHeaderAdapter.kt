package chen.you.stickytest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chen.you.stickytest.R

class HorHeaderAdapter  : RecyclerView.Adapter<TestViewHolder>() {

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header_h, parent, false))
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
    }
}