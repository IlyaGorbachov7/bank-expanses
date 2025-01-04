package gorbachev.id.parent;

import java.nio.file.Path;

public class BootstrapParent {
    public static void main(String[] args) {
//        Application.launch(HelloApplication.class, args);
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BootstrapParent.class);
        log.error("FFFFFFFFFFF");
        System.out.println(Path.of("").toAbsolutePath());
        System.out.println(BootstrapParent.class.getResource("/log4j.xml"));
    }
}
