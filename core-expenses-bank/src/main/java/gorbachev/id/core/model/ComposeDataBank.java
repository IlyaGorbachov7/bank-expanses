package gorbachev.id.core.model;

import gorbachev.id.core.DitailStatment;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
@ToString
public class ComposeDataBank {

    private DitailStatment detail;

    private Map<Integer, Map<Integer, Map<Integer, Map<Integer, SummarizedItemCost>>>> gropedList;

    private Map.Entry<Integer, SummarizedItemCost> maxItem;

    private Map.Entry<Integer, SummarizedItemCost> minItem;

    public double getSumExpenses() {
        var f = gropedList.values().stream().flatMap(
                map -> map.values().stream().flatMap(
                        map2 -> map2.values().stream().map(
                                map3 -> map3.values().stream().mapToDouble(SummarizedItemCost::getSumExpensesCost).sum()
                        )
                )
        );
        return f.mapToDouble(Double::valueOf).sum();
    }

}