package com.fast.mentor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PDFViewerActivity extends AppCompatActivity {
    private static final String TAG = "PDFViewerActivity";

    private WebView pdfWebView;
    private int contentId;
    private int userId;
    private String pdfUrl;
    private String pdfTitle;

    private ContentTracker contentTracker;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfWebView = findViewById(R.id.pdfWebView);

        // Get intent data
        Intent intent = getIntent();
        contentId = intent.getIntExtra("content_id", -1);
        userId    = intent.getIntExtra("user_id", -1);
        pdfUrl    = intent.getStringExtra("pdf_url");
        pdfTitle  = intent.getStringExtra("pdf_title");
        if (contentId < 0 || userId < 0 || pdfUrl == null) {
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tracker
        contentTracker = ContentTracker.getInstance(this);
        contentTracker.startTracking(userId, contentId, "pdf");

        // Toolbar back
        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());

        // Setup WebView
        WebSettings ws = pdfWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        pdfWebView.setWebChromeClient(new WebChromeClient());

        // Expose bridge
        pdfWebView.addJavascriptInterface(new PageChangeHandler(), "Android");

        // Load the pdf.js viewer from assets, passing the remote URL as &file=
        String viewerUrl = "file:///android_asset/pdfjs/web/viewer.html"
                + "?file=" + java.net.URLEncoder.encode(pdfUrl);
        pdfWebView.loadUrl(viewerUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop tracking
        contentTracker.stopTracking(userId, contentId);
    }

    /**
     * JavaScript bridge to receive page change and load-complete events
     */
    private class PageChangeHandler {
        @JavascriptInterface
        public void onDocumentLoaded(int pageCount) {
            Log.d(TAG, "PDF loaded, total pages: " + pageCount);
            // initial position = page 0
            contentTracker.updatePosition(userId, contentId, 0, pageCount);
        }

        @JavascriptInterface
        public void onPageChanged(int currentPage, int pageCount) {
            Log.d(TAG, "Page changed: " + currentPage + " / " + pageCount);
            contentTracker.updatePosition(userId, contentId, currentPage, pageCount);

            // If last page, mark after a short delay
            if (currentPage >= pageCount - 1) {
                runOnUiThread(() -> contentTracker.markAsCompleted(userId, contentId));
            }
        }
    }
}
