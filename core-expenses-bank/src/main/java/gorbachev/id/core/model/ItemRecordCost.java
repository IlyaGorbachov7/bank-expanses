package gorbachev.id.core.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Currency;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ItemRecordCost {

    private double amount;

    private Currency currency;

    private RecordCostStatement operation;

    private LocalDateTime dateOperation;
}
