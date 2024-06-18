
import java.util.ArrayList;
import java.util.List;

/**
 * 无限占用空间（浪费系统内存）
 * 不能定义对象或简单常量 JVM会帮忙回收 测不到占用内存情况
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[100000]);
        }
    }
}
