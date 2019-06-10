package com.tl.film.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.tl.film.MyAPP;
import com.tl.film.R;
import com.tl.film.model.Base_Model;
import com.tl.film.servlet.Bind_Servlet;
import com.tl.film.servlet.Register_Servlet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class Register_Activity extends Base_Activity {
    private static final String TAG = "Register_Activity";

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, Register_Activity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        setContentView(R.layout.activity_register);

        findViewById(R.id.submit).setOnClickListener(v -> {
            String tlsh = ((TextView) findViewById(R.id.tlsh)).getText().toString();
            if (!TextUtils.isEmpty(tlsh)) {
                new Register_Servlet().execute(tlsh);
            } else {
                Toast.makeText(this, "请输入渠道号", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        MyAPP.activity.finish();
        finish();
    }

    @Override
    protected void onDestroy() {
        MyAPP.register_activity = null;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        MyAPP.register_activity = this;
        super.onResume();
    }

    @Override
    protected void onStop() {
        MyAPP.register_activity = null;
        super.onStop();
    }

    @Subscribe
    public void onMessage(Base_Model model) {
        Toast.makeText(this, model.getMessage(), Toast.LENGTH_SHORT).show();
        switch (model.getCode()) {
            case 1000:
            case 13406:
                new Bind_Servlet().execute();
                finish();
                break;
        }
    }

}
