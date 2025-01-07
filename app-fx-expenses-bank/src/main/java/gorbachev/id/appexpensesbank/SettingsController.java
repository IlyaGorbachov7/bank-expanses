package gorbachev.id.appexpensesbank;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    public Label optionBank;

    public BorderPane rootOptionPane;

    public AnchorPane optionPanel;
    public AnchorPane managerBankSettingsPane;

    public HelloController rootController;

    public SettingsController(HelloController rootController) {
        this.rootController = rootController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        optionBank.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                if (managerBankSettingsPane == null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ManagerBankSettingsController.fxml"));
                    fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
                        @Override
                        public Object call(Class<?> aClass) {
                            return new ManagerBankSettingsController(rootController);
                        }
                    });
                    try {
                        managerBankSettingsPane = fxmlLoader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                rootOptionPane.setCenter(managerBankSettingsPane);
            });
        });
    }
}
