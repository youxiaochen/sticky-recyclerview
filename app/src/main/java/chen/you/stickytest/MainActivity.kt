package chen.you.stickytest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import chen.you.stickytest.databinding.ActMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActMainBinding

    private val adapter by lazy { TabAdapter(this) }

    private lateinit var mediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_main)
        binding.vp.adapter = adapter
        val strategy = TabLayoutMediator.TabConfigurationStrategy { tab, pos -> tab.text = adapter.getTitle(pos) }
        mediator = TabLayoutMediator(binding.tl, binding.vp, strategy)
        mediator.attach()

        binding.tvTwo.setOnClickListener {
            startActivity(Intent(this, HorizontalActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediator.detach()
        binding.unbind()
    }

    private class TabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        companion object {
            private val TITLES = arrayOf("StickyRecyclerView", "Gravity=Center|Right",
                "GridLayout样式", "StaggeredGrid样式", "StickyLayoutView方式")
        }

        override fun getItemCount() = TITLES.size

        fun getTitle(position: Int) = TITLES[position]

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return StickyFragment()
                1 -> return StickyGravityFragment()
                2 -> return StickyGridFragment.newInstance(false)
                3 -> return StickyGridFragment.newInstance(true)
                4 -> return StickyLayoutFragment()
            }
            return Fragment()
        }
    }

}