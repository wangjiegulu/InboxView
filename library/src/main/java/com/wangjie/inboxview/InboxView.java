package com.wangjie.inboxview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.wangjie.inboxview.util.IVUtil;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 5/27/15.
 */
public abstract class InboxView extends LinearLayout {
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

    public InboxView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InboxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InboxView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected Bitmap previewBm;
    private int previewBmHeight;
    //    private int previewBmHeightHalf;
    private ImageView topIv;
    private ImageView bottomIv;
    private View rootView;

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
        previewBmHeight = previewBm.getHeight();
//        previewBmHeightHalf = previewBmHeight / 2;
    }


    private void init() {
        this.setOrientation(VERTICAL);
    }

    protected void setContentView(int resId) {
        if (resId <= 0) {
            Log.w(TAG, "");
            return;
        }
        setContentView(View.inflate(getContext(), resId, null));

        addPreviewViews();

    }

    private void addPreviewViews() {
        topIv = new ImageView(getContext());
        LayoutParams topLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topLp.topMargin = -topPreviewHeight;
        topIv.setLayoutParams(topLp);
        topIv.setImageBitmap(Bitmap.createBitmap(previewBm, 0, 0, previewBm.getWidth(), topPreviewHeight));
        IVUtil.changeBrightness(topIv, -50f);
        this.addView(topIv, 0);

        bottomIv = new ImageView(getContext());
        LayoutParams bottomLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        bottomLp.topMargin = previewBm.getHeight() / 2;
//        bottomLp.bottomMargin = -previewBmHeightHalf;
        bottomIv.setImageBitmap(Bitmap.createBitmap(previewBm, 0, topPreviewHeight, previewBm.getWidth(), bottomPreviewHeight));
        bottomIv.setLayoutParams(bottomLp);
        IVUtil.changeBrightness(bottomIv, -50f);
        this.addView(bottomIv);

    }


    protected void setContentView(View view) {
        this.rootView = view;
        LayoutParams lp = (LayoutParams) this.rootView.getLayoutParams();
        if (null == lp) {
            lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        this.rootView.setLayoutParams(lp);
        this.addView(this.rootView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            LayoutParams lp = (LayoutParams) this.rootView.getLayoutParams();
            if (null == lp) {
                lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            // 固定大小
            lp.width = rootView.getMeasuredWidth();
            lp.height = rootView.getMeasuredHeight();
            this.rootView.setLayoutParams(lp);

            LayoutParams bottomLp = (LayoutParams) bottomIv.getLayoutParams();
            bottomLp.width = topIv.getMeasuredWidth();
            bottomLp.height = bottomPreviewHeight;
            bottomIv.setLayoutParams(bottomLp);

            openRootView();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 如果设置了拦截所有move事件，即interceptAllMoveEvents为true
        if (MotionEvent.ACTION_MOVE == ev.getAction()) {
            return true;
        }
        return true;
    }

    private int status = STATUS_NORMAL;
    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_NOT_ARRIVED = 1;
    private static final int STATUS_BOTTOM_ARRIVED = 2;
    private static final int STATUS_TOP_ARRIVED = 3;

    private float startY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "event: " + event);
        boolean isBottom = isBottom();
        boolean isTop = isTop();
        if (!isBottom && !isTop) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                int halfDeltaY = (int) ((currentY - startY) / 3);
                if ((currentY > startY && isTop) // 向下拖动
                        ||
                        (currentY < startY && isBottom) // 向上拖动
                        ) {
                    int expectTopMargin = halfDeltaY + ((LayoutParams) topIv.getLayoutParams()).topMargin;
                    setTopMargin(topIv, expectTopMargin);
                    // -1/3 ~ -2/3 高度则到达下拉的高度
                    if (/*expectTopMargin < -1 / 3f * previewBmHeightHalf && */expectTopMargin > -2 / 3f * topPreviewHeight) {
                        status = STATUS_TOP_ARRIVED;
                    } else if (expectTopMargin < -(topPreviewHeight + 1 / 3f * bottomPreviewHeight)/* && expectTopMargin > -4 / 3f * previewBmHeightHalf*/) { // -6/3 ~ -4/3 则到达上拉的高度
                        status = STATUS_BOTTOM_ARRIVED;
                    } else {
                        status = STATUS_NOT_ARRIVED;
                    }
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
                break;
            default:
                break;
        }
//        return super.onTouchEvent(event);
        return true;
    }

    private void setTopMargin(View view, int topMargin) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (null == lp) {
            lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        lp.topMargin = topMargin;
        view.setLayoutParams(lp);
    }

    private void setBottomMargin(View view, int bottomMargin) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (null == lp) {
            lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

    private ValueAnimator topMarginAnimator = new ValueAnimator();

    private void resetAnimator() {
        getResetAnimator().start();
    }

    private ValueAnimator getResetAnimator() {
        LayoutParams lp = (LayoutParams) topIv.getLayoutParams();
        topMarginAnimator.setIntValues(lp.topMargin, -topPreviewHeight);
        topMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LayoutParams lp = (LayoutParams) topIv.getLayoutParams();
                lp.topMargin = value;
                topIv.setLayoutParams(lp);
            }
        });
        topMarginAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                status = STATUS_NORMAL;
            }
        });
        topMarginAnimator.setDuration(300);
        return topMarginAnimator;
    }

    private ValueAnimator rootViewParamLayoutAnimator = new ValueAnimator();
    private ValueAnimator brightnessAnimator = new ValueAnimator();

    /**
     * 关闭rootView
     */
    private void closeRootView() {
        int height = rootView.getMeasuredHeight();
        rootViewParamLayoutAnimator.setIntValues(height, 0);
        rootViewParamLayoutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LayoutParams lp = (LayoutParams) rootView.getLayoutParams();
                lp.height = value;
                rootView.setLayoutParams(lp);
            }
        });

        LayoutParams lp = (LayoutParams) topIv.getLayoutParams();
        topMarginAnimator.setIntValues(lp.topMargin, 0);
        topMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LayoutParams lp = (LayoutParams) topIv.getLayoutParams();
                lp.topMargin = value;
                topIv.setLayoutParams(lp);
            }
        });

        brightnessAnimator.setFloatValues(-50f, 0);
        brightnessAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                IVUtil.changeBrightness(bottomIv, value);
                IVUtil.changeBrightness(topIv, value);
            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (null != onInboxViewListener) {
                    onInboxViewListener.onInboxViewClosed();
                }
            }
        });
        animatorSet.setDuration(300);
        animatorSet.playTogether(rootViewParamLayoutAnimator, topMarginAnimator, brightnessAnimator);
        animatorSet.start();
    }


    /**
     * 打开RootView
     */
    private void openRootView() {
        int height = rootView.getMeasuredHeight();
        rootViewParamLayoutAnimator.setIntValues(0, height);
        rootViewParamLayoutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LayoutParams lp = (LayoutParams) rootView.getLayoutParams();
                lp.height = value;
                rootView.setLayoutParams(lp);
            }
        });

        LayoutParams lp = (LayoutParams) topIv.getLayoutParams();
        topMarginAnimator.setIntValues(0, lp.topMargin);
        topMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LayoutParams lp = (LayoutParams) topIv.getLayoutParams();
                lp.topMargin = value;
                topIv.setLayoutParams(lp);
            }
        });

        brightnessAnimator.setFloatValues(0, -50f);
        brightnessAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                IVUtil.changeBrightness(bottomIv, value);
                IVUtil.changeBrightness(topIv, value);
            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.playTogether(rootViewParamLayoutAnimator, topMarginAnimator, brightnessAnimator);
        animatorSet.start();
    }


}
