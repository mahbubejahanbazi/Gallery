package ir.mjahanbazi.mygallary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;



import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class PopupWindowImage extends CustomPopupWindow {
    private ViewGroup menu;
    private WebView webView;
    private final Charset UTF8 = Charset.forName("UTF-8");

    public PopupWindowImage(Context context, String filePath) {
        this(context, null, filePath);
        ;
    }

    public PopupWindowImage(Context context, AttributeSet attrs, String filePath) {
        super(context, attrs);
        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflator.inflate(R.layout.popup_image, this);
        menu = (ViewGroup) findViewById(R.id.popup_image_root);
        webView = (WebView) menu.findViewById(R.id.popup_image_show);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setBuiltInZoomControls(true);
        InputStream is = context.getResources().openRawResource(R.raw.imageview_html);
        String html = null;
        try {
            html = IOUtils.toString(is, UTF8).replace("filepath", "file://" + filePath);
        } catch (IOException e) {
            html = "<html></html>";
        }
        try {
            is.close();
        } catch (IOException e) {
        }
        webView.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public ViewGroup getMenu() {
        return this.menu;
    }

    @Override
    protected boolean backProcessor() {
        if (webView != null) {
            webView.stopLoading();
            webView = null;
        }
        return super.backProcessor();
    }
}
