package com.sunrain.timetablev4.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.sunrain.timetablev4.R;

public class AppWidgetConfigureActivity extends Activity implements View.OnClickListener {

    private int mAppWidgetId;
    private RadioGroup mRgBgColor;
    private SeekBar mSbTransparent;

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
        mSbTransparent = findViewById(R.id.sb_transparent);
    }

    private void setListener() {
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_confirm:
                int color = getSettingColor();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                DayAppWidgetProvider.updateAppWidget(appWidgetManager, mAppWidgetId, color);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public int getSettingColor() {
        int progress = mSbTransparent.getProgress();
        int alpha = progress * 255 / 100;

        if (mRgBgColor.getCheckedRadioButtonId() == R.id.rb_black) {
            return Color.argb(alpha, 0, 0, 0);
        } else {
            return Color.argb(alpha, 255, 255, 255);
        }
    }
}
