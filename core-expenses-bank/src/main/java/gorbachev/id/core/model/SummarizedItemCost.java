package gorbachev.id.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class SummarizedItemCost {
    List<ItemRecordCost> expensesCostByDetail;

    Double sumExpensesCost;

}
