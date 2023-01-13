package chen.you.stickytest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import chen.you.expandable.ExpandableRecyclerView
import chen.you.stickytest.adapter.CharIndexAdapter
import chen.you.stickytest.adapter.FooterAdapter
import chen.you.stickytest.adapter.HeaderAdapter
import chen.you.stickytest.adapter.StickyLayoutAdapter
import chen.you.stickytest.bean.HeaderData
import chen.you.stickytest.databinding.FragmentStickyLayoutBinding
import chen.you.stickyview.CharIndexView
import chen.you.stickyview.StickyLayoutView
import chen.you.stickyview.StickyLayoutView.OnStickyScrollListener

class StickyLayoutFragment : Fragment() {

    private lateinit var binding: FragmentStickyLayoutBinding
    private val adapter by lazy { StickyLayoutAdapter(requireContext()) }
    private val charAdapter by lazy {
        object : CharIndexView.Adapter() {
            override fun getItemCount(): Int = adapter.groupCount

            override fun getItemChar(position: Int): Char = adapter.data[position].name[0]
        }
    }

    private val tmpGroupInfo = ExpandableRecyclerView.GroupInfo.obtainEmpty()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, savedState: Bundle?): View {
        binding = FragmentStickyLayoutBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        binding.slv.setLayoutManager(LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false))

        val glm = GridLayoutManager(requireContext(), 2).also {
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    //-1 为Header count
                    if (position == 0 || position == it.itemCount - 1) return 2
                    if (binding.slv.isGroupType(position - 1)) return 2
                    return 1
                }
            }
        }

        binding.slv.setLayoutManager(glm)
        binding.slv.setAdapter(adapter, HeaderAdapter(), FooterAdapter())
        binding.civ.adapter = charAdapter


        binding.civ.addOnCharIndexChangedListener { _, index ->
            if (index == CharIndexView.NO_ID) {
                binding.slv.scrollToPosition(0)
            } else {
                val info = binding.slv.expandableRecyclerView.findGroupInfoByIndex(index, tmpGroupInfo)
                info?.apply {
                    binding.slv.scrollToBindingPosition(position)
                }
            }
        }

        binding.slv.addOnStickyScrollListener(object : OnStickyScrollListener() {
            override fun onStickied(slv: StickyLayoutView, groupPos: Int) {
                binding.civ.currentIndex = groupPos
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}