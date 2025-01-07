package gorbachev.id.core;

public enum DitailStatment {
    YEAR("Годом"),
    MONTH("Месяцам"),
    DAY("Дням"),
    HOURS("Часам");

    private String viewName;

    DitailStatment(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    @Override
    public String toString() {
        return viewName;
    }


}
