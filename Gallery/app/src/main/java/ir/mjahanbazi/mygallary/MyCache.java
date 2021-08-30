package ir.mjahanbazi.mygallary;

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

public class MyCache {

    private static ImageLoader imageLoader;

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
