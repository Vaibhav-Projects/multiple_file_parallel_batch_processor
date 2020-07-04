package org.springframework.batch.item.excel.support.rowset;

public interface RowSetMetaData {

	String[] getColumnNames();

	String getColumnName(int idx);

	int getColumnCount();

	String getSheetName();
}
