package chen.you.stickytest

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ScaleImageView : AppCompatImageView {

    private var widthScale = 1f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height: Int = (width * widthScale).toInt()
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    fun setWidthScale(widthScale: Float) {
        if (this.widthScale == widthScale) return
        this.widthScale = widthScale
        requestLayout()
    }
}