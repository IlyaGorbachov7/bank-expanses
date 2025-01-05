package gorbachev.id.core.model;

import gorbachev.id.core.DitailStatment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ParamParser {
    @NonNull
    private File fileSource;

    @NonNull
    private LocalDate dateFrom;

    @NonNull
    private LocalDate dateTo;

    @NonNull
    private DitailStatment detail;
}
