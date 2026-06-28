package com.chenboda01.blauncherv1;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity {
    private WebView webView;

    public class AndroidBridge {
        @JavascriptInterface
        public String getInstalledApps() {
            JSONArray arr = new JSONArray();
            try {
                PackageManager pm = getPackageManager();
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
                Collections.sort(apps, new Comparator<ResolveInfo>() {
                    public int compare(ResolveInfo a, ResolveInfo b) {
                        return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
                    }
                });
                for (ResolveInfo info : apps) {
                    JSONObject obj = new JSONObject();
                    obj.put("title", info.loadLabel(pm).toString());
                    obj.put("pkg", info.activityInfo.packageName);
                    obj.put("cls", info.activityInfo.name);
                    obj.put("desc", info.activityInfo.packageName);
                    obj.put("icon", "📱");
                    arr.put(obj);
                }
            } catch (Exception e) {}
            return arr.toString();
        }

        @JavascriptInterface
        public void openApp(String pkg, String cls, String label) {
            runOnUiThread(() -> {
                try {
                    PackageManager pm = getPackageManager();
                    Intent launch = pm.getLaunchIntentForPackage(pkg);
                    if (launch == null && cls != null && cls.length() > 0) {
                        launch = new Intent(Intent.ACTION_MAIN);
                        launch.addCategory(Intent.CATEGORY_LAUNCHER);
                        launch.setClassName(pkg, cls);
                    }
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launch);
                    } else {
                        Toast.makeText(MainActivity.this, label + " is not installed yet.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Could not open " + label + ".", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public void onBackPressed() {
        webView.evaluateJavascript("window.blauncherBack && window.blauncherBack()", null);
    }
}
