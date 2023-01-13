package chen.you.stickytest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import chen.you.stickytest.adapter.FooterAdapter
import chen.you.stickytest.adapter.HeaderAdapter
import chen.you.stickytest.adapter.StickyGridAdapter
import chen.you.stickytest.databinding.FragmentStickyGridBinding

class StickyGridFragment : Fragment() {
    private var isStaggered = false
    private lateinit var binding: FragmentStickyGridBinding
    private val adapter by lazy { StickyGridAdapter(requireContext(), isStaggered) }
    
    companion object {
        fun newInstance(isStaggered: Boolean): StickyGridFragment {
            val args = Bundle().apply { putBoolean("isStaggered", isStaggered) }
            return StickyGridFragment().apply { arguments = args }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isStaggered = arguments?.getBoolean("isStaggered") ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, savedState: Bundle?): View {
        binding = FragmentStickyGridBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isStaggered) {
            binding.erv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            val glm = GridLayoutManager(requireContext(), 2).also {
                it.spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        //-1 为Header count
                        if (position == 0 || position == it.itemCount - 1) return 2
                        if (binding.erv.isGroupTypeByPosition(position)) return 2
                        return 1
                    }
                }
            }
            binding.erv.layoutManager = glm
        }
        binding.erv.setStickyAdapter(adapter, HeaderAdapter(), FooterAdapter())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}