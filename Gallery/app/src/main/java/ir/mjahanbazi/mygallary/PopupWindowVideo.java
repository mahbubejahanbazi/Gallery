/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.mjahanbazi.mygallary;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;



import static ir.mjahanbazi.mygallary.GalleryUtils.activity;


public class PopupWindowVideo extends MyPopupWindow  {

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
