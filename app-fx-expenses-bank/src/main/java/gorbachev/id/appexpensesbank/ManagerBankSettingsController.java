package gorbachev.id.appexpensesbank;

import gorbachev.id.core.ExpensesBankInfo;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    public ManagerBankSettingsController(HelloController rootController) {
        this.rootController = rootController;
    }

    public List<Path> addedNewJarToListViewTmp = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jarListView.getItems().addListener(new ListChangeListener<Path>() {
            @Override
            public void onChanged(Change<? extends Path> change) {
                Platform.runLater(() -> {
                    if (change.wasAdded()) {
                        try {
                            for (Path jarFile : change.getAddedSubList()) {
                                List<ExpensesBankInfo> listExpensesBankInfo = Bootstrap.extractExpensesBankInfoFrom(jarFile);
                                rootController.updateBankComBoBox(jarFile, listExpensesBankInfo);
                            }
                        } catch (IOException e) {
                            showAlert(jarListView.getScene(), e.getMessage());
                        }
                    } else if (change.wasRemoved()) {
                        try {
                            for (Path jarFile : change.getAddedSubList()) {
                                List<ExpensesBankInfo> listExpensesBankInfo = Bootstrap.extractExpensesBankInfoFrom(jarFile);
                                rootController.updateBankComBoBox(jarFile, listExpensesBankInfo);
                            }
                        } catch (IOException e) {
                            showAlert(jarListView.getScene(), e.getMessage());
                        }
                    }
                });
            }
        });
        btnAdd.setOnAction(actionEvent -> {
            File file = HelloController.getFromFileChooser(btnAdd.getScene(), "jar", "*.jar");
            if (file != null) {
                try {
                    Bootstrap.addPathToJarBankInfo(file.toPath());
                    jarListView.getItems().add(file.toPath());
                } catch (IOException e) {
                    System.out.println(String.format("File: %s don't added to file", file));
                }
            }
        });

        btnDelete.setOnAction(actionEvent -> {
            Path selectedItem = jarListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) { // если элемент выбран
                try {
                    Bootstrap.removePathToJarBankInfo(selectedItem);
                    jarListView.getItems().remove(selectedItem);
                } catch (IOException e) {
                    System.out.println(String.format("File: %s don't deleted to file", selectedItem));
                }
            }
        });
        jarListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void showAlert(Scene root, String text) {
        TextFlow textFlow = new TextFlow();
        textFlow.getChildren().add(new Text(text));

        Scene sceneMsg = new Scene(textFlow, 300, 200);
        Stage stage = new Stage();
        stage.setTitle("Сообщение");
        stage.initOwner(root.getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(sceneMsg);
        stage.showAndWait();
    }
}
