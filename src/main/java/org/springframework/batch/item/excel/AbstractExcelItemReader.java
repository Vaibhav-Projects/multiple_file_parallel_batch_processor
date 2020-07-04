package org.springframework.batch.item.excel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.excel.support.rowset.DefaultRowSetFactory;
import org.springframework.batch.item.excel.support.rowset.RowSet;
import org.springframework.batch.item.excel.support.rowset.RowSetFactory;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public abstract class AbstractExcelItemReader<T> extends AbstractItemCountingItemStreamItemReader<T>
		implements ResourceAwareItemReaderItemStream<T>, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());
	private Resource resource;
	private int linesToSkip = 0;
	private int currentSheet = 0;
	private RowMapper<T> rowMapper;
	private RowCallbackHandler skippedRowsCallback;
	private boolean noInput = false;
	private boolean strict = true;
	private RowSetFactory rowSetFactory = new DefaultRowSetFactory();
	private RowSet rs;

	public AbstractExcelItemReader() {
		super();
		this.setName(ClassUtils.getShortName(this.getClass()));
	}

	@Override
	protected T doRead() throws Exception {
		if (this.noInput || this.rs == null) {
			return null;
		}

		if (rs.next()) {
			try {
				return this.rowMapper.mapRow(rs);
			} catch (final Exception e) {
				throw new ExcelFileParseException("Exception parsing Excel file.", e, this.resource.getDescription(),
						rs.getMetaData().getSheetName(), rs.getCurrentRowIndex(), rs.getCurrentRow());
			}
		} else {
			this.currentSheet++;
			if (this.currentSheet >= this.getNumberOfSheets()) {
				if (logger.isDebugEnabled()) {
					logger.debug("No more sheets in '" + this.resource.getDescription() + "'.");
				}
				return null;
			} else {
				this.openSheet();
				return this.doRead();
			}
		}
	}

	@Override
	protected void doOpen() throws Exception {
		Assert.notNull(this.resource, "Input resource must be set");
		this.noInput = true;
		if (!this.resource.exists()) {
			if (this.strict) {
				throw new IllegalStateException(
						"Input resource must exist (reader is in 'strict' mode): " + this.resource);
			}
			logger.warn("Input resource does not exist '" + this.resource.getDescription() + "'.");
			return;
		}

		if (!this.resource.isReadable()) {
			if (this.strict) {
				throw new IllegalStateException(
						"Input resource must be readable (reader is in 'strict' mode): " + this.resource);
			}
			logger.warn("Input resource is not readable '" + this.resource.getDescription() + "'.");
			return;
		}

		this.openExcelFile(this.resource);
		this.openSheet();
		this.noInput = false;
		if (logger.isDebugEnabled()) {
			logger.debug("Opened workbook [" + this.resource.getFilename() + "] with " + this.getNumberOfSheets()
					+ " sheets.");
		}
	}

	private void openSheet() {
		final Sheet sheet = this.getSheet(this.currentSheet);
		this.rs = rowSetFactory.create(sheet);

		if (logger.isDebugEnabled()) {
			logger.debug("Opening sheet " + sheet.getName() + ".");
		}

		for (int i = 0; i < this.linesToSkip; i++) {
			if (rs.next() && this.skippedRowsCallback != null) {
				this.skippedRowsCallback.handleRow(rs);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Openend sheet " + sheet.getName() + ", with " + sheet.getNumberOfRows() + " rows.");
		}

	}

	public void setResource(final Resource resource) {
		this.resource = resource;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.rowMapper, "RowMapper must be set");
	}

	public void setLinesToSkip(final int linesToSkip) {
		this.linesToSkip = linesToSkip;
	}

	protected abstract Sheet getSheet(int sheet);

	protected abstract int getNumberOfSheets();

	protected abstract void openExcelFile(Resource resource) throws Exception;

	public void setStrict(final boolean strict) {
		this.strict = strict;
	}

	public void setRowMapper(final RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
	}

	public void setRowSetFactory(RowSetFactory rowSetFactory) {
		this.rowSetFactory = rowSetFactory;
	}

	public void setSkippedRowsCallback(final RowCallbackHandler skippedRowsCallback) {
		this.skippedRowsCallback = skippedRowsCallback;
	}
}
