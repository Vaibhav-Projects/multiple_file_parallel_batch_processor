package org.springframework.batch.item.excel.support.rowset;

import org.springframework.batch.item.excel.Sheet;

public class StaticColumnNameExtractor implements ColumnNameExtractor {

	private final String[] columnNames;

	public StaticColumnNameExtractor(String[] columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public String[] getColumnNames(Sheet sheet) {
		return this.columnNames;
	}

}
