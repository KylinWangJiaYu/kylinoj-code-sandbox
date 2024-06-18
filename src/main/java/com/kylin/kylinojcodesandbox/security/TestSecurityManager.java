package com.kylin.kylinojcodesandbox.security;

import cn.hutool.core.io.FileUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {
    public static void main(String[] args) {
        System.setSecurityManager((new MySecurityManager()));
        //List<String> strings = FileUtil.readLines("D:\\code\\oj\\kylinoj-code-sandbox\\src\\main\\resources\\application.yml", StandardCharsets.UTF_8);
        FileUtil.writeString("aaa","afile", Charset.defaultCharset());
        //System.out.println(strings);
    }
}
