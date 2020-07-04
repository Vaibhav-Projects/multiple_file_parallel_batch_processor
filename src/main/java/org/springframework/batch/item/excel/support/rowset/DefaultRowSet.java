package org.springframework.batch.item.excel.support.rowset;

import org.springframework.batch.item.excel.Sheet;

import java.util.Properties;

public class DefaultRowSet implements RowSet {

	private final Sheet sheet;
	private final RowSetMetaData metaData;

	private int currentRowIndex = -1;
	private String[] currentRow;

	DefaultRowSet(Sheet sheet, RowSetMetaData metaData) {
		this.sheet = sheet;
		this.metaData = metaData;
	}

	@Override
	public RowSetMetaData getMetaData() {
		return metaData;
	}

	@Override
	public boolean next() {
		currentRow = null;
		currentRowIndex++;
		if (currentRowIndex < sheet.getNumberOfRows()) {
			currentRow = sheet.getRow(currentRowIndex);
			return true;
		}
		return false;
	}

	@Override
	public int getCurrentRowIndex() {
		return this.currentRowIndex;
	}

	@Override
	public String[] getCurrentRow() {
		return this.currentRow;
	}

	@Override
	public String getColumnValue(int idx) {
		return currentRow[idx];
	}

	@Override
	public Properties getProperties() {
		final String[] names = metaData.getColumnNames();
		if (names == null) {
			throw new IllegalStateException("Cannot create properties without meta data");
		}

		Properties props = new Properties();
		for (int i = 0; i < currentRow.length; i++) {
			String value = currentRow[i];
			if (value != null) {
				props.setProperty(names[i], value);
			}
		}
		return props;
	}
}
