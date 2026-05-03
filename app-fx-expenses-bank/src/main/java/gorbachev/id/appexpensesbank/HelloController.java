package gorbachev.id.appexpensesbank;

import by.gorbachevid.perse.util.FilesUtil;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.core.ManagerExpensesBank;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ComposeDataBank;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.SummarizedItemCost;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.print.PageLayout;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import gorbachev.id.core.DitailStatment;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import lombok.*;

import java.beans.XMLDecoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class HelloController implements Initializable {

    public DatePicker dateFrom;
    public Button btnLoadFile;
    public DatePicker dateTo;
    public ComboBox<WrapExpensesBankInfo> bankBox;
    public BorderPane borderPaneDiagram;
    public ScrollPane scrollPane;
    public ComboBox<DitailStatment> ditalizationBox;
    public Button generate;
    public Button settings;

    public Label sumExpenses;

    private SimpleObjectProperty<File> fileBankStatement;

    private ResultParser resultParser;

    private static org.slf4j.Logger log;
    ManagerExpensesBank manager = new ManagerExpensesBank();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sumExpenses.setText("Еще не определено");
        fileBankStatement = new SimpleObjectProperty<>();

        dateFrom.setValue(Bootstrap.getDateFrom());
        dateFrom.setEditable(false);
        dateFrom.valueProperty().addListener((observableValue, localDate, newV) -> {
            if (newV != null) {
                Bootstrap.setDateFrom(newV);
            }
        });
        dateTo.setValue(Bootstrap.getDateTo());
        dateTo.setEditable(false);
        dateTo.valueProperty().addListener((observableValue, localDate, newV) -> {
            if (newV != null) {
                Bootstrap.setDateTo(newV);
            }
        });
        fileBankStatement.addListener((observableValue, file, t1) -> {
            // обнулить все
        });
        btnLoadFile.setOnAction(actionEvent -> {
            try {
                if (!bankBox.getItems().isEmpty()) {
                    fileBankStatement.set(getFromFileChooser(bankBox.getScene(), "file", bankBox.getSelectionModel().getSelectedItem().instanse.parser().supportedExtensions()));
                } else {
                    showAlert(btnLoadFile.getScene(), AlertType.MESSAGE, "Вы должны выбрать банк для того чтобы" +
                                                                         "прочитать загружаемый файл");
                }
            }catch (RuntimeException e) {
                showAlert(btnLoadFile.getScene(), AlertType.ERROR, e.getMessage());
            }
        });

        bankBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (t1 != null) {
                    Bootstrap.setSelectedBankByPosition(t1.intValue());
                }
            }
        });
        bankBox.setItems(FXCollections.observableList(getSupportedBank()));
        int indexSelectedBank = Bootstrap.getSelectedBankByPosition();
        if (!bankBox.getItems().isEmpty()) {
            if (indexSelectedBank < 0 || indexSelectedBank >= bankBox.getItems().size()) {
                bankBox.getSelectionModel().select(0);
            } else {
                bankBox.getSelectionModel().select(indexSelectedBank);
            }
        }
        bankBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(WrapExpensesBankInfo expensesBankInfo, boolean empty) {
                super.updateItem(expensesBankInfo, empty);
                if (!empty) {
                    setGraphic(buildCell(expensesBankInfo.instanse));
                } else {
                    setGraphic(null);
                }
            }

            private Node buildCell(ExpensesBankInfo expensesBankInfo) {
                HBox rootHBox = new HBox();
                rootHBox.setStyle("""
                           -fx-min-height: 20px;
                           -fx-pref-height: 20px;
                           -fx-spacing: 4px;
                           -fx-alignment: center;
                        """);
                Image image = new Image(expensesBankInfo.getBankIcon());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(20d);
                imageView.setFitWidth(20d);
                rootHBox.getChildren().add(imageView);
                Label label = new Label(expensesBankInfo.getBankName());
                label.setStyle("""
                        -fx-fill: red;
                        -fx-text-fill: black;
                        """);
                rootHBox.getChildren().add(label);
                return rootHBox;
            }
        });
        bankBox.setCellFactory(new Callback<ListView<WrapExpensesBankInfo>, ListCell<WrapExpensesBankInfo>>() {
            @Override
            public ListCell<WrapExpensesBankInfo> call(ListView<WrapExpensesBankInfo> expensesBankInfoListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(WrapExpensesBankInfo expensesBankInfo, boolean empty) {
                        super.updateItem(expensesBankInfo, empty);
                        if (!empty) {
                            setGraphic(buildCell(expensesBankInfo));
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }

            private Node buildCell(WrapExpensesBankInfo expensesBankInfo) {
                HBox rootHBox = new HBox();
                rootHBox.setStyle("""
                           -fx-border-width: 1.5;
                           -fx-border-color: black;
                           -fx-padding: 1px;
                           -fx-min-height: 25px;
                           -fx-pref-height: 25px;
                           -fx-spacing: 4px;
                           -fx-alignment: center;
                        """);
                Image image = new Image(expensesBankInfo.instanse.getBankIcon());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(20d);
                imageView.setFitWidth(20d);
                rootHBox.getChildren().add(imageView);

                TextFlow textFlow = new TextFlow();
                textFlow.getChildren().add(new Text(expensesBankInfo.instanse.getBankName()));
                rootHBox.getChildren().add(textFlow);
                return rootHBox;
            }
        });

        ditalizationBox.setItems(FXCollections.observableList(Arrays.stream(DitailStatment.values()).toList()));
        ditalizationBox.getSelectionModel().select(DitailStatment.YEAR);
        ditalizationBox.setButtonCell(new ComboBoxListCell<>(new StringConverter<DitailStatment>() {
            @Override
            public String toString(DitailStatment ditailStatment) {
                return ditailStatment.getViewName();
            }

            @Override
            public DitailStatment fromString(String s) {
                return Arrays.stream(DitailStatment.values()).filter((ditail) -> ditail.getViewName().equals(s)).findFirst().get();
            }
        }));

        settings.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("settings-view.fxml"));
                fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
                    @Override
                    public Object call(Class<?> aClass) {
                        return new SettingsController(HelloController.this);
                    }
                });
                try {
                    Scene scene = new Scene(fxmlLoader.load(), 500, 400);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(settings.getScene().getWindow());

                    stage.setTitle("Настройки");
                    stage.showAndWait();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        generate.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (fileBankStatement.get() != null) {
                try {
                    if (bankBox.getSelectionModel().getSelectedItem() != null) {
                        ParamParser paramParser = new ParamParser(fileBankStatement.get(),
                                dateFrom.getValue(), dateTo.getValue(), ditalizationBox.getSelectionModel().getSelectedItem());
                        if (Objects.isNull(resultParser)) {
                            resultParser = ManagerExpensesBank.parse(paramParser, bankBox.getSelectionModel().getSelectedItem().instanse.parser());
                        }
                        ComposeDataBank composeData = manager.recompose(paramParser, resultParser);
                        compseDeagram(composeData);
                        sumExpenses.setText(String.valueOf(composeData.getSumExpenses()));
                        Platform.runLater(()-> {
                            showAlert(generate.getScene(), AlertType.SUCCESS, "Всё получилось УРА!!!!!!!!!!!!");
                        });
                    } else {
                        System.out.println("не выбран банк");
                    }
                } catch (RuntimeException | IOException e) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    PrintStream errorPrint = new PrintStream(bos, true, StandardCharsets.UTF_8);
                    e.printStackTrace(errorPrint);
                    e.printStackTrace();
                    showAlert(generate.getScene(), AlertType.ERROR, bos.toString(StandardCharsets.UTF_8));
                }
            }else {
                showAlert(generate.getScene(), AlertType.MESSAGE_2, "Пожалуйста\n" +
                                                                " загрузите файл 'Выписки' выбранного банка: "+ bankBox.getSelectionModel().getSelectedItem().instanse.getBankName());
            }
        });
        Platform.runLater(()-> {
            btnLoadFile.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
                @SneakyThrows
                @Override
                public void handle(WindowEvent windowEvent) {
                    Bootstrap.getProperties().save();
                    System.out.println("Properties file is saved");
                }
            });
        });
    }

    /**
     * Use Set because elements should be unique in bankBox. Now every time you will be added new item in {@link #bankBox}
     * then method {@link WrapExpensesBankInfo#equals(Object)} will be checking uniqueness
     */
    private List<WrapExpensesBankInfo> getSupportedBank() {
        List<ExpensesBankInfo> result = new ArrayList<>();

        /*// reading from core
        ServiceLoader<ExpensesBankInfo> bankInfoServiceLoader = ServiceLoader.load(ExpensesBankInfo.class);
        bankInfoServiceLoader.forEach(result::add);*/

        try {
            // reading jar files
            List<Path> jars = Bootstrap.getPathJarBankInfoFromFile();
            for (Path jar : jars) {
                result.addAll(Bootstrap.extractExpensesBankInfoFrom(jar));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.stream().map(WrapExpensesBankInfo::new)
                .distinct() // unique elements next
                .collect(Collectors.toList());
    }

    private void compseDeagram(ComposeDataBank composeData) {
        SummarizedItemCost max = composeData.getMaxItem().getValue();
        ComposeChart composeChart = new ComposeChart();
        composeChart.setTitle("--------------------Диаграмма трат ------------");
        CategoryAxis xA = new CategoryAxis();
        NumberAxis yA = new NumberAxis();
        yA.setLabel(String.format("Затраты(%s)", max.getExpensesCostByDetail().get(0).getCurrency()));
        composeChart.setXA(xA);
        composeChart.setYA(yA);
        if (composeData.getDetail() == DitailStatment.YEAR) {
            xA.setLabel("Года");
            manager.callbackOnForEach(composeData, (obj) -> {
                SummarizedItemCost summarizedItemCost = (SummarizedItemCost) obj;

                String xD = String.valueOf(summarizedItemCost.getParentKey());
                Number yD = summarizedItemCost.getSumExpensesCost();

                XYChart.Series<String, Number> series;
                if (composeChart.getSeries().peekLast() == null) {
                    series = new XYChart.Series<>();
                    series.setName(xD);
                    composeChart.getSeries().add(series);
                } else {
                    series = composeChart.getSeries().getLast();
                    if (!series.getName().equals(xD)) {
                        series = new XYChart.Series<>();
                        series.setName(xD);
                        composeChart.getSeries().add(series);
                    }
                }
                composeChart.getItems().compute(xD, (keyXD, list) -> {
                    if(list == null) {
                        list = new ArrayList<>();
                    }
                    list.addAll(summarizedItemCost.getExpensesCostByDetail());
                    return list;
                });
                series.getData().add(new XYChart.Data<>(xD, yD));
            }, null, null, null);
        } else if (composeData.getDetail() == DitailStatment.MONTH) {
            xA.setLabel("Месяца");
            manager.callbackOnForEach(composeData, (yearKey) -> {
                XYChart.Series<String, Number> series;
                String xD = serilsNameToDisplayString(composeData.getDetail(), (Integer) yearKey);

                if (composeChart.getSeries().peekLast() == null) {
                    series = new XYChart.Series<>();
                    series.setName(xD);
                    composeChart.getSeries().add(series);
                } else {
                    series = composeChart.getSeries().getLast();
                    if (!series.getName().equals(xD)) {
                        series = new XYChart.Series<>();
                        series.setName(xD);
                        composeChart.getSeries().add(series);
                    }
                }
            }, (obj) -> {
                SummarizedItemCost summarizedItemCost = (SummarizedItemCost) obj;
                String xD = uniqueCodeToDisplayString(composeData.getDetail(), summarizedItemCost.getUniqueCode());
                Number yD = summarizedItemCost.getSumExpensesCost();
                composeChart.getItems().compute(xD, (keyXD, list) -> {
                    if(list == null) {
                        list = new ArrayList<>();
                    }
                    list.addAll(summarizedItemCost.getExpensesCostByDetail());
                    return list;
                });
                XYChart.Series<String, Number> series = composeChart.getSeries().getLast();
                series.getData().add(new XYChart.Data<>(xD, yD));
            }, null, null);
        }
        if (composeData.getDetail() == DitailStatment.DAY) {
            xA.setLabel("Дни");
            manager.callbackOnForEach(composeData, (yearKode) -> {
                composeChart.setYearStateTmp((Integer) yearKode);
            }, (monthKey) -> {
                XYChart.Series<String, Number> series;
                String xD = serilsNameToDisplayString(composeData.getDetail(), ((Integer) monthKey), composeChart.getYearStateTmp());

                if (composeChart.getSeries().peekLast() == null) {
                    series = new XYChart.Series<>();
                    series.setName(xD);
                    composeChart.getSeries().add(series);
                } else {
                    series = composeChart.getSeries().getLast();
                    if (!series.getName().equals(xD)) {
                        series = new XYChart.Series<>();
                        series.setName(xD);
                        composeChart.getSeries().add(series);
                    }
                }
            }, (obj) -> {
                SummarizedItemCost summarizedItemCost = (SummarizedItemCost) obj;
                XYChart.Series<String, Number> series = composeChart.getSeries().getLast();
                String xD = uniqueCodeToDisplayString(composeData.getDetail(), summarizedItemCost.getUniqueCode());
                Number yD = summarizedItemCost.getSumExpensesCost();
                composeChart.getItems().compute(xD, (keyXD, list) -> {
                    if(list == null) {
                        list = new ArrayList<>();
                    }
                    list.addAll(summarizedItemCost.getExpensesCostByDetail());
                    return list;
                });
                series.getData().add(new XYChart.Data<>(xD, yD));
            }, null);
        } else if (composeData.getDetail() == DitailStatment.HOURS) {
            xA.setLabel("часы");
            manager.callbackOnForEach(composeData,
                    (yearKey) -> {
                        composeChart.setYearStateTmp((Integer) yearKey);
                    }, (monthKey) -> {
                        composeChart.setMonthStateTmp((Integer) monthKey);
                    }, (dayKey) -> {
                        XYChart.Series<String, Number> series;
                        String xD = serilsNameToDisplayString(composeData.getDetail(), ((Integer) dayKey), composeChart.getMonthStateTmp(), composeChart.getYearStateTmp());

                        if (composeChart.getSeries().peekLast() == null) {
                            series = new XYChart.Series<>();
                            series.setName(xD);
                            composeChart.getSeries().add(series);
                        } else {
                            series = composeChart.getSeries().getLast();
                            if (!series.getName().equals(xD)) {
                                series = new XYChart.Series<>();
                                series.setName(xD);
                                composeChart.getSeries().add(series);
                            }
                        }

                    }, (obj) -> {
                        SummarizedItemCost summarizedItemCost = (SummarizedItemCost) obj;
                        XYChart.Series<String, Number> series = composeChart.getSeries().getLast();
                        String xD = uniqueCodeToDisplayString(composeData.getDetail(), summarizedItemCost.getUniqueCode());
                        Number yD = summarizedItemCost.getSumExpensesCost();
                        composeChart.getItems().compute(xD, (keyXD, list) -> {
                            if(list == null) {
                                list = new ArrayList<>();
                            }
                            list.addAll(summarizedItemCost.getExpensesCostByDetail());
                            return list;
                        });
                        series.getData().add(new XYChart.Data<>(xD, yD));
                    });
        }
        LineChart<String, Number> lineChart = composeChart.compose();
        if (lineChart == null) {
            throw new RuntimeException();
        }
//        borderPaneDiagram.setTop(new Label("FFFFFFFFFFFFFFFFF"));
        scrollPane.setPannable(true);
        lineChart.setMinSize(1000, 600); // Устанавливаем минимальные размеры графика
        lineChart.setPrefSize(1200, 800); // Предпочтительные размеры
        lineChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Позволяет масштабироваться

        lineChart.prefWidthProperty().bind(scrollPane.widthProperty());
        scrollPane.setContent(lineChart);
    }

    public String uniqueCodeToDisplayString(DitailStatment detail, String uniqueCode) {
        String[] splitUniqueDate = uniqueCode.split("-");
        if (detail == DitailStatment.YEAR) {
            return uniqueCode;
        } else if (detail == DitailStatment.MONTH) {
            return String.join(" ",
                    Month.of(Integer.parseInt(splitUniqueDate[0])).getDisplayName(TextStyle.FULL, new Locale("ru")),
                    splitUniqueDate[1]);
        } else if (detail == DitailStatment.DAY) {
            return String.join(" ",
                    splitUniqueDate[0],
                    Month.of(Integer.parseInt(splitUniqueDate[1])).getDisplayName(TextStyle.FULL, new Locale("ru")),
                    splitUniqueDate[2]);
        } else if (detail == DitailStatment.HOURS) {
            return String.join(" ",
                    splitUniqueDate[0],
                    Month.of(Integer.parseInt(splitUniqueDate[1])).getDisplayName(TextStyle.FULL, new Locale("ru")),
                    splitUniqueDate[2],
                    splitUniqueDate[3]);
        }
        throw new UnsupportedOperationException();
    }

    public String serilsNameToDisplayString(DitailStatment detail, Integer... data) {
        if (detail == DitailStatment.MONTH) {
            return String.valueOf(data[0]);
        } else if (detail == DitailStatment.DAY) {
            return String.join(" ",
                    Month.of(data[0]).getDisplayName(TextStyle.FULL, new Locale("ru")),
                    String.valueOf(data[1]));
        } else if (detail == DitailStatment.HOURS) {
            return String.join(" ",
                    String.valueOf(data[0]),
                    Month.of(data[1]).getDisplayName(TextStyle.FULL, new Locale("ru")),
                    String.valueOf(data[2]));
        }
        throw new UnsupportedOperationException();


    }

    public boolean updateBankComBoBox(Path jarFile, List<ExpensesBankInfo> expensesBankInfos) throws IOException {
        List<Path> jars = Bootstrap.getPathJarBankInfoFromFile();
        int oldCount = bankBox.getItems().size();
        if (jars.contains(jarFile)) { // is added
            expensesBankInfos.stream().map(WrapExpensesBankInfo::new)
                    .filter(elem -> !bankBox.getItems().contains(elem))
                    .forEach(bankBox.getItems()::add);
            if(!bankBox.getButtonCell().isSelected()) {
                int indexSelectedBank = Bootstrap.getSelectedBankByPosition();
                if (!bankBox.getItems().isEmpty()) {
                    if (indexSelectedBank < 0 || indexSelectedBank >= bankBox.getItems().size()) {
                        bankBox.getSelectionModel().select(0);
                    } else {
                        bankBox.getSelectionModel().select(indexSelectedBank);
                    }
                }
            }
        } else { // is remoted
            expensesBankInfos.stream().map(WrapExpensesBankInfo::new)
                    .filter(elem -> bankBox.getItems().contains(elem))
                    .forEach(bankBox.getItems()::remove);
            if(!bankBox.getButtonCell().isSelected()) {
                int indexSelectedBank = Bootstrap.getSelectedBankByPosition();
                if (!bankBox.getItems().isEmpty()) {
                    if (indexSelectedBank < 0 || indexSelectedBank >= bankBox.getItems().size()) {
                        bankBox.getSelectionModel().select(0);
                    } else {
                        bankBox.getSelectionModel().select(indexSelectedBank);
                    }
                }
            }
        }
        return oldCount != bankBox.getItems().size();
    }

    /**
     * This class created that implement logic unique elements in {{@link #bankBox}}
     *
     * @see #getSupportedBank()
     */
    @AllArgsConstructor
    public static class WrapExpensesBankInfo {

        private ExpensesBankInfo instanse;

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof WrapExpensesBankInfo other) {
                return this.instanse.getClass().getName().equals(other.instanse.getClass().getName());
            }
            return false;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public class ComposeChart {
        String title;
        CategoryAxis xA;
        NumberAxis yA;
        Deque<XYChart.Series<String, Number>> series = new ArrayDeque<>();
        Map<String, List<ItemRecordCost>> items= new HashMap<>();

        Integer yearStateTmp;
        Integer monthStateTmp;
        Integer DayStateTmp;
        Integer hoursStateTmp;

        private double dragStartX;
        private double dragStartY;

        public LineChart<String, Number> compose() {
            LineChart<String, Number> res = new LineChart<>(xA, yA);
            addListeners(res);
            res.setTitle(title);
            for (XYChart.Series<String, Number> seria : series) {
                seria.getData().forEach(data -> {
                    data.nodeProperty().addListener(new ChangeListener<Node>() {
                        @Override
                        public void changed(ObservableValue<? extends Node> observableValue, Node node, Node newNode) {
                            if (newNode != null) {
                                Node point = data.getNode();
                                Label textValue = new Label(String.valueOf(BigDecimal.valueOf(data.getYValue().doubleValue()).setScale(2, RoundingMode.HALF_EVEN)));
                                textValue.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent event) {
                                        if(event.getClickCount() == 2) {
                                            Popup popup = new Popup();
                                            popup.setAutoHide(true);

                                            List<ItemRecordCost> itemRecordCosts = items.get(data.getXValue());
                                            showResizablePopup(event, itemRecordCosts);
                                        }
                                    }
                                });
                                textValue.setStyle("-fx-font-size: 5; -fx-text-fill: black;");
                                ((StackPane) point).getChildren().add(textValue);
                            }
                        }
                    });
                });
            }
            res.getData().addAll(series);
            return res;
        }

        public void showResizablePopup(MouseEvent event, List<ItemRecordCost> itemRecordCosts) {

            if (itemRecordCosts == null || itemRecordCosts.isEmpty()) {
//                showEmptyAlert();
                return;
            }

            // Создаём Stage вместо Popup
            Stage stage = new Stage();

            // ВАЖНО: делаем окно изменяемым
            stage.setResizable(true);

            // Устанавливаем минимальный размер (опционально)
            stage.setMinWidth(400);
            stage.setMinHeight(300);

            // Устанавливаем начальный размер
            stage.setWidth(750);
            stage.setHeight(450);

            // Делаем модальным относительно родительского окна
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Создаём таблицу
            TableView<ItemRecordCost> tableView = createTableView(itemRecordCosts);

            // Заголовок с количеством
            Label headerLabel = new Label("Операции (" + itemRecordCosts.size() + " записей)");
            headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px;");
            Label headerGeneralExpenses = new Label("Общая сумма затрат: " + itemRecordCosts.stream().mapToDouble(ItemRecordCost::getAmount).sum());
            headerGeneralExpenses.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px;");
            // Контейнер
            HBox containerHBox = new HBox(10);
            containerHBox.getChildren().addAll(headerLabel, headerGeneralExpenses);

            VBox container = new VBox(5);
            container.setStyle("-fx-background-color: white;");
            container.getChildren().addAll(containerHBox, tableView);

            // Таблица должна растягиваться
            VBox.setVgrow(tableView, Priority.ALWAYS);

            Scene scene = new Scene(container);
            stage.setScene(scene);

            // Позиционируем рядом с кнопкой
            Node source = (Node) event.getSource();
            Bounds bounds = source.localToScreen(source.getBoundsInLocal());
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMaxY());

            stage.show();
        }

        private TableView<ItemRecordCost> createTableView(List<ItemRecordCost> items) {
            TableView<ItemRecordCost> tableView = new TableView<>();

            // Колонки (как в предыдущем примере)
            TableColumn<ItemRecordCost, LocalDateTime> dateColumn = new TableColumn<>("Дата операции");
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOperation"));
            dateColumn.setCellFactory(column -> new TableCell<ItemRecordCost, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                    }
                }
            });

            TableColumn<ItemRecordCost, Number> amountColumn = new TableColumn<>("Сумма");
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            amountColumn.setCellFactory(column -> new TableCell<ItemRecordCost, Number>() {
                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        double value = item.doubleValue();
                        Currency currency = getTableView().getItems().get(getIndex()).getCurrency();
                        setText(String.format("%.2f %s", value, currency));
                        setStyle(value < 0 ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                    }
                }
            });

            TableColumn<ItemRecordCost, String> mccColumn = new TableColumn<>("MCC код");
            mccColumn.setCellValueFactory(new PropertyValueFactory<>("operationMcc"));

            TableColumn<ItemRecordCost, String> placeColumn = new TableColumn<>("Место операции");
            placeColumn.setCellValueFactory(new PropertyValueFactory<>("operationPlace"));
            placeColumn.setPrefWidth(250);

            tableView.getColumns().addAll(dateColumn, amountColumn, mccColumn, placeColumn);

            // Растягиваем последнюю колонку
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            tableView.getItems().addAll(items);
            return tableView;
        }

        void addListeners(LineChart<String, Number> lineChart) {

            // Добавление масштабирования
            lineChart.setOnScroll((ScrollEvent event) -> {
                double deltaY = event.getDeltaY();
                System.out.println(deltaY);
                if (deltaY == 0) return;

                double scaleFactor = (deltaY > 0) ? 1.1 : 0.9;
                if (event.isControlDown()) {
                    lineChart.prefWidthProperty().unbind();
                    lineChart.setPrefWidth(lineChart.getPrefWidth() + (deltaY > 0 ? 200 : -200));
                } else {
                    lineChart.setScaleX(lineChart.getScaleX() * scaleFactor);
                    lineChart.setScaleY(lineChart.getScaleY() * scaleFactor);
                }
            });

            // Добавление панорамирования
            lineChart.setOnMousePressed((MouseEvent event) -> {
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
            });

            lineChart.setOnMouseDragged((MouseEvent event) -> {
                double deltaX = event.getSceneX() - dragStartX;
                double deltaY = event.getSceneY() - dragStartY;

                lineChart.setTranslateX(lineChart.getTranslateX() + deltaX);
                lineChart.setTranslateY(lineChart.getTranslateY() + deltaY);

                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
            });
        }
    }


    public static File getFromFileChooser(Scene scene, String s, String... extension) {
        return getFromFileChooser(scene, null, s, extension);
    }

    public static File getFromFileChooser(Scene scene, Path initialPath, String s, String... extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Some Files");
        File initialReal = FilesUtil.findDirIsExist(initialPath);
        if (initialReal != null) {
            initialPath = initialReal.toPath();
            if (initialPath.toFile().isFile()) {
                initialPath = initialPath.getParent();
            }
            fileChooser.setInitialDirectory(initialPath.toFile());
        }
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("all", "*"),
                new FileChooser.ExtensionFilter(s, extension));

        return fileChooser.showOpenDialog(scene.getWindow());
    }

    public void showAlert(Scene root, AlertType type, String text) {
        BorderPane rootPnl = new BorderPane();

        TextFlow textFlow = new TextFlow();
        textFlow.getChildren().add(new Text(text));

        ImageView messageIcon = new ImageView(new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(type.getInnerImage()))));
        messageIcon.fitHeightProperty().bind(rootPnl.heightProperty());
        messageIcon.setFitWidth(200);


        rootPnl.setCenter(textFlow);
        rootPnl.setRight(messageIcon);
        Scene sceneMsg = new Scene(rootPnl, 400, 200);
        Stage stage = new Stage();
        stage.setMaxHeight(400);
        stage.setMaxWidth(600);
        stage.setTitle("Сообщение");
        stage.initOwner(root.getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(sceneMsg);
        stage.showAndWait();
    }

    enum AlertType {
        MESSAGE("img/message-img.jpg"),
        MESSAGE_2("img/message-img2.png"),

        ERROR("img/message-error.jpg"),
        ERROR_2("img/message-error2.jpg"),

        SUCCESS("img/message-success.jpg");

        @Getter
        private String innerImage;

        AlertType(String innerImage) {
            this.innerImage = innerImage;
        }
    }
}