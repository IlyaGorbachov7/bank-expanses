package gorbachev.id.appexpensesbank;


import javafx.application.Application;

import java.io.IOException;

public class LauncherApp {
    public static void main(String[] args) throws IOException {
        Bootstrap.configure(args);
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LauncherApp.class);
        log.warn("--------- Application was configured ----------------");
        Application.launch(HelloApplication.class, args);
    }
}
