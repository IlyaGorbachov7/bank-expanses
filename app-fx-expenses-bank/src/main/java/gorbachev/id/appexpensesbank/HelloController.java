package gorbachev.id.appexpensesbank;

import gorbachev.id.core.ParserExpensesBank;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.bank.parsers.BelGosPromBankParser;
import gorbachev.id.core.model.ParamParser;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import gorbachev.id.core.DitailStatment;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@Slf4j
public class HelloController implements Initializable {

    public DatePicker dateFrom;
    public Button btnLoadFile;
    public DatePicker dateTo;
    public ComboBox<String> bankBox;
    public LineChart<Double, String> chartLine;
    public ComboBox<DitailStatment> ditalizationBox;
    public Button generate;

    private SimpleObjectProperty<File> fileBankStatement;

    private ResultParser resultParser;

    private static org.slf4j.Logger log;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileBankStatement = new SimpleObjectProperty<>();
        fileBankStatement.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File file, File t1) {
             // обнулить все
            }
        });
        btnLoadFile.setOnAction(actionEvent -> {
            log.debug("click on select file: ");
            fileBankStatement.set(getFromFileChooser(bankBox.getScene()));
        });


      /*  bankBox.setSelectionModel(new SingleSelectionModel<BankStatement>() {
            @Override
            protected BankStatement getModelItem(int i) {
                return Stream.of(BankStatement.values()).toList().get(i);
            }

            @Override
            protected int getItemCount() {
                return (int) Stream.of(BankStatement.values()).count();
            }
        });
        bankBox.setCellFactory(new Callback<ListView<BankStatement>, ListCell<BankStatement>>() {
            @Override
            public ListCell<BankStatement> call(ListView<BankStatement> bankStatementListView) {
                return null;
            }
        });*/
        bankBox.setButtonCell(new ListCell<>());


        generate.onMouseClickedProperty().addListener(new ChangeListener<EventHandler<? super MouseEvent>>() {
            @Override
            public void changed(ObservableValue<? extends EventHandler<? super MouseEvent>> observableValue, EventHandler<? super MouseEvent> eventHandler, EventHandler<? super MouseEvent> t1) {
                if(fileBankStatement.get() != null) {

                    resultParser = ParserExpensesBank.parse(new ParamParser(null, null, null, null), new BelGosPromBankParser());
                    log.info("Parser is complied");

                }else {
                    log.warn("File don't selected");
                }

            }
        });
    }



    public static File getFromFileChooser(Scene scene) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Some Files");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("html", "*.html"));

        return fileChooser.showOpenDialog(scene.getWindow());
    }
}