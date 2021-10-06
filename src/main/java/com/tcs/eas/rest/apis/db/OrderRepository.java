package com.tcs.eas.rest.apis.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.eas.rest.apis.model.PurchaseOrder;


/**
 * 
 * @author 44745
 *
 */
public interface OrderRepository extends JpaRepository<PurchaseOrder,Integer>{
	/**
	 * 
	 * @param orderid
	 * @return
	 */
	
	public List<PurchaseOrder> findByCustomerid(int customerid);
}
