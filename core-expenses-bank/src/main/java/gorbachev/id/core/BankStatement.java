package gorbachev.id.core;

public enum BankStatement {
    BAL_GASPROM_BANK("БелГазПромБанк");

    private String viewName;

    BankStatement(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public String toString() {
        return "BankStatement{" +
               "viewName='" + viewName + '\'' +
               '}';
    }
}
