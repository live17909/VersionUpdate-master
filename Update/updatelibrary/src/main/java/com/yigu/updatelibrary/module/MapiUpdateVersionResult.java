package com.yigu.updatelibrary.module;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/7.
 */
public class MapiUpdateVersionResult implements Serializable {

    /**
     * version : 2
     * content : 1.本次更新 2.本次更新.本次更新
     * url : http://img.bstapp.cn/ws/apk/app-release.apk
     */

    private int version;
    private String content;
    private String url;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

