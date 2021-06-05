package com.nzy.plugin

import java.io.BufferedReader
import java.io.InputStreamReader

class Tools {


    companion object {
        @JvmStatic
        fun cmd(cmd: String, params: String) {
            val cmdStr = if (isCmdExist(cmd)) {
                "$cmd $params"
            } else {
                when {
                    isMac() ->
                        FileUtil.TOOLS_DIRPATH + "mac/" + "$cmd $params"
                    isLinux() ->
                        FileUtil.TOOLS_DIRPATH + "linux/" + "$cmd $params"
                    isWindows() ->
                        FileUtil.TOOLS_DIRPATH + "windows/" + "$cmd $params"
                    else -> ""
                }
            }
            if (cmdStr == "") {
                return
            }
            outputMessage(cmdStr)
        }

        @JvmStatic
        fun isLinux(): Boolean {
            val system = System.getProperty("os.name")
            return system.startsWith("Linux")
        }

        @JvmStatic
        fun isMac(): Boolean {
            val system = System.getProperty("os.name")
            return system.startsWith("Mac OS")
        }
        @JvmStatic
        fun isWindows(): Boolean {
            val system = System.getProperty("os.name")
            return system.toLowerCase().contains("win")
        }
        @JvmStatic
        fun chmod() {
            outputMessage("chmod 755 -R ${FileUtil.TOOLS_DIRPATH}")
        }
        @JvmStatic
        private fun outputMessage(cmd: String) {
            val process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
        }
        @JvmStatic
        private fun isCmdExist(cmd: String): Boolean {
            val result = if (isMac() || isLinux()) {
                executeCmd("which $cmd")
            } else {
                executeCmd("where $cmd")
            }
            return result != null && !result.isEmpty()
        }
        @JvmStatic
        private fun executeCmd(cmd: String): String? {
            val process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
            val bufferReader = BufferedReader(InputStreamReader(process.inputStream))
            return try {
                bufferReader.readLine()
            } catch (e: Exception) {
                null
            }
        }
    }
}