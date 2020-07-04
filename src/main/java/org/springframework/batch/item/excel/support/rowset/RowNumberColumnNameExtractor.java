package org.springframework.batch.item.excel.support.rowset;

import org.springframework.batch.item.excel.Sheet;

public class RowNumberColumnNameExtractor implements ColumnNameExtractor {

	private int headerRowNumber;

	@Override
	public String[] getColumnNames(final Sheet sheet) {
		return sheet.getRow(headerRowNumber);
	}

	public void setHeaderRowNumber(int headerRowNumber) {
		this.headerRowNumber = headerRowNumber;
	}
}
