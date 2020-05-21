package mainPackage;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jassu Sharma
 */
public class StockTableModel extends AbstractTableModel {

    private String[] columns;
    private Object[][] rows;
    
    public StockTableModel() {}
    
    public StockTableModel(Object[][] data, String[] columnNames) {
        this.rows = data;
        this.columns = columnNames;
    }
    
    public Class getColumnClass(int column) {
        if(column == 6)
            return Icon.class;
        else
            return getValueAt(0, column).getClass();
    }
    
    public int getRowCount() {
        return this.rows.length;
    }

    public int getColumnCount() {
        return this.columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.rows[rowIndex][columnIndex];
    }
    
    public String getColumn(int cols) {
        return this.columns[cols];
    }
    
    public String getColumnName(int i) {
        return columns[i];
    }
    public void emptyData() {
        int rows = getRowCount();
        if (rows == 0) {
            return;
        }
        fireTableRowsDeleted(0, rows - 1);
    }
}
