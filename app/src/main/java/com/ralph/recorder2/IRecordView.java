
package com.ralph.recorder2;

/**
 * Description : Created by zhuxinhong on 2017/3/14.
 */
public interface IRecordView {

    void onCloseRecord();

    /**
     * @param type 1 video,2 picture
     * @param path 文件保存路径
     */
    void onFinished(int type, String path, int width, int height, int duration);

    /**
     *
     * 1 up,2 down ,3 left,4 right
     *
     * @param originOrient
     * @param orient
     */
    void orientChanged(int originOrient, int orient);
}
