package gorbachev.id.appexpensesbank;

import java.nio.file.Path;

public class Bootstrap {

    public static final String SYS_KEY_PATH_LOGS = "PATH_LOGS";

    public static void configure(String[] args) {
        if(System.getProperty(SYS_KEY_PATH_LOGS) == null) {
            System.setProperty(SYS_KEY_PATH_LOGS, Path.of(System.getProperty("user.home"),"expenses-app").toString());
            System.out.println("SYS_KEY_PATH_LOGS : "+System.getProperty(SYS_KEY_PATH_LOGS));
        }
    }
}
