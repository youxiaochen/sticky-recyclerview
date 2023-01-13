package chen.you.stickytest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import chen.you.expandable.ExpandableRecyclerView.GroupInfo
import chen.you.stickytest.adapter.CharIndexAdapter
import chen.you.stickytest.adapter.FooterAdapter
import chen.you.stickytest.adapter.HeaderAdapter
import chen.you.stickytest.adapter.StickyTestAdapter
import chen.you.stickytest.databinding.FragmentStickyBinding
import chen.you.stickyview.CharIndexMediator
import chen.you.stickyview.StickyRecyclerView.StickyParams

class StickyFragment : Fragment() {

    private lateinit var binding: FragmentStickyBinding
    private val adapter by lazy { StickyTestAdapter(requireContext(), StickyParams.START) }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, savedState: Bundle?): View {
        binding = FragmentStickyBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.erv.setStickyAdapter(adapter, HeaderAdapter(), FooterAdapter())
        CharIndexMediator(binding.erv, binding.civ).attach(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}