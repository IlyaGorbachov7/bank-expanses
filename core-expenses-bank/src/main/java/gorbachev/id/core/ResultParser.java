package gorbachev.id.core;

import gorbachev.id.core.model.ItemRecordCost;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ResultParser {

    List<ItemRecordCost> cost = new ArrayList<>();

    Map.Entry<Double, Currency> totalExpenses;

    Map.Entry<LocalDateTime, LocalDateTime> totalSpanDate;
}
