package com.tl.film.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.tl.film.R;
import com.tl.film.model.DefTheme_Model;
import com.tl.film.model.Perpay_Model;
import com.tl.film.model.Push_Model;
import com.tl.film.model.Save_Key;
import com.tl.film.servlet.Get_PerPay_Servlet;
import com.tl.film.utils.ImageUtils;
import com.tl.film.utils.LogUtil;
import com.tl.film.utils.SaveUtils;
import com.tl.film.utils.Tools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Buy_Vip_Activity extends Base_Activity {
    private static final String TAG = "Buy_Vip_Activity";

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, Buy_Vip_Activity.class);
        context.startActivity(intent);
    }

    ImageView bg, qrcode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        setContentView(R.layout.activity_buy_vip);

        bg = findViewById(R.id.bg);
        qrcode = findViewById(R.id.qrcode);

        new Get_PerPay_Servlet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (!TextUtils.isEmpty(SaveUtils.getString(Save_Key.S_DefTheme_Model))) {
            try {
                DefTheme_Model model = new Gson().fromJson(SaveUtils.getString(Save_Key.S_DefTheme_Model), DefTheme_Model.class);
                if (model.getData().getChargeBg() != null) {
                    Glide.with(this).load(model.getData().getChargeBg()).into(bg);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Push_Model model) {
        if (model.getEventId().equals("OPEN_TX_APP")) {
            if (Tools.install(this)) {
                PackageManager packageManager = getPackageManager();
                Intent intent = new Intent();
                intent = packageManager.getLaunchIntentForPackage("com.ktcp.tvvideo");
                if (intent == null) {
                    Toast.makeText(this, "未安装", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(intent);
                }
                finish();
            }
        }
    }

    /**
     * 付费信息处理
     *
     * @param model 数据模型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Perpay_Model model) {
        switch (model.getCode()) {
            case 1000:
                try {
                    qrcode.setImageBitmap(ImageUtils.getQRcode(URLDecoder.decode(model.getData(), "utf-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case 37003:
                PackageManager packageManager = getPackageManager();
                Intent intent = new Intent();
                intent = packageManager.getLaunchIntentForPackage("com.ktcp.tvvideo");
                if (intent == null) {
                    Toast.makeText(this, "未安装", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(intent);
                }
                finish();
                break;

            default:
                Toast.makeText(this, model.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
