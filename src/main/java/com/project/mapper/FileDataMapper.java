package com.project.mapper;

import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;

import com.project.domain.Order;

import io.micrometer.core.instrument.util.StringUtils;

public class FileDataMapper implements RowMapper<Order> {

	@Override
	public Order mapRow(RowSet rs) throws Exception {
		Order order = new Order();
		if (StringUtils.isNotEmpty(rs.getColumnValue(0))) {
			order.setOrderCode(rs.getColumnValue(0));
		} else {
			order.setOrderCode(null);
		}
		if (StringUtils.isNotEmpty(rs.getColumnValue(1))) {
			order.setCustomerName(rs.getColumnValue(1));
		} else {
			order.setCustomerName(null);
		}
		if (StringUtils.isNotEmpty(rs.getColumnValue(2))) {
			order.setCustomerAddress(rs.getColumnValue(2));
		} else {
			order.setCustomerAddress(null);
		}
		if (StringUtils.isNotEmpty(rs.getColumnValue(3))) {
			order.setCutomerNumber(rs.getColumnValue(3));
		} else {
			order.setCutomerNumber(null);
		}
		if (StringUtils.isNotEmpty(rs.getColumnValue(4))) {
			order.setProductId(rs.getColumnValue(4));
		} else {
			order.setProductId(null);
		}
		if (StringUtils.isNotEmpty(rs.getColumnValue(5))) {
			order.setProductName(rs.getColumnValue(5));
		} else {
			order.setProductName(null);
		}
		if (StringUtils.isNotEmpty(rs.getColumnValue(6))) {
			order.setProductPrice(rs.getColumnValue(6));
		} else {
			order.setProductPrice(null);
		}
		return order;
	}

}
