package gorbachev.id.core.bank.parsers;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.RecordCostStatement;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class BelGosPromBankParser implements BankParser {
    DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.ROOT);
    DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);

    @Override
    public ResultParser parse(ParamParser params) throws IOException {
        ResultParser result = new ResultParser();
        File html = params.getFileSource();
        Document doc = Jsoup.parse(html);
        defineTotalCost(doc, result);
        defineExpenses(doc, result);

        return result;
    }

    @Override
    public String[] supportedExtensions() {
        return new String[] {"*.html"};
    }

    private void defineExpenses(Document doc, ResultParser result) throws IOException {
        Element tableExpenses = doc.selectFirst("table.section_3");
        Element tableHeaders = tableExpenses.select("tr").first();
        if (Objects.isNull(tableHeaders)) {
            throw new IOException("Not founded headers of table expenses");
        }
        int indexDateOperation = -1;
        int indexTypeOperation = -1;
        int indexAmountCost = -1;
        int indexCurrency = -1;
        int inter = 0;
        for (Element columnHeader : tableHeaders.select("th")) {
            if (columnHeader.text().contains("Дата операции")) {
                indexDateOperation = inter;
            } else if (columnHeader.text().contains("Тип операции")) {
                indexTypeOperation = inter;
            } else if (columnHeader.text().contains("Сумма в валюте счета")) {
                indexAmountCost = inter;
            } else if (columnHeader.text().contains("Валюта счета")) {
                indexCurrency = inter;
            }
            inter++;
        }
        if (indexDateOperation < 0 || indexTypeOperation < 0 || indexAmountCost < 0 || indexCurrency < 0) {
            throw new IOException(String.format("Not founded: any columnHeader: dataOperation: %d, typeOperation: %d, amountCost: %d, currency: %d",
                    indexDateOperation, indexTypeOperation, indexAmountCost, indexCurrency));
        }

        Element rowStart = null;
        for (Element row : tableExpenses.select("tr")) {
            if (row.text().contains("ОПЕРАЦИИ С КАРТАМИ")) {
                rowStart = row;
                break;
            }
        }
        if (Objects.isNull(rowStart)) {
            throw new IOException("Not founded: ОПЕРАЦИИ С КАРТАМИ: row");
        }
        List<ItemRecordCost> expenses = result.getCost();
        /*Когда вы нашли строку rowStart, которая содержит текст "ОПЕРАЦИИ С КАРТАМИ", вызов nextElementSiblings() возвращает список всех строк (<tr>), которые следуют за rowStart в таблице tableExpenses.*/
        for (Element row : rowStart.nextElementSiblings().next()) {
            if (row.is("tr")) {
                Elements columns = row.select("td");
                if (inter == columns.size()) {

                    String dateOperation = row.getElementsByIndexEquals(indexDateOperation).text();
                    String typeOperation = row.getElementsByIndexEquals(indexTypeOperation).text();
                    String amountCost = row.getElementsByIndexEquals(indexAmountCost).text();
                    String currency = row.getElementsByIndexEquals(indexCurrency).text();

                    var item = createItemContent(dateOperation, typeOperation, amountCost, currency);
                    if (item.getOperation() == RecordCostStatement.COST_WRITE_DOWN) {
                        expenses.add(item);
                    }
                } else {
                    break;
                }

            }
        }
    }

    protected void defineTotalCost(Document doc, ResultParser result) throws IOException {
        Element table = findTableFirstOperation(doc.select("table.section_2"));
        Elements rows = table.select("tr");
        for (Element row : rows) {
            Element column = row.selectFirst("td.col1");
            if (Objects.nonNull(column) && column.text().contains("Итого по операциям")) {
                column = row.selectFirst("td.col4");
                if (Objects.isNull(column)) {
                    throw new IOException("Not founded: Итого по операциям: column4 = cost");
                }
                String totalCost = column.text();
                column = row.selectFirst("td.col5");
                if (Objects.isNull(column)) {
                    throw new IOException("Not founded: Итого по операциям: column5 = currency");
                }
                String currency = column.text();
                column = row.selectFirst("td.col2");
                if (Objects.isNull(column)) {
                    throw new IOException("Not founded: Итого по операциям: column2 = span date time");
                }
                String[] spanDateTime = column.text().split("-");
                if (spanDateTime.length != 2) {
                    throw new IOException("Not founded: Итого по операциям: column2 = Not defined span separator='-'");
                }
                LocalDateTime dateTimeFrom;
                LocalDateTime dateTimeTo;
                try {
                    dateTimeFrom = LocalDateTime.parse(spanDateTime[0].trim(), formatterDateTime);
                    dateTimeTo = LocalDateTime.parse(spanDateTime[1].trim(), formatterDateTime);
                } catch (RuntimeException ex) {
                    try {
                        dateTimeFrom = LocalDate.parse(spanDateTime[0].strip(), formatterDate).atStartOfDay();
                        dateTimeTo = LocalDate.parse(spanDateTime[1].strip(), formatterDate).atStartOfDay();
                    } catch (RuntimeException e) {
                        throw new IOException("Not founded: Итого по операциям: column2 = Not defined value of span date time");
                    }
                }

                Currency c;
                try {
                    c = Currency.getInstance(currency);
                } catch (RuntimeException e) {
                    c = Currency.getInstance(Locale.ROOT);
                    log.warn("Undefined currency code: {} Will be use default currency: {}", currency, c);
                }

                result.setTotalExpenses(Map.entry(toPriceDouble(totalCost), c));
                result.setTotalSpanDate(Map.entry(dateTimeFrom, dateTimeTo));
                break;
            }
        }
    }

    private ItemRecordCost createItemContent(String dateOperation, String typeOperation, String amountCost, String currency) throws IOException {
        ItemRecordCost result = new ItemRecordCost();
        // date operation
        try {
            result.setDateOperation(LocalDateTime.parse(dateOperation, formatterDateTime));
        } catch (RuntimeException e) {
            try {
                result.setDateOperation(LocalDate.parse(dateOperation, formatterDateTime).atStartOfDay());
            } catch (RuntimeException ex) {
                throw new IOException(String.format("Not matched date format: dateOperation: %s", dateOperation));
            }
        }
        // type operation
        if (typeOperation.equalsIgnoreCase("СПИСАНИЕ")) {
            result.setOperation(RecordCostStatement.COST_WRITE_DOWN);
        } else if (typeOperation.equalsIgnoreCase("ЗАЧИСЛЕНИЕ")) {
            result.setOperation(RecordCostStatement.COST_ADDED);
        } else {
            throw new IOException(String.format("Not defined value of typeOperation: %s", typeOperation));
        }
        // amountCost
        Currency c;
        try {
            c = Currency.getInstance(currency);
        } catch (RuntimeException e) {
            c = Currency.getInstance(Locale.ROOT);
            log.warn("Undefined currency code: {} Will be use default currency: {}", currency, c);
        }
        result.setCurrency(c);
        try {
            result.setAmount(toPriceDouble(amountCost));
        } catch (RuntimeException e) {
            throw new IOException(String.format("Not defined value of amount cost: %s. ParseException", amountCost));
        }


        return result;
    }

    protected Element findTableFirstOperation(Elements gussTable) throws IOException {
        for (Element table : gussTable) {
            Element row = table.selectFirst("tr");
            if (Objects.nonNull(row)) {
                Element column = row.selectFirst("th");
                if (Objects.nonNull(column) && column.text().contains("1. ОПЕРАЦИИ")) {
                    return table;
                }
            }
        }
        throw new IOException("Not founded: 1. ОПЕРАЦИИ");
    }

    double toPriceDouble(String price) throws NumberFormatException {
        return Double.parseDouble(price.replace(" ", "").replace(",", "."));
    }
}
