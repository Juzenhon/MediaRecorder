package com.ralph.recorder;

/**
 * @author juzenhon
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
