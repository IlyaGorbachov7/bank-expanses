package gorbachev.id.parent;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

public class BootstrapParent {
    public static final String SYS_KEY_PATH_LOGS = "PATH_LOGS";

    public static void main(String[] args) {
        System.out.println("fsd");
    }
    @SneakyThrows
    public static void configure(String[] args) {
        if (System.getProperty(SYS_KEY_PATH_LOGS) == null) {
            System.setProperty(SYS_KEY_PATH_LOGS, Path.of(System.getProperty("user.home"), ".expenses-app").toString());
            System.out.println("SYS_KEY_PATH_LOGS : " + System.getProperty(SYS_KEY_PATH_LOGS));
            if (!Files.exists(Path.of(System.getProperty(SYS_KEY_PATH_LOGS)))) {
                Files.createDirectory(Path.of(System.getProperty(SYS_KEY_PATH_LOGS)));
            }
        }
    }
}
