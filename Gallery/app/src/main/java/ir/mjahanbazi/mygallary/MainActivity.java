package ir.mjahanbazi.mygallary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;



import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GalleryUtils.activity = MainActivity.this;
        GalleryUtils.root = findViewById(R.id.activity_main_root);
        CustomPopupWindow.processTransient();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (hasPermission()) {
            ft.replace(R.id.your_placeholder, new GalleryFragment());
            ft.commit();
        } else {
            ft.replace(R.id.your_placeholder, new RequestPermissionFragment());
            ft.commit();

        }
    }

    private boolean hasPermission() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<String>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE_SOME_FEATURES_PERMISSIONS == requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.your_placeholder, new GalleryFragment());
                    ft.commit();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (CustomPopupWindow.popupBackProcessor()) {
            return;
        }
        super.onBackPressed();
    }
}
