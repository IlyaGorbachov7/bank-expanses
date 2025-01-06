package gorbachev.id.core.model;

import gorbachev.id.core.DitailStatment;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class ComposeDataBank {

    private DitailStatment detail;

    private Map<Integer, Map<Integer, Map<Integer, Map<Integer, SummarizedItemCost>>>> gropedList;

    private Map.Entry<Integer, SummarizedItemCost> maxItem;

    private Map.Entry<Integer, SummarizedItemCost> minItem;

}