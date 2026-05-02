package gorbachev.id;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ParamParser;

import java.io.IOException;

public class BelarusBankParser implements BankParser {
	@Override
	public ResultParser parse(ParamParser params) throws IOException {
		return new ResultParser();
	}

	@Override
	public String[] supportedExtensions() {
		return new String[] {".pdf"};
	}
}
