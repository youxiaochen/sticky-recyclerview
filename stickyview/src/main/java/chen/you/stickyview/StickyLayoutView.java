package chen.you.stickyview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import chen.you.expandable.ExpandableRecyclerView;
import chen.you.expandable.ExpandableRecyclerView.ChildInfo;
import chen.you.expandable.ExpandableRecyclerView.ChildViewHolder;
import chen.you.expandable.ExpandableRecyclerView.ExpandableAdapter;
import chen.you.expandable.ExpandableRecyclerView.GroupInfo;
import chen.you.expandable.ExpandableRecyclerView.GroupViewHolder;

/**
 * author: you : 2020/12/15
 * 作QQ/W 86207610
 */
public final class StickyLayoutView extends FrameLayout {

    ExpandableRecyclerView mRecyclerView;
    //是否已经附着到窗体中
    private boolean hasAttachedToWindow = false;
    //StickyManager
    private LayoutStickyManager mStickyManager;
    //StickyScrollCallback
    private StickyScrolledCallback mScrolledCallback;
    //StickyViewHolder
    private GroupViewHolder mStickyViewHolder;
    //滑动监听类
    private List<OnStickyScrollListener> mScrollListeners;
    //adapter
    private Adapter mAdapter;

    public StickyLayoutView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public StickyLayoutView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickyLayoutView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        mRecyclerView = new ExpandableRecyclerView(context);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mStickyManager = new LayoutStickyManager();
        mScrolledCallback = new StickyScrolledCallback();
        super.addView(mRecyclerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hasAttachedToWindow = true;
        mStickyManager.mScrollCallback = mScrolledCallback;
        mRecyclerView.addOnScrollListener(mStickyManager);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRecyclerView.removeOnScrollListener(mStickyManager);
        mStickyManager.mScrollCallback = null;
        hasAttachedToWindow = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mStickyViewHolder == null) {
            if (mAdapter != null) {
                mStickyViewHolder = mAdapter.onCreateStickyViewHolder(this);
                super.addView(mStickyViewHolder.itemView);
            }
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private class StickyScrolledCallback implements StickyScrollCallback {

        @Override
        public void onStickyScrolled(int diffOffset, boolean isVertical) {
            if (mStickyViewHolder != null) {
                int diff = Math.max(mStickyViewHolder.itemView.getHeight() - diffOffset, 0);// > 0 时有偏移
                if (isVertical) {
                    mStickyViewHolder.itemView.setTranslationY(-diff);
                } else {
                    mStickyViewHolder.itemView.setTranslationX(-diff);
                }
                if (mScrollListeners != null) {
                    for (OnStickyScrollListener listener : mScrollListeners) {
                        listener.onStickyScrolled(-diff, isVertical);
                    }
                }
            }
        }

        @Override
        public void onStickySelected(int position) {
            if (mStickyViewHolder != null) {
                if (position != RecyclerView.NO_POSITION) {
                    mAdapter.onBindStickyViewHolder(mStickyViewHolder, position);
                }
                if (mScrollListeners != null) {
                    for (OnStickyScrollListener listener : mScrollListeners) {
                        listener.onStickied(StickyLayoutView.this, position);
                    }
                }
            }
        }

        @Override
        public void onStickyStateChanged(boolean showState) {
            if (mStickyViewHolder != null) {
                mStickyViewHolder.itemView.setVisibility(showState ? View.VISIBLE : View.INVISIBLE);
                if (mScrollListeners != null) {
                    for (OnStickyScrollListener listener : mScrollListeners) {
                        listener.onStickyStateChanged(showState);
                    }
                }
            }
        }
    }

    public void setLayoutManager(@Nullable RecyclerView.LayoutManager layout) {
        mRecyclerView.setLayoutManager(layout);
    }

    public void setAdapter(@Nullable Adapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter) {
        this.mAdapter = adapter;
        if (this.mStickyViewHolder != null) {
            removeView(this.mStickyViewHolder.itemView);
            this.mStickyViewHolder = null;
        }
        mRecyclerView.setAdapter(adapter);
        requestLayout();
    }

    public void setAdapter(@Nullable Adapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                           @Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> headerAdapter,
                           @Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> footerAdapter) {
        setAdapter(ConcatAdapter.Config.DEFAULT, adapter, headerAdapter, footerAdapter);
    }

    public void setAdapter(ConcatAdapter.Config config,
                           @Nullable Adapter<? extends GroupViewHolder, ? extends ChildViewHolder> adapter,
                           @Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> headerAdapter,
                           @Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> footerAdapter) {
        this.mAdapter = adapter;
        this.mStickyViewHolder = null;
        mRecyclerView.setAdapter(config, adapter, headerAdapter, footerAdapter);
        requestLayout();
    }

    public void setLayoutStickyManager(@NonNull LayoutStickyManager stickyManager) {
        mStickyManager.mScrollCallback = null;
        mRecyclerView.removeOnScrollListener(mStickyManager);
        this.mStickyManager = stickyManager;
        if (this.hasAttachedToWindow) {
            mStickyManager.mScrollCallback = mScrolledCallback;
            mRecyclerView.addOnScrollListener(mStickyManager);
        }
    }

    //滑动到指定位置,无偏移, Adapter中的位置
    public void scrollToAdapterPosition(int position) {
        RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            ((LinearLayoutManager) lm).scrollToPositionWithOffset(position, 0);
        } else if (lm instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) lm).scrollToPositionWithOffset(position, 0);
        } else {
            mRecyclerView.scrollToPosition(position);
        }
    }

    //滑动到指定位置,无偏移, StickyAdapter中的位置
    public void scrollToBindingPosition(int bindingPosition) {
        scrollToAdapterPosition(bindingPosition + mRecyclerView.getHeaderCount());
    }

    public void scrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    public void addOnStickyScrollListener(OnStickyScrollListener listener) {
        if (mScrollListeners == null) mScrollListeners = new ArrayList<>();
        mScrollListeners.add(listener);
    }

    public void removeOnStickyScrollListener(OnStickyScrollListener listener) {
        if (mScrollListeners != null) {
            mScrollListeners.remove(listener);
        }
    }

    @Nullable public Adapter<?, ?> getAdapter() {
        return mAdapter;
    }

    @NonNull public LayoutStickyManager getStickyManager() {
        return mStickyManager;
    }

    public int getHeaderCount() {
        return mRecyclerView.getHeaderCount();
    }

    public int getFooterCount() {
        return mRecyclerView.getFooterCount();
    }

    public boolean isGroupType(int expandablePosition) {
        return mRecyclerView.isGroupTypeByBindingPosition(expandablePosition);
    }

    @NonNull public ExpandableRecyclerView getExpandableRecyclerView() {
        return mRecyclerView;
    }

    /**
     * StickyLayoutView Adapter
     */
    public static abstract class Adapter<GVH extends GroupViewHolder, CVH extends ChildViewHolder> extends ExpandableAdapter<GVH, CVH> {

        /**
         * 创建StickyViewHolder, 不与ExpandableRecyclerView中的GroupViewHolder混合复用
         * 若要监听点击事件亦可在此方法中设置
         */
        public abstract @NonNull GVH onCreateStickyViewHolder(@NonNull ViewGroup parent);

        //绑定当前StickyViewHolder
        public abstract void onBindStickyViewHolder(@NonNull GVH holder, int groupPos);

        @Override
        public boolean groupCanClick() {
            return false;
        }
    }

    /**
     * Sticky滑动回调
     */
    interface StickyScrollCallback {

        void onStickyScrolled(int diffOffset, boolean isVertical);

        void onStickySelected(int position);

        void onStickyStateChanged(boolean showState);
    }

    /**
     * Sticky滑动监听
     */
    public static abstract class OnStickyScrollListener {

        //Sticky滑动偏移
        public void onStickyScrolled(int diff, boolean isVertical) {
        }

        //当前显示的Sticky的Position
        public void onStickied(@NonNull StickyLayoutView stickyLayoutView, int groupPos) {
        }

        //显示状态改变
        public void onStickyStateChanged(boolean showState) {
        }
    }

    /**
     * 布局型的StickyManager, 为了不入侵Adapter及GroupViewHolder的代码, 布局方式不支持背景颜色字体颜色渐变动画效果
     * 如果需要渐变动画效果可参考{@link DrawingStickyManager}的原理
     */
    public static class LayoutStickyManager extends RecyclerView.OnScrollListener {
        //临时GroupInfo对象, 避免大量创建销毁
        final GroupInfo mTmpGroupInfo = GroupInfo.obtainEmpty();
        //临时ChildInfo对象
        final ChildInfo mTmpChildInfo = ChildInfo.obtainEmpty();
        //计算下一个stick的矩阵区域判断是否需要偏移
        private final Rect nextRect = new Rect();
        //当前sticky的Group position
        private int stickyGroupPos = RecyclerView.NO_POSITION;
        //滑动回调
        private StickyScrollCallback mScrollCallback;
        //显示Sticky状态
        boolean showState;

        @Override
        public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
            performScrollStateChanged(onScrolledState(rv, dx, dy));
        }

        boolean onScrolledState(@NonNull RecyclerView rv, int dx, int dy) {
            if (!(rv instanceof ExpandableRecyclerView) || rv.getLayoutManager() == null) return false;
            RecyclerView.LayoutManager lm = rv.getLayoutManager();
            ExpandableRecyclerView erv = (ExpandableRecyclerView) rv;
            if (!(erv.getExpandableAdapter() instanceof Adapter)) return false;
            int firstPos = findFirstVisibleItemPosition(lm);
            if (firstPos < 0) return false;
            RecyclerView.ViewHolder vh = erv.findViewHolderForAdapterPosition(firstPos);
            if (vh instanceof GroupViewHolder) {
                GroupInfo groupInfo = erv.findGroupInfoByPosition(vh.getAbsoluteAdapterPosition(), mTmpGroupInfo);
                return stickViewHolder(erv, groupInfo, dx == 0);
            } else if (vh instanceof ChildViewHolder) {
                ChildInfo childInfo = erv.findChildInfoByPosition(vh.getAbsoluteAdapterPosition(), mTmpChildInfo);
                return stickViewHolder(erv, childInfo == null ? null : childInfo.group, dx == 0);
            }
            return false;
        }

        boolean stickViewHolder(ExpandableRecyclerView erv, GroupInfo groupInfo, boolean isVertical) {
            if (groupInfo == null) return false;//?不会为null
            Adapter<?, ?> adapter = (Adapter<?, ?>) erv.getExpandableAdapter();
            if (groupInfo.getIndex() >= adapter.getGroupCount() - 1) { //最后一项组
                return scrollSticker(erv, groupInfo.getIndex(), null, isVertical);
            }
            int childCount = adapter.getChildCount(groupInfo.getIndex());
            int nextPosition = groupInfo.getPosition() + childCount + 1 + erv.getHeaderCount();
            RecyclerView.ViewHolder next = erv.findViewHolderForAdapterPosition(nextPosition);
            if (!(next instanceof GroupViewHolder)) {//下一组还未显示到界面上
                return scrollSticker(erv, groupInfo.getIndex(), null, isVertical);
            }
            nextRect.set(next.itemView.getLeft(), next.itemView.getTop(), next.itemView.getRight(), next.itemView.getBottom());
            return scrollSticker(erv, groupInfo.getIndex(), nextRect, isVertical);
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

        //分发onStickyStateChanged
        protected void performScrollStateChanged(boolean showState) {
            if (!showState && stickyGroupPos != RecyclerView.NO_POSITION) {
                stickyGroupPos = RecyclerView.NO_POSITION;
                if (mScrollCallback != null) {
                    mScrollCallback.onStickySelected(RecyclerView.NO_POSITION);
                }
            }
            if (this.showState == showState) return;
            this.showState = showState;
            if (mScrollCallback != null) {
                mScrollCallback.onStickyStateChanged(showState);
            }
        }

        //分发onStickied
        protected void performStickied(int position) {
            if (mScrollCallback != null) {
                mScrollCallback.onStickySelected(position);
            }
        }

        //perform onStickyScrolled
        protected void performScroll(int diffOffset, boolean isVertical) {
            if (mScrollCallback != null) {
                mScrollCallback.onStickyScrolled(diffOffset, isVertical);
            }
        }

        //可重写此方法
        protected boolean scrollSticker(@NonNull ExpandableRecyclerView srv, int groupPos, Rect nextRect, boolean isVertical) {
            if (this.stickyGroupPos != groupPos) {
                this.stickyGroupPos = groupPos;
                performStickied(groupPos);
            }
            int diffOffset = Integer.MAX_VALUE;
            if (nextRect != null) {
                if (isVertical) {
                    diffOffset = nextRect.top - srv.getPaddingTop();
                } else {
                    diffOffset = nextRect.left - srv.getPaddingLeft();
                }
            }
            performScroll(diffOffset, isVertical);
            return true;
        }
    }
}
