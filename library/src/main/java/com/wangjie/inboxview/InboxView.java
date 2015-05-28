package com.wangjie.inboxview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.nineoldandroids.animation.*;
import com.wangjie.inboxview.util.IVUtil;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 5/27/15.
 */
public abstract class InboxView extends LinearLayout {
    private static final long DEFAULT_ANIMATION_DURATION = 200l;

    public interface OnInboxViewListener {
        void onInboxViewClosed();
    }

    private OnInboxViewListener onInboxViewListener;

    public void setOnInboxViewListener(OnInboxViewListener onInboxViewListener) {
        this.onInboxViewListener = onInboxViewListener;
    }

    private static final String TAG = InboxView.class.getSimpleName();

    public InboxView(Context context) {
        super(context);
        init();
    }

    private InboxView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private InboxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private InboxView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected Bitmap previewBm;

    private View topVg;
    private ImageView topShadowIv;
    private View bottomVg;
    private ImageView bottomShadowIv;

    private ViewGroup contentVg;

    private int topPreviewHeight;
    private int bottomPreviewHeight;

    public void setTopPreviewHeight(int topPreviewHeight) {
        this.topPreviewHeight = topPreviewHeight;
    }

    public void setBottomPreviewHeight(int bottomPreviewHeight) {
        this.bottomPreviewHeight = bottomPreviewHeight;
    }

    protected void setPreviewBm(Bitmap previewBm) {
        this.previewBm = previewBm;
    }


    private void init() {
        this.setOrientation(VERTICAL);
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View view = View.inflate(getContext(), R.layout.iv__inbox_view, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(view);

        topVg = findViewById(R.id.iv__inbox_view_top_view);
        topShadowIv = (ImageView) findViewById(R.id.iv__inbox_view_top_view_shadow_iv);
        bottomVg = findViewById(R.id.iv__inbox_view_bottom_view);
        bottomShadowIv = (ImageView) findViewById(R.id.iv__inbox_view_bottom_view_shadow_iv);

    }

    protected void setContentView(int resId) {
        if (resId <= 0) {
            Log.w(TAG, "");
            return;
        }
        setContentView(View.inflate(getContext(), resId, null));
    }

    private void setContentView(View view) {
        contentVg = ((ViewGroup) findViewById(R.id.iv__inbox_view_content_view));
        contentVg.removeAllViews();
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        contentVg.addView(view);

        IVUtil.setBackgroundDrawable(topVg, new BitmapDrawable(getResources(), Bitmap.createBitmap(previewBm, 0, 0, previewBm.getWidth(), topPreviewHeight)));
        IVUtil.setBackgroundDrawable(bottomVg, new BitmapDrawable(getResources(), Bitmap.createBitmap(previewBm, 0, topPreviewHeight, previewBm.getWidth(), bottomPreviewHeight)));

        RelativeLayout.LayoutParams topLp = (RelativeLayout.LayoutParams) topVg.getLayoutParams();
        topLp.height = topPreviewHeight;
        topLp.topMargin = -topPreviewHeight;
        topVg.setLayoutParams(topLp);

        RelativeLayout.LayoutParams bottomLp = (RelativeLayout.LayoutParams) bottomVg.getLayoutParams();
        bottomLp.height = bottomPreviewHeight;
        bottomLp.bottomMargin = -bottomPreviewHeight;
        bottomVg.setLayoutParams(bottomLp);


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            openRootView();
        }
    }

    private boolean isInterceptEvent = true;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (MotionEvent.ACTION_MOVE == ev.getAction()) {
//            return true;
//        }
        return isInterceptEvent;
    }

    private int status = STATUS_NORMAL;
    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_NOT_ARRIVED = 1;
    private static final int STATUS_BOTTOM_ARRIVED = 2;
    private static final int STATUS_TOP_ARRIVED = 3;

    private float startY;

    /**
     * 当前的模式
     */
    private int mode = MODE_NONE;
    private static final int MODE_NONE = 0;
    /**
     * 下拉模式
     */
    private static final int MODE_TOP = 1;
    /**
     * 上拉模式
     */
    private static final int MODE_BOTTOM = 2;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "event: " + event);
        boolean isBottom = isBottom();
        boolean isTop = isTop();
        if (!isBottom && !isTop) {
            isInterceptEvent = false;
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                int halfDeltaY = (int) ((currentY - startY) / 3);

                if (MODE_NONE == mode) {
                    if (currentY > startY && isTop) { // 向下拖动
                        mode = MODE_TOP;
                        changeTopPos(halfDeltaY);
                    } else if (currentY < startY && isBottom) { // 向上拖动
                        mode = MODE_BOTTOM;
                        changeBottomPos(halfDeltaY);
                    } else {
                        status = STATUS_NORMAL;
                    }
                } else if (MODE_TOP == mode) {
                    changeTopPos(halfDeltaY);
                } else if (MODE_BOTTOM == mode) {
                    changeBottomPos(halfDeltaY);
                }

                startY = currentY;
                break;
            case MotionEvent.ACTION_UP:
                if (STATUS_TOP_ARRIVED == status) { // 到达顶部下拉
                    Log.d(TAG, "到达顶部下拉");
                    closeRootView();
                } else if (STATUS_BOTTOM_ARRIVED == status) { // 到达底部上拉
                    Log.d(TAG, "到达底部上拉");
                    closeRootView();
                } else { // 没有到达
                    resetAnimator();
                }
                mode = MODE_NONE;
                break;
            default:
                break;
        }
//        return super.onTouchEvent(event);
        return true;
    }

    private void changeBottomPos(int halfDeltaY) {
        int expectBottomMargin = -halfDeltaY + ((RelativeLayout.LayoutParams) bottomVg.getLayoutParams()).bottomMargin;
        setBottomMargin(bottomVg, expectBottomMargin);
        setTopMargin(contentVg, halfDeltaY + ((RelativeLayout.LayoutParams) contentVg.getLayoutParams()).topMargin);

        // -6/3 ~ -4/3 则到达上拉的高度
//        if (expectBottomMargin > -2 / 3f * bottomPreviewHeight) {
        if (expectBottomMargin > -(bottomPreviewHeight - IVUtil.dip2px(getContext(), 100))) {
            status = STATUS_BOTTOM_ARRIVED;
        } else {
            status = STATUS_NOT_ARRIVED;
        }
    }

    private void changeTopPos(int halfDeltaY) {
        int expectTopMargin = halfDeltaY + ((RelativeLayout.LayoutParams) topVg.getLayoutParams()).topMargin;
        setTopMargin(topVg, expectTopMargin);
        setTopMargin(contentVg, halfDeltaY + ((RelativeLayout.LayoutParams) contentVg.getLayoutParams()).topMargin);

        // -1/3 ~ -2/3 高度则到达下拉的高度
//        if (expectTopMargin > -2 / 3f * topPreviewHeight) {
        if (expectTopMargin > -(topPreviewHeight - IVUtil.dip2px(getContext(), 100))) {
            status = STATUS_TOP_ARRIVED;
        } else {
            status = STATUS_NOT_ARRIVED;
        }
    }

    private void setTopMargin(View view, int topMargin) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (null == lp) {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        lp.topMargin = topMargin;
        view.setLayoutParams(lp);
    }

    private void setBottomMargin(View view, int bottomMargin) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (null == lp) {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        lp.bottomMargin = bottomMargin;
        view.setLayoutParams(lp);
    }

    /**
     * 是否处于底部状态
     *
     * @return
     */
    protected abstract boolean isBottom();

    /**
     * 是否处于顶部状态
     *
     * @return
     */
    protected abstract boolean isTop();

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        ABIOUtil.recycleBitmap(previewBm);
    }


    /**
     * **************************************** 动画 待优化 ***************************************
     */

    private void resetAnimator() {
        ValueAnimator topAnimator = ValueAnimator.ofInt(((RelativeLayout.LayoutParams) topVg.getLayoutParams()).topMargin, -topPreviewHeight);
        topAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) topVg.getLayoutParams();
                lp.topMargin = value;
                topVg.setLayoutParams(lp);
            }
        });

        ValueAnimator bottomAnimator = ValueAnimator.ofInt(((RelativeLayout.LayoutParams) bottomVg.getLayoutParams()).bottomMargin, -bottomPreviewHeight);
        bottomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bottomVg.getLayoutParams();
                lp.bottomMargin = value;
                bottomVg.setLayoutParams(lp);
            }
        });

        ValueAnimator contentAnimator = ValueAnimator.ofInt(((RelativeLayout.LayoutParams) contentVg.getLayoutParams()).topMargin, 0);
        contentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) contentVg.getLayoutParams();
                lp.topMargin = value;
                contentVg.setLayoutParams(lp);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topAnimator, bottomAnimator, contentAnimator);
        animatorSet.setDuration(DEFAULT_ANIMATION_DURATION);
        animatorSet.start();
    }

    /**
     * 关闭rootView
     */
    private void closeRootView() {
        ValueAnimator topAnimator = ValueAnimator.ofInt(((RelativeLayout.LayoutParams) topVg.getLayoutParams()).topMargin, 0);
        topAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) topVg.getLayoutParams();
                lp.topMargin = value;
                topVg.setLayoutParams(lp);
            }
        });

        ValueAnimator bottomAnimator = ValueAnimator.ofInt(((RelativeLayout.LayoutParams) bottomVg.getLayoutParams()).bottomMargin, 0);
        bottomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bottomVg.getLayoutParams();
                lp.bottomMargin = value;
                bottomVg.setLayoutParams(lp);
            }
        });

        ObjectAnimator topShadowAnimator = ObjectAnimator.ofFloat(topShadowIv, "alpha", 1f, 0f);
        ObjectAnimator bottomShadowAnimator = ObjectAnimator.ofFloat(bottomShadowIv, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topAnimator, bottomAnimator, topShadowAnimator, bottomShadowAnimator);
        animatorSet.setDuration(DEFAULT_ANIMATION_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (null != onInboxViewListener) {
                    onInboxViewListener.onInboxViewClosed();
                }
            }
        });
        animatorSet.start();
    }


    /**
     * 打开RootView
     */
    private void openRootView() {
        ValueAnimator topAnimator = ValueAnimator.ofInt(0, -topPreviewHeight);
        topAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) topVg.getLayoutParams();
                lp.topMargin = value;
                topVg.setLayoutParams(lp);
            }
        });

        ValueAnimator bottomAnimator = ValueAnimator.ofInt(0, -bottomPreviewHeight);
        bottomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bottomVg.getLayoutParams();
                lp.bottomMargin = value;
                bottomVg.setLayoutParams(lp);
            }
        });

        ObjectAnimator topShadowAnimator = ObjectAnimator.ofFloat(topShadowIv, "alpha", 0f, 1f);
        ObjectAnimator bottomShadowAnimator = ObjectAnimator.ofFloat(bottomShadowIv, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topAnimator, bottomAnimator, topShadowAnimator, bottomShadowAnimator);
        animatorSet.setDuration(DEFAULT_ANIMATION_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator(0.4f));
        animatorSet.start();
    }


}
