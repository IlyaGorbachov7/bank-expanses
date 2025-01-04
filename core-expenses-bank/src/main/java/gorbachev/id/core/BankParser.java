package gorbachev.id.core;

import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.ResultParser;

public interface BankParser {

    ResultParser parse(ParamParser params);
}
