package com.nzy.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.tasks.MergeResources;

import org.antlr.v4.misc.Utils;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskInputs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author niezhiyang
 * since 2021/6/1
 */
public class CwebpPlugin implements Plugin<Project> {
    int oldSize = 0;
    int newSize = 0;
    public static final String TAG = "CwebpPlugin :";
    /**
     * 用户config的配置
     */
    private WebpConfig mConfig;

    /**
     * 打印日志
     */
    private Logger mLogger;

    private ArrayList<String> bigImgList = new ArrayList<String>();

    private Project mProject;
    private AppExtension mAppExtension;

    private ArrayList<String> cacheList = new ArrayList<String>();

    /**
     * 所有图片的文件
     */

    private ArrayList<File> imageFileList = new ArrayList<File>();

    @Override
    public void apply(Project project) {

        if (!project.getPlugins().hasPlugin(AppPlugin.class)) {
            throw new GradleException("必须在android application插件中使用改插件");
        }
        //得到android的配置
        mAppExtension = project.getExtensions().getByType(AppExtension.class);

        // 创建 WebpConfig 的扩展，使用户可以在build.gradle中使用
        project.getExtensions().create("webpConfig", WebpConfig.class);

        mProject = project;
        mLogger = project.getLogger();
        LoggerUtil.sLogger = mLogger;
        printAllTask();
        convertTask();
    }

    /**
     * 开始转化
     */

    private void convertTask() {
        //就和引入了 apply plugin: 'com.android.application' 一样，可以配置android{}

        //gradle执行会解析build.gradle文件，afterEvaluate表示在解析完成之后再执行我们的代码
        mProject.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(@NotNull Project project) {
                initConfig();
            }
        });
    }

    /**
     * 拿到用户的配置
     */
    private void initConfig() {
        mConfig = mProject.getExtensions().findByType(WebpConfig.class);
        int quality = mConfig.quality;
        ArrayList<String> whiteList = mConfig.whiteList;
        boolean debugOn = mConfig.debugOn;
        mLogger.log(LogLevel.ERROR, TAG + "config的质量是:" + quality + "  白名单是" + Arrays.toString(whiteList.toArray()) + " debug模式时是否开启：" + debugOn);

        // android项目默认会有 debug和release，
        // 那么getApplicationVariants就是包含了debug和release的集合，all表示对集合进行遍历
        mAppExtension.getApplicationVariants().all(new Action<ApplicationVariant>() {
            @Override
            public void execute(ApplicationVariant applicationVariant) {
                //当前用户是debug模式，并且没有配置debug运行执行热修复
                if (applicationVariant.getName().contains("debug") && !debugOn) {
                    return;
                }
                //开始压缩等一些列的
                convert(applicationVariant);
            }
        });

    }

    private void convert(ApplicationVariant variant) {
        //获得: debug/release
        String variantName = variant.getName();
        //首字母大写
        String capitalizeName = Utils.capitalize(variantName);

        //获得 mergeResources 的task
        MergeResources mergeResources = variant.getMergeResourcesProvider().get();


        // 这是项目的根路径
        String rootPath = mProject.getRootDir().getPath();

        // 这是 cwebp 工具的地址
        FileUtil.TOOLS_DIRPATH = rootPath + "/mctools/";

        // 创建自己任务
        Task convertTask = mProject.task("convertTask" + capitalizeName);

        // 第一种拿到资源
        getRes1(capitalizeName);


        // 第二种拿到资源
        //getRes2(variant, mergeResources, convertTask);


        // 第三种拿到资源
        //getRes3(mergeResources);

    }


    /**
     * 第一种拿到资源的方法
     *
     * @param capitalizeName
     */
    private void getRes1(String capitalizeName) {
        // 获得android的mergeDebugResources/mergeReleaseResources任务
        final Task mergeResTask =
                mProject.getTasks().findByName("merge" + capitalizeName + "Resources");
        if (mergeResTask != null) {
            imageFileList.clear();
            cacheList.clear();
            mergeResTask.doFirst(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    //资源输出的所有文件
                    TaskInputs outputs = mergeResTask.getInputs();
                    Set<File> files = outputs.getFiles().getFiles();
                    for (File file : files) {
                        // 遍历文件夹 拿到 所有的 图片
                        traverseDir(file);
                    }
                    // 开始压缩
                    startConvertAndCompress();
                }
            });
        }
    }

    /**
     * 处理图片压缩任务
     */
    private void startConvertAndCompress() {
        if (imageFileList.isEmpty()) {
            return;
        }
        for (File file : imageFileList) {
            // 压缩
            CompressUtil.compressImg(file, mProject);
            // 转化webp
            ConvertWebpUtil.securityFormatWebp(file, mConfig, mProject, mLogger);
        }

    }


    private void countNewSize(String path) {
        if (new File(path).exists()) {
            newSize += new File(path).length();
        } else {
            int indexOfDot = path.lastIndexOf(".");
            String webpPath = path.substring(0, indexOfDot) + ".webp";
            if (new File(webpPath).exists()) {
                newSize += new File(webpPath).length();
            }
        }
        mLogger.log(LogLevel.ERROR, TAG + "压缩的大小：oldSize - newSize = " + (oldSize - newSize));
    }

    /**
     * 递归遍历文件夹，找到需要转变的图片
     *
     * @param file
     */
    private void traverseDir(File file) {
        if (cacheList.contains(file.getAbsolutePath())) {
            return;
        } else {
            cacheList.add(file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                if (listFile.isDirectory()) {
                    traverseDir(listFile);
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
        if ((mConfig.isCheckSize && ImageUtil.isBigSizeImage(file, mConfig.maxSize)) && !mConfig.bigImageWhiteList.contains(file.getName())) {
            // 添加到大图列表
            bigImgList.add(file.getName());
            mLogger.log(LogLevel.ERROR, TAG + "大图片地址：" + imageFileList.size() + "-----" + file.getAbsolutePath());
            throw new GradleException("有个图片你需要吃处理一下");
        }
        // 将图片添加到图片目录
        imageFileList.add(file);
        mLogger.log(LogLevel.ERROR, TAG + "图片地址：" + imageFileList.size() + "-----" + file.getAbsolutePath());

    }

    /**
     * 打印所有的执行的task
     */
    private void printAllTask() {
        mProject.getGradle().getTaskGraph().whenReady(new Action<TaskExecutionGraph>() {
            @Override
            public void execute(TaskExecutionGraph taskGraph) {
                List<Task> allTasks = taskGraph.getAllTasks();
                for (int i = 0; i < allTasks.size(); i++) {
                    Task task = allTasks.get(i);
                    //打印出所有的task的名字
                    mLogger.log(LogLevel.ERROR, TAG + "所有的task ：" + i + "-----" + task.getName());
                }
            }
        });
    }


    /**
     * 第三种拿到资源的方法
     *
     * @param mergeResources
     */
    private void getRes3(MergeResources mergeResources) {
        mergeResources.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                TaskInputs outputs = task.getInputs();
                Set<File> files = outputs.getFiles().getFiles();
                //遍历资源文件目录
                for (File file : files) {
                    traverseDir(file);
                }
                startConvertAndCompress();
            }
        });
    }

    /**
     * 第二种拿到资源的方法
     *
     * @param mergeResources
     */
    private void getRes2(ApplicationVariant variant, MergeResources mergeResources, Task convertTask) {
        convertTask.doLast(new Action<Task>() {
            @Override
            public void execute(@NotNull Task task) {
                Set<File> files = variant.getAllRawAndroidResources().getFiles();
                //遍历资源文件目录
                for (File file : files) {
                    traverseDir(file);
                }
                startConvertAndCompress();
            }
        });
        mergeResources.dependsOn(mProject.getTasks().findByName(convertTask.getName()));
    }

}
