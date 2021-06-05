package com.nzy.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author niezhiyang
 * since 2021/6/5
 */
public class Tools {
    public static void cmd(String cmd, String params) {
        String cmdStr = "";

        if (isCmdExist(cmd)) {
            cmdStr = cmd + " " + params;
        } else {
            if (isMac()) {
                cmdStr = FileUtil.TOOLS_DIRPATH + "mac/" + cmd + " " + params;
            } else if (isWindows()) {
                cmdStr = FileUtil.TOOLS_DIRPATH + "windows/" + cmd + " " + params;

            } else if (isLinux()) {
                cmdStr = FileUtil.TOOLS_DIRPATH + "linux/" + cmd + " " + params;

            }
        }
        if (cmdStr == "") {
            LoggerUtil.log("Tools: cmdStr == null");
            return;
        }
        LoggerUtil.log("Tools: cmdStr == outputMessage");
        outputMessage(cmdStr);

    }

    private static void outputMessage(String cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            LoggerUtil.log("Tools: 成功了");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LoggerUtil.log("Tools: 错误了"+e.toString());

        }

    }

    private static void chmod() {
        outputMessage("chmod 755 -R "+FileUtil.TOOLS_DIRPATH);
    }


    private static boolean isCmdExist(String cmd) {
        String result = "";
        if (isMac() || isLinux()) {
            result = executeCmd("which " + cmd);
        } else {
            executeCmd("where " + cmd);
        }

        return result != null && !result.isEmpty();
    }

    private static boolean isLinux() {
        return System.getProperty("os.name").startsWith("Linux");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("win");
    }

    private static boolean isMac() {
        return System.getProperty("os.name").startsWith("Mac OS");
    }

    private static String executeCmd(String cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return bufferReader.readLine();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";

    }
}
