package com.wangjie.inboxview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import com.wangjie.inboxview.util.IVUtil;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 5/27/15.
 */
public class InboxCompat {
    public static void startInboxView(Activity activity, InboxView inboxView) {
        startInboxView(activity, inboxView, 0.5f);
    }

    public static void startInboxView(Activity activity, InboxView inboxView, float topScale) {
        View rootView = activity.findViewById(android.R.id.content);
        Bitmap bitmap = convertViewToBitmap(rootView);
        inboxView.setPreviewBm(bitmap);
//        new InboxContainer(activity, inboxView).show();

        int leastHeight = IVUtil.dip2px(activity, 10);
        int bitmapHeight = bitmap.getHeight();

        int topPreviewHeight = (int) (bitmapHeight * topScale);
        topPreviewHeight = topPreviewHeight <= 0 ? leastHeight : topPreviewHeight;
        topPreviewHeight = topPreviewHeight >= bitmapHeight ? bitmapHeight - leastHeight : topPreviewHeight;

        inboxView.setTopPreviewHeight(topPreviewHeight);
        inboxView.setBottomPreviewHeight(bitmapHeight - topPreviewHeight);
        new InboxPopContainer(activity, inboxView).showAtLocation(rootView, Gravity.CENTER, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }


    public static Bitmap convertViewToBitmap(View view) {
//        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    public static Bitmap convertViewToBitmapWithDraw(View view, int width, int height) {
        Bitmap viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(viewBitmap));
        return viewBitmap;
    }

}
