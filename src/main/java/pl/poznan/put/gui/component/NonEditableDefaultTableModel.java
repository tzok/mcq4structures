package pl.poznan.put.gui.component;

import javax.swing.table.DefaultTableModel;

public class NonEditableDefaultTableModel extends DefaultTableModel {
    public NonEditableDefaultTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
