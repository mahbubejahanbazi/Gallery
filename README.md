# Custom Gallery
A custom galley with some features:

- Checking for required permissions
  - android.permission.WRITE_EXTERNAL_STORAGE
  - android.permission.READ_EXTERNAL_STORAGE
- Caching images
- Using a custom popup window
- Display images by clicking
- Play videos by clicking
- Categorized images and videos based on their path on device


## Tech Stack

Java

<p align="center">
  <img  src="https://github.com/mahbubejahanbazi/gallery/blob/main/images/permission_fragment.jpg" />

  <img src="https://github.com/mahbubejahanbazi/gallery/blob/main/images/grant_permission.jpg" />
</p>

<p align="center">
  <img src="https://github.com/mahbubejahanbazi/gallery/blob/main/images/homepage.jpg" />

  <img src="https://github.com/mahbubejahanbazi/gallery/blob/main/images/gallery_1.jpg" />

  <img src="https://github.com/mahbubejahanbazi/gallery/blob/main/images/gallery_2.jpg" />
</p>

## Source code

CustomPopupWindow.java
```java
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static ir.mjahanbazi.mygallary.GalleryUtils.activity;
import static ir.mjahanbazi.mygallary.GalleryUtils.root;


public abstract class CustomPopupWindow extends RelativeLayout {
    private static List<CustomPopupWindow> dialogsList = new ArrayList<CustomPopupWindow>();
    private VisibilityListener listener;

    public CustomPopupWindow(Context context) {
        this(context, null);
    }

    public CustomPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT) {
            {
                setMargins(0, 0, 0, 0);
                addRule(RelativeLayout.CENTER_HORIZONTAL);
                addRule(RelativeLayout.CENTER_VERTICAL);
            }
        });
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    public synchronized static void processTransient() {
        int i = 0;
        while (i < dialogsList.size()) {
            CustomPopupWindow popup = dialogsList.get(i);
            if (popup.isTransient()) {
                popup.closeMe();
            } else {
                popup.show();
                i++;
            }
        }
    }

    public static boolean popupBackProcessor() {
        if (dialogsList.isEmpty()) {
            return false;
        }
        CustomPopupWindow lastDialog = dialogsList.get(dialogsList.size() - 1);
        return lastDialog.backProcessor();
    }

    public void setShowListener(VisibilityListener l) {
        this.listener = l;
    }

    public abstract ViewGroup getMenu();

    public boolean isTransient() {
        return true;
    }

    public boolean closeMe() {
        if (listener != null) {
            listener.beforeHide();
        }
        dialogsList.remove(this);
        final ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            removeKeyboard();
            parent.removeView(CustomPopupWindow.this);
            if (listener != null) {
                listener.afterHide();
            }
            return true;
        }
        return false;
    }

    public void show() {
        if (this.getParent() != null) {
            ViewGroup parent = (ViewGroup) this.getParent();
            parent.removeView(this);
        }
        if (!dialogsList.contains(this)) {
            dialogsList.add(this);
        }
        if (listener != null) {
            listener.beforeShow();
        }
        final ViewGroup rootView = root;
        final ViewGroup menu = getMenu();
        rootView.addView(this);
        removeKeyboard();
        if (listener != null) {
            listener.afterShow();
        }
    }

    protected boolean backProcessor() {
        return closeMe();
    }

    private void removeKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            final View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
                imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
            }
        }
    }

    public interface VisibilityListener {

        public void beforeShow();

        public void afterShow();

        public void beforeHide();

        public void afterHide();

    }

    public class VisibilityListenerDefault implements VisibilityListener {

        public void beforeShow() {
        }

        public void afterShow() {
        }

        public void beforeHide() {
        }

        public void afterHide() {
        }

    }
}
```
PopupWindowImage.java
```java
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
    private final Charset UTF8 = Charset.forName("UTF-8");
    private ViewGroup menu;
    private WebView webView;

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
```
PopupWindowVideo.java
```java
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import static ir.mjahanbazi.mygallary.GalleryUtils.activity;


public class PopupWindowVideo extends CustomPopupWindow {

    private ViewGroup menu;
    private VideoView videoView;

    public PopupWindowVideo(Context context, Uri videoUri) {
        this(context, null, videoUri);
    }

    public PopupWindowVideo(Context context, AttributeSet attrs, final Uri videoUri) {
        super(context, attrs);
        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflator.inflate(R.layout.popup_video, this);
        menu = (ViewGroup) findViewById(R.id.popup_video_root);
        videoView = (VideoView) menu.findViewById(R.id.popup_video_videoview);
        setShowListener(new VisibilityListenerDefault() {
            @Override
            public void afterShow() {
                MediaController mediaController = new MediaController(activity);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoURI(videoUri);
                //videoView.requestFocus();
                videoView.start();
            }
        });

    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public ViewGroup getMenu() {
        return menu;
    }

    @Override
    protected boolean backProcessor() {
        if (videoView != null) {
            videoView = null;
        }
        return super.backProcessor();
    }
}
```
GalleryFragment.java
```
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static ir.mjahanbazi.mygallary.GalleryUtils.activity;

public class GalleryFragment extends Fragment {
    public HashMap<String, HashSet<String>> filesPath;
    public String rootFolder;
    private GridView grid;
    private HashSet<Integer> seletedPositions;
    private String ROOT = "ROOT_NAJVA";
    private GalleryAdapter adapter;
    private Context context;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!rootFolder.equals(ROOT)) {
                seletedPositions.clear();
                List<String> list = adapter.getList();
                list.clear();
                list.addAll(getFilePathes());
                adapter.notifyDataSetChanged();
                rootFolder = ROOT;
                return;
            }
            activity.finishAffinity();
        }
    };

    public GalleryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        context = getActivity();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        grid = view.findViewById(R.id.gallery_grid);
        ImageButton back = view.findViewById(R.id.gallery_back);
        back.setOnClickListener(onClickListener);
        filesPath = new HashMap<String, HashSet<String>>();
        seletedPositions = new HashSet<>();
        getPicturesPath();
        getVideosPath();
        rootFolder = ROOT;
        adapter = new GalleryAdapter(getFilePathes(), seletedPositions, this);
        grid.setAdapter(adapter);
    }

    public void ItemClicked(String filePath, View view, String fileType) {
        if (filesPath.containsKey(filePath)) {
            seletedPositions.clear();
            List<String> list = adapter.getList();
            list.clear();
            rootFolder = filePath;
            HashSet<String> get = filesPath.get(filePath);
            list.addAll(new ArrayList<String>(get));
            adapter.notifyDataSetChanged();
        } else if (fileType.equals(GalleryUtils.FILE_TYPE_IMAGE)) {
            PopupWindowImage image = new PopupWindowImage(context, null, filePath);
            image.show();
        } else {
            PopupWindowVideo video = new PopupWindowVideo(context, null, Uri.fromFile(new File(filePath)));
            video.show();
        }
    }

    private List<String> getFilePathes() {
        List<String> pathes = new ArrayList<String>();
        for (Map.Entry<String, HashSet<String>> entry : filesPath.entrySet()) {
            String key = entry.getKey();
            pathes.add(key);
        }
        return pathes;
    }

    private void getPicturesPath() {
        String[] projection = new String[]{
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA
        };
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(
                images, projection, null, null, null);
        if (cur.moveToFirst()) {
            String folder;
            String pathFile;
            int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                folder = cur.getString(bucketColumn);
                pathFile = cur.getString(dataColumn);
                if (!filesPath.containsKey(folder)) {
                    filesPath.put(folder, new HashSet<String>());
                }
                filesPath.get(folder).add(pathFile);
            } while (cur.moveToNext());
        }
    }

    private void getVideosPath() {
        String[] projection = new String[]{
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA
        };
        Uri images = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(
                images, projection, null, null, null);
        if (cur.moveToFirst()) {
            String folder;
            String pathFile;
            int bucketColumn = cur.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int dataColumn = cur.getColumnIndex(MediaStore.Video.Media.DATA);
            do {
                folder = cur.getString(bucketColumn);
                pathFile = cur.getString(dataColumn);
                if (!filesPath.containsKey(folder)) {
                    filesPath.put(folder, new HashSet<String>());
                }
                filesPath.get(folder).add(pathFile);
            } while (cur.moveToNext());
        }
    }
}
```
GalleryUtils.java
```java
import android.app.Activity;
import android.widget.RelativeLayout;

public class GalleryUtils {
    public static String FILE_TYPE_IMAGE = "image";
    public static String FILE_TYPE_VIDEO = "video";
    public static String FILE_TYPE_FOLDER = "folder";
    public static Activity activity;
    public static RelativeLayout root;
}
```
ListAdapter.java
```java
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ListAdapter<T> extends BaseAdapter {

    protected final List<T> list;

    public ListAdapter() {
        this(new ArrayList<T>());
    }

    public ListAdapter(List<T> list) {
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }

    public T getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<T> getList() {
        return list;
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void add(int location, T object) {
        list.add(location, object);
        notifyDataSetChanged();
    }

    public void add(T object) {
        list.add(object);
        notifyDataSetChanged();
    }

    public void addAll(int location, Collection<T> collection) {
        list.addAll(location, collection);
        notifyDataSetChanged();
    }

    public void addAll(Collection<T> collection) {
        list.addAll(collection);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void remove(int location) {
        list.remove(location);
        notifyDataSetChanged();
    }

    public void remove(T object) {
        list.remove(object);
        notifyDataSetChanged();
    }

    public void removeAll(Collection<T> collection) {
        list.removeAll(collection);
        notifyDataSetChanged();
    }

}
```
GalleryAdapter.java
```java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class GalleryAdapter extends ListAdapter<String> {
    private static String[] extensions = {"jpg", "jpeg", "bmp", "png"};
    private final GalleryFragment gallery;
    private static String fileType;
    private static final View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            TagData tag = (TagData) v.getTag();
            final GalleryAdapter adapter = tag.adapter;
            String file = tag.file;
            adapter.gallery.ItemClicked(file, tag.view, tag.fileType);
        }
    };

    public GalleryAdapter(List<String> filesPath, HashSet<Integer> seletedPositions, GalleryFragment gallery) {
        super(filesPath);
        this.gallery = gallery;
    }

    class TagData {
        ImageView fileImage;
        TextView fileName;
        TextView number;
        String file;
        GalleryAdapter adapter;
        int position;
        ImageView videoIcon;
        View view;
        String fileType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String filePath = getItem(position);
        File file = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
            convertView.setOnClickListener(onClickListener);
            final ImageView fileImageXml = (ImageView) convertView.findViewById(R.id.gallery_item_icon);
            fileImageXml.setOnClickListener(onClickListener);
            final TextView fileNameXml = (TextView) convertView.findViewById(R.id.gallery_item_name);
            final TextView numberXml = (TextView) convertView.findViewById(R.id.gallery_item_number);
            final ImageView videoIconXml = (ImageView) convertView.findViewById(R.id.gallery_item_video_icon);
            fileNameXml.setOnClickListener(onClickListener);
            TagData tag = new TagData() {
                {
                    fileImage = fileImageXml;
                    fileName = fileNameXml;
                    videoIcon = videoIconXml;
                    number = numberXml;
                    adapter = GalleryAdapter.this;
                }
            };
            tag.fileImage.setTag(tag);
            tag.fileName.setTag(tag);
            tag.number.setTag(tag);
            convertView.setTag(tag);
        }
        TagData tag = (TagData) convertView.getTag();
        if (gallery.filesPath.containsKey(filePath)) {
            fileType = GalleryUtils.FILE_TYPE_FOLDER;
            Iterator<String> iterator = gallery.filesPath.get(filePath).iterator();
            if (iterator.hasNext()) {
                file = new File(iterator.next());
                ImageCache.image(file, tag.fileImage);//pictures
                tag.fileName.setVisibility(VISIBLE);
                tag.fileName.setText(filePath);
                tag.videoIcon.setVisibility(INVISIBLE);
                tag.number.setVisibility(VISIBLE);
                tag.number.setText(gallery.filesPath.get(filePath).size() + "");
            }
        } else {
            file = new File(filePath);
            boolean extension = FilenameUtils.isExtension(file.getName().toLowerCase(), extensions);
            if (extension) {
                fileType = GalleryUtils.FILE_TYPE_IMAGE;
                ImageCache.image(file, tag.fileImage);//pictures
                tag.videoIcon.setVisibility(INVISIBLE);
            } else if (FilenameUtils.isExtension(file.getName().toLowerCase(), new String[]{"mp4", "mkv","avi"})) {
                fileType = GalleryUtils.FILE_TYPE_VIDEO;
                ImageCache.video(file, tag.fileImage);//video
                tag.videoIcon.setVisibility(VISIBLE);
            }
            tag.fileName.setVisibility(GONE);
            tag.number.setVisibility(GONE);
        }
        tag.file = filePath;
        tag.position = position;
        tag.view = convertView;
        tag.fileType=fileType;
        return convertView;
    }
}
```
ImageCache.java
```java
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.io.File;

import static ir.mjahanbazi.mygallary.GalleryUtils.activity;

public class ImageCache {

    private static final DisplayImageOptions displayImageOptionsCacheImage = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .delayBeforeLoading(50)
            .showImageForEmptyUri(R.drawable.ic_default_picture)
            .showImageOnFail(R.drawable.ic_default_picture)
            .showImageOnLoading(R.drawable.ic_default_picture)
            .build();
    private static final DisplayImageOptions displayImageOptionsCacheVideo = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .delayBeforeLoading(100)
            .imageScaleType(ImageScaleType.EXACTLY)
            .showImageForEmptyUri(R.drawable.ic_default_video)
            .showImageOnFail(R.drawable.ic_default_video)
            .showImageOnLoading(R.drawable.ic_default_video)
            .build();
    private static final ImageLoaderConfiguration config = new
            ImageLoaderConfiguration.Builder(activity)
            .defaultDisplayImageOptions(displayImageOptionsCacheImage)
            .denyCacheImageMultipleSizesInMemory()
            .diskCacheExtraOptions(200, 200, new BitmapProcessor() {
                public Bitmap process(Bitmap bitmap) {
                    return bitmap;
                }
            })
            .diskCache(new LimitedAgeDiskCache
                    (new File(Environment.getExternalStorageDirectory(), "." + "GalleryCache"),
                            24 * 60 * 60 * 1000))
            .diskCacheFileCount(2000)
            .diskCacheFileNameGenerator(new Md5FileNameGenerator())
            .tasksProcessingOrder(QueueProcessingType.LIFO)
            .build();
    private static ImageLoader imageLoader;

    public synchronized static ImageLoader getImageLoader() {
        if (imageLoader == null) {
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        }
        return imageLoader;
    }


    public static void image(File file, ImageView imageView) {
        getImageLoader().displayImage("file://" + file.getAbsolutePath(), imageView, displayImageOptionsCacheImage);
    }

    public static void video(File file, ImageView imageView) {
        getImageLoader().displayImage("file://" + file.getAbsolutePath(), imageView, displayImageOptionsCacheVideo);
    }
}
```
MainActivity.java
```java
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

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
```
## Contact

mjahanbazi@protonmail.com