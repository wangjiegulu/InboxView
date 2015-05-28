package com.wangjie.inboxview.example;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.wangjie.androidbucket.adapter.ABaseAdapter;
import com.wangjie.androidbucket.utils.ABTextUtil;
import com.wangjie.androidinject.annotation.annotations.base.AIItemClick;
import com.wangjie.androidinject.annotation.annotations.base.AILayout;
import com.wangjie.androidinject.annotation.annotations.base.AIView;
import com.wangjie.androidinject.annotation.present.AIActionBarActivity;
import com.wangjie.inboxview.InboxCompat;


@AILayout(R.layout.activity_main)
public class MainActivity extends AIActionBarActivity {
    @AIView(R.id.activity_main_lv)
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lv.setAdapter(new MyAdapter(lv));

    }

    @Override
    @AIItemClick({R.id.activity_main_lv})
    public void onItemClickCallbackSample(AdapterView<?> parent, View view, int position, long id) {
//        showToastMessage("position " + position);
        int firstPos = lv.getFirstVisiblePosition();
        int lastPos = lv.getLastVisiblePosition();
        InboxCompat.startInboxView(this, new InboxViewText(context), 1f * (position - firstPos) / (lastPos - firstPos));


    }


    class MyAdapter extends ABaseAdapter {

        protected MyAdapter(AbsListView listView) {
            super(listView);
        }

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = new TextView(context);
                int padding = ABTextUtil.dip2px(context, 20);
                convertView.setPadding(padding, padding, padding, padding);
                ((TextView) convertView).setGravity(Gravity.CENTER);
                ((TextView) convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            }
            ((TextView) convertView).setText("test " + position);
            return convertView;
        }
    }


}
