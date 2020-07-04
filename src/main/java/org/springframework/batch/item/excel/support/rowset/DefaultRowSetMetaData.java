package org.springframework.batch.item.excel.support.rowset;

import org.springframework.batch.item.excel.Sheet;

public class DefaultRowSetMetaData implements RowSetMetaData {

	private final Sheet sheet;

	private ColumnNameExtractor columnNameExtractor;

	DefaultRowSetMetaData(Sheet sheet) {
		this.sheet = sheet;
	}

	@Override
	public String[] getColumnNames() {
		return columnNameExtractor.getColumnNames(sheet);
	}

	@Override
	public String getColumnName(int idx) {
		String[] names = getColumnNames();
		return names[idx];
	}

	@Override
	public int getColumnCount() {
		return sheet.getNumberOfColumns();
	}

	@Override
	public String getSheetName() {
		return sheet.getName();
	}

	public void setColumnNameExtractor(ColumnNameExtractor columnNameExtractor) {
		this.columnNameExtractor = columnNameExtractor;
	}
}
