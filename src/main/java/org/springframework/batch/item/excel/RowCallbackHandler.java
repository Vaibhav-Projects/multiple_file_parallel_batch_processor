package org.springframework.batch.item.excel;

import org.springframework.batch.item.excel.support.rowset.RowSet;

public interface RowCallbackHandler {

    void handleRow(RowSet rs);

}
