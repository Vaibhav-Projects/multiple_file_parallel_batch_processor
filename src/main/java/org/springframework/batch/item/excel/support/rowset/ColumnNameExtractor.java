package org.springframework.batch.item.excel.support.rowset;

import org.springframework.batch.item.excel.Sheet;

public interface ColumnNameExtractor {

	String[] getColumnNames(Sheet sheet);

}
