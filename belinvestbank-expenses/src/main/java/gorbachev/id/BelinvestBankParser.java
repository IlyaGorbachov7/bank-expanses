package gorbachev.id;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BelinvestBankParser implements BankParser {
	private static final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	@Override
	public ResultParser parse(ParamParser params) throws IOException {
		PDDocument doc = Loader.loadPDF(params.getFileSource());
		ResultParser result = new ResultParser();

		List<ItemRecordCost> itemsCost = new ArrayList<>();
		Map.Entry<Double, Currency> totalExpenses = null;
		Map.Entry<LocalDateTime, LocalDateTime> totalSpanDate = null;

		PDFTextStripper stripper = new PDFTextStripper();
		String text;

		// find currency this expenses list and totalExpanses
		String currencyAccount = "BYN"; // by default
		Double totalExpensesD = 0d;
		stripper.setStartPage(1);
		stripper.setEndPage(1);
		text = stripper.getText(doc);
		String findText = "Валюта счета:";
		int findIndex = text.indexOf(findText);
		if(findIndex != -1) {
			currencyAccount = text.substring(findIndex + findText.length()).stripLeading();;
			currencyAccount = currencyAccount.substring(0, currencyAccount.indexOf('\n')).strip();
		}
		System.err.println("Extract total spans");
		stripper.setStartPage(doc.getNumberOfPages());
		stripper.setEndPage(doc.getNumberOfPages());
		text = stripper.getText(doc);
		System.out.println(text);
		findText = "Расход:";
		findIndex = text.indexOf(findText);
		if(findIndex != -1) {
			String expenses = text.substring(findIndex+ findText.length()).stripLeading();
			expenses = expenses.substring(0, expenses.indexOf('\n')).strip();
			totalExpensesD = Double.parseDouble(expenses);
		}
		totalExpenses = new AbstractMap.SimpleEntry<>(totalExpensesD, Currency.getInstance(currencyAccount));

		// find date from and to
		stripper.setStartPage(1);
		stripper.setEndPage(1);
		text = stripper.getText(doc);
		System.out.println(text);
		Pattern pattern = Pattern.compile("с\\s+(\\d{2}\\.\\d{2}\\.\\d{4})\\s+по\\s+(\\d{2}\\.\\d{2}\\.\\d{4})");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			String startDate = matcher.group(1);  // 01.02.2026
			String endDate = matcher.group(2);    // 02.05.2026
			totalSpanDate = new AbstractMap.SimpleEntry<>(
					LocalDate.parse(startDate, formatDate).atStartOfDay(),
					LocalDate.parse(endDate, formatDate).atStartOfDay()
			);
		}

		result.setCost(itemsCost);
		result.setTotalExpenses(totalExpenses);
		result.setTotalSpanDate(totalSpanDate);
		doc.close();
		return null;
	}

	@Override
	public String[] supportedExtensions() {
		return new String[] {".pdf"};
	}
}
