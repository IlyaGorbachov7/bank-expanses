package gorbachev.id.core;

import gorbachev.id.core.model.ComposeDataBank;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.SummarizedItemCost;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ManagerExpensesBank {

    public static int EMPTY_KEY_MAP = -1;

    public ManagerExpensesBank() {

    }

    public static ResultParser parse(ParamParser params, BankParser bankParser) throws IOException {
        return bankParser.parse(params);
    }

    public ComposeDataBank recompose(ParamParser params, ResultParser resultParser) {
        List<ItemRecordCost> list = resultParser.getCost().stream().filter(item -> {
            LocalDate dateOperation = item.getDateOperation().toLocalDate();
            return dateOperation.compareTo(params.getDateFrom()) >= 0 && dateOperation.compareTo(params.getDateTo()) <= 0;
        }).toList();
        var summarized = summingTotalCostByDetail(params.getDetail(), groupingBy(params.getDetail(), list));
        var maxAndMin = maxAndMinSummarizedItem(summarized);
        return new ComposeDataBank(params.getDetail(), summarized, maxAndMin.getKey(), maxAndMin.getValue());
    }


    public void callbackOnForEach(ComposeDataBank composeDataBank, Consumer callbackOnYear, Consumer callbackOnMonth, Consumer callbackOnDay, Consumer callbackOnHours) {
        Consumer[] callbacksReal = defineRealcallback(composeDataBank.getDetail(), callbackOnYear, callbackOnMonth, callbackOnDay, callbackOnHours);
        Consumer callbackOnYearReal = callbacksReal[0];
        Consumer callbackOnMonthReal = callbacksReal[1];
        Consumer callbackOnDayReal = callbacksReal[2];
        Consumer callbackOnHoursReal = callbacksReal[3];
        composeDataBank.getGropedList().forEach((yearKey, mapByYear) -> {
            if (Objects.nonNull(callbackOnYear) && yearKey != EMPTY_KEY_MAP) {
                callbackOnYearReal.accept(yearKey);
            }
            mapByYear.forEach((monthKey, mapByMonth) -> {
                if (Objects.nonNull(callbackOnMonthReal) && monthKey != EMPTY_KEY_MAP) {
                    callbackOnMonthReal.accept(monthKey);
                }
                mapByMonth.forEach((dayKey, mapByDay) -> {
                    if (Objects.nonNull(callbackOnDayReal) && dayKey != EMPTY_KEY_MAP) {
                        callbackOnDayReal.accept(dayKey);
                    }
                    mapByDay.forEach((hoursKey, summarizedItemCost) -> {
                        if (Objects.nonNull(callbackOnHoursReal) && hoursKey != EMPTY_KEY_MAP) {
                            callbackOnHoursReal.accept(summarizedItemCost);
                        }
                    });
                });
            });
        });
    }

    protected Consumer[] defineRealcallback(DitailStatment detail, Consumer callbackOnYear, Consumer callbackOnMonth, Consumer callbackOnDay, Consumer callbackOnHours) {
        Consumer[] callbackReal = new Consumer[4];
        if (detail == DitailStatment.YEAR) {
            callbackReal[3] = callbackOnYear;
        } else if (detail == DitailStatment.MONTH) {
            callbackReal[3] = callbackOnMonth;
            callbackReal[2] = callbackOnYear;
        } else if (detail == DitailStatment.DAY) {
            callbackReal[3] = callbackOnDay;
            callbackReal[2] = callbackOnMonth;
            callbackReal[1] = callbackOnYear;
        } else if (detail == DitailStatment.HOURS) {
            callbackReal[3] = callbackOnHours;
            callbackReal[2] = callbackOnDay;
            callbackReal[1] = callbackOnMonth;
            callbackReal[0] = callbackOnYear;
        }
        return callbackReal;
    }

    protected Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<ItemRecordCost>>>>> groupingBy(DitailStatment detail, List<ItemRecordCost> list) {
        switch (detail) {
            case YEAR -> {
                var map = list.stream().collect(Collectors.groupingBy(item -> item.getDateOperation().getYear()));
                return Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, map)));
            }
            case MONTH -> {
                var map = list.stream()
                        .collect(Collectors.groupingBy(item -> item.getDateOperation().getYear(),
                                Collectors.groupingBy(item -> item.getDateOperation().getMonthValue())));
                return Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, map));
            }
            case DAY -> {
                var map = list.stream()
                        .collect(Collectors.groupingBy(item -> item.getDateOperation().getYear(),
                                Collectors.groupingBy(item -> item.getDateOperation().getMonthValue(),
                                        Collectors.groupingBy(item -> item.getDateOperation().getDayOfMonth()))));
                return Map.of(EMPTY_KEY_MAP, map);
            }
            case HOURS -> {
                return list.stream()
                        .collect(Collectors.groupingBy(item -> item.getDateOperation().getYear(),
                                Collectors.groupingBy(item -> item.getDateOperation().getMonthValue(),
                                        Collectors.groupingBy(item -> item.getDateOperation().getDayOfMonth(),
                                                Collectors.groupingBy(item -> item.getDateOperation().getHour())))));
            }
        }
        throw new UnsupportedOperationException();
    }


    protected Map.Entry<Map.Entry<Integer, SummarizedItemCost>, Map.Entry<Integer, SummarizedItemCost>>
    maxAndMinSummarizedItem(Map<Integer, Map<Integer, Map<Integer, Map<Integer, SummarizedItemCost>>>> summarized) {
        AtomicReference<SummarizedItemCost> max = new AtomicReference<>();
        AtomicReference<Integer> maxKey = new AtomicReference<>();
        AtomicReference<SummarizedItemCost> min = new AtomicReference<>();
        AtomicReference<Integer> minKey = new AtomicReference<>();

        summarized.forEach((yearKey, mapByYear) -> {
            mapByYear.forEach((monthKey, mapByMonth) -> {
                mapByMonth.forEach((dayKey, mapByDay) -> {
                    mapByDay.forEach((hoursKey, summarizedItemCost) -> {
                        if (Objects.isNull(max.get()) && Objects.isNull(min.get())) {
                            max.set(summarizedItemCost);
                            min.set(summarizedItemCost);

                            maxKey.set(hoursKey);
                            minKey.set(hoursKey);
                        }
                        if (summarizedItemCost.getSumExpensesCost() > max.get().getSumExpensesCost()) {
                            max.set(summarizedItemCost);
                            maxKey.set(hoursKey);
                        }
                        if (summarizedItemCost.getSumExpensesCost() < min.get().getSumExpensesCost()) {
                            min.set(summarizedItemCost);
                            minKey.set(hoursKey);
                        }
                    });
                });
            });
        });
        return Map.entry(Map.entry(maxKey.get(), max.get()), Map.entry(minKey.get(), min.get()));
    }

    protected Map<Integer, Map<Integer, Map<Integer, Map<Integer, SummarizedItemCost>>>>
    summingTotalCostByDetail(DitailStatment detail,
                             Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<ItemRecordCost>>>>> list) {
        switch (detail) {
            case YEAR -> {
                var mapByYear = list.get(EMPTY_KEY_MAP).get(EMPTY_KEY_MAP).get(EMPTY_KEY_MAP);
                var mapSumByYear = mapByYear.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry_year_costList -> {
                    List<ItemRecordCost> listByYear = entry_year_costList.getValue();
                    LocalDateTime dataOperation = listByYear.get(0).getDateOperation();
                    String uniqueCode = buildUniqueCode(detail, dataOperation);
                    return createSummarizedItem(entry_year_costList.getKey(), listByYear, uniqueCode);
                }));
                return Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, mapSumByYear)));
            }
            case MONTH -> {
                var mapByYear = list.get(EMPTY_KEY_MAP).get(EMPTY_KEY_MAP);
                var mapSumByMouth = mapByYear.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry_year_month -> {
                    return entry_year_month.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry_month_costList -> {
                        List<ItemRecordCost> listByMonth = entry_month_costList.getValue();
                        LocalDateTime dataOperation = listByMonth.get(0).getDateOperation();
                        String uniqueCode = buildUniqueCode(detail, dataOperation);
                        return createSummarizedItem(entry_month_costList.getKey(), listByMonth, uniqueCode);
                    }));
                }));
                return Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, mapSumByMouth));

            }
            case DAY -> {
                var mapByYear = list.get(EMPTY_KEY_MAP);
                var mapSumByDay = mapByYear.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_year_month) -> {
                    return entry_year_month.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_month_day) -> {
                        return entry_month_day.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_day_costList) -> {
                            List<ItemRecordCost> listByDay = entry_day_costList.getValue();
                            LocalDateTime dataOperation = listByDay.get(0).getDateOperation();
                            String uniqueCode = buildUniqueCode(detail, dataOperation);
                            return createSummarizedItem(entry_day_costList.getKey(), listByDay, uniqueCode);
                        }));
                    }));
                }));
                return Map.of(EMPTY_KEY_MAP, mapSumByDay);
            }
            case HOURS -> {
                var mapSumByHours = list.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_year_month) -> {
                    return entry_year_month.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_month_day) -> {
                        return entry_month_day.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_day_hours) -> {
                            return entry_day_hours.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry_hours_costList) -> {
                                List<ItemRecordCost> listByHours = entry_hours_costList.getValue();
                                LocalDateTime dataOperation = listByHours.get(0).getDateOperation();
                                String uniqueCode = buildUniqueCode(detail, dataOperation);
                                return createSummarizedItem(entry_hours_costList.getKey(), listByHours, uniqueCode);
                            }));
                        }));
                    }));
                }));
                return mapSumByHours;
            }
        }
        throw new UnsupportedOperationException();
    }

    public String buildUniqueCode(DitailStatment detail, LocalDateTime dataOperation) {
        String uniqueCode = "";
        if (detail == DitailStatment.YEAR) {
            uniqueCode = String.format("%d", dataOperation.getYear());
        } else if (detail == DitailStatment.MONTH) {
            uniqueCode = String.format("%d-%d", dataOperation.getMonthValue(), dataOperation.getYear());
        } else if (detail == DitailStatment.DAY) {
            uniqueCode = String.format("%d-%d-%d", dataOperation.getDayOfMonth(), dataOperation.getMonthValue(), dataOperation.getYear());
        } else if (detail == DitailStatment.HOURS) {
            uniqueCode = String.format("%d-%d-%d-%d", dataOperation.getDayOfMonth(), dataOperation.getMonthValue(), dataOperation.getYear(), dataOperation.getHour());
        }
        return uniqueCode;
    }

    public String buildUniqueCode(DitailStatment detail, Integer... dateCode) {
        if(dateCode == null || dateCode.length == 0) {
            throw new IllegalArgumentException("dateCode == null || dateCode.length == 0: dateCode: " + dateCode);
        }
        String uniqueCode = "";
        if (detail == DitailStatment.YEAR) {
            uniqueCode = String.format("%d", dateCode[0]);
        } else if (detail == DitailStatment.MONTH) {
            uniqueCode = String.format("%d-%d", dateCode[0], dateCode[1]);
        } else if (detail == DitailStatment.DAY) {
            uniqueCode = String.format("%d-%d-%d", dateCode[0], dateCode[1], dateCode[2]);
        } else if (detail == DitailStatment.HOURS) {
            uniqueCode = String.format("%d-%d-%d-%d", dateCode[0], dateCode[1], dateCode[2], dateCode[3]);
        }
        return uniqueCode;
    }
    protected SummarizedItemCost createSummarizedItem(Integer parentKey, List<ItemRecordCost> list, String uniqueCode) {
        double sum = list.stream().mapToDouble(ItemRecordCost::getAmount).sum();
        return new SummarizedItemCost(uniqueCode, parentKey, list, sum);
    }

}
