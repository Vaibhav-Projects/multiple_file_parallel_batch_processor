package org.springframework.batch.item.excel;

public interface Sheet {

	int getNumberOfRows();

	String getName();

	String[] getRow(int rowNumber);

	int getNumberOfColumns();
}
