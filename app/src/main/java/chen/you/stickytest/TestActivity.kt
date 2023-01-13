package chen.you.stickytest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import chen.you.stickytest.adapter.FooterAdapter
import chen.you.stickytest.adapter.HeaderAdapter
import chen.you.stickytest.adapter.TestAdapter
import chen.you.stickytest.bean.StickyData
import chen.you.stickytest.databinding.ActHorizontalBinding
import chen.you.stickytest.databinding.ActTestBinding
import chen.you.stickyview.CharIndexMediator

/**
 *  author: you : 2021/1/13
 *  测试具体方法
 */
class TestActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActTestBinding
    private val adapter by lazy { TestAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_test)

        binding.erv.setStickyAdapter(adapter, HeaderAdapter(), FooterAdapter())
        CharIndexMediator(binding.erv, binding.civ).attach(this)

        binding.bt0.setOnClickListener(this)
        binding.bt1.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt0 -> {
                val add = StickyData.test0()
                val start = adapter.data.size
                adapter.data.addAll(add)
                adapter.notifyGroupRangeInserted(start, add.size, true)
            }
            R.id.bt1 -> {
                adapter.data.clear()
                adapter.data.addAll(StickyData.test())
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}