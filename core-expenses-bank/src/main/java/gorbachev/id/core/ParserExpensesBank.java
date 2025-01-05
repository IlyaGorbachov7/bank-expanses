package gorbachev.id.core;

import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;

import java.io.IOException;
import java.util.List;

public class ParserExpensesBank {
    public static ResultParser parse(ParamParser params, BankParser bankParser) {
        ResultParser res = new ResultParser();
        try {
            res.cost.addAll(bankParser.parse(params).getCost());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
}
