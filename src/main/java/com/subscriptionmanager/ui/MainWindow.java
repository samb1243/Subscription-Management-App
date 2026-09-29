package com.subscriptionmanager.ui;

import com.subscriptionmanager.model.BillingCycle;
import com.subscriptionmanager.model.Subscription;
import com.subscriptionmanager.model.SubscriptionManager;
import com.subscriptionmanager.storage.SubscriptionStore;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;

/** The main application window: an entry form, the subscription list and running totals. */
public class MainWindow extends JFrame {

    /** Suggested in the category dropdown alongside any categories already in use. */
    private static final List<String> SUGGESTED_CATEGORIES = List.of(
            "Streaming", "Music", "Gaming", "Software", "Cloud storage", "News", "Fitness", "Utilities", "Other");

    private final SubscriptionManager manager;
    private final SubscriptionStore store;

    private final SubscriptionTableModel tableModel = new SubscriptionTableModel();
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField(18);
    private final JTextField priceField = new JTextField(8);
    private final JComboBox<BillingCycle> cycleBox = new JComboBox<>(BillingCycle.values());
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JButton addButton = new JButton("Add");
    private final JButton updateButton = new JButton("Save changes");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear");

    private final JLabel monthlyTotalLabel = new JLabel();
    private final JLabel yearlyTotalLabel = new JLabel();
    private final JLabel countLabel = new JLabel();
    private final TitledBorder formBorder = BorderFactory.createTitledBorder("Add a subscription");

    /** Id of the subscription currently loaded into the form for editing, or null when adding. */
    private String editingId;

    public MainWindow(SubscriptionManager manager, SubscriptionStore store) {
        super("Subscription Manager");
        this.manager = manager;
        this.store = store;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildTotals(), BorderLayout.SOUTH);

        wireActions();
        cycleBox.setSelectedItem(BillingCycle.MONTHLY);
        refresh();
        clearForm();

        setMinimumSize(new Dimension(640, 420));
        setSize(820, 560);
        setLocationRelativeTo(null);
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(formBorder);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.gridx = 0;
        form.add(new JLabel("Name"), c);
        c.gridx = 1;
        form.add(new JLabel("Category"), c);
        c.gridx = 2;
        form.add(new JLabel("Price"), c);
        c.gridx = 3;
        form.add(new JLabel("Billed"), c);

        c.gridy = 1;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(nameField, c);
        c.gridx = 1;
        c.weightx = 0;
        form.add(categoryBox, c);
        c.gridx = 2;
        form.add(priceField, c);
        c.gridx = 3;
        form.add(cycleBox, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.add(clearButton);
        buttons.add(updateButton);
        buttons.add(addButton);
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        form.add(buttons, c);

        nameField.setToolTipText("e.g. Netflix");
        categoryBox.setEditable(true);
        categoryBox.setPrototypeDisplayValue("Cloud storage  ");
        // GridBagLayout drops to minimum sizes when space is short; keep the small fields usable.
        priceField.setMinimumSize(priceField.getPreferredSize());
        categoryBox.setMinimumSize(categoryBox.getPreferredSize());
        nameField.setMinimumSize(new Dimension(120, nameField.getPreferredSize().height));
        categoryBox.setToolTipText("Optional. Pick one or type your own, e.g. Streaming");
        priceField.setToolTipText("The amount charged each billing period, e.g. 9.99");
        return form;
    }

    private JComponent buildTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(Math.max(table.getRowHeight(), 24));
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof BigDecimal amount ? MoneyFormat.format(amount) : "");
            }
        };
        moneyRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(moneyRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);

        JScrollPane scroll = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(scroll, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.add(deleteButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildTotals() {
        JPanel totals = new JPanel();
        totals.setLayout(new BoxLayout(totals, BoxLayout.X_AXIS));
        totals.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, table.getGridColor()),
                BorderFactory.createEmptyBorder(10, 4, 0, 4)));

        JLabel monthlyCaption = new JLabel("Total per month: ");
        Font base = monthlyCaption.getFont();
        monthlyCaption.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 4f));
        monthlyTotalLabel.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 8f));

        totals.add(monthlyCaption);
        totals.add(monthlyTotalLabel);
        totals.add(Box.createHorizontalStrut(24));
        totals.add(yearlyTotalLabel);
        totals.add(Box.createHorizontalGlue());
        totals.add(countLabel);
        return totals;
    }

    private void wireActions() {
        addButton.addActionListener(e -> onAdd());
        updateButton.addActionListener(e -> onUpdate());
        deleteButton.addActionListener(e -> onDelete());
        clearButton.addActionListener(e -> {
            table.clearSelection();
            clearForm();
        });

        // Enter in any form field adds (or saves, when editing).
        nameField.addActionListener(e -> onSubmit());
        priceField.addActionListener(e -> onSubmit());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectionChanged();
            }
        });

        table.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSubscription");
        table.getActionMap().put("deleteSubscription", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDelete();
            }
        });
    }

    private void onSubmit() {
        if (editingId == null) {
            onAdd();
        } else {
            onUpdate();
        }
    }

    private void onAdd() {
        FormValues values = readForm();
        if (values == null) {
            return;
        }
        if (applyAndSave(() -> manager.add(Subscription.create(values.name, values.price, values.cycle, values.category)))) {
            table.clearSelection();
            clearForm();
        }
    }

    private void onUpdate() {
        if (editingId == null) {
            return;
        }
        FormValues values = readForm();
        if (values == null) {
            return;
        }
        String id = editingId;
        Subscription original = findById(id);
        if (applyAndSave(() -> manager.update(original.withDetails(values.name, values.price, values.cycle, values.category)))) {
            selectById(id);
        }
    }

    private void onDelete() {
        Subscription selected = selectedSubscription();
        if (selected == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.name() + "\"?",
                "Delete subscription",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.OK_OPTION && applyAndSave(() -> manager.remove(selected.id()))) {
            clearForm();
        }
    }

    /**
     * Applies a change, saves to disk and refreshes the view. If saving fails the
     * change is rolled back so what's on screen always matches what's on disk.
     */
    private boolean applyAndSave(Runnable change) {
        List<Subscription> snapshot = List.copyOf(manager.getAll());
        change.run();
        try {
            store.save(manager.getAll());
        } catch (IOException e) {
            manager.replaceAll(snapshot);
            showError("Could not save your subscriptions to " + store.getFile() + ":\n" + e.getMessage());
            return false;
        }
        refresh();
        return true;
    }

    private void onSelectionChanged() {
        Subscription selected = selectedSubscription();
        if (selected == null) {
            if (editingId != null) {
                clearForm();
            }
            return;
        }
        editingId = selected.id();
        nameField.setText(selected.name());
        priceField.setText(selected.price().toPlainString());
        cycleBox.setSelectedItem(selected.cycle());
        categoryBox.setSelectedItem(selected.category());
        updateFormState();
    }

    private Subscription selectedSubscription() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getRow(table.convertRowIndexToModel(viewRow));
    }

    private Subscription findById(String id) {
        return manager.getAll().stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private void selectById(String id) {
        int modelIndex = tableModel.indexOf(id);
        if (modelIndex >= 0) {
            int viewIndex = table.convertRowIndexToView(modelIndex);
            table.getSelectionModel().setSelectionInterval(viewIndex, viewIndex);
        }
    }

    private record FormValues(String name, BigDecimal price, BillingCycle cycle, String category) {
    }

    /** Validates the form, showing an error and returning null if anything is wrong. */
    private FormValues readForm() {
        String name = nameField.getText().strip();
        if (name.isEmpty()) {
            showError("Please enter a name for the subscription.");
            nameField.requestFocusInWindow();
            return null;
        }
        BigDecimal price;
        try {
            price = MoneyFormat.parse(priceField.getText());
        } catch (NumberFormatException e) {
            showError("\"" + priceField.getText().strip() + "\" is not a valid price. Enter an amount like 9.99.");
            priceField.requestFocusInWindow();
            priceField.selectAll();
            return null;
        }
        // Read the editor rather than the selection so text typed but not yet committed is used.
        String category = String.valueOf(categoryBox.getEditor().getItem()).strip();
        return new FormValues(name, price, (BillingCycle) cycleBox.getSelectedItem(), category);
    }

    private void clearForm() {
        editingId = null;
        nameField.setText("");
        priceField.setText("");
        cycleBox.setSelectedItem(BillingCycle.MONTHLY);
        categoryBox.setSelectedItem("");
        updateFormState();
        nameField.requestFocusInWindow();
    }

    private void updateFormState() {
        boolean editing = editingId != null;
        formBorder.setTitle(editing ? "Edit subscription" : "Add a subscription");
        nameField.getParent().repaint();
        addButton.setEnabled(!editing);
        updateButton.setEnabled(editing);
        deleteButton.setEnabled(editing);
        getRootPane().setDefaultButton(editing ? updateButton : addButton);
    }

    private void refresh() {
        tableModel.setRows(manager.getAll());
        refreshCategorySuggestions();
        monthlyTotalLabel.setText(MoneyFormat.format(manager.totalMonthly()));
        yearlyTotalLabel.setText("(" + MoneyFormat.format(manager.totalYearly()) + " per year)");
        int count = manager.getAll().size();
        countLabel.setText(count + (count == 1 ? " subscription" : " subscriptions"));
    }

    private void refreshCategorySuggestions() {
        Object typed = categoryBox.getEditor().getItem();
        TreeSet<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        categories.addAll(SUGGESTED_CATEGORIES);
        manager.getAll().stream()
                .map(Subscription::category)
                .filter(category -> !category.isEmpty())
                .forEach(categories::add);
        categoryBox.setModel(new DefaultComboBoxModel<>(categories.toArray(String[]::new)));
        categoryBox.setSelectedItem(typed);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Subscription Manager", JOptionPane.ERROR_MESSAGE);
    }
}
