package chen.you.stickyview;

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * author: you : 2020/10/29 支持START, CENTER, RIGHT , 水平, 垂直 多种方式和渐变效果显示
 * 或者使用 {@link chen.you.stickyview.StickyLayoutView}
 * 作QQ/W 86207610
 */
public class DrawingStickyManager extends StickyRecyclerView.StickyManager {
    //text画笔
    private final Paint textPaint;
    //画矩阵背景画笔
    private final Paint bgPaint;
    //画文本的Y坐标点
    private float textY;
    //Sticky中心大小
    private float halfSize;
    //背景矩阵
    private final Rect bgRect = new Rect();
    //如果有配置颜色过度时不为null
    private ColorEvaluator textColorEvaluator;
    //背景颜色过度器
    private ColorEvaluator bgColorEvaluator;

    public DrawingStickyManager() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
    }

    @Override
    public void setStickyParams(StickyRecyclerView.StickyParams params) {
        super.setStickyParams(params);
        textColorEvaluator = new ColorEvaluator(params.selectStickyTextColor, params.stickyTextColor);
        if (!textColorEvaluator.isSameColor) {
            textPaint.setColor(params.stickyTextColor);
        }
        bgColorEvaluator = new ColorEvaluator(params.selectStickyBgColor, params.stickyBgColor);
        if (!bgColorEvaluator.isSameColor) {
            bgPaint.setColor(params.stickyBgColor);
        }
        textPaint.setTextSize(params.stickyTextSize);
        halfSize = params.stickySize / 2.f;
        if (params.isVertical()) {
            switch (params.stickyGravity) {
                case StickyRecyclerView.StickyParams.CENTER:
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    break;
                case StickyRecyclerView.StickyParams.END:
                    textPaint.setTextAlign(Paint.Align.RIGHT);
                    break;
                case StickyRecyclerView.StickyParams.START:
                default:
                    textPaint.setTextAlign(Paint.Align.LEFT);
                    break;
            }
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textFontCenterY = (fm.bottom + fm.top) / 2.0f;
            textY = halfSize - textFontCenterY;
        } else {
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            switch (params.stickyGravity) {
                case StickyRecyclerView.StickyParams.CENTER:
                    textY = -(fm.bottom + fm.top) / 2.0f;
                    break;
                case StickyRecyclerView.StickyParams.END:
                    textY = -fm.bottom;
                    break;
                case StickyRecyclerView.StickyParams.START:
                    textY = -fm.top;
                default:
                    break;
            }
        }
    }

    @Override
    protected void drawSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv,
                               @NonNull String stickyItem, String nextStickyItem) {
        if (stickyParams.isVertical()) {
            drawVerticalSticker(c, srv, stickyItem, nextStickyItem);
        } else {
            drawHorizontalSticker(c, srv, stickyItem, nextStickyItem);
        }
    }

    private void drawVerticalSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv,
                                     @NonNull String stickyItem, String nextStickyItem) {
        int left = srv.getPaddingLeft();
        int top = srv.getPaddingTop();
        int right = srv.getWidth() - srv.getPaddingRight();
        int diff = 0;
        float scrollOffset = 0;
        if (nextStickyItem != null) {
            int nextTop = nextRect.top - top;
            diff = Math.max(stickyParams.stickySize - nextTop, 0);// > 0 时有偏移
            scrollOffset = diff / (float) stickyParams.stickySize;
        }
        bgRect.set(left, top, right, top + stickyParams.stickySize - diff);

        float tx;
        switch (stickyParams.stickyGravity) {
            case StickyRecyclerView.StickyParams.CENTER:
                tx = bgRect.centerX() + stickyParams.stickyMargin * 0.5f;
                break;
            case StickyRecyclerView.StickyParams.END:
                tx = right - stickyParams.stickyMargin;
                break;
            case StickyRecyclerView.StickyParams.START:
            default:
                tx = left + stickyParams.stickyMargin;
                break;
        }
        if (stickyParams.needGradient()) { //过度动画时需要画下一个Item
            if (nextStickyItem != null && diff > 0) {
                float nextScrollOffset = 1 - scrollOffset;
                bgPaint.setColor(bgColorEvaluator.getOffsetColor(nextScrollOffset));
                textPaint.setColor(textColorEvaluator.getOffsetColor(nextScrollOffset));
                c.drawRect(nextRect, bgPaint);
                float nextTextCenterY = top + textY - diff + nextRect.height();
                c.drawText(nextStickyItem, tx, nextTextCenterY, textPaint);
            }
            bgPaint.setColor(bgColorEvaluator.getOffsetColor(scrollOffset));
            textPaint.setColor(textColorEvaluator.getOffsetColor(scrollOffset));
        }
        c.drawRect(bgRect, bgPaint);
        c.drawText(stickyItem, tx, top + textY - diff, textPaint);
    }

    private void drawHorizontalSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv,
                                     @NonNull String stickyItem, String nextStickyItem) {
        int left = srv.getPaddingLeft();
        int top = srv.getPaddingTop();
        int bottom = srv.getHeight() - srv.getPaddingBottom();
        int diff = 0;
        float scrollOffset = 0;
        if (nextStickyItem != null) {
            int nextLeft = nextRect.left - left;
            diff = Math.max(stickyParams.stickySize - nextLeft, 0);// > 0 时有偏移
            scrollOffset = diff / (float) stickyParams.stickySize;
        }
        bgRect.set(left, top, left + stickyParams.stickySize - diff, bottom);

        float ty;
        switch (stickyParams.stickyGravity) {
            case StickyRecyclerView.StickyParams.CENTER:
                ty = bgRect.centerY() + textY + stickyParams.stickyMargin * 0.5f;
                break;
            case StickyRecyclerView.StickyParams.END:
                ty = srv.getHeight() - srv.getPaddingBottom() + textY - stickyParams.stickyMargin;
                break;
            case StickyRecyclerView.StickyParams.START:
            default:
                ty = srv.getPaddingTop() + textY + stickyParams.stickyMargin;
                break;
        }
        if (stickyParams.needGradient()) { //过度动画时需要画下一个Item
            if (nextStickyItem != null && diff > 0) {
                float nextScrollOffset = 1 - scrollOffset;
                bgPaint.setColor(bgColorEvaluator.getOffsetColor(nextScrollOffset));
                textPaint.setColor(textColorEvaluator.getOffsetColor(nextScrollOffset));
                c.drawRect(nextRect, bgPaint);
                c.drawText(nextStickyItem, nextRect.centerX(), ty, textPaint);
            }
            bgPaint.setColor(bgColorEvaluator.getOffsetColor(scrollOffset));
            textPaint.setColor(textColorEvaluator.getOffsetColor(scrollOffset));
        }
        c.drawRect(bgRect, bgPaint);
        c.drawText(stickyItem, left + halfSize - diff, ty, textPaint);
    }

    @Override
    protected void preDrawSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv, @NonNull String nextStickyItem) {
        //预画Sticker时,说明一定有设置渐变效果
        if (stickyParams.isVertical) {
            preDrawVerticalSticker(c, srv, nextStickyItem);
        } else {
            preDrawHorizontalSticker(c, srv, nextStickyItem);
        }
     }

    private void preDrawVerticalSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv, @NonNull String nextStickyItem) {
        int left = srv.getPaddingLeft();
        int top = srv.getPaddingTop();
        int nextTop = nextRect.top - top;
        int diff = Math.max(stickyParams.stickySize - nextTop, 0);// > 0 时有偏移
        if (diff > 0) {
            float nextScrollOffset = 1 - diff / (float) stickyParams.stickySize;
            bgPaint.setColor(bgColorEvaluator.getOffsetColor(nextScrollOffset));
            textPaint.setColor(textColorEvaluator.getOffsetColor(nextScrollOffset));
            c.drawRect(nextRect, bgPaint);
            float tx;
            switch (stickyParams.stickyGravity) {
                case StickyRecyclerView.StickyParams.CENTER:
                    tx = nextRect.centerX() + stickyParams.stickyMargin * 0.5f;
                    break;
                case StickyRecyclerView.StickyParams.END:
                    tx = srv.getWidth() - srv.getPaddingRight() - stickyParams.stickyMargin;
                    break;
                case StickyRecyclerView.StickyParams.START:
                default:
                    tx = left + stickyParams.stickyMargin;
                    break;
            }
            float ty = top + textY - diff + nextRect.height();
            c.drawText(nextStickyItem, tx, ty, textPaint);
        }
    }

    private void preDrawHorizontalSticker(@NonNull Canvas c, @NonNull StickyRecyclerView srv, @NonNull String nextStickyItem) {
        int left = srv.getPaddingLeft();
        int nextLeft = nextRect.left - left;
        int diff = Math.max(stickyParams.stickySize - nextLeft, 0);// > 0 时有偏移
        if (diff > 0) {
            float nextScrollOffset = 1 - diff / (float) stickyParams.stickySize;
            bgPaint.setColor(bgColorEvaluator.getOffsetColor(nextScrollOffset));
            textPaint.setColor(textColorEvaluator.getOffsetColor(nextScrollOffset));
            c.drawRect(nextRect, bgPaint);

            float ty;
            switch (stickyParams.stickyGravity) {
                case StickyRecyclerView.StickyParams.CENTER:
                    ty = nextRect.centerY() + textY + stickyParams.stickyMargin * 0.5f;
                    break;
                case StickyRecyclerView.StickyParams.END:
                    ty = srv.getHeight() - srv.getPaddingBottom() + textY - stickyParams.stickyMargin;
                    break;
                case StickyRecyclerView.StickyParams.START:
                default:
                    ty = srv.getPaddingTop() + textY + stickyParams.stickyMargin;
                    break;
            }
            c.drawText(nextStickyItem, nextRect.centerX(), ty, textPaint);
        }
    }

    /**
     * 颜色过度器
     */
    public static class ColorEvaluator extends ArgbEvaluator {
        //超始颜色
        private final int fromColor;
        //结束颜色
        private final int toColor;
        //过度颜色是否一样
        public final boolean isSameColor;

        public ColorEvaluator(@ColorInt int fromColor, @ColorInt int toColor) {
            this.fromColor = fromColor;
            this.toColor = toColor;
            isSameColor = fromColor == toColor;
        }

        /**
         * @param offset 0 ~ 1
         */
        public final int getOffsetColor(float offset) {
            if (isSameColor) return fromColor;
            return (int) this.evaluate(offset, fromColor, toColor);
        }
    }
}
