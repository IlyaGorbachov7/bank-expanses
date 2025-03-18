package gorbachev.id.appexpensesbank;

import by.gorbachevid.perse.resbndl.impl.PropertiesManagerBase;
import by.gorbachevid.perse.util.FilesUtil;
import by.gorbachevid.perse.util.NotAccessToFileException;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.parent.BootstrapParent;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static gorbachev.id.parent.BootstrapParent.SYS_KEY_PATH_LOGS;


public class Bootstrap {

    public static final String KEY_DATE_FROM = "dateFrom";
    public static final String KEY_DATE_TO = "dateTo";
    public static final String KEY_SELECTED_BANK = "selectedBank";
    public static final String KEY_SELECTED_DEF_DIR_FILE = "defDir";

    public static final String fileNameManagerBank = "path-jar-expenses-bank-info.txt";

    public static final String fileNameProperties = "configuration.properties";

    private static Path filePathJarBankInfo;

    @Getter
    private static PropertiesManagerBase properties;

    public static void configure(String[] args) throws IOException {
        BootstrapParent.configure(args);
        Path pathApp = Path.of(System.getProperty(SYS_KEY_PATH_LOGS));
        filePathJarBankInfo = Path.of(pathApp.toAbsolutePath().toString(), fileNameManagerBank);
        if (!Files.exists(filePathJarBankInfo)) {
            Files.createFile(filePathJarBankInfo);
        }
        Path fileProperties = Path.of(pathApp.toAbsolutePath().toString(), fileNameProperties);
        if (Files.notExists(fileProperties)) {
            properties = PropertiesManagerBase.builder().setFileSore(fileProperties.toFile()).build();
        } else {
            properties = PropertiesManagerBase.builder().build(fileProperties.toFile());
        }
    }

    public static void addPathToJarBankInfo(Path path) throws NotAccessToFileException {
        try {
            if (!Files.exists(path)) {
                throw new IOException("jar file: " + path + " not founded");
            }
            List<Path> pathsToJar = Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).collect(Collectors.toList());
            if (!pathsToJar.contains(path)) {
                pathsToJar.add(path);
                Files.write(filePathJarBankInfo, pathsToJar.stream().map(Path::toString).toList());
            }
        } catch (IOException e) {
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
        } catch (IOException e) {
            throw new NotAccessToFileException(e.getMessage(), e, path);
        }
    }

    public static List<Path> getPathJarBankInfoFromFile() throws IOException {

        return Files.readAllLines(filePathJarBankInfo).stream().map(Path::of).toList();
    }

    public static List<ExpensesBankInfo> extractExpensesBankInfoFrom(Path jarFile) throws IOException {
        if (!Files.isReadable(jarFile) || FilesUtil.getExtensionWithPoint(jarFile.toString()).isEmpty()) {
            return Collections.emptyList();
        }
        URLClassLoader foreignLoader = URLClassLoader.newInstance(new URL[]{jarFile.toFile().toURI().toURL()});
        List<ExpensesBankInfo> result = new ArrayList<>();
        ServiceLoader<ExpensesBankInfo> expensesBankInfos = ServiceLoader.load(ExpensesBankInfo.class, foreignLoader);
        expensesBankInfos.stream()
                .filter(provider -> provider.type().getClassLoader() == foreignLoader)
                .forEach((provider) -> result.add(provider.get()));
        return result;
    }

    public static LocalDate getDateFrom() {
        String defaultV = "01.01.2024";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);
        try {
            return LocalDate.parse(properties.getValue(KEY_DATE_FROM, defaultV), formatter);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(defaultV, formatter);
        }
    }

    public static int getSelectedBankByPosition() {
        return properties.getInt(KEY_SELECTED_BANK, -1);
    }

    public static void setSelectedBankByPosition(int index) {
        properties.setValue(KEY_SELECTED_BANK, index);
    }

    public static void setDateFrom(LocalDate date) {
        properties.setValue(KEY_DATE_FROM, date.toString());
    }

    public static LocalDate getDateTo() {
        String defaultV = "01.01.2025";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);
        try {
            return LocalDate.parse(properties.getValue(KEY_DATE_TO, defaultV), formatter);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(defaultV, formatter);
        }
    }

    public static void setDateTo(LocalDate date) {
        properties.setValue(KEY_DATE_TO, date.toString());
    }

    public static File getDefSelectedDir() {
        String pathStr = properties.getValue(KEY_SELECTED_DEF_DIR_FILE, null);
        if (pathStr == null) return null;
        Path path = Path.of(pathStr);
        File file = FilesUtil.findDirIsExist(path);
        if (file != null && !file.isDirectory()) {
            file = file.getParentFile();
        }
        return file;
    }

    public static void setDefSelectedDir(File dir) {
        dir = FilesUtil.findDirIsExist(dir.toPath());
        if (dir != null) {
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();
                properties.setValue(KEY_SELECTED_DEF_DIR_FILE, dir.toString());
            }
        }
    }
}
