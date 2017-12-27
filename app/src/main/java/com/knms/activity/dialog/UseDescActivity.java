package com.knms.activity.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.knms.android.R;

/**
 * Created by Administrator on 2017/3/23.
 */

public class UseDescActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResID());
        initData();
    }
    protected int layoutResID() {
        return R.layout.activity_use_desc;
    }
    protected void initData() {
        findViewById(R.id.ivBtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoomax, R.anim.zoomin);
    }
}
