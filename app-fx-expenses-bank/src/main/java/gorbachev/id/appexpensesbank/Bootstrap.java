package gorbachev.id.appexpensesbank;

import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.parent.BootstrapParent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static gorbachev.id.parent.BootstrapParent.SYS_KEY_PATH_LOGS;


public class Bootstrap {

    public static final String fileNameManagerBank = "path-jar-expenses-bank-info.txt";

    public static Path pathApp;

    public static Path filePathJarBankInfo;

    public static void configure(String[] args) {
        BootstrapParent.configure(args);
        pathApp = Path.of(System.getProperty(SYS_KEY_PATH_LOGS));
        filePathJarBankInfo = Path.of(pathApp.toAbsolutePath().toString(), fileNameManagerBank);
    }

    public static void addPathToJarBankInfo(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("jar file: " + path + " not founded");
        }
        if (!Files.exists(filePathJarBankInfo)) {
            Files.createFile(filePathJarBankInfo);
        }
        List<Path> pathsToJar = Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).toList();
        if (!pathsToJar.contains(path)) {
            Files.write(filePathJarBankInfo, pathsToJar.stream().map(Path::toString).toList());
        }
    }

    public static void removePathToJarBankInfo(Path path) throws IOException {
        if (Files.exists(filePathJarBankInfo)) {
            List<Path> pathsToJar = new java.util.ArrayList<>((Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).collect(Collectors.toSet())));
            pathsToJar.remove(path);
            Files.write(filePathJarBankInfo, pathsToJar.stream().map(Path::toString).toList());
        }
    }

    public static List<Path> getPathJarBankInfoFromFile() throws IOException {
        return Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).toList();
    }

    public static List<ExpensesBankInfo> extractExpensesBankInfoFrom(Path jarFile) throws MalformedURLException {
        URLClassLoader foreignLoader = URLClassLoader.newInstance(new URL[]{jarFile.toFile().toURI().toURL()}, Bootstrap.class.getClassLoader());
        ServiceLoader<ExpensesBankInfo> expensesBankInfos = ServiceLoader.load(ExpensesBankInfo.class, foreignLoader);
        List<ExpensesBankInfo> result = new ArrayList<>();
        expensesBankInfos.forEach(result::add);
        return result;
    }
}
