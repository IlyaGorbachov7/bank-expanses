package gorbachev.id.core;

import gorbachev.id.core.model.ParamParser;

import java.io.IOException;

public interface BankParser {

    ResultParser parse(ParamParser params) throws IOException;
}
