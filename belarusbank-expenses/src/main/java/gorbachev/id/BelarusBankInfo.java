package gorbachev.id;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ExpensesBankInfo;

import java.io.InputStream;
import java.util.stream.Stream;

public class BelarusBankInfo implements ExpensesBankInfo {
	@Override
	public String getBankName() {
		return "Беларусь банк";
	}

	@Override
	public InputStream getBankIcon() {
		/*
		 You need require use classLoader from getClass().getClassLoader()
		 because this jar file loaded via URLClassLoader which only his known about this jar file.
		 If you try load images from SystemClassLoader you receive NULL inputStream because SystemClassLoader nothing known about this jar file
		 */
		return this.getClass().getClassLoader().getResourceAsStream("belarus30.jpg");
	}

	@Override
	public Stream<DitailStatment> availableDitalization() {
		return Stream.of(DitailStatment.values());
	}

	@Override
	public BankParser parser() {
		return new BelarusBankParser();
	}
}
