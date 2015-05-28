package com.wangjie.inboxview;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 5/27/15.
 */
public class InboxPopContainer extends PopupWindow implements InboxView.OnInboxViewListener {
    private InboxView inboxView;

    public InboxPopContainer(Context context, InboxView inboxView) {
        super(inboxView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.inboxView = inboxView;
        this.inboxView.setOnInboxViewListener(this);
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
