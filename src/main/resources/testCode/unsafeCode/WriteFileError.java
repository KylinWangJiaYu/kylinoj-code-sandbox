
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 向服务器写文件（植入危险程序）
 * 可以直接向服务器上写入文件。
 * 比如一个木马程序：`java -version 2>&1`（示例命令）
 * 1. `java -version` 用于显示 Java 版本信息。这会将版本信息输出到标准错误流（stderr）而不是标准输出流（stdout）。
 * 2. `2>&1` 将标准错误流重定向到标准输出流。这样，Java 版本信息就会被发送到标准输出流。
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        String errorProgram = " java -version 2>&1";
        Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
        System.out.println("执行木马异常程序成功");
    }
}
