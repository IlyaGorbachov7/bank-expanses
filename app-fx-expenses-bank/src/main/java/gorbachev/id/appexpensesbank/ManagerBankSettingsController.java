package gorbachev.id.appexpensesbank;

import by.gorbachevid.perse.util.NotAccessToFileException;
import gorbachev.id.core.ExpensesBankInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ManagerBankSettingsController implements Initializable {

    public Button btnAdd;
    public Button btnDelete;
    public ListView<Path> jarListView;

    public HelloController rootController;

    public Path oldSelectedDir;

    public ManagerBankSettingsController(HelloController rootController) {
        this.rootController = rootController;
    }

    public List<Path> addedNewJarToListViewTmp = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        oldSelectedDir = Bootstrap.getDefSelectedDir();
        try { // installation abservablelist
            jarListView.setItems(FXCollections.observableList(new ArrayList<>(Bootstrap.getPathJarBankInfoFromFile())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // gettting instalieted list and add new listener
        jarListView.getItems().addListener(new ListChangeListener<Path>() {
            @Override
            public void onChanged(Change<? extends Path> change) {
                Platform.runLater(() -> {
                    if (change.wasAdded()) {
                        for (Path jarFile : change.getAddedSubList()) {
                            try {
                                Bootstrap.addPathToJarBankInfo(jarFile);
                                List<ExpensesBankInfo> listExpensesBankInfo = Bootstrap.extractExpensesBankInfoFrom(jarFile);
                                if (listExpensesBankInfo.isEmpty()) {
                                    throw new IOException(String.format("""
                                            Jar файл: %s
                                            не содержит реализации интерфейса %s.
                                            Вам необходимо загрузить jar-файл реализации данного интерфейса.
                                            """, jarFile, ExpensesBankInfo.class));
                                }
                                rootController.updateBankComBoBox(jarFile, listExpensesBankInfo);
                            } catch (IOException e) {
                                rootController.showAlert(jarListView.getScene(), HelloController.AlertType.ERROR_2, e.getMessage());
                                if (!(e instanceof NotAccessToFileException)) { // if exception don't connected with saving to file. This necessary that don't permit repeated saving same item to file
                                    jarListView.getItems().remove(jarFile);
                                }
                                break;
                            }
                        }
                    } else if (change.wasRemoved()) {
                        for (Path jarFile : change.getRemoved()) {
                            try {
                                Bootstrap.removePathToJarBankInfo(jarFile);
                                List<ExpensesBankInfo> listExpensesBankInfo = Bootstrap.extractExpensesBankInfoFrom(jarFile);
                                rootController.updateBankComBoBox(jarFile, listExpensesBankInfo);
                            } catch (IOException e) {
                                rootController.showAlert(jarListView.getScene(),HelloController.AlertType.ERROR_2, e.getMessage());
                                if (!(e instanceof NotAccessToFileException)) { // if exception don't connected with saving to file. This necessary that don't permit repeated saving same item to file
                                    jarListView.getItems().add(jarFile); // then you have rollback removed item
                                }
                                break;
                            }
                        }
                    }
                });
            }
        });
        btnAdd.setOnAction(actionEvent -> {
            File file = HelloController.getFromFileChooser(btnAdd.getScene(), oldSelectedDir, "jar", "*.jar");
            if (file != null) {
                oldSelectedDir = file.toPath();
                jarListView.getItems().add(file.toPath());
                Bootstrap.setDefSelectedDir(oldSelectedDir.toFile());
                try {
                    Bootstrap.getProperties().save();
                } catch (IOException e) {
                    System.out.println("ERROR : don't saved");
                }

            }
        });

        btnDelete.setOnAction(actionEvent ->

        {
            Path selectedItem = jarListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) { // если элемент выбран
                jarListView.getItems().remove(selectedItem);
            }
        });
        jarListView.getSelectionModel().

                setSelectionMode(SelectionMode.SINGLE);

    }

}
