package base.impl;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ExpensesBankInfo;

import java.io.InputStream;
import java.util.stream.Stream;

public class BelGosPromBankInfoImpl implements ExpensesBankInfo {
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
        return new BelGosPromBankParserImpl();
    }
}
