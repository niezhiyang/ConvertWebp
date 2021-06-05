package com.nzy.plugin;

import java.util.ArrayList;

/**
 * @author niezhiyang
 * since 2021/6/3
 */
public class WebpConfig {
    /**
     * 转化web的质量
     */
    public int quality = 80;
    /**
     * 白名单避免转化
     */
    public ArrayList<String> whiteList = new ArrayList<>();

    /**
     * 大图白名单
     */
    public String[] bigImageWhiteList = new String[]{};


    /**
     * debug 模式是否开启
     */
    public boolean debugOn = true;

    /**
     * 是否检测大图
     */
    public boolean isCheckSize = false;


    public boolean isSupportAlphaWebp = true;


}
