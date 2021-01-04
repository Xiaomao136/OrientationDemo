# Android处理横竖屏切换、刘海屏、隐藏底部导航栏个人总结

## 横竖屏切换的处理：

* 首先需要将Activity的`android:configChanges`配置上`orientation`,这样当发生横竖屏切换时，Activity不会销毁重建，而是会回调Activity的`public void onConfigurationChanged(Configuration newConfig)`方法。

  需要处理横竖屏的Activity在清单文件中的配置：

```xml
   <activity
            android:name=".TestActivity"
            android:configChanges="screenSize|orientation"
            ...
   />
```
这个配置告诉系统当Activity的横竖屏和尺寸发生变化时，不要重新销毁当前Activity，而是回调当前Activity的`onConfigurationChanged`方法。这里之所以要加`screenSize`是因为Activity横竖屏发生切换事一般都会引起Activity的尺寸变化，只使用`orientation`进行配置系统还是会重新创建Activity的。

* 处理完Manifest中的配置，之后就是对`public void onConfigurationChanged(Configuration newConfig)`方法的处理了。

由于项目原因，当横竖屏发生切换后，需要主动告知WebView尺寸，不然WebView的展示会出现异常。这就需要计算需要设置给WebView的尺寸了。~~由于我们的Activity不可以缩放窗口，那计算起来就是整个屏幕的宽高减去屏幕刘海屏区域的安全距离不就得了，这样计算在一些设备上是有问题的！！~~

计算当前Activity可用的宽高：

```java
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
                    //observer.removeOnPreDrawListener(this); 这样会有问题：java.lang.IllegalStateException This ViewTreeObserver is not alive, call getViewTreeObserver() again
                    content.getViewTreeObserver().removeOnPreDrawListener(this); 
                    return true;
                }
            });
        }
        Log.e(TAG, "onConfigurationChanged ");
    }
```

这样就得到了当前Activity可以使用的真实宽高了。

经过在Xml里面对Activity的`android:configChanges`的配置和`onConfigurationChanged`方法的处理，我们的Activity就可以在横竖屏切换时候不被系统重新创建了，也能够得到真正可以使用的宽高了。下面就看看刘海屏的处理。

## 刘海屏的处理

由于项目中需要空出刘海屏区域，也就是如果是刘海屏的设备的话就将刘海屏的区域设置为黑色区域，不能有内容展示，防止关键的地方被遮挡掉。

```java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        //告诉系统，不适配刘海屏，这样刘海屏的区域就不会展示Activity的内容了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            getWindow().setAttributes(params);
        }

       ...
    }
```

关键是 `params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;`这句代码，控制刘海展示的还有其他几个：

> - LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT：这是一种默认的属性，在不进行明确指定的情况下，系统会自动使用这种属性。这种属性允许应用程序的内容在竖屏模式下自动延伸到刘海区域，而在横屏模式下则不会延伸到刘海区域。
> - LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES：这种属性表示，不管手机处于横屏还是竖屏模式，都会允许应用程序的内容延伸到刘海区域。
> - LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER：这种属性表示，永远不允许应用程序的内容延伸到刘海区域。

关于适配刘海屏的更多资料可以查看郭霖大神的这篇文章 [Android 9.0系统新特性，对刘海屏设备进行适配](https://guolin.blog.csdn.net/article/details/103112795)

## 底部导航栏的处理

在处理底部导航栏的时候，使用到了[ImmersionBar](https://github.com/gyf-dev/ImmersionBar)这个库，当然也可以使用Google官方文档给的解决方案。

隐藏底部虚拟导航栏，在onCreate调用

```
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        //隐藏虚拟导航栏
        ImmersionBar.with(this)
                .navigationBarColor(android.R.color.transparent)
                .hideBar(BarHide.FLAG_HIDE_BAR).init();
                .....

```

如果只是这样处理的话，当页面弹出对话框时候，虚拟导航栏会被再次唤起来，不够完美。为此需要在Activity的窗口获取到焦点的时候进行调用下：

```
 @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            ImmersionBar.with(this)
                    .navigationBarColor(android.R.color.transparent)
                    .hideBar(BarHide.FLAG_HIDE_BAR).init();
        }
    }
```

这样，当弹出对话框后，对话框关闭时Activity获取到焦点，会再次将虚拟导航栏隐藏掉，就完美了。

## Demo地址

 [OrientationDemo](https://github.com/itsmallant/OrientationDemo)



参考文章：

[Google官方文档《隐藏导航栏》](https://developer.android.google.cn/training/system-ui/navigation#java)

[郭霖大神的《Android 9.0系统新特性，对刘海屏设备进行适配》](https://guolin.blog.csdn.net/article/details/103112795)














