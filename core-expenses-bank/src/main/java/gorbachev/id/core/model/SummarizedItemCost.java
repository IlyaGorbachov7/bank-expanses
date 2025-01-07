package gorbachev.id.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class SummarizedItemCost {
    String uniqueCode;

    Integer parentKey;

    List<ItemRecordCost> expensesCostByDetail;

    Double sumExpensesCost;

}
