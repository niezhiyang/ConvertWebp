package com.nzy.plugin;

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;

import java.io.File;

/**
 * @author niezhiyang
 * since 2021/6/4
 */
class CompressUtil {
    public static void compressImg(File imgFile, Project project) {
        if (!ImageUtil.isImage(imgFile)) {
            return;
        }
        long oldSize = imgFile.length();
        long newSize = 0;
        if (ImageUtil.isJPG(imgFile)) {
            String tempFilePath = imgFile.getPath().substring(0, imgFile.getPath().lastIndexOf(".")) + "_temp" + imgFile.getPath().substring(imgFile.getPath().lastIndexOf("."));
            Tools.cmd("guetzli", imgFile.getPath() + " " + tempFilePath);
            File tempFile = new File(tempFilePath);
            newSize = tempFile.length();
            if (newSize < oldSize) {
                String imgFileName = imgFile.getPath();
                if (imgFile.exists()) {
                    imgFile.delete();
                }
                tempFile.renameTo(new File(imgFileName));
            } else {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } else {
            Tools.cmd("pngquant", "--skip-if-larger --speed 1 --nofs --strip --force --output " + imgFile.getPath() + " -- " + imgFile.getPath());
            newSize = new File(imgFile.getPath()).length();
        }

        project.getLogger().log(LogLevel.ERROR, CwebpPlugin.TAG + imgFile.getPath(), oldSize, newSize);

    }
}
