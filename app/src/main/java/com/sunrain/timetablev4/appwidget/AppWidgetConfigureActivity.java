package com.sunrain.timetablev4.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sunrain.timetablev4.R;

public class AppWidgetConfigureActivity extends Activity implements View.OnClickListener, SeekBar
        .OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private int mAppWidgetId;
    private RadioGroup mRgBgColor;
    private RadioGroup mRgTimeStyle;
    private SeekBar mSbIntensity;
    private TextView mTvIntensity;
    private TextView mTvTimeStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        setContentView(R.layout.activity_appwidget_configure);

        initView();
        setListener();
    }

    private void initView() {
        mRgBgColor = findViewById(R.id.rg_bg_color);
        mTvIntensity = findViewById(R.id.tv_intensity);
        mSbIntensity = findViewById(R.id.sb_intensity);
        mRgTimeStyle = findViewById(R.id.rg_time_style);
        mTvTimeStyle = findViewById(R.id.tv_time_style);
    }

    private void setListener() {
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        mSbIntensity.setOnSeekBarChangeListener(this);
        mRgTimeStyle.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_confirm:
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                DayAppWidgetProvider.updateAppWidgetConfig(appWidgetManager, mAppWidgetId, getSettingColor(),
                        getTimeStyle());
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
                break;
        }
    }

    public int getSettingColor() {
        int progress = mSbIntensity.getProgress();
        int alpha = progress * 255 / 100;

        if (mRgBgColor.getCheckedRadioButtonId() == R.id.rb_black) {
            return Color.argb(alpha, 0, 0, 0);
        } else {
            return Color.argb(alpha, 255, 255, 255);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_intensity) {
            mTvIntensity.setText(getString(R.string.app_widget_configure_intensity, progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_time_style_1:
                mTvTimeStyle.setText(getString(R.string.app_widget_configure_time_style, "上午 第一节"));
                break;
            case R.id.rb_time_style_2:
                mTvTimeStyle.setText(getString(R.string.app_widget_configure_time_style, "上 一"));
                break;
            case R.id.rb_time_style_3:
                mTvTimeStyle.setText(getString(R.string.app_widget_configure_time_style, "上 1"));
                break;
        }
    }

    public int getTimeStyle() {
        switch (mRgTimeStyle.getCheckedRadioButtonId()) {
            case R.id.rb_time_style_2:
                return 1;
            case R.id.rb_time_style_3:
                return 2;
            case R.id.rb_time_style_1:
            default:
                return 0;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
