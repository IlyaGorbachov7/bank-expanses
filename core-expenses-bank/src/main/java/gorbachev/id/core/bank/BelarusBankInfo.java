package gorbachev.id.core.bank;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.core.bank.parsers.BelarusBankParser;

import java.io.InputStream;
import java.util.stream.Stream;

public class BelarusBankInfo{ /*implements ExpensesBankInfo {
    @Override
    public String getBankName() {
        return "Беларусь Банк";
    }

    @Override
    public InputStream getBankIcon() {
        return ClassLoader.getSystemResourceAsStream("icons/belarus30.jpg");
    }

    @Override
    public Stream<DitailStatment> availableDitalization() {
        return Stream.of(DitailStatment.MONTH);
    }

    @Override
    public BankParser parser() {
        return new BelarusBankParser();
    }*/
}
