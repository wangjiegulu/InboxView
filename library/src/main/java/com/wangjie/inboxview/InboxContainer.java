package com.wangjie.inboxview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 5/27/15.
 */
public class InboxContainer extends Dialog implements InboxView.OnInboxViewListener {
    private InboxView inboxView;

    public InboxContainer(Context context, InboxView inboxView) {
//        super(context, com.wangjie.androidbucket.R.style.customDialogStyle);
        super(context);
        this.inboxView = inboxView;
        init();
    }

    private InboxContainer(Context context, int theme) {
        super(context, theme);
    }

    private InboxContainer(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void init() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //        lp.dimAmount = 0.5f; // 设置进度条周边暗度（0.0f ~ 1.0f）
        lp.dimAmount = 0.5f; // 设置全黑
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setWindowAnimations(0);

        this.getWindow().setGravity(Gravity.CENTER);

        inboxView.setOnInboxViewListener(this);
        this.setContentView(inboxView);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        this.setCanceledOnTouchOutside(false);

//        getWindow().setWindowAnimations(R.style.DialogNoAnimation);
    }

    @Override
    public void onInboxViewClosed() {
        try {
            dismiss();
        } catch (Exception ex) {
            // igonre
        }
    }
}
