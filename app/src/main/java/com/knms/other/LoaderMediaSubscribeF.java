package com.knms.other;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.other.AlbumFolder;
import com.knms.bean.other.Pic;
import com.yuyh.library.imgsel.bean.Folder;
import com.yuyh.library.imgsel.bean.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

import static android.R.attr.path;
import static com.knms.bean.other.Pic.IMG_ALBUM;
import static com.yuyh.library.imgsel.common.Constant.pics;


/**
 * Created by Administrator on 2016/11/16.
 */

public class LoaderMediaSubscribeF implements Observable.OnSubscribe<List<AlbumFolder>>{
    public static final int LOADER_ALL = 0;
    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID};
    private final Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private final String ORDERBY = MediaStore.Images.Media.DATE_TAKEN;
    private String where = MediaStore.Images.Media.MIME_TYPE + "=? or "+MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=?";
    private String[] values = new String[] {"image/jpeg","image/png" ,"image/bmp"};
    @Override
    public void call(Subscriber<? super List<AlbumFolder>> subscriber) {
        List<AlbumFolder> albumFolders = new ArrayList<>();
        List<Pic> allPics = new ArrayList<>();
        AlbumFolder albumFolder = new AlbumFolder();
        albumFolder.name = "所有图片";

        Pic one = new Pic();
        one.url = "drawable://" + R.drawable.icon_xc;
        one.setItemType(IMG_ALBUM);
        allPics.add(0,one);
        Cursor mCursor = KnmsApp.getInstance().getContentResolver()
                .query(URI, IMAGE_PROJECTION,where, values, ORDERBY + " DESC");
        if (mCursor == null){
            subscriber.onNext(albumFolders);
            subscriber.onCompleted();
            return;
        }
        int count = mCursor.getCount();
        if(count == 0){
            subscriber.onNext(albumFolders);
            subscriber.onCompleted();
            return;
        }
        Pic pic = null;
        while (mCursor.moveToNext()) {
            pic = new Pic();
            // 获取图片的路径
            String path = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            // 预先验证图片的有效性
            final File file = new File(path);
            if (!file.exists()) continue;//图片不存在
            pic.url = path;
            allPics.add(pic);

            File imageFile = new File(path);
            File folderFile = imageFile.getParentFile();
            AlbumFolder folder = new AlbumFolder();
            folder.name = folderFile.getName();
            folder.path = folderFile.getAbsolutePath();
            folder.cover = pic;
            if (!albumFolders.contains(folder)) {
                List<Pic> imageList = new ArrayList<>();
                imageList.add(one);
                imageList.add(pic);
                folder.pics = imageList;
                albumFolders.add(folder);
            } else {
                AlbumFolder f = albumFolders.get(albumFolders.indexOf(folder));
                f.pics.add(pic);
            }
        }
        albumFolder.pics = allPics;
        albumFolders.add(albumFolder);
        subscriber.onNext(albumFolders);
        subscriber.onCompleted();
    }

}
