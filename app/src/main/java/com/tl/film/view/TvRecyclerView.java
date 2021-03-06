package com.tl.film.view;

import android.content.Context;
import android.graphics.Rect;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.tl.film.adapter.RecyclerCoverFlow_Adapter;
import com.tl.film.utils.LogUtil;

import java.util.Objects;

public class TvRecyclerView extends RecyclerView {
    private static final String TAG = "TvRecyclerView";
    private int mPosition;
    private static long lastClickTime = 0L;

    public TvRecyclerView(Context context) {
        this(context, null);
    }

    public TvRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TvRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        initView();
    }

    private void initView() {
        //父View和子View间处理焦点关系
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);

        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        /**
         防止RecyclerView刷新时焦点不错乱bug的步骤如下:
         (1)adapter执行setHasStableIds(true)方法
         (2)重写getItemId()方法,让每个view都有各自的id
         (3)RecyclerView的动画必须去掉
         */
        setItemAnimator(null);
    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean hasFocus() {
        return super.hasFocus();
    }

    @Override
    public boolean isInTouchMode() {
        // 解决4.4版本抢焦点的问题
//        if (Build.VERSION.SDK_INT == 19) {
        return !(hasFocus() && !super.isInTouchMode());
//        } else {
//            return super.isInTouchMode();
//        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();

        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;

        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft);
        final int offScreenRight = Math.max(0, childRight - parentRight);

        final int offScreenTop = Math.min(0, childTop - parentTop);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);

        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if (canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }

        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            postInvalidate();
            return true;
        }
        return false;
    }


    @Override
    public int getBaseline() {
        return -1;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
    }


    /**
     * 设置为0，这样可以防止View获取焦点的时候，ScrollView自动滚动到焦点View的位置
     */

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View view = getFocusedChild();
        if (null != view) {
            mPosition = getChildAdapterPosition(view) - getFirstVisiblePosition();
            if (mPosition < 0) {
                return i;
            } else {
                if (i == childCount - 1) {
                    if (mPosition > i) {
                        mPosition = i;
                    }
                    return mPosition;
                }
                if (i == mPosition) {
                    return childCount - 1;
                }
            }
        }
        return i;
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0)
            return 0;
        else
            return getChildAdapterPosition(getChildAt(0));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getRepeatCount() > 0) {
            return super.dispatchKeyEvent(event);
        }
        try {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int itemCount = Objects.requireNonNull(getAdapter()).getItemCount();
                View focusView = getFocusedChild();
                int focusPosition = Integer.valueOf(focusView.getTag().toString());
                int nextPosition = 0;
                RecyclerCoverFlow_Adapter adapter;
                View vh;
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_ENTER:
                        return super.dispatchKeyEvent(event);
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (isFastDoubleClick()) {
                            return true;
                        }
                        nextPosition = focusPosition + 1;
                        if (nextPosition == itemCount) {
                            nextPosition = 0;
                        }
                        LogUtil.e(TAG, "右键=" + nextPosition);
                        adapter = (RecyclerCoverFlow_Adapter) this.getAdapter();
                        LogUtil.e(TAG, "存入量：" + adapter.ViewHolderList.size());
                        vh = adapter.getViewHolder(nextPosition);
                        if (vh != null) {
                            vh.requestFocus();
                            smoothScrollToPosition(nextPosition);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (isFastDoubleClick()) {
                            return true;
                        }
                        nextPosition = focusPosition - 1;
                        if (nextPosition < 0) {
                            nextPosition = itemCount + nextPosition;
                        }
                        LogUtil.e(TAG, "左键=" + nextPosition);
                        adapter = (RecyclerCoverFlow_Adapter) this.getAdapter();
                        vh = adapter.getViewHolder(nextPosition);
                        if (vh != null) {
                            vh.requestFocus();
                            smoothScrollToPosition(nextPosition);
                        }
                        return true;
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            return super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    //防止Activity时,RecyclerView崩溃
    @Override
    protected void onDetachedFromWindow() {
        if (getLayoutManager() != null) {
            super.onDetachedFromWindow();
        }
    }

    /**
     * 延时操作
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        //第一次按
        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis();
            return false;
        }
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 300) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

}
