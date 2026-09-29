package com.subscriptionmanager.ui;

import com.subscriptionmanager.model.BillingCycle;
import com.subscriptionmanager.model.Subscription;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Table model showing each subscription and its monthly equivalent cost. */
class SubscriptionTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Subscription", "Price", "Billed", "Per month"};

    private List<Subscription> rows = new ArrayList<>();

    void setRows(List<Subscription> subscriptions) {
        rows = new ArrayList<>(subscriptions);
        fireTableDataChanged();
    }

    Subscription getRow(int modelIndex) {
        return rows.get(modelIndex);
    }

    int indexOf(String id) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 1, 3 -> BigDecimal.class;
            case 2 -> BillingCycle.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int row, int column) {
        Subscription s = rows.get(row);
        return switch (column) {
            case 0 -> s.name();
            case 1 -> s.price();
            case 2 -> s.cycle();
            case 3 -> s.monthlyCost();
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
