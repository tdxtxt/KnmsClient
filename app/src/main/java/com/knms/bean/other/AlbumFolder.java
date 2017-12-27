package com.knms.bean.other;
import com.yuyh.library.imgsel.bean.Folder;

import java.util.List;

/**
 * Created by tdx on 2016/11/24.
 * 相册文件夹
 */

public class AlbumFolder {
    public String name;
    public String path;
    public Pic cover;
    public List<Pic> pics;
    @Override
    public boolean equals(Object o) {
        try {
            AlbumFolder other = (AlbumFolder) o;
            return this.path.equalsIgnoreCase(other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
