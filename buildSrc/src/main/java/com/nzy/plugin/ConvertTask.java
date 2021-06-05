package com.nzy.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static com.nzy.plugin.CwebpPlugin.TAG;

/**
 * @author niezhiyang
 * since 2021/6/3
 */
public class ConvertTask extends DefaultTask {
    /**
     * 所有图片的文件
     */
    /**
     * 用户config的配置
     */
    private WebpConfig mConfig;

    /**
     * 打印日志
     */
    private Logger mLogger;
    private ArrayList<File> imageFileList = new ArrayList<File>();
    int oldSize = 0;
    int newSize = 0;
    private ArrayList<String> bigImgList = new ArrayList<String>();

    private Project mProject = getProject();

    private ArrayList<String> cacheList = new ArrayList<String>();
    /**
     * 当Task执行时候都会调用action方法
     */
    @TaskAction
    public void action() {
        try {
            mConfig = mProject.getExtensions().findByType(WebpConfig.class);
            //得到android的配置
            AppExtension
                    appExtension = getProject().getExtensions().getByType(AppExtension.class);
            DomainObjectSet<ApplicationVariant>
                    variants = appExtension.getApplicationVariants();
            cacheList.clear();
            imageFileList.clear();
            // android项目默认会有 debug和release，
            // 那么getApplicationVariants就是包含了debug和release的集合，all表示对集合进行遍历
            variants.all(new Action<ApplicationVariant>() {
                @Override
                public void execute(ApplicationVariant variant) {
                    mConfig = mProject.getExtensions().findByType(WebpConfig.class);
                    int quality = mConfig.quality;
                    ArrayList<String> whiteList = mConfig.whiteList;
                    boolean debugOn = mConfig.debugOn;
                    mLogger.log(LogLevel.ERROR, TAG + "config的质量是:" + quality + "  白名单是" + Arrays.toString(whiteList.toArray()) + " debug模式时是否开启：" + debugOn);
                    Set<File> files = variant.getAllRawAndroidResources().getFiles();
                    //遍历资源文件目录
                    for (File file : files) {
                        traverseResDir(file);
                    }
                    dispatchOptimizeTask(imageFileList);
                }
            });
        } catch (UnknownDomainObjectException e) {
            mLogger.log(LogLevel.ERROR,e.toString()+"-------");

        }


    }

    /**
     * 递归遍历文件夹，找到需要转变的图片
     *
     * @param file
     */
    private void traverseResDir(File file) {
        if (cacheList.contains(file.getAbsolutePath())) {
            return;
        } else {
            cacheList.add(file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                if (listFile.isDirectory()) {
                    traverseResDir(listFile);
                } else {
                    filterImage(listFile);
                }
            }


        } else {
            filterImage(file);
        }
    }

    /**
     * 过滤不合规的图片文件
     */
    private void filterImage(File file) {
        // 如果添加了图片白名单或者文件不是图片格式,过滤
        if (mConfig.whiteList.contains(file.getName()) || !ImageUtil.isImage(file)) {
            return;
        }
//        // 如果图片尺寸合规,并且图片是大图,大图白名单没有图片
//        if ((mConfig.isCheckSize && ImageUtil.isBigSizeImage(file, mConfig.maxSize)) && !mConfig.bigImageWhiteList.contains(file.getName())) {
//            // 添加到大图列表
//            iBigImage.onBigImage(file);
//        }
        // 将图片添加到图片目录
        imageFileList.add(file);
        mLogger.log(LogLevel.ERROR, TAG + file.getAbsolutePath());

    }
    /**
     * 处理图片压缩任务
     *
     * @param imageFileList
     */
    private void dispatchOptimizeTask(ArrayList<File> imageFileList) {
        if (imageFileList.isEmpty() || bigImgList.isEmpty()) {
            return;
        }
        for (File file : imageFileList) {

            optimizeImage(file);
        }

    }

    private void optimizeImage(File file) {

        String path  = file.getPath();
        if (new File(path).exists()) {
            oldSize += new File(path).length();
        }
        ImageUtil.convert2Webp(file, mLogger);
        newSize(path);
    }

    private void newSize(String path) {
        if (new File(path).exists()) {
            newSize += new File(path).length();
        } else {
            int indexOfDot = path.lastIndexOf(".");
            String webpPath = path.substring(0, indexOfDot) + ".webp";
            if (new File(webpPath).exists()) {
                newSize += new File(webpPath).length();
            }
        }
    }

}
