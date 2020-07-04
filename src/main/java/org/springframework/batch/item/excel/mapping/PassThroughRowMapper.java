package org.springframework.batch.item.excel.mapping;

import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;

public class PassThroughRowMapper implements RowMapper<String[]> {

    @Override
    public String[] mapRow(final RowSet rs) throws Exception {
        return rs.getCurrentRow();
    }

}
