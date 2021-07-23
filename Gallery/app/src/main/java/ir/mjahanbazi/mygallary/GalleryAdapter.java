package ir.mjahanbazi.mygallary;

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

public class GalleryAdapter extends MyListAdapter<String> {
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
                MyCache.image(file, tag.fileImage);//pictures
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
                MyCache.image(file, tag.fileImage);//pictures
                tag.videoIcon.setVisibility(INVISIBLE);
            } else if (FilenameUtils.isExtension(file.getName().toLowerCase(), new String[]{"mp4", "mkv","avi"})) {
                fileType = GalleryUtils.FILE_TYPE_VIDEO;
                MyCache.video(file, tag.fileImage);//video
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
