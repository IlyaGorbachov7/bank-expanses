package gorbachev.id.core;

import gorbachev.id.core.model.ComposeDataBank;
import gorbachev.id.core.model.ItemRecordCost;
import gorbachev.id.core.model.ParamParser;
import gorbachev.id.core.model.SummarizedItemCost;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
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
                    return createSummarizedItem(listByYear);
                }));
                return Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, Map.of(EMPTY_KEY_MAP, mapSumByYear)));
            }
            case MONTH -> {
                var mapByYear = list.get(EMPTY_KEY_MAP).get(EMPTY_KEY_MAP);
                var mapSumByMouth = mapByYear.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry_year_month -> {
                    return entry_year_month.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry_month_costList -> {
                        List<ItemRecordCost> listByMonth = entry_month_costList.getValue();
                        return createSummarizedItem(listByMonth);
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
                            return createSummarizedItem(listByDay);
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
                                return createSummarizedItem(listByHours);
                            }));
                        }));
                    }));
                }));
            }
        }
        throw new UnsupportedOperationException();
    }

    protected SummarizedItemCost createSummarizedItem(List<ItemRecordCost> list) {
        double sum = list.stream().mapToDouble(ItemRecordCost::getAmount).sum();
        return new SummarizedItemCost(list, sum);
    }

}
