package chen.you.stickytest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView

/**
 *  author: you : 2021/1/9
 */
class TestView : View {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val d: NinePatchDrawable = context.resources.getDrawable(R.drawable.bpg) as NinePatchDrawable

    val rect = Rect()

    override fun onDraw(canvas: Canvas) {
        val clip = canvas.getClipBounds(rect)

        Log.d("youxiaochen", "onDraw $clip, rect = $rect")


    }

    fun test() {

        invalidate(0, 0, 50, 50)
    }
}