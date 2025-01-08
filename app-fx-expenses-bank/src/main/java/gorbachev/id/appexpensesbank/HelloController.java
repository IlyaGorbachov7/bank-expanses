package gorbachev.id.appexpensesbank;

import by.gorbachevid.perse.util.FilesUtil;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.core.ManagerExpensesBank;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ComposeDataBank;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.SummarizedItemCost;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import gorbachev.id.core.DitailStatment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
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

    private SimpleObjectProperty<File> fileBankStatement;

    private ResultParser resultParser;

    private static org.slf4j.Logger log;
    ManagerExpensesBank manager = new ManagerExpensesBank();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileBankStatement = new SimpleObjectProperty<>();

        LocalDate from = LocalDate.parse("01.01.2024", DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT));
        LocalDate to = LocalDate.parse("01.02.2025", DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT));
        dateFrom.setValue(from);
        dateTo.setValue(to);

        fileBankStatement.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File file, File t1) {
                // обнулить все
                resultParser = null;
            }
        });
        btnLoadFile.setOnAction(actionEvent -> {
            log.debug("click on select file: ");
            fileBankStatement.set(getFromFileChooser(bankBox.getScene(), "html", "*.html"));
        });

        bankBox.setItems(FXCollections.observableList(getSupportedBank()));
        if (bankBox.getItems().size() > 0) {
            bankBox.getSelectionModel().select(0);
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

            try {
                if (bankBox.getSelectionModel().getSelectedItem() != null) {
                    ParamParser paramParser = new ParamParser(Path.of(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("report (3).html")).toURI()).toFile(),
                            dateFrom.getValue(), dateTo.getValue(), ditalizationBox.getSelectionModel().getSelectedItem());
                    if (Objects.isNull(resultParser)) {
                        resultParser = ManagerExpensesBank.parse(paramParser, bankBox.getSelectionModel().getSelectedItem().instanse.parser());
                    }
                    ComposeDataBank composeData = manager.recompose(paramParser, resultParser);
                    compseDeagram(composeData);
                } else {
                    System.out.println("не выбран банк");
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
//                log.info("Parser is complied");
        });

    }

    /**
     * Use Set because elements should be unique in bankBox. Now every time you will be added new item in {@link #bankBox}
     * then method {@link WrapExpensesBankInfo#equals(Object)} will be checking uniqueness
     */
    private List<WrapExpensesBankInfo> getSupportedBank() {
        // reading from core
        List<ExpensesBankInfo> result = new ArrayList<>();
        ServiceLoader<ExpensesBankInfo> bankInfoServiceLoader = ServiceLoader.load(ExpensesBankInfo.class);
        bankInfoServiceLoader.forEach(result::add);

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

    public void updateBankComBoBox(Path jarFile, List<ExpensesBankInfo> expensesBankInfos) throws IOException {
        List<Path> jars = Bootstrap.getPathJarBankInfoFromFile();
        if (jars.contains(jarFile)) { // is added
            expensesBankInfos.stream().map(WrapExpensesBankInfo::new)
                    .filter(elem -> !bankBox.getItems().contains(elem))
                    .forEach(bankBox.getItems()::add);
        } else { // is remoted
            expensesBankInfos.stream().map(WrapExpensesBankInfo::new)
                    .filter(elem -> bankBox.getItems().contains(elem))
                    .forEach(bankBox.getItems()::remove);
        }
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
                return this.instanse.getClass() == other.instanse.getClass();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Some Files");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(s, extension));

        return fileChooser.showOpenDialog(scene.getWindow());
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
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(s, extension));

        return fileChooser.showOpenDialog(scene.getWindow());
    }
}