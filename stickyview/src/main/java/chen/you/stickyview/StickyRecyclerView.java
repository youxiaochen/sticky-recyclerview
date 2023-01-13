package chen.you.stickyview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import chen.you.expandable.ExpandableRecyclerView;

/**
 * author: you : 2020/10/28 StickyHeader By ExpandableRecyclerView
 * 作QQ/W 86207610
 */
public class StickyRecyclerView extends ExpandableRecyclerView {
    //Sticky想关参数
    private StickyParams mStickyParams;
    //DrawingStickyManager
    private StickyManager mStickyManager;
    //WheelView是否已经附着到窗体中
    private boolean hasAttachedToWindow = false;
    //Sticky监听
    private List<OnStickiedListener> mStickiedListeners;
    //Sticky回调,用分发监听
    private final StickiedCallback mCallback = groupPos -> {
        if (mStickiedListeners != null) {
            for (OnStickiedListener listener : mStickiedListeners) {
                listener.onStickied(this, groupPos);
            }
        }
    };

    public StickyRecyclerView(@NonNull Context context) {
        super(context);
        initialize(new StickyParams(context, null), new DrawingStickyManager());
    }

    //用于代码生成控件
    public StickyRecyclerView(@NonNull Context context, @NonNull StickyParams params) {
        this(context, params, new DrawingStickyManager());
    }

    //用于代码生成控件
    public StickyRecyclerView(@NonNull Context context, @NonNull StickyParams params, @NonNull StickyManager stickyManager) {
        super(context);
        initialize(params, stickyManager);
    }

    public StickyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(new StickyParams(context, attrs), new DrawingStickyManager());
    }

    public StickyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(new StickyParams(context, attrs), new DrawingStickyManager());
    }

    private void initialize(@NonNull StickyParams params, @NonNull StickyManager stickyManager) {
        this.mStickyParams = params;
        this.mStickyManager = stickyManager;
        mStickyParams.isVertical = mStickyManager.isVertical(getLayoutManager());
        mStickyManager.setStickyParams(mStickyParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hasAttachedToWindow = true;
        mStickyManager.mStickiedCallback = mCallback;
        addItemDecoration(mStickyManager);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStickyManager.mStickiedCallback = null;
        removeItemDecoration(mStickyManager);
        hasAttachedToWindow = false;
    }

    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        super.setLayoutManager(layout);
        if (mStickyParams != null && mStickyManager != null) {
            mStickyParams.isVertical = mStickyManager.isVertical(layout);
            mStickyManager.setStickyParams(mStickyParams);
        }
    }

    @NonNull
    public StickyManager getStickyManager() {
        return mStickyManager;
    }

    @NonNull
    public StickyParams getStickyParams() {
        return mStickyParams;
    }

    @Nullable
    public StickyAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> getStickyAdapter() {
        return (StickyAdapter<? extends GroupViewHolder, ? extends ChildViewHolder>) getExpandableAdapter();
    }

    @Override
    public void setAdapter(ExpandableAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter) {
        throw new RuntimeException("Use setStickyAdapter(StickAdapter)");
    }

    @Override
    public void setAdapter(ExpandableAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                           Adapter<? extends ViewHolder> headerAdapter) {
        throw new RuntimeException("Use setStickyAdapter(StickAdapter, Adapter)");
    }

    @Override
    public void setAdapter(ExpandableAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                           Adapter<? extends ViewHolder> headerAdapter,
                           Adapter<? extends ViewHolder> footerAdapter) {
        throw new RuntimeException("Use setStickyAdapter(StickAdapter, Adapter, Adapter)");
    }

    @Override
    public void setAdapter(ConcatAdapter.Config config,
                           ExpandableAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                           Adapter<? extends ViewHolder> headerAdapter,
                           Adapter<? extends ViewHolder> footerAdapter) {
        throw new RuntimeException("Use setStickyAdapter(ConcatAdapter.Config, StickAdapter, Adapter, Adapter)");
    }

    public void setStickyAdapter(@Nullable StickyAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter) {
        super.setAdapter(adapter);
    }

    public void setStickyAdapter(@Nullable StickyAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                                 Adapter<? extends ViewHolder> headerAdapter) {
        setStickyAdapter(adapter, headerAdapter, null);
    }

    public void setStickyAdapter(StickyAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                                 @Nullable Adapter<? extends ViewHolder> headerAdapter,
                                 @Nullable Adapter<? extends ViewHolder> footerAdapter) {
        setStickyAdapter(ConcatAdapter.Config.DEFAULT, adapter, headerAdapter, footerAdapter);
    }

    public void setStickyAdapter(ConcatAdapter.Config config,
                                 @Nullable StickyAdapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                                 @Nullable Adapter<? extends ViewHolder> headerAdapter,
                                 @Nullable Adapter<? extends ViewHolder> footerAdapter) {
        super.setAdapter(config, adapter, headerAdapter, footerAdapter);
    }

    /**
     * 设置StickyManager
     * @param stickyManager 可自定义方式
     */
    public void setStickyManager(@NonNull StickyManager stickyManager) {
        mStickyManager.mStickiedCallback = null;
        removeItemDecoration(mStickyManager);
        stickyManager.setStickyParams(mStickyParams);
        this.mStickyManager = stickyManager;
        if (this.hasAttachedToWindow) {
            this.mStickyManager.mStickiedCallback = mCallback;
            addItemDecoration(mStickyManager);
        }
    }

    public void setStickyParams(@NonNull StickyParams params) {
        params.isVertical = mStickyManager.isVertical(getLayoutManager());
        this.mStickyParams = params;
        mStickyManager.setStickyParams(params);
        invalidate();
    }

    //当前Sticky的索引位置
    public int getCurrentStickyIndex() {
        return mStickyManager.currentStickiedPosition;
    }

    //滑动到指定位置,无偏移, Adapter中的位置
    public void scrollToAdapterPosition(int position) {
        LayoutManager lm = getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            ((LinearLayoutManager) lm).scrollToPositionWithOffset(position, 0);
        } else if (lm instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) lm).scrollToPositionWithOffset(position, 0);
        } else {
            scrollToPosition(position);
        }
    }

    //滑动到指定位置,无偏移, StickyAdapter中的位置
    public void scrollToBindingPosition(int bindingPosition) {
        scrollToAdapterPosition(bindingPosition + getHeaderCount());
    }

    public void addOnStickiedListener(@NonNull OnStickiedListener listener) {
        if (mStickiedListeners == null) mStickiedListeners = new ArrayList<>();
        mStickiedListeners.add(listener);
    }

    public void removeOnStickiedListener(@NonNull OnStickiedListener listener) {
        if (mStickiedListeners != null) {
            mStickiedListeners.remove(listener);
        }
    }

    /**
     * StickyAdapter 适配器
     */
    public static abstract class StickyAdapter<GVH extends GroupViewHolder, CVH extends ChildViewHolder>
            extends ExpandableAdapter<GVH, CVH> {

        @Override
        public boolean groupCanClick() {
            return false;
        }

        @Override
        public void onGroupViewAttachedToWindow(@NonNull GVH holder) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if ((params instanceof StaggeredGridLayoutManager.LayoutParams)) {
                StaggeredGridLayoutManager.LayoutParams sParams = (StaggeredGridLayoutManager.LayoutParams) params;
                sParams.setFullSpan(true);
            }
        }

        //获取Sticky的item对象
        @NonNull
        public abstract String getStickerItem(int groupPos);
    }

    //StickyRecycler相关的参数配置
    public static class StickyParams {
        public static final int START = 0;
        public static final int CENTER = 1;
        public static final int END = 2;

        @IntDef({START, CENTER, END})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Gravity {
        }

        //默认sticky大小
        public static final int DEF_STICKY_SIZE = 96;
        //默认字体大小
        public static final int DEF_STICK_TEXT_SIZE = 48;
        //sticky矩阵大小, 垂直方向时为高度, 横方向时为宽度
        public int stickySize = DEF_STICKY_SIZE;
        //文字大小
        public float stickyTextSize = DEF_STICK_TEXT_SIZE;
        //过度文字颜色
        public int stickyTextColor = Color.BLACK;
        //sticky状态时文字颜色
        public int selectStickyTextColor = Color.BLACK;
        //矩阵过度背景颜色
        public int stickyBgColor = Color.GRAY;
        //sticky状态时的背景颜色
        public int selectStickyBgColor = Color.GRAY;
        //sticky文字位置方式
        @Gravity
        public int stickyGravity = START;
        /**
         * stickyGravity=LEFT时相当于marginLeft, =CENTER时相当于(width - margin) / 2
         */
        public float stickyMargin = 0;
        //是否为垂直方向
        boolean isVertical = true;

        public final boolean isVertical() {
            return isVertical;
        }

        StickyParams(@NonNull Context context, @Nullable AttributeSet attrs) {
            if (attrs != null) {
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StickyRecyclerView);
                stickySize = a.getDimensionPixelSize(R.styleable.StickyRecyclerView_stickySize, stickySize);
                stickyTextSize = a.getDimension(R.styleable.StickyRecyclerView_stickyTextSize, stickyTextSize);
                stickyGravity = a.getInt(R.styleable.StickyRecyclerView_stickyGravity, stickyGravity);
                stickyMargin = a.getDimension(R.styleable.StickyRecyclerView_stickyMargin, stickyMargin);

                int[] stateSet = {android.R.attr.state_first};
                int[] notNeedStateSet = {android.R.attr.stateNotNeeded};
                if (a.hasValue(R.styleable.StickyRecyclerView_stickyTextColor)) {
                    ColorStateList textColors = a.getColorStateList(R.styleable.StickyRecyclerView_stickyTextColor);
                    selectStickyTextColor = stickyTextColor = textColors.getDefaultColor();
                    if (textColors.isStateful()) {
                        stickyTextColor = textColors.getColorForState(notNeedStateSet, stickyTextColor);
                        selectStickyTextColor = textColors.getColorForState(stateSet, selectStickyTextColor);
                    }
                }
                if (a.hasValue(R.styleable.StickyRecyclerView_stickyBackgroundColor)) {
                    ColorStateList bgColors = a.getColorStateList(R.styleable.StickyRecyclerView_stickyBackgroundColor);
                    selectStickyBgColor = stickyBgColor = bgColors.getDefaultColor();
                    if (bgColors.isStateful()) {
                        stickyBgColor = bgColors.getColorForState(notNeedStateSet, stickyBgColor);
                        selectStickyBgColor = bgColors.getColorForState(stateSet, selectStickyBgColor);
                    }
                }
                a.recycle();
            }
        }

        //有配置颜色过度时,需要渐变动画
        public final boolean needGradient() {
            return selectStickyTextColor != stickyTextColor || stickyBgColor != selectStickyBgColor;
        }
    }

    //Sticky callback
    interface StickiedCallback {

        void onStickied(int groupPos);
    }

    /**
     * 绘制型的StickyManager, 支持相邻Sticky时的渐变动画效果
     */
    public abstract static class StickyManager extends ItemDecoration {
        //some params
        protected StickyParams stickyParams;
        //临时GroupInfo对象, 避免大量创建销毁
        final GroupInfo mTmpGroupInfo = GroupInfo.obtainEmpty();
        //临时ChildInfo对象
        final ChildInfo mTmpChildInfo = ChildInfo.obtainEmpty();
        //渐变效果时,需要画下一个sticky的区域内容
        protected final Rect nextRect = new Rect();
        //监听Sticky
        private StickiedCallback mStickiedCallback;
        //上一次的位置
        private int currentStickiedPosition = RecyclerView.NO_POSITION;

        void setStickyParams(StickyParams params) {
            this.stickyParams = params;
            currentStickiedPosition = RecyclerView.NO_POSITION;
        }

        @Override
        public final void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
        }

        @Override
        public final void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
            if (stickyParams == null || parent.getLayoutManager() == null) return;
            if (!(parent instanceof StickyRecyclerView)) return;
            StickyRecyclerView srv = (StickyRecyclerView) parent;
            if (srv.getExpandableAdapter() == null) return;
            RecyclerView.LayoutManager lm = parent.getLayoutManager();
            int firstPos = findFirstVisibleItemPosition(lm);
            if (firstPos < 0) return;
            RecyclerView.ViewHolder vh = srv.findViewHolderForAdapterPosition(firstPos);
            if (vh instanceof GroupViewHolder) {
                GroupInfo groupInfo = srv.findGroupInfoByPosition(vh.getAbsoluteAdapterPosition(), mTmpGroupInfo);
                stickViewHolder(c, srv, groupInfo);
            } else if (vh instanceof ChildViewHolder) {
                ChildInfo childInfo = srv.findChildInfoByPosition(vh.getAbsoluteAdapterPosition(), mTmpChildInfo);
                stickViewHolder(c, srv, childInfo == null ? null : childInfo.group);
            } else {
                if (stickyParams.needGradient()) {//渐变处理下一个
                    RecyclerView.ViewHolder nextVH = srv.findViewHolderForAdapterPosition(firstPos + 1);
                    if (nextVH instanceof GroupViewHolder) {
                        preStickViewHolder(c, srv, nextVH);
                    }
                }
                performStickerListener(RecyclerView.NO_POSITION);
            }
        }

        /**
         * 处理Sticky项和和即将Sticky的项
         */
        void stickViewHolder(Canvas c, StickyRecyclerView srv, GroupInfo groupInfo) {
            if (groupInfo == null) return; //?不会为null
            StickyAdapter<?, ?> adapter = (StickyAdapter<?, ?>) srv.getExpandableAdapter();
            String stickyItem = adapter.getStickerItem(groupInfo.getIndex());
            if (groupInfo.getIndex() >= adapter.getGroupCount() - 1) { //最后一组
                drawSticker(c, srv, stickyItem, null);
                performStickerListener(groupInfo.getIndex());
                return;
            }
            int childCount = adapter.getChildCount(groupInfo.getIndex());
            int nextPosition = groupInfo.getPosition() + childCount + 1 + srv.getHeaderCount();
            RecyclerView.ViewHolder next = srv.findViewHolderForAdapterPosition(nextPosition);
            if (!(next instanceof GroupViewHolder)) {//该组的child数量多下一组还未显示到界面上
                drawSticker(c, srv, stickyItem, null);
                performStickerListener(groupInfo.getIndex());
                return;
            }
            nextRect.set(next.itemView.getLeft(), next.itemView.getTop(), next.itemView.getRight(), next.itemView.getBottom());
            String nextStickyItem = adapter.getStickerItem(groupInfo.getIndex() + 1);
            drawSticker(c, srv, stickyItem, nextStickyItem);
            performStickerListener(groupInfo.getIndex());
        }

        /**
         * 处理有Header且Header下是Group时的预处理过度效果
         */
        void preStickViewHolder(Canvas c, StickyRecyclerView srv, ViewHolder vh) {
            GroupInfo groupInfo = srv.findGroupInfoByPosition(vh.getAbsoluteAdapterPosition(), mTmpGroupInfo);
            if (groupInfo == null) return;//null ?
            nextRect.set(vh.itemView.getLeft(), vh.itemView.getTop(), vh.itemView.getRight(), vh.itemView.getBottom());
            StickyAdapter<?, ?> adapter = (StickyAdapter<?, ?>) srv.getExpandableAdapter();
            preDrawSticker(c, srv, adapter.getStickerItem(groupInfo.getIndex()));
        }

        //防止滑动时的大量临时对象
        protected int[] staggeredPositions;

        //自定义的LayoutManager可以重写此方法
        protected int findFirstVisibleItemPosition(RecyclerView.LayoutManager lm) {
            if (lm instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
            } else if (lm instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager glm = (StaggeredGridLayoutManager) lm;
                if (staggeredPositions == null || staggeredPositions.length != glm.getSpanCount()) {
                    staggeredPositions = new int[glm.getSpanCount()];
                }
                glm.findFirstVisibleItemPositions(staggeredPositions);
                int minPosition = Integer.MAX_VALUE;
                for (int pos : staggeredPositions) {
                    if (pos < minPosition) {
                        minPosition = pos;
                    }
                }
                return minPosition;
            }
            return -1;
        }

        /**
         * 判断是否为垂直方向布局,自定义的LayoutManager可以重写此方法
         */
        protected boolean isVertical(RecyclerView.LayoutManager lm) {
            if (lm instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) lm).getOrientation() == LinearLayoutManager.VERTICAL;
            } else if (lm instanceof StaggeredGridLayoutManager) {
                return ((StaggeredGridLayoutManager) lm).getOrientation() == StaggeredGridLayoutManager.VERTICAL;
            }
            return true;
        }

        private void performStickerListener(int position) {
            if (currentStickiedPosition != position) {
                currentStickiedPosition = position;
                if (mStickiedCallback != null) {
                    mStickiedCallback.onStickied(currentStickiedPosition);
                }
            }
        }

        /**
         * 画Sticky
         *
         * @param stickyItem     当前sticky 不会null
         * @param nextStickyItem 下一个sticky 可能为null
         */
        protected abstract void drawSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv,
                                            @NonNull String stickyItem, @Nullable String nextStickyItem);

        /**
         * 当StickyRecyclerView有Header时且有过度动画时, 在第一个显示前需要预画动画效果
         *
         * @param nextStickyItem 即将需要Sticky的item
         */
        protected abstract void preDrawSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv,
                                               @NonNull String nextStickyItem);
    }

    /**
     * Sticky监听
     */
    public interface OnStickiedListener {

        /**
         * @param groupPos 当前没有Sticky时, -1即RecyclerView.NO_POSITION
         */
        void onStickied(StickyRecyclerView srv, int groupPos);
    }
}
