package chen.you.stickyview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by you on 2017/9/11
 * 字符索引控件
 * 作QQ/W 86207610
 */
public class CharIndexView extends View {
    //没有触摸时的无效的索引
    public static final int NO_INDEX = -1;
    //默认item大小
    private static final int DEF_ITEM_SIZE = 30;
    //隐藏指示器的延时时间
    private static final int DISMISS_DELAY_DURATION = 300;
    /**
     * 垂直与水平布局两种状态
     */
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 0;
    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }
    //CharIndexView params
    private CharIndexParams mParams;
    //paint
    private TextPaint textPaint;
    //文本居中时的位置
    private float textY;

    //indicator paint
    private TextPaint indicatorPaint;
    //indicator文字中心Y
    private float indicatorTextY;
    //指示背景长/宽
    private int indicatorBgWidth;
    private int indicatorBgHeight;
    //如果背景是.9或者有设置padding时, 为.9中的内容区域, 否则大小与indicatorBgDrawable一样
    private final Rect indicatorTextRect = new Rect();
    //itemSize < indicator的大小时, 画indicator就会超出屏幕,因此需要计算这个偏移
    private int indicatorOffset;
    //显示指示器, 手指离开触摸后延时false
    private boolean showIndicator = false;

    //绘制字符数量
    private int itemCount;
    //adapter
    private Adapter mAdapter;
    private CharIndexViewObserver mObserver;
    //当前touch位置
    private int currentIndex = NO_INDEX;
    //监听类
    private List<OnCharIndexChangedListener> listeners;
    //记录索引的最大值, sticky的索引 <= groupIndex, 指示器滑动 > maxIndex时自动校正
    private int maxIndex = NO_INDEX;
    //记录索引的最小值, sticky没有header时最小值为0,指示器< minIndex时自动校正
    private int minIndex = Integer.MAX_VALUE;
    //自动校正时有个校正的动画效果
    private final DismissRunnable dismissRunnable = new DismissRunnable(this);

    public CharIndexView(@NonNull Context context) {
        super(context);
        init(new CharIndexParams(context, null));
    }

    //用于代码生成控件
    public CharIndexView(@NonNull Context context, @NonNull CharIndexParams params) {
        super(context);
        init(params);
    }

    public CharIndexView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(new CharIndexParams(context, attrs));
    }

    public CharIndexView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(new CharIndexParams(context, attrs));
    }

    private void init(CharIndexParams params) {
        this.mParams = params;
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        indicatorPaint = new TextPaint();
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setTextAlign(Paint.Align.CENTER);
        initCharParams();
        initIndicatorParams();
    }

    //初始化Char相关的参数
    private void initCharParams() {
        textPaint.setTextSize(mParams.textSize);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textFontCenterY = (fm.bottom + fm.top) * 0.5f;
        textY = mParams.itemSize / 2.0f - textFontCenterY;
    }

    //初始化指示器相关参数
    private void initIndicatorParams() {
        indicatorPaint.setTextSize(mParams.indicatorTextSize);
        indicatorPaint.setColor(mParams.indicatorTextColor);
        Paint.FontMetrics ifm = indicatorPaint.getFontMetrics();
        float fmHeight = ifm.bottom - ifm.top;
        int rectSize = (int) Math.ceil(fmHeight);
        if (mParams.indicatorBgDrawable == null) {
            indicatorBgWidth = indicatorBgHeight = rectSize;
            indicatorTextRect.set(0, 0, indicatorBgWidth, indicatorBgHeight);
        } else {
            indicatorBgWidth = mParams.indicatorBgDrawable.getIntrinsicWidth();
            indicatorBgHeight = mParams.indicatorBgDrawable.getIntrinsicHeight();
            //drawable配置文件未指明宽高时
            if (indicatorBgWidth < 0) indicatorBgWidth = rectSize;
            if (indicatorBgHeight < 0) indicatorBgHeight = rectSize;
            Rect paddingRect = new Rect();
            mParams.indicatorBgDrawable.getPadding(paddingRect);
            int rectW = indicatorBgWidth - paddingRect.left - paddingRect.right;
            int rectH = indicatorBgHeight - paddingRect.top - paddingRect.bottom;
            if (rectW < fmHeight || rectH < fmHeight) {//需要扩大
                float enlarge = Math.max(fmHeight / rectW, fmHeight / rectH);
                int newRectW = (int) (rectW * enlarge);
                int newRectH = (int) (rectH * enlarge);
                indicatorBgWidth += newRectW - rectW;
                indicatorBgHeight += newRectH - rectH;
                indicatorTextRect.set(paddingRect.left, paddingRect.top, paddingRect.left + newRectW, paddingRect.top + newRectH);
            } else {
                indicatorTextRect.set(paddingRect.left, paddingRect.top,
                        indicatorBgWidth - paddingRect.right, indicatorBgHeight - paddingRect.bottom);
            }
        }
        indicatorTextY = indicatorTextRect.exactCenterY() - (ifm.bottom + ifm.top) * 0.5f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeftRight = getPaddingLeft() + getPaddingRight();
        int paddingTopBottom = getPaddingTop() + getPaddingBottom();
        int width, height;
        int startDrawableSize = mParams.getStartDrawableSize();
        indicatorOffset = 0;
        if (mParams.isVertical()) {
            width = paddingLeftRight + mParams.itemSize + indicatorBgWidth + mParams.indicatorPadding;
            if (mParams.itemSize < indicatorBgHeight) {
                indicatorOffset = (int) Math.ceil((indicatorBgHeight - mParams.itemSize) / 2.f);
            }
            height = paddingTopBottom + itemCount * mParams.itemSize + startDrawableSize + indicatorOffset * 2;
        } else {
            height = paddingTopBottom + mParams.itemSize + indicatorBgHeight + mParams.indicatorPadding;
            if (mParams.itemSize < indicatorBgWidth) {
                indicatorOffset = (int) Math.ceil((indicatorBgWidth - mParams.itemSize) / 2.f);
            }
            width = paddingLeftRight + itemCount * mParams.itemSize + startDrawableSize + indicatorOffset * 2;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (itemCount <= 0) return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return onEventDown(event);
            case MotionEvent.ACTION_MOVE:
                float touchOffset;
                if (mParams.isVertical()) {
                    touchOffset = event.getY() - getPaddingTop();
                } else {
                    touchOffset = event.getX() - getPaddingLeft();
                }
                int currentIndex = computeCurrentIndex(touchOffset - indicatorOffset);
                if (this.currentIndex != currentIndex) {
                    this.currentIndex = currentIndex;
                    invalidate();
                    performCharIndexChanged();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onEventUpCancel();
                break;
        }
        return true;
    }

    //按下事件
    private boolean onEventDown(MotionEvent event) {
        float touchOffset;
        if (mParams.isVertical()) {
            if (event.getX() < indicatorBgWidth + mParams.indicatorPadding) return false;
            float y = event.getY();
            if (y > getHeight() - indicatorOffset - getPaddingBottom()) return false;
            touchOffset = y - getPaddingTop() - indicatorOffset;
        } else {
            if (event.getY() < indicatorBgHeight + mParams.indicatorPadding) return false;
            float x = event.getX();
            if (x > getWidth() - indicatorOffset - getPaddingRight()) return false;
            touchOffset = x - getPaddingLeft() - indicatorOffset;
        }
        if (touchOffset < 0) return false;
        showIndicator = true;
        if (mParams.indicatorAutoCheck) {
            removeCallbacks(dismissRunnable);
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        int currentIndex = computeCurrentIndex(touchOffset);
        invalidate();
        if (this.currentIndex != currentIndex) {
            this.currentIndex = currentIndex;
            performCharIndexChanged();
        }
        return true;
    }

    private void performCharIndexChanged() {
        if (this.listeners != null) {
            for (OnCharIndexChangedListener listener : this.listeners) {
                listener.onCharIndexChanged(this, this.currentIndex);
            }
        }
    }

    //事件UP,CANCEL
    private void onEventUpCancel() {
        if (mParams.indicatorAutoCheck) {
            //实际sticky的位置超出范围时校正
            boolean needCheck = false;
            if (this.currentIndex > maxIndex) {
                this.currentIndex = maxIndex;
                needCheck = true;
            }
            if (this.currentIndex < minIndex) {
                this.currentIndex = minIndex;
                needCheck = true;
            }
            if (needCheck) {
                invalidate();
                postDelayed(dismissRunnable, DISMISS_DELAY_DURATION);
            } else {
                showIndicator = false;
                postInvalidateDelayed(DISMISS_DELAY_DURATION);
            }
        } else {
            showIndicator = false;
            postInvalidateDelayed(DISMISS_DELAY_DURATION);
        }
    }

    //计算出当前触摸的索引位置
    private int computeCurrentIndex(float touchOffset) {
        touchOffset -= mParams.getStartDrawableSize();
        if (touchOffset < 0) return NO_INDEX;
        int index = (int) ((touchOffset - 1) / mParams.itemSize);
        if (index < 0) return NO_INDEX;
        if (index >= itemCount) return itemCount - 1;
        return index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mParams.isVertical()) {
            drawVertical(canvas);
        } else {
            drawHorizontal(canvas);
        }
    }

    //画垂直方向
    private void drawVertical(Canvas canvas) {
        int left = getPaddingLeft();
        int charLeft = left + indicatorBgWidth + mParams.indicatorPadding;
        int charTop = getPaddingTop() + indicatorOffset;
        int charRight = charLeft + mParams.itemSize;
        int charBottom = charTop;

        if (mParams.startDrawable != null) {
            charBottom += mParams.itemSize;
            mParams.startDrawable.setBounds(charLeft, charTop, charRight, charBottom);
            mParams.startDrawable.draw(canvas);
            charBottom += mParams.startDrawablePadding;
        }
        if (mAdapter == null) return;
        int itemCount = mAdapter.getItemCount();
        if (itemCount <= 0) return;

        float cx = charLeft + mParams.itemSize / 2.0f;
        float cy = charBottom + textY;
        for (int i = 0; i < itemCount; i++) {
            String text = String.valueOf(mAdapter.getItemChar(i));
            if (i == currentIndex) {
                textPaint.setColor(mParams.selectTextColor);
                int currentTop = charBottom + mParams.itemSize * i;
                if (mParams.selectDrawable != null) { //画当前选择的索引背景
                    mParams.selectDrawable.setBounds(charLeft, currentTop, charRight, currentTop + mParams.itemSize);
                    mParams.selectDrawable.draw(canvas);
                }
                if (showIndicator) {
                    float indicatorTopOffset = (indicatorBgHeight - mParams.itemSize) * 0.5f;
                    if (mParams.indicatorBgDrawable != null) {//画指示背景
                        int indicatorTop = currentTop - (int) indicatorTopOffset;
                        mParams.indicatorBgDrawable.setBounds(left, indicatorTop, left + indicatorBgWidth, indicatorTop + indicatorBgHeight);
                        mParams.indicatorBgDrawable.draw(canvas);
                    }
                    //画指示文字
                    float indicatorCY = currentTop + indicatorTextY - indicatorTopOffset;
                    canvas.drawText(text, left + indicatorTextRect.centerX(), indicatorCY, indicatorPaint);
                }
            } else {
                textPaint.setColor(mParams.textColor);
            }
            canvas.drawText(text, cx, cy, textPaint);
            cy += mParams.itemSize;
        }
    }

    //画水平方向
    private void drawHorizontal(Canvas canvas) {
        int top = getPaddingTop();
        int charLeft = getPaddingLeft() + indicatorOffset;
        int charTop = top + indicatorBgHeight + mParams.indicatorPadding;
        int charRight = charLeft;
        int charBottom = charTop + mParams.itemSize;
        if (mParams.startDrawable != null) {
            charRight += mParams.itemSize;
            mParams.startDrawable.setBounds(charLeft, charTop, charRight, charBottom);
            mParams.startDrawable.draw(canvas);
            charRight += mParams.startDrawablePadding;
        }
        if (mAdapter == null) return;
        int itemCount = mAdapter.getItemCount();
        if (itemCount <= 0) return;

        float cx = charRight + mParams.itemSize / 2.0f;
        float cy = charTop + textY;
        for (int i = 0; i < itemCount; i++) {
            String text = String.valueOf(mAdapter.getItemChar(i));
            if (i == currentIndex) {
                textPaint.setColor(mParams.selectTextColor);
                int currentLeft = charRight + mParams.itemSize * i;
                if (mParams.selectDrawable != null) { //画当前选择的索引背景
                    mParams.selectDrawable.setBounds(currentLeft, charTop, currentLeft + mParams.itemSize, charBottom);
                    mParams.selectDrawable.draw(canvas);
                }
                if (showIndicator) {
                    float indicatorLeftOffset = (indicatorBgWidth - mParams.itemSize) * 0.5f;
                    if (mParams.indicatorBgDrawable != null) {//画指示背景
                        int indicatorLeft = currentLeft - (int) indicatorLeftOffset;
                        mParams.indicatorBgDrawable.setBounds(indicatorLeft, top, indicatorLeft + indicatorBgWidth, top + indicatorBgHeight);
                        mParams.indicatorBgDrawable.draw(canvas);
                    }
                    //画指示文字
                    float indicatorCX = currentLeft + indicatorTextRect.centerX() - indicatorLeftOffset;
                    canvas.drawText(text, indicatorCX, top + indicatorTextY, indicatorPaint);
                }
            } else {
                textPaint.setColor(mParams.textColor);
            }
            canvas.drawText(text, cx, cy, textPaint);
            cx += mParams.itemSize;
        }
    }

    //指示器隐藏
    private void dismissIndicator() {
        showIndicator = false;
        invalidate();
    }

    //适配器内容改变
    private void onDataSetChanged() {
        int itemCount = mAdapter.getItemCount();
        boolean needReLayout = this.itemCount != itemCount;
        this.itemCount = itemCount;
        if (needReLayout) requestLayout();
        invalidate();
        maxIndex = NO_INDEX;
        minIndex = Integer.MAX_VALUE;
    }

    public void setAdapter(@Nullable Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.setAdapterObserver(null);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            if (mObserver == null) {
                mObserver = new CharIndexViewObserver();
            }
            mAdapter.setAdapterObserver(mObserver);
            onDataSetChanged();
        }
    }

    public void setCurrentIndex(int currentIndex) {
        if (maxIndex < currentIndex) maxIndex = currentIndex;
        if (minIndex > currentIndex) minIndex = currentIndex;
        if (this.currentIndex == currentIndex) return;
        this.currentIndex = currentIndex;
        invalidate();
    }

    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
        invalidate();
    }

    public void setMinIndex(int minIndex) {
        this.minIndex = minIndex;
        invalidate();
    }

    public void setCharIndexParams(@NonNull CharIndexParams params) {
        this.mParams = params;
        initCharParams();
        initIndicatorParams();
        requestLayout();
        invalidate();
    }

    public void setOrientation(int orientation) {
        mParams.orientation = orientation;
        requestLayout();
        invalidate();
    }

    public void setItemSize(int itemSize) {
        mParams.itemSize = itemSize;
        initCharParams();
        requestLayout();
        invalidate();
    }

    @Nullable
    public Adapter getAdapter() {
        return mAdapter;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void addOnCharIndexChangedListener(@NonNull OnCharIndexChangedListener listener) {
        if (this.listeners == null) this.listeners = new ArrayList<>();
        this.listeners.add(listener);
    }

    public void removeOnCharIndexChangedListener(@NonNull OnCharIndexChangedListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    public CharIndexParams getCharIndexParams() {
        return mParams;
    }

    private class CharIndexViewObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            onDataSetChanged();
        }
    }

    static class DismissRunnable implements Runnable {

        final WeakReference<CharIndexView> viewWeakReference;

        DismissRunnable(CharIndexView view) {
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void run() {
            CharIndexView indexView = this.viewWeakReference.get();
            if (indexView != null) {
                indexView.dismissIndicator();
            }
        }
    }

    /**
     * CharIndexView参数类
     */
    public static class CharIndexParams {
        //水平与垂直布局
        @Orientation public int orientation = VERTICAL;
        //每个字符item大小
        public int itemSize = DEF_ITEM_SIZE;
        //text大小
        public float textSize = 24.f;
        //字符颜色
        public int textColor = Color.BLACK;
        //select索引颜色
        public int selectTextColor = Color.RED;
        //索引背景
        public Drawable selectDrawable;
        //类似TextView的水平时drawableLeft/垂直时drawableTop, 大小为itemSize-itemSize
        public Drawable startDrawable;
        //类似TextView的drawablePadding
        public int startDrawablePadding;

        //指示的文字大小
        public float indicatorTextSize = textSize * 3;
        public int indicatorTextColor = Color.BLACK;
        public Drawable indicatorBgDrawable;
        //indicator离CharIndex的填充距离
        public int indicatorPadding = 96;
        //自动校正与Sticky的大小
        public boolean indicatorAutoCheck = true;

        CharIndexParams(@NonNull Context context, @Nullable AttributeSet attrs) {
            if (attrs != null) {
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CharIndexView);
                textSize = a.getDimension(R.styleable.CharIndexView_indexTextSize, textSize);
                orientation = a.getInt(R.styleable.CharIndexView_indexOrientation, orientation);
                textColor = a.getColor(R.styleable.CharIndexView_indexTextColor, textColor);
                selectTextColor = a.getColor(R.styleable.CharIndexView_selectIndexTextColor, selectTextColor);
                selectDrawable = a.getDrawable(R.styleable.CharIndexView_selectIndexBgDrawable);
                itemSize = a.getDimensionPixelSize(R.styleable.CharIndexView_indexItemSize, itemSize);
                if (itemSize <= 0) {
                    itemSize = DEF_ITEM_SIZE;
                }
                startDrawable = a.getDrawable(R.styleable.CharIndexView_indexDrawableStart);
                startDrawablePadding = a.getDimensionPixelSize(R.styleable.CharIndexView_indexDrawablePadding, startDrawablePadding);

                indicatorTextSize = a.getDimension(R.styleable.CharIndexView_indicatorTextSize, indicatorTextSize);
                indicatorTextColor = a.getColor(R.styleable.CharIndexView_indicatorTextColor, indicatorTextColor);
                indicatorBgDrawable = a.getDrawable(R.styleable.CharIndexView_indicatorBgDrawable);
                indicatorPadding = a.getDimensionPixelSize(R.styleable.CharIndexView_indicatorPadding, indicatorPadding);

                indicatorAutoCheck = a.getBoolean(R.styleable.CharIndexView_indicatorAutoCheck, indicatorAutoCheck);
                a.recycle();
            }
        }

        public boolean isVertical() {
            return orientation == VERTICAL;
        }

        public int getStartDrawableSize() {
            if (startDrawable != null) {
                return itemSize + startDrawablePadding;
            }
            return 0;
        }
    }

    /**
     * CharIndexView适配器
     */
    public static abstract class Adapter {

        private DataSetObserver charsObserver;

        void setAdapterObserver(DataSetObserver observer) {
            synchronized (this) {
                charsObserver = observer;
            }
        }

        //刷新适配器
        public final void notifyDataSetChanged() {
            synchronized (this) {
                if (charsObserver != null) {
                    charsObserver.onChanged();
                }
            }
        }

        //字符索引数量
        public abstract int getItemCount();

        //当前字符索引
        public abstract char getItemChar(int position);
    }

    /**
     * 字符index监听
     */
    public interface OnCharIndexChangedListener {

        void onCharIndexChanged(CharIndexView indexView, int index);
    }
}
