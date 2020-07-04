package org.springframework.batch.item.excel;

import org.springframework.batch.item.ParseException;

public class ExcelFileParseException extends ParseException {
	private static final long serialVersionUID = 1L;
	private final String filename;
	private final String sheet;
	private final String[] row;
	private final int rowNumber;

	public ExcelFileParseException(final String message, final Throwable cause, final String filename,
			final String sheet, final int rowNumber, final String[] row) {
		super(message, cause);
		this.filename = filename;
		this.sheet = sheet;
		this.rowNumber = rowNumber;
		this.row = row;
	}

	public String getFilename() {
		return this.filename;
	}

	public String getSheet() {
		return this.sheet;
	}

	public int getRowNumber() {
		return this.rowNumber;
	}

	public String[] getRow() {
		return this.row;
	}

}
