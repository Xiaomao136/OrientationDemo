package com.znq.orientationdemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        //隐藏虚拟导航栏
        ImmersionBar.with(this)
                .navigationBarColor(android.R.color.transparent)
                .hideBar(BarHide.FLAG_HIDE_BAR).init();

        //告诉系统，不适配刘海屏，这样刘海屏的区域就不会展示Activity的内容了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            getWindow().setAttributes(params);
        }

        Button button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.juejin.cn");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    Toast.makeText(MainActivity.this, "切换为横屏了", Toast.LENGTH_SHORT).show();
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    Toast.makeText(MainActivity.this, "切换为竖屏了", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            ImmersionBar.with(this)
                    .navigationBarColor(android.R.color.transparent)
                    .hideBar(BarHide.FLAG_HIDE_BAR).init();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View content = findViewById(android.R.id.content);
        int contentWidth = content.getWidth();
        int contentHeight = content.getHeight();
        if (contentWidth > 0 && contentHeight > 0) {
            int realWidth, realHeight;
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                realWidth = Math.min(contentWidth, contentHeight);
                realHeight = Math.max(contentWidth, contentHeight);
                textView.setText("当前为竖屏；\r\n" + " content width = " + content.getWidth() + " content height = " + content.getHeight()+"\r\n realWidth = "+realWidth+" realHeight = "+realHeight);
            } else {
                realWidth = Math.max(contentWidth, contentHeight);
                realHeight = Math.min(contentWidth, contentHeight);
                textView.setText("当前为横屏；\r\n" + " content width = " + content.getWidth() + " content height = " + content.getHeight()+"\r\n realWidth = "+realWidth+" realHeight = "+realHeight);
            }
        } else {
            ViewTreeObserver observer = content.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int realWidth, realHeight;
                    if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        realWidth = Math.min(contentWidth, contentHeight);
                        realHeight = Math.max(contentWidth, contentHeight);
                        textView.setText("当前为竖屏；\r\n" + " content width = " + content.getWidth() + " content height = " + content.getHeight()+"\r\n realWidth = "+realWidth+" realHeight = "+realHeight);
                    } else {
                        realWidth = Math.max(contentWidth, contentHeight);
                        realHeight = Math.min(contentWidth, contentHeight);
                        textView.setText("当前为横屏；\r\n" + " content width = " + content.getWidth() + " content height = " + content.getHeight()+"\r\n realWidth = "+realWidth+" realHeight = "+realHeight);
                    }
                    observer.removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
        Log.e(TAG, "onConfigurationChanged ");
    }
}
