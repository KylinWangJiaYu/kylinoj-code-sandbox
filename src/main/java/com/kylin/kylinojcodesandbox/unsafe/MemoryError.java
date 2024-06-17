package com.kylin.kylinojcodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * 无限占用空间（浪费系统内存）
 * 不能定义对象或简单常量 JVM会帮忙回收 测不到占用内存情况
 * 实际运行程序时，我们会发现，内存占用到达一定空间后，
 * 程序就自动报错：`java.lang.OutOfMemoryError: Java heap space`，
 * 而不是无限增加内存占用，直到系统死机。这是 JVM 的一个保护机制。
 * 可以使用 JVisualVM 或 JConsole 工具，连接到 JVM 虚拟机上来可视化查看运行状态。
 */
public class MemoryError {
    public static void main(String[] args) throws InterruptedException {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[100000]);
        }
    }
}
