package gorbachev.id;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.RecordCostStatement;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BelinvestBankParser implements BankParser {
	private static final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final String lockedText = "Заблокировано";
	private static final String appliedText = "Проведено";

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
		if (findIndex != -1) {
			currencyAccount = text.substring(findIndex + findText.length()).stripLeading();
			;
			currencyAccount = currencyAccount.substring(0, currencyAccount.indexOf('\n')).strip();
		}
		System.err.println("Extract total spans");
		stripper.setStartPage(doc.getNumberOfPages());
		stripper.setEndPage(doc.getNumberOfPages());
		text = stripper.getText(doc);
		findText = "Расход:";
		findIndex = text.indexOf(findText);
		if (findIndex != -1) {
			String expenses = text.substring(findIndex + findText.length()).stripLeading();
			expenses = expenses.substring(0, expenses.indexOf('\n')).strip();
			totalExpensesD = Double.parseDouble(expenses);
		}
		totalExpenses = new AbstractMap.SimpleEntry<>(totalExpensesD, Currency.getInstance(currencyAccount));

		// find date from and to
		stripper.setStartPage(1);
		stripper.setEndPage(1);
		text = stripper.getText(doc);
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

		ItemRecordCost item = new ItemRecordCost();
		for (int page = 1; page <= doc.getNumberOfPages(); page++) {
			stripper.setStartPage(page);
			stripper.setEndPage(page);
			text = stripper.getText(doc);
			System.out.println(text);
			if (page == 1) {
				findText = "Статус";
				findIndex = text.indexOf(findText);
				text = text.substring(findIndex + findText.length()).stripLeading();
				Deque<String> itemsCostStr = new ArrayDeque<>(Arrays.stream(text.split(String.format("(%s)|(%s)", lockedText, appliedText))).toList());
				itemsCostStr.removeLast(); // delete junk data
				itemsCostStr = itemsCostStr.stream().map(costStr -> costStr.replaceAll("[\r|\n]", "")).collect(Collectors.toCollection((Supplier<Deque<String>>) ArrayDeque::new));
				itemsCost.addAll(itemsCostStr.stream().map(this::buildCost).toList());
			}
			break;
		}


		result.setCost(itemsCost);
		result.setTotalExpenses(totalExpenses);
		result.setTotalSpanDate(totalSpanDate);
		doc.close();
		return null;
	}

	private ItemRecordCost buildCost(String str) {
		ItemRecordCost res = new ItemRecordCost();
		Pattern patternCost = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s*(\\d{2}:\\d{2}:\\d{2})\\s*(\\d{4}-\\d{2}-\\d{2})?\\s*(\\d{4})\\s*(\\d+)\\s*(\\D+)\\s(\\d+)\\s*(.+)\\s+([-|+]\\d+.?\\d*)\\s*([A-Z]{3})");
		/*This pattern matcher with
		* 2026-05-0202:13:41 1053 503021 Покупка 4121 MOBIL. PRIL. -YAN-DEXGO>MINSK BY -22.8 BYN 0.0/0 109.27
		* */
		Matcher matcher = patternCost.matcher(str);
		if(matcher.find()) {
			String dateTimeOperation = matcher.group(1) + " " + matcher.group(2);
			res.setDateOperation(LocalDateTime.parse(dateTimeOperation, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

			String operationName = matcher.group(6);
			res.setOperationTypeName(operationName);

			String operationMcc = matcher.group(7);
			res.setOperationMcc(operationMcc);

			String operationPlace = matcher.group(8);
			res.setOperationPlace(operationPlace);

			double expensesValue = Double.parseDouble(matcher.group(9));
			RecordCostStatement recordCostStatement;
			if(expensesValue < 0) {
				recordCostStatement = RecordCostStatement.COST_WRITE_DOWN;
			} else {
				recordCostStatement = RecordCostStatement.COST_ADDED;
			}
			res.setOperation(recordCostStatement);
			res.setAmount(expensesValue);

			res.setCurrency(Currency.getInstance(matcher.group(10)));
		}
		return res;
	}

	@Override
	public String[] supportedExtensions() {
		return new String[]{".pdf"};
	}
}
