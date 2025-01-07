package gorbachev.id.appexpensesbank;

import gorbachev.id.core.ManagerExpensesBank;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.bank.parsers.BelGosPromBankParser;
import gorbachev.id.core.model.ComposeDataBank;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.SummarizedItemCost;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import gorbachev.id.core.DitailStatment;
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

public class HelloController implements Initializable {

    public DatePicker dateFrom;
    public Button btnLoadFile;
    public DatePicker dateTo;
    public ComboBox<String> bankBox;
    public BorderPane borderPaneDiagram;
    public ScrollPane scrollPane;
    public ComboBox<DitailStatment> ditalizationBox;
    public Button generate;

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
        ditalizationBox.setSelectionModel(new SingleSelectionModel<DitailStatment>() {
            List<DitailStatment> detail = Arrays.stream(DitailStatment.values()).toList();

            @Override
            protected DitailStatment getModelItem(int i) {
                return detail.get(i);
            }

            @Override
            protected int getItemCount() {
                return detail.size();
            }
        });


        generate.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                try {

                    ParamParser paramParser = new ParamParser(Path.of(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("report (3).html")).toURI()).toFile(),
                            dateFrom.getValue(), dateTo.getValue(), DitailStatment.HOURS);
                    if (Objects.isNull(resultParser)) {
                        resultParser = ManagerExpensesBank.parse(paramParser, new BelGosPromBankParser());
                    }
                    ComposeDataBank composeData = manager.recompose(paramParser, resultParser);
                    compseDeagram(composeData);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
//                log.info("Parser is complied");
            }
        });

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
                    lineChart.setPrefWidth(lineChart.getPrefWidth() + (deltaY > 0 ? 100 : -100));
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


    public static File getFromFileChooser(Scene scene) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Some Files");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("html", "*.html"));

        return fileChooser.showOpenDialog(scene.getWindow());
    }
}