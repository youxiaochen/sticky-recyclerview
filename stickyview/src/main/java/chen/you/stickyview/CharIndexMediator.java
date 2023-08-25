package chen.you.stickyview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

import chen.you.expandable.ExpandableRecyclerView;

/**
 * author: you : 2020/12/15 CharIndexView与StickyRecyclerView组合器
 * 作QQ/W 86207610
 */
public final class CharIndexMediator {
    //Sticky
    @NonNull private final StickyRecyclerView stickyRecyclerView;
    //Char Indicator
    @NonNull private final CharIndexView charIndexView;
    //是否自动刷新绑定, 用此方式时可以不用设置CharIndexView.Adapter
    private final boolean autoRefresh;
    //Sticky adapter
    StickyRecyclerView.StickyAdapter<?, ?> adapter;

    CharIndexChangedListener charIndexChangedListener;
    StickiedListener stickiedListener;
    private boolean attached;
    //StickyAdapter观察者, 自动刷新时只需要添加此观察者即可
    private StickyDataObserver stickyDataObserver;
    //用于自动生命周期管理start/stop
    private LifecycleOwner mLifecycleOwner;
    private AttachObserver attachObserver;

    public CharIndexMediator(@NonNull StickyRecyclerView stickyRecyclerView,
                             @NonNull CharIndexView charIndexView) {
        this(stickyRecyclerView, charIndexView, true);
    }

    /**
     * 是否绑定自动刷新数据
     * @param autoRefresh false时 需要给CharIndexView设置adapter,且手动notifyDataSetChanged
     */
    public CharIndexMediator(@NonNull StickyRecyclerView stickyRecyclerView,
                             @NonNull CharIndexView charIndexView,
                             boolean autoRefresh) {
        this.stickyRecyclerView = stickyRecyclerView;
        this.charIndexView = charIndexView;
        this.autoRefresh = autoRefresh;
    }

    /**
     * attach前, StickyRecyclerView要setAdapter
     * 在onDestroy时当调用{@link #detach()}方法
     */
    public void attach() {
        if (attached) return;
        adapter = stickyRecyclerView.getStickyAdapter();
        if (adapter == null) {
            throw new IllegalStateException("CharIndexMediator attached before StickyRecyclerView has adapter");
        }
        attached = true;
        if (autoRefresh) {
            CharIndexAdapter indexAdapter = new CharIndexAdapter(adapter);
            charIndexView.setAdapter(indexAdapter);
            stickyDataObserver = new StickyDataObserver();
            adapter.registerAdapterDataObserver(stickyDataObserver);
        }

        charIndexChangedListener = new CharIndexChangedListener(stickyRecyclerView);
        charIndexView.addOnCharIndexChangedListener(charIndexChangedListener);
        stickiedListener = new StickiedListener(charIndexView);
        stickyRecyclerView.addOnStickiedListener(stickiedListener);

        charIndexView.setCurrentIndex(stickyRecyclerView.getCurrentStickyIndex());
    }

    /**
     * attach前, StickyRecyclerView要setAdapter
     * 自动管理不需要手动detach()
     */
    public void attach(@Nullable LifecycleOwner lifecycleOwner) {
        if (mLifecycleOwner == lifecycleOwner) return;
        if (mLifecycleOwner != null && attachObserver != null) {
            mLifecycleOwner.getLifecycle().removeObserver(attachObserver);
        }
        mLifecycleOwner = lifecycleOwner;
        if (lifecycleOwner != null) {
            if (lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) return;
            if (attachObserver == null) {
                attachObserver = new AttachObserver(this);
            }
            lifecycleOwner.getLifecycle().addObserver(attachObserver);
        }
        attach();
    }

    //onDestroy时分离
    public void detach() {
        if (!attached) return;
        stickyRecyclerView.removeOnStickiedListener(stickiedListener);
        charIndexView.removeOnCharIndexChangedListener(charIndexChangedListener);
        if (autoRefresh) {
            adapter.unregisterAdapterDataObserver(stickyDataObserver);
            stickyDataObserver = null;
        }

        stickiedListener = null;
        charIndexChangedListener = null;
        adapter = null;
        attached = false;
    }

    void dataSetChanged() {
        CharIndexView.Adapter adapter = charIndexView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    //自动刷新时的适配器
    static class CharIndexAdapter extends CharIndexView.Adapter {

        final StickyRecyclerView.StickyAdapter<?, ?> adapter;

        CharIndexAdapter(StickyRecyclerView.StickyAdapter<?, ?> adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getItemCount() {
            return adapter.getGroupCount();
        }

        @Override
        public char getItemChar(int position) {
            String stickerItem = adapter.getStickerItem(position);
            return stickerItem.isEmpty() ? ' ' : stickerItem.charAt(0);
        }
    }

    //监听CharIndexView的索引
    static class CharIndexChangedListener implements CharIndexView.OnCharIndexChangedListener {

        final StickyRecyclerView stickyRecyclerView;
        //tmp避免大量创建
        final ExpandableRecyclerView.GroupInfo tmpInfo = ExpandableRecyclerView.GroupInfo.obtainEmpty();

        CharIndexChangedListener(StickyRecyclerView stickyRecyclerView) {
            this.stickyRecyclerView = stickyRecyclerView;
        }

        @Override
        public void onCharIndexChanged(CharIndexView indexView, int index) {
            if (index == CharIndexView.NO_INDEX) {
                stickyRecyclerView.scrollToPosition(0);
            } else {
                ExpandableRecyclerView.GroupInfo info = stickyRecyclerView.findGroupInfoByIndex(index, tmpInfo);
                if (info != null) {
                    stickyRecyclerView.scrollToBindingPosition(info.getPosition());
                }
            }
        }
    }

    //监听StickyRecyclerView
    static class StickiedListener implements StickyRecyclerView.OnStickiedListener {
        final CharIndexView charIndexView;

        StickiedListener(CharIndexView charIndexView) {
            this.charIndexView = charIndexView;
        }

        @Override
        public void onStickied(StickyRecyclerView srv, int groupPos) {
            charIndexView.setCurrentIndex(groupPos);
        }
    }

    //StickyAdapter适配器观察者
    private class StickyDataObserver extends ExpandableRecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            dataSetChanged();
        }

        @Override
        public void onGroupRangeChanged(int groupStart, int itemCount, boolean childChanged, Object payload) {
            dataSetChanged();
        }

        @Override
        public void onGroupRangeInserted(int groupPosStart, int itemCount, boolean expand) {
            dataSetChanged();
        }

        @Override
        public void onGroupRangeRemoved(int groupPosStart, int itemCount) {
            dataSetChanged();
        }

        @Override
        public void onChildRangeChanged(int groupPos, int posStart, int itemCount, Object payload) {
            dataSetChanged();
        }

        @Override
        public void onChildRangeInserted(int groupPos, int posStart, int itemCount) {
            dataSetChanged();
        }

        @Override
        public void onChildRangeRemoved(int groupPos, int posStart, int itemCount) {
            dataSetChanged();
        }
    }

    static class AttachObserver implements LifecycleEventObserver {

        final WeakReference<CharIndexMediator> mediatorWeakReference;

        AttachObserver(CharIndexMediator mediator) {
            this.mediatorWeakReference = new WeakReference<>(mediator);
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                CharIndexMediator mediator = mediatorWeakReference.get();
                owner.getLifecycle().removeObserver(this);
                if (mediator != null) {
                    mediator.detach();
                    mediator.mLifecycleOwner = null;
                }
            }
        }
    }
}
