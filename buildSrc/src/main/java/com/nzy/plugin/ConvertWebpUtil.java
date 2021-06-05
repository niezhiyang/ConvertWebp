package com.nzy.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;

import java.io.File;

/**
 * @author niezhiyang
 * since 2021/6/4
 */
public class ConvertWebpUtil {
    private static final int VERSION_SUPPORT_WEBP = 14;

    public static void securityFormatWebp(File file, WebpConfig config, Project project) {
        AppExtension appExtension = project.getExtensions().getByType(AppExtension.class);
        if (!(appExtension.getDefaultConfig().getMinSdk() > VERSION_SUPPORT_WEBP)) {
            throw new GradleException("转化webp，必须大于14");
        }
        if (ImageUtil.isImage(file)) {
            if (config.isSupportAlphaWebp) {
                formatWebp(file, project);
            } else {
                if (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg")) {
                    //jpg
                    formatWebp(file, project);
                } else if (file.getName().endsWith("png")) {
                    //png
                    if (!ImageUtil.isAlphaPNG(file)) {
                        //不包含透明通道
                        formatWebp(file, project);
                    } else {
                        //包含透明通道的png，进行压缩
                        CompressUtil.compressImg(file, project);
                    }
                }
            }
        }

    }

    private static void formatWebp(File imgFile, Project project) {
        if (ImageUtil.isImage(imgFile)) {
            String filePath = imgFile.getPath().substring(0, imgFile.getPath().lastIndexOf(".")) + "..webp";
            File webpFile = new File(filePath);
            Tools.cmd("cwebp", "${imgFile.path} -o ${webpFile.path} -m 6 -quiet");
            if (webpFile.length() < imgFile.length()) {
                project.getLogger().log(LogLevel.ERROR, imgFile.getPath(), imgFile.length(), webpFile.length());
                if (imgFile.exists()) {
                    imgFile.delete();
                }
            } else {
                //如果webp的大的话就抛弃
                if (webpFile.exists()) {
                    webpFile.delete();
                }
                project.getLogger().log(LogLevel.ERROR, imgFile.getName() + " do not convert webp because the size become larger!");
            }
        }
    }


}
