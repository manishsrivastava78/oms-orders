package com.tcs.eas.rest.apis.db;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tcs.eas.rest.apis.log.LoggingService;
import com.tcs.eas.rest.apis.model.PurchaseOrder;

@Component
public class OrderDaoService {

	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	LoggingService loggingService;
	
	public PurchaseOrder save(PurchaseOrder order) {
		return orderRepository.save(order);
	}
	
	
	public PurchaseOrder getOrderById(Integer id) {
		
		Optional<PurchaseOrder> order = orderRepository.findById(id);
		if(order.isPresent())
			return order.get();
		else 
			return null;
	}
	
	public List<PurchaseOrder> findByCustomerId(int customerid){
		return orderRepository.findByCustomerid(customerid);
		
	}


	public List<PurchaseOrder> findAll() {
		// TODO Auto-generated method stub
	 return orderRepository.findAll();
	}
	
}
