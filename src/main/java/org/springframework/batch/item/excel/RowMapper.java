package org.springframework.batch.item.excel;

import org.springframework.batch.item.excel.support.rowset.RowSet;

public interface RowMapper<T> {

	T mapRow(RowSet rs) throws Exception;

}
