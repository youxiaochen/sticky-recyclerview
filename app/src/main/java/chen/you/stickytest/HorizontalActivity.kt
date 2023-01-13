package chen.you.stickytest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import chen.you.stickytest.adapter.HorHeaderAdapter
import chen.you.stickytest.adapter.StickyHorTestAdapter
import chen.you.stickytest.databinding.ActHorizontalBinding
import chen.you.stickyview.CharIndexMediator
import chen.you.stickyview.StickyRecyclerView.StickyParams

class HorizontalActivity : AppCompatActivity() {

    private lateinit var binding: ActHorizontalBinding

    private val startAdapter by lazy { StickyHorTestAdapter(this, StickyParams.START) }
    private val centerAdapter by lazy { StickyHorTestAdapter(this, StickyParams.CENTER) }
    private val endAdapter by lazy { StickyHorTestAdapter(this, StickyParams.END) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_horizontal)

        binding.erv.setStickyAdapter(startAdapter, HorHeaderAdapter())
        binding.erv2.setStickyAdapter(centerAdapter, HorHeaderAdapter())
        binding.erv3.setStickyAdapter(endAdapter, HorHeaderAdapter())


        CharIndexMediator(binding.erv2, binding.civ).attach(this)
    }
}