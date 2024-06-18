package com.kylin.kylinojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.kylin.kylinojcodesandbox.model.ExecuteCodeRequest;
import com.kylin.kylinojcodesandbox.model.ExecuteCodeResponse;
import com.kylin.kylinojcodesandbox.model.ExecuteMessage;
import com.kylin.kylinojcodesandbox.model.JudgeInfo;
import com.kylin.kylinojcodesandbox.security.DefaultSecurityManager;
import com.kylin.kylinojcodesandbox.security.DenySecurityManager;
import com.kylin.kylinojcodesandbox.utils.ProcessUtils;

import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_CODE_FILE_NAME = "Main.java";
    private static final long TIME_OUT = 5000L;
    private static final String SECURITY_MANAGER_PATH = "D:\\code\\oj\\kylinoj-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME ="MySecurityManager";
    // 黑名单
    public static final List<String> blackList = Arrays.asList("Files", "exec");
    //使用字典树代替列表存储单词，用更少的空间存储更多的敏感词汇，并且实现更高效的敏感词查找。
    private static final WordTree WORD_TREE;

    static {
        // 静态对象初始化 初始化字典树 没必要每次启动都加
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }


    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        //readStr可以直接读resource下的文件
        //String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/unsafeCode/SleepError.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/unsafeCode/MemoryError.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/unsafeCode/ReadFileError.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/unsafeCode/WriteFileError.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //System.setSecurityManager(new DenySecurityManager());
        /**
         * 执行 `process.waitFor` 等待程序执行完成，
         * 并通过返回的 `exitValue`判断程序是否正常返回，
         * 然后从 Process 的输入流 `inputStream`和错误流 `errorStream`获取控制台输出。
         */
        String code = executeCodeRequest.getCode();
//        // 校验代码是否包含有黑名单中命令
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if (foundWord != null) {
//            System.out.println("此文件包含敏感词：" + foundWord.getFoundWord());
//            return null;
//        }

        // ================1、保存用户代码为文件===================
        // 获取用户工作文件路径
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局文件路径是否存在
        if (!FileUtil.exist(globalCodePathName)) {
            // 不存在，则创建
            FileUtil.mkdir(globalCodePathName);
        }
        // 存在，则保存用户提交代码，用户代码隔离存放 每次提交的代码弄个文件夹存放,相当于父目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        // 实际存放文件的目录：Main.java
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_CODE_FILE_NAME;
        File userCodeFile = FileUtil.writeBytes(code.getBytes(StandardCharsets.UTF_8), userCodePath);//文件内容 文件路径 把用户代码写入进来

        // ===============2、编译代码，得到class文件===================
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
            //throw new RuntimeException(e);
        }
        //===============3、执行代码，得到输出结果===================
        List<ExecuteMessage> executeMessageList = new ArrayList<>();//输出信息列表
        // 此处mac下是使用 : 分割不同类，windows下是使用 ; 分割不同类名
        //***********在运行 java 程序时，指定安全管理器 class 文件的路径、安全管理器的名称。****************
        //指定安全管理器时制定了类名，MySecurityManager，所以resource/security/MySecurityManager不用改成Main就能编译
        String runCmdPattern = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s";
//        String runCmdPattern = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s";
        List<String> inputList = executeCodeRequest.getInputList();
        for (String inputArgs : inputList) {
            String runCmd = String.format(runCmdPattern, userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME,inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);//Runtime.getRuntime()开启了一个新的子进程
                //超时控制：创建一个守护线程，超时后自动终止Process线程
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("程序运行超时，已经中断");
                        runProcess.destroy();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess,inputArgs);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                return getErrorResponse(e);
                //throw new RuntimeException(e);
            }
        }

        //=================4、收集整理输出结果===================
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxExecTime = 0L;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                // 执行中出错
                executeCodeResponse.setMessage(errorMessage);
                //用户提交的代码中程序存在错误
                executeCodeResponse.setStatus(3);//FAILED.getValue()
                break;
            }
            outputList.add(executeMessage.getMessage());
            //使用最大值【因为有一个程序超时，判题就要判超时】来统计时间（扩展：可以每个测试用例都有一个独立的内存、时间占用的统计）
            Long execTime = executeMessage.getTime();
            if (execTime != null) {
                maxExecTime = Math.max(maxExecTime, execTime);
            }
        }
        // 正常执行
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);//RUNNING.getValue()
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        // todo Java原生获取内存占用
        //获取内存信息：实现比较复杂，无法从 Process 对象中获取到子进程号，也不推荐在 Java 原生实现代码沙箱的过程中获取。
//        judgeInfo.setMemory(0L);//要借用第三方库来获取内存占用，非常麻烦
        judgeInfo.setTime(maxExecTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //=================5、收集整理输出结果===================
        if (userCodeFile.getParentFile() != null) {
            boolean delFileRes = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (delFileRes ? "成功" : "失败"));
        }


        return executeCodeResponse;
    }
//=================6、错误处理，提升程序健壮性===================

    /**
     * 获取错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setOutputList(new ArrayList<>());
        response.setMessage(e.getMessage());
        //2 示例代码杀向错误
        response.setStatus(2);//FAILED.getValue()
        response.setJudgeInfo(new JudgeInfo());
        return response;
    }
}
