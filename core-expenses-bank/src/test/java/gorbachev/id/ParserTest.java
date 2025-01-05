package gorbachev.id;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ParserExpensesBank;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.bank.parsers.BelGosPromBankParser;
import gorbachev.id.core.bank.parsers.BelarusBankParser;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.parent.BootstrapParent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Unit test for simple App.
 */
public class ParserTest {
    static protected org.slf4j.Logger log;

    static ParamParser params;

    @BeforeAll
    public static void init() throws URISyntaxException {
        BootstrapParent.configure(new String[]{});
        log = org.slf4j.LoggerFactory.getLogger(ParserTest.class);
        Path path = Path.of(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("report.html")).toURI());
        File fireSource = path.toFile();
        LocalDate dateFrom = LocalDate.parse("01.11.2024", DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT));
        LocalDate dateTo = LocalDate.parse("01.01.2025", DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT));
        params = new ParamParser(fireSource, dateFrom, dateTo, DitailStatment.DAY);

    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void test1() throws IOException {
        BankParser parser = new BelGosPromBankParser();
        ResultParser res = parser.parse(params);
        for (ItemRecordCost cost : res.getCost()) {
            System.out.println(cost);
        }
        double sum = res.getCost().stream().map(ItemRecordCost::getAmount).mapToDouble(value -> value).sum();
        System.out.println(BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_EVEN));
        System.out.println(res.getTotalExpenses().getKey());
        System.out.println(sum - res.getTotalExpenses().getKey());
        System.out.println(res.getTotalSpanDate().getKey().equals(BigDecimal.valueOf(sum).setScale(2,RoundingMode.HALF_EVEN)));

    }


}
