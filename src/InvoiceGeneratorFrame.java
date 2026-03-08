import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InvoiceGeneratorFrame extends JFrame {

    private JTextField productNameField;
    private JTextField unitPriceField;
    private JTextField quantityField;

    private DefaultTableModel tableModel;
    private JTable invoiceTable;
    private JLabel totalLabel;

    private Invoice invoice;

    private static final String[] COLUMNS = {"Description", "Qty", "Unit Price", "Line Total"};

    public InvoiceGeneratorFrame() {
        invoice = new Invoice();
        initUI();
    }

    private void initUI() {
        setTitle("Invoice");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // ── Input panel (GridLayout: label | field pairs) ──
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inputPanel.add(new JLabel("Product Name:"));
        productNameField = new JTextField();
        inputPanel.add(productNameField);

        inputPanel.add(new JLabel("Unit Price ($):"));
        unitPriceField = new JTextField();
        inputPanel.add(unitPriceField);

        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);

        // ── Button row ──
        JButton addButton    = new JButton("Add Item");
        JButton removeButton = new JButton("Remove Selected");
        JButton clearButton  = new JButton("Clear All");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);

        // ── Top section: input + buttons ──
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ── Table ──
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(invoiceTable);

        // ── Total label ──
        totalLabel = new JLabel("Total Amount Due: $0.00");
        totalLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));

        // ── Assemble ──
        setLayout(new BorderLayout(5, 5));
        add(topPanel,   BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(totalLabel, BorderLayout.SOUTH);

        // ── Listeners ──
        addButton.addActionListener(e -> addLineItem());
        removeButton.addActionListener(e -> removeSelectedItem());
        clearButton.addActionListener(e -> clearAll());
        quantityField.addActionListener(e -> addLineItem());
    }

    private void addLineItem() {
        String name     = productNameField.getText().trim();
        String priceStr = unitPriceField.getText().trim();
        String qtyStr   = quantityField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        double unitPrice;
        int quantity;
        try {
            unitPrice = Double.parseDouble(priceStr);
            quantity  = Integer.parseInt(qtyStr);
            if (unitPrice < 0 || quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid price and a positive whole-number quantity.");
            return;
        }

        Product  product = new Product(name, unitPrice);
        LineItem item    = new LineItem(product, quantity);
        invoice.addLineItem(item);

        tableModel.addRow(new Object[]{
                name,
                quantity,
                String.format("$%.2f", unitPrice),
                String.format("$%.2f", item.getTotal())
        });

        updateTotal();
        productNameField.setText("");
        unitPriceField.setText("");
        quantityField.setText("");
        productNameField.requestFocus();
    }

    private void removeSelectedItem() {
        int row = invoiceTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row to remove."); return; }
        invoice.removeLineItem(row);
        tableModel.removeRow(row);
        updateTotal();
    }

    private void clearAll() {
        invoice.clearItems();
        tableModel.setRowCount(0);
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(String.format("Total Amount Due: $%.2f", invoice.getTotalAmountDue()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InvoiceGeneratorFrame().setVisible(true));
    }
}