package gorbachev.id.appexpensesbank;

import by.gorbachevid.perse.util.NotAccessToFileException;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.parent.BootstrapParent;

import java.io.IOException;
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

    public static void addPathToJarBankInfo(Path path) throws NotAccessToFileException {
        try {
        if (!Files.exists(path)) {
            throw new IOException("jar file: " + path + " not founded");
        }
        if (!Files.exists(filePathJarBankInfo)) {
            Files.createFile(filePathJarBankInfo);
        }
        List<Path> pathsToJar = Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).collect(Collectors.toList());
        if (!pathsToJar.contains(path)) {
            pathsToJar.add(path);
            Files.write(filePathJarBankInfo, pathsToJar.stream().map(Path::toString).toList());
        }
        }catch (IOException e) {
            throw new NotAccessToFileException(e.getMessage(), e, path);
        }
    }

    public static void removePathToJarBankInfo(Path path) throws NotAccessToFileException {
        try {
            if (Files.exists(filePathJarBankInfo)) {
                List<Path> pathsToJar = new java.util.ArrayList<>((Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).collect(Collectors.toSet())));
                pathsToJar.remove(path);
                Files.write(filePathJarBankInfo, pathsToJar.stream().map(Path::toString).toList());
            }
        }catch (IOException e) {
            throw new NotAccessToFileException(e.getMessage(), e, path);
        }
    }

    public static List<Path> getPathJarBankInfoFromFile() throws IOException {
        return Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).toList();
    }

    public static List<ExpensesBankInfo> extractExpensesBankInfoFrom(Path jarFile) throws IOException {
        URLClassLoader foreignLoader = URLClassLoader.newInstance(new URL[]{jarFile.toFile().toURI().toURL()});
        List<ExpensesBankInfo> result = new ArrayList<>();
        ServiceLoader<ExpensesBankInfo> expensesBankInfos = ServiceLoader.load(ExpensesBankInfo.class, foreignLoader);
        expensesBankInfos.stream()
                .filter(provider -> provider.type().getClassLoader() == foreignLoader)
                .forEach((provider) -> result.add(provider.get()));
        return result;
    }
}
