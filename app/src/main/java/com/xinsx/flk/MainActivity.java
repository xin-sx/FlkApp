package com.xinsx.flk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends android.app.Activity {

    private WebView webView;
    private ScrollBarView scrollBar;
    private FrameLayout root;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 16; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0");

        webView.setWebViewClient(new WebViewClient());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(webView, webParams);

        scrollBar = new ScrollBarView(this, webView);
        FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(
                dp(6), ViewGroup.LayoutParams.MATCH_PARENT);
        barParams.gravity = android.view.Gravity.RIGHT;
        barParams.rightMargin = dp(2);
        root.addView(scrollBar, barParams);

        setContentView(root);

        webView.loadUrl("https://flk.npc.gov.cn/index");
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /**
     * 细窄的右侧白色滑动条:拖动滑块可控制 WebView 上下滚动
     */
    static class ScrollBarView extends View {
        private final WebView webView;
        private final Paint trackPaint;
        private final Paint thumbPaint;
        private float thumbTop;
        private float thumbHeight;
        private float lastTouchY;
        private boolean dragging = false;

        ScrollBarView(Context context, WebView webView) {
            super(context);
            this.webView = webView;
            trackPaint = new Paint();
            trackPaint.setColor(Color.argb(40, 255, 255, 255));
            trackPaint.setAntiAlias(true);
            thumbPaint = new Paint();
            thumbPaint.setColor(Color.argb(220, 255, 255, 255));
            thumbPaint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            canvas.drawRoundRect(0, 0, w, h, w / 2f, w / 2f, trackPaint);
            if (thumbHeight < 30) thumbHeight = 30;
            if (thumbTop < 0) thumbTop = 0;
            if (thumbTop + thumbHeight > h) thumbTop = h - thumbHeight;
            canvas.drawRoundRect(0, thumbTop, w, thumbTop + thumbHeight, w / 2f, w / 2f, thumbPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float h = getHeight();
            int contentHeight = (int) (webView.getContentHeight() * webView.getScale());
            int viewHeight = webView.getHeight();
            int scrollable = Math.max(1, contentHeight - viewHeight);
            float ratio = viewHeight / (float) contentHeight;
            thumbHeight = Math.max(30, h * ratio);
            float scrollableTrack = h - thumbHeight;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchY = event.getY();
                    float center = thumbTop + thumbHeight / 2f;
                    if (Math.abs(event.getY() - center) <= thumbHeight) {
                        dragging = true;
                    } else {
                        // 点击空白:直接跳转
                        float targetThumb = event.getY() - thumbHeight / 2f;
                        if (targetThumb < 0) targetThumb = 0;
                        if (targetThumb > scrollableTrack) targetThumb = scrollableTrack;
                        thumbTop = targetThumb;
                        float percent = thumbTop / scrollableTrack;
                        webView.scrollTo(0, (int) (scrollable * percent));
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        float dy = event.getY() - lastTouchY;
                        lastTouchY = event.getY();
                        thumbTop += dy;
                        if (thumbTop < 0) thumbTop = 0;
                        if (thumbTop > scrollableTrack) thumbTop = scrollableTrack;
                        float percent = thumbTop / scrollableTrack;
                        webView.scrollTo(0, (int) (scrollable * percent));
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
            }
            return super.onTouchEvent(event);
        }
    }
}
