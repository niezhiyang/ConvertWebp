package com.nzy.plugin;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * @author niezhiyang
 * since 2021/6/4
 */
public class ImageUtil {

    public static boolean isImage(File file){
        return (file.getName().endsWith("jpg") ||
                file.getName().endsWith("png") ||
                file.getName().endsWith("jpeg")
        ) && !file.getName().endsWith("9.png");
    }

    public static boolean isBigSizeImage(File file){
        return (file.getName().endsWith("jpg") ||
                file.getName().endsWith("png") ||
                file.getName().endsWith("jpeg")
        ) && !file.getName().endsWith(".9");
    }

    public static boolean isAlphaPNG(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            return img.getColorModel().hasAlpha();
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isJPG(File file) {
        return file.getName().endsWith("jpg") || file.getName().endsWith("jpeg");
    }

    public static void convert2Webp(File imgFile, Logger logger) {
        if (isImage(imgFile)) {
            File webpFile =
                    new File(imgFile.getPath().substring(0, imgFile.getPath().lastIndexOf("."))+".webp");
            Tools.Companion.cmd("cwebp", imgFile.getPath()+" -o +"+webpFile.getPath()+" -m 6 -quiet");
            if (webpFile.length() < imgFile.length()) {
                if (imgFile.exists()) {
//                    imgFile.delete();
                    logger.log(LogLevel.ERROR,"sssssssssssssss"+webpFile.exists());
                }
            } else {
                if (webpFile.exists()) {
                    webpFile.delete();
                }
            }
        }
    }
}
