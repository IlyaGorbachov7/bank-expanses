package gorbachev.id;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ExpensesBankInfo;

import java.io.InputStream;
import java.util.stream.Stream;

public class BelinvestBankInfo implements ExpensesBankInfo {
	@Override
	public String getBankName() {
		return "Беливестбанк";
	}

	@Override
	public InputStream getBankIcon() {
		return getClass().getResourceAsStream("belinvestbank.png");
	}

	@Override
	public Stream<DitailStatment> availableDitalization() {
		return Stream.of(DitailStatment.values());
	}

	@Override
	public BankParser parser() {
		return new BelinvestBankParser();
	}
}
