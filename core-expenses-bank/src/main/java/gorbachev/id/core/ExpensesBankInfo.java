package gorbachev.id.core;

import java.io.InputStream;
import java.util.stream.Stream;

public interface ExpensesBankInfo {

    String getBankName();

    InputStream getBankIcon();

    Stream<DitailStatment> availableDitalization();

    BankParser parser();
}
