package gorbachev.id.core.bank;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.core.bank.parsers.BelGosPromBankParser;

import java.io.InputStream;
import java.util.stream.Stream;

public class BelGosPromBankInfo {/*implements ExpensesBankInfo {
    @Override
    public String getBankName() {
        return "Белгоспромбанк";
    }

    @Override
    public InputStream getBankIcon() {
        return ClassLoader.getSystemResourceAsStream("icons/belgospromjpg30.jpg");
    }

    @Override
    public Stream<DitailStatment> availableDitalization() {
        return Stream.of(DitailStatment.values());
    }

    @Override
    public BankParser parser() {
        return new BelGosPromBankParser();
    }*/
}
