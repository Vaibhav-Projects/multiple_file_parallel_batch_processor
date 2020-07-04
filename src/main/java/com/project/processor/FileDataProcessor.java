package com.project.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.project.domain.Order;

public class FileDataProcessor implements ItemProcessor<Order, Order> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileDataProcessor.class);

	@Override
	public Order process(Order order) throws Exception {
		// currently no business processing required
		LOGGER.info("Processing data with id[{}]", order.getOrderCode());
		return order;
	}

}
