package com.ralph.recorder;

/**
 * Description :
 * Created by zhuxinhong on 2017/3/14.
 * Job number：135198
 * Phone ：13520295328
 * Email：zhuxinhong@syswin.com
 * Person in charge : zhuxinhong
 * Leader：zhuxinhong
 */
public interface IRecorder {

    void setOutputPath(String path);

    /**
     * 开始录制
     *
     * @return
     */
    boolean start();

    /**
     * 取消录制操作
     */
    void cancel();

    /**
     * 录制完成
     *
     * @return  path 录制成功时为返回的文件路径，否则null
     */
    String end();

}
