package gorbachev.id.core.bank;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.ExpensesBankInfo;
import gorbachev.id.core.bank.parsers.BelGosPromBankParser;

import java.io.InputStream;

public class BelGosPromBankInfo implements ExpensesBankInfo {
    @Override
    public String getBankName() {
        return "Белгоспромбанк";
    }

    @Override
    public InputStream getBankIcon() {
        return ClassLoader.getSystemResourceAsStream("icons/belgospromjpg30.jpg");
    }

    @Override
    public BankParser parser() {
        return new BelGosPromBankParser();
    }
}
