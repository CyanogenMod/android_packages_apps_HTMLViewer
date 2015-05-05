/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.htmlviewer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Simple activity that shows the requested HTML page.
 */
public class ExternalLegalViewerActivity extends Activity {
    private static final String TAG = "ExternalLegalViewerActivity";

    private WebView mWebView;
    private View mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.legal);

        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mWebView = (WebView) findViewById(R.id.webview);
        mLoading = findViewById(R.id.loading);

        mWebView.setWebViewClient(new ViewClient());

        WebSettings s = mWebView.getSettings();
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSavePassword(false);
        s.setSaveFormData(false);

        s.setDefaultTextEncodingName("utf-8");

        final Intent intent = getIntent();

        mWebView.loadUrl(String.valueOf(intent.getData()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    private class ViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            mLoading.setVisibility(View.GONE);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                WebResourceRequest request) {
            final Uri uri = request.getUrl();
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())
                    && uri.getPath().endsWith(".gz")) {
                Log.d(TAG, "Trying to decompress " + uri + " on the fly");
                try {
                    final InputStream in = new GZIPInputStream(
                            getContentResolver().openInputStream(uri));
                    final WebResourceResponse resp = new WebResourceResponse(
                            getIntent().getType(), "utf-8", in);
                    resp.setStatusCodeAndReasonPhrase(200, "OK");
                    return resp;
                } catch (IOException e) {
                    Log.w(TAG, "Failed to decompress; falling back", e);
                }
            }
            return null;
        }
    }
}
