package gorbachev.id.core;

import java.io.InputStream;

public interface ExpensesBankInfo {

    String getBankName();

    InputStream getBankIcon();

    BankParser parser();
}
