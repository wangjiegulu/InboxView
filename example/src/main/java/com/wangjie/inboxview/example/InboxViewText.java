package com.wangjie.inboxview.example;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.wangjie.androidbucket.log.Logger;
import com.wangjie.inboxview.InboxView;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 5/27/15.
 */
public class InboxViewText extends InboxView implements View.OnClickListener {
    private static final String TAG = InboxViewText.class.getSimpleName();

    public InboxViewText(Context context) {
        super(context);
    }

    @Override
    protected boolean isBottom() {
        return true;
    }

    @Override
    protected boolean isTop() {
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Logger.d(TAG, "onAttachedToWindow");

        setContentView(R.layout.inbox_view_text);
        findViewById(R.id.inbox_view_text_iv).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inbox_view_text_iv:
                Toast.makeText(getContext(), "image clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Logger.d(TAG, "onDetachedFromWindow");
    }

}
