package gorbachev.id.appexpensesbank;

import by.gorbachevid.perse.util.NotAccessToFileException;
import gorbachev.id.core.ExpensesBankInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
import java.util.Objects;
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
                                showAlert(jarListView.getScene(), e.getMessage());
                                if(!(e instanceof NotAccessToFileException)) { // if exception don't connected with saving to file. This necessary that don't permit repeated saving same item to file
                                    jarListView.getItems().remove(jarFile);
                                }
                                break;
                            }
                        }
                    } else if (change.wasRemoved()) {
                        for (Path jarFile : change.getAddedSubList()) {
                            try {
                                Bootstrap.removePathToJarBankInfo(jarFile);
                                List<ExpensesBankInfo> listExpensesBankInfo = Bootstrap.extractExpensesBankInfoFrom(jarFile);
                                rootController.updateBankComBoBox(jarFile, listExpensesBankInfo);
                            } catch (IOException e) {
                                showAlert(jarListView.getScene(), e.getMessage());
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

    public void showAlert(Scene root, String text) {
        BorderPane rootPnl = new BorderPane();

        TextFlow textFlow = new TextFlow();
        textFlow.getChildren().add(new Text(text));

        ImageView messageIcon = new ImageView(new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("img/message-icon.jpg"))));
        messageIcon.setFitHeight(70);
        messageIcon.setFitHeight(70);


        rootPnl.setCenter(textFlow);
        rootPnl.setLeft(messageIcon);
        Scene sceneMsg = new Scene(rootPnl, 400, 200);
        Stage stage = new Stage();

        stage.setTitle("Сообщение");
        stage.initOwner(root.getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(sceneMsg);
        stage.showAndWait();
    }
}
