package chen.you.stickytest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import chen.you.stickytest.adapter.FooterAdapter
import chen.you.stickytest.adapter.HeaderAdapter
import chen.you.stickytest.adapter.StickyTestAdapter
import chen.you.stickytest.databinding.FragmentGravityStickyBinding
import chen.you.stickyview.StickyRecyclerView.StickyParams

class StickyGravityFragment : Fragment() {

    private lateinit var binding: FragmentGravityStickyBinding
    private val centerAdapter by lazy { StickyTestAdapter(requireContext(), StickyParams.CENTER) }
    private val rightAdapter by lazy { StickyTestAdapter(requireContext(), StickyParams.END) }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, savedState: Bundle?): View {
        binding = FragmentGravityStickyBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.erv.setStickyAdapter(centerAdapter, HeaderAdapter(), FooterAdapter())
        binding.erv2.setStickyAdapter(rightAdapter, HeaderAdapter(), FooterAdapter())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}