package gorbachev.id;

import gorbachev.id.core.BankParser;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ParamParser;

import java.io.IOException;

public class BelinvestBankParser implements BankParser {
	@Override
	public ResultParser parse(ParamParser params) throws IOException {

		return null;
	}

	@Override
	public String[] supportedExtensions() {
		return new String[] {".pdf"};
	}
}
