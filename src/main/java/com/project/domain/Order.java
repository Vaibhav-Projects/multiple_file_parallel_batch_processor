package com.project.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Order {

	private String orderCode;
	private String customerName;
	private String customerAddress;
	private String cutomerNumber;
	private String productId;
	private String productName;
	private String productPrice;

}
