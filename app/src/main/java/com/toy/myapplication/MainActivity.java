package com.toy.myapplication;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvText;
    private SoundPool soundPool;
    private Button btnAgain;
    private Subscription subscribe;
    private int loadId;
    private PickerView minute_pv;
    private PickerView second_pv;
    private String second = "0";
    private String minutues = "0";

    private int nextTime = 0;
    private Button btnSetting;
    private RelativeLayout llPickerView;
    private Button btnYes;
    private Button btnCancle;
    private int pressId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAgain = (Button) findViewById(R.id.btn_again);
        mTvText = (TextView) findViewById(R.id.tv_time);
        btnSetting = (Button) findViewById(R.id.btn_setting);
        llPickerView = (RelativeLayout) findViewById(R.id.rl_picker_view);
        btnYes = (Button) findViewById(R.id.btn_yes);
        btnCancle = (Button) findViewById(R.id.btn_cancel);

        nextTime = getSharedPreferences("nextTime", 0).getInt("nextTime", 30);
        if (getSharedPreferences("nextTime", 0).getBoolean("first", true)) {
            mTvText.setText("Hello YKYK!\n" +
                    "点击按钮开始计时\n" +
                    "点击左下角按钮可以设置超时时间");
            getSharedPreferences("nextTime", 0).edit().putBoolean("first", false).commit();
        }

        initNumberPicker();


         /* 获取SoundPool对象：参1:同时播放流的最大数量 ；  参2：流的类型，一般为：STREAM_MUSIC(具体在AudioManager类中列出) ； 参3：采样率转化质量，用0默认*/
        soundPool = new SoundPool(2, AudioManager.STREAM_ALARM, 0);

        btnAgain.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        btnCancle.setOnClickListener(this);
        btnYes.setOnClickListener(this);

         /* 加载文件 *//* 执行该方法返回的是该音频文件在音效池中的位置，用HashMap保存 */
        loadId = soundPool.load(this, R.raw.olykyk, 1);
        pressId = soundPool.load(this, R.raw.press, 1);

    }

    private void initNumberPicker() {
        minute_pv = (PickerView) findViewById(R.id.minute_pv);
        second_pv = (PickerView) findViewById(R.id.second_pv);
        List<String> data = new ArrayList<String>();
        List<String> seconds = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            data.add("0" + i);
        }
        for (int i = 0; i < 60; i++) {
            seconds.add(i < 10 ? "0" + i : "" + i);
        }
        minute_pv.setData(data);
        minute_pv.setOnSelectListener(new PickerView.onSelectListener() {

            @Override
            public void onSelect(String text) {
                minutues = text;
            }
        });
        second_pv.setData(seconds);
        second_pv.setOnSelectListener(new PickerView.onSelectListener() {

            @Override
            public void onSelect(String text) {
                second = text;
            }
        });

        minute_pv.setSelected(nextTime / 60);
        second_pv.setSelected(nextTime % 60);

    }


    private void showTime(Long positon) {
        Log.i("FBC", "==========" + positon);
        mTvText.setText(positon + 1 + "秒后休息");
        if (positon + 1 > nextTime && nextTime != 0) {
            mTvText.setText("可以休息了。。。");
            /*  @ soundID 音效池中的ID
                 *  @ leftVolume  左声道 ：0.0-1.0
                 *  @ rightVolume 右声道 ：0.0-1.0
                 *  @ priority  优先权   ： 0 表示最低权限；
                 *  @ loop : 循环  0 == 不循环  -1==永远循环  other==循环指定次数
                 *  @ rate 比率 ：playback 录音重放 rate ： 0.5-2.0
                 * */
            soundPool.play(loadId, 1, 1, 1, 1, 1);
            if (subscribe != null && !subscribe.isUnsubscribed()) {
                subscribe.unsubscribe();
            }
        }

    }

    /**
     * 开始计时
     */
    public void satrtCount() {
        subscribe = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showTime);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_again:
                soundPool.play(pressId, 1, 1, 1, 0, 1);
                satrtCount();
                break;
            case R.id.btn_cancel:
                btnAgain.setVisibility(View.VISIBLE);
                llPickerView.setVisibility(View.GONE);
                break;
            case R.id.btn_setting:
                btnAgain.setVisibility(View.GONE);
                llPickerView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_yes:
                nextTime = Integer.parseInt(minutues) * 60 + Integer.parseInt(second);
                getSharedPreferences("nextTime", 0)
                        .edit().putInt("nextTime", nextTime)
                        .commit();
                llPickerView.setVisibility(View.GONE);
                btnAgain.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, nextTime + "", Toast.LENGTH_SHORT).show();
                break;

        }

    }

    @Override
    protected void onDestroy() {
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        super.onDestroy();
    }
}
