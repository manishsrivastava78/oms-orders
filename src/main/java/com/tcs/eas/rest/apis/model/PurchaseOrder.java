package com.tcs.eas.rest.apis.model;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import io.swagger.annotations.ApiModel;

@ApiModel
@Entity(name="PurchaseOrder")
public class PurchaseOrder implements Serializable{

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 963858392013746375L;

	@Id
	@GenericGenerator(name = "order_id", strategy = "com.tcs.eas.rest.apis.utility.KeyGenerator")
	@GeneratedValue(generator = "order_id")
	private int orderid;
	
	@NotNull(message="Customer field is missing")
	private int customerid;
	
	@NotNull(message="Product field is missing")
	private int productid;
	
	
	private Date dop;
	
	
	public PurchaseOrder()
	{
	}
	
	public PurchaseOrder(@NotNull(message="Customer field is missing") int customerid,@NotNull(message="Product field is missing") int productid)
	{
		super();
		
		this.customerid = customerid;
		this.productid = productid;
	}

	public int getOrderid() {
		System.out.println("getter orderid="+orderid);
		return orderid;
	}

	public void setOrderid(int orderid) {
		this.orderid = orderid;
		System.out.println("setter orderid="+orderid);
	}

	public int getCustomerid() {
		return customerid;
	}

	public void setCustomerid(int customerid) {
		this.customerid = customerid;
	}

	public int getProductid() {
		return productid;
	}

	public void setProductid(int productid) {
		this.productid = productid;
	}
	
	/**
	 * @return the dop
	 */
	public Date getDop() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * @param dop the dop to set
	 */
	public void setDop(Date dop) {
		this.dop = dop;
	}
}
