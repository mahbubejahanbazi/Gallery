package ir.mjahanbazi.mygallary;

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
    private GridView grid;
    public HashMap<String, HashSet<String>> filesPath;
    private HashSet<Integer> seletedPositions;
    public String rootFolder;
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
