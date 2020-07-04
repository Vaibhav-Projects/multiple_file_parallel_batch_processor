package org.springframework.batch.item.excel.support.rowset;

import java.util.Properties;

public interface RowSet {

	RowSetMetaData getMetaData();

	boolean next();

	int getCurrentRowIndex();

	String[] getCurrentRow();

	String getColumnValue(int idx);

	Properties getProperties();
}
