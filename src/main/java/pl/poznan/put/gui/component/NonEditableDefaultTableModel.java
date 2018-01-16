package pl.poznan.put.gui.component;

import javax.swing.table.DefaultTableModel;

public class NonEditableDefaultTableModel extends DefaultTableModel {
    private static final long serialVersionUID = -33078784276254350L;

    public NonEditableDefaultTableModel(final Object[][] data,
                                        final Object[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public final boolean isCellEditable(final int i, final int i1) {
        return false;
    }
}
