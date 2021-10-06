package com.tcs.eas.rest.apis.controller;

import java.net.URI;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.eas.rest.apis.db.OrderDaoService;
import com.tcs.eas.rest.apis.event.producer.Producer;
import com.tcs.eas.rest.apis.exception.InventoryNotFound;
import com.tcs.eas.rest.apis.exception.OrderNotFound;
import com.tcs.eas.rest.apis.log.LoggingService;
import com.tcs.eas.rest.apis.model.Customer;
import com.tcs.eas.rest.apis.model.Inventory;
import com.tcs.eas.rest.apis.model.MailData;
import com.tcs.eas.rest.apis.model.Product;
import com.tcs.eas.rest.apis.model.PurchaseOrder;
import com.tcs.eas.rest.apis.utility.Utility;

@RestController
@RequestMapping("/apis/v1/orders")
public class OrderController {
	
	@Autowired
	LoggingService loggingService;
	
	@Autowired
	OrderDaoService orderDaoService;
	

	@Autowired
	private Producer producer;
	
	@Value(value = "${CUSTOMER_SERVICE_HOST}")
	private String customerServiceHost;

	@Value(value = "${CUSTOMER_SERVICE_PORT}")
	private int customerServicePort;

	@Value(value = "${PRODUCT_SERVICE_HOST}")
	private String productServiceHost;

	@Value(value = "${PRODUCT_SERVICE_PORT}")
	private int productServicePort;
	
	@Value(value = "${KAFKA_MAIL_TOPIC}")
	private String mailTopic;
	
	@Value(value = "${INVENTORY_SERVICE_HOST}")
	private String inventoryServiceHost;
	
	@Value(value = "${INVENTORY_SERVICE_PORT}")
	private int inventoryServicePort;
	
	@GetMapping("/{orderid}")
	public ResponseEntity<PurchaseOrder> getOrderById(@PathVariable int orderid, @RequestHeader Map<String, String> headers){
		PurchaseOrder order = orderDaoService.getOrderById(orderid);
		if(order ==null)
		{
			throw new OrderNotFound("Orderid " + orderid+" does not exist");
		}
		loggingService.writeProcessLog("GET", "orders", "getOrder by id", order);
		return ResponseEntity.ok().headers(Utility.getCustomResponseHeaders(headers)).body(order);
		//return Order;
	}
	
	
	/***
	 * Get orders by customer id
	 */
	@GetMapping("/customers/{custid}")
	public ResponseEntity<List<PurchaseOrder>> getOrderByCustId(@PathVariable int custid, @RequestHeader Map<String, String> headers){
		List<PurchaseOrder> order = orderDaoService.findByCustomerId(custid);
		if(order ==null)
		{
			throw new OrderNotFound("No Orders Found");
		}
		loggingService.writeProcessLog("GET", "orders", "getOrder by Customerid", order);
		return ResponseEntity.ok().headers(Utility.getCustomResponseHeaders(headers)).body(order);
		//return Order;
	}
	
	/**
	 * 
	 * @param inventory
	 */
	
	private void updateInventory(int productId) {
		//Inventory inventory = new Inventory();
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:8080/apis/v1/inventories/products/"+ productId;
		restTemplate.put(url,Inventory.class);
	}
	
	/**
	 * 
	 * @param PurchaseOrder
	 * @param headers
	 * @return
	 */
	@PostMapping
	public ResponseEntity<Object> createOrder(@Valid @RequestBody PurchaseOrder order,
			@RequestHeader Map<String, String> headers) {
		System.out.println("creating order");
		
		if(isInventory(order.getProductid())){
			loggingService.writeProcessLog("POST", "Order", "createOrder", order);
			System.out.println("inventory updated");
			order.setDop(new Date(System.currentTimeMillis()));
			orderDaoService.save(order);
			// Send msg to Kafka
			sendMailMessage(order);
			sendShippingMessage(order);
		}else {
			throw new InventoryNotFound("Inventory is not available");
		}
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{orderid}")
				.buildAndExpand(order.getOrderid()).toUri();
		return ResponseEntity.created(location).headers(Utility.getCustomResponseHeaders(headers)).build();
	}
	
	


	@GetMapping
	public ResponseEntity<List<PurchaseOrder>> getOrder(@RequestHeader Map<String, String> headers) {
		List<PurchaseOrder> Order = orderDaoService.findAll();
		loggingService.writeProcessLog("GET", "Order", "getOrder", Order);
		return ResponseEntity.ok().headers(Utility.getCustomResponseHeaders(headers)).body(Order);
	}
	
	/**
	 * 
	 * @return
	 */
	private Customer getCustomer(int customerId) {
		Customer customer = null;
		RestTemplate restTemplate = new RestTemplate();
		String customerUrl = "http://" + customerServiceHost + ":" + customerServicePort + "/apis/v1/customers/"
				+ customerId;
		try {
			ResponseEntity<Customer> response = restTemplate.getForEntity(customerUrl, Customer.class);
			customer = response.getBody();
		} catch (Exception e) {
			loggingService.logError(e.getMessage());
		}
		
		/*
		 * customer = new Customer(); customer.setFirstname("Manish");
		 * customer.setLastname("Srivastava"); customer.setCity("GGN");
		 * customer.setCountry("INDIA"); customer.setCustomerid(123);
		 * customer.setEmail("m@paglait.com"); customer.setMobile("9818661431");
		 * customer.setPostcode("110096"); customer.setState("paglaitpur");
		 * customer.setStreet("view apartment");
		 */
		return customer;

	}

	/**
	 * 
	 * @return
	 */
	private Product getProduct(int productId) {
		Product product = null;
		RestTemplate restTemplate = new RestTemplate();
		String productUrl = "http://" + productServiceHost + ":" + productServicePort + "/apis/v1/products/"
				+ productId;
		try {
			ResponseEntity<Product> response = restTemplate.getForEntity(productUrl, Product.class);
			product = response.getBody();
		} catch (Exception e) {
			loggingService.logError(e.getMessage());
		}
		
		/*
		 * product = new Product(); product.setBatchnumber("123");
		 * product.setCategory("test"); product.setCurrency("INR");
		 * product.setManufactureddate(new Date(System.currentTimeMillis()));
		 * product.setManufacturer("tata"); product.setPrice(12.12);
		 * product.setProductdesc("testsa"); product.setProductid(123);
		 * product.setProductname("Product 1");
		 */
		return product;
	}
	
	/**
	 * 
	 * @param tracking
	 */
	private void sendMailMessage(PurchaseOrder order) {
		MailData data = new MailData();

		Customer customer = getCustomer(order.getCustomerid());
		data.setCustomer(customer);

		Product product = getProduct(order.getProductid());
		data.setCustomer(customer);

		data.setDop(order.getDop());

		data.setOrderId(order.getOrderid());

		data.setProduct(product);

		data.setMailTemplate(1);
		data.setStatus("Order Confirmed");
		data.setRemarks("Your order has been confirmed");
		
		
		producer.sendMessageToMailTopic(getMailDataInJson(data));
	}
	
	private void sendShippingMessage(@Valid PurchaseOrder order) {
		// TODO Auto-generated method stub
		producer.sendMessageToShippingTopic(getShipDataInJson(order));
		
		
	}
	
	/**
	 * 
	 * @param mailData
	 * @return
	 */
	private String getMailDataInJson(MailData mailData) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(mailData);
			System.out.println("ResultingJSONstring = " + json);
			// System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	/**
	 * 
	 * @param mailData
	 * @return
	 */
	private String getShipDataInJson(PurchaseOrder orderData) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(orderData);
			System.out.println("ResultingJSONstring = " + json);
			// System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	private boolean isInventory(int productId) {
		boolean found = false;
		RestTemplate restTemplate = new RestTemplate();
		//String url = "http://localhost:8080/apis/v1/inventories/products/"+ productId;
		String url = "http://" + inventoryServiceHost + ":" + inventoryServicePort + "/apis/v1/inventories/products/"
				+ productId;
		
		try {
			ResponseEntity<Inventory> response = restTemplate.getForEntity(url, Inventory.class);
			Inventory inventory = response.getBody();
			inventory.setAvailablequantity(inventory.getAvailablequantity()-1);
			updateInventory(inventory);
			//reserve quantity
			//inventory.setAvailablequantity(inventory.getAvailablequantity()-1);
			//url = "http://localhost:8080/apis/v1/inventories/"+inventory.getInventoryid();
			//restTemplate.put(url,inventory);
			if (inventory.getAvailablequantity() >= inventory.getMinquantity()) {
				found = true;
			}else {
				found = false;
				inventory.setAvailablequantity(inventory.getAvailablequantity()+1);
				updateInventory(inventory);
				//url = "http://localhost:8080/apis/v1/inventories/"+inventory.getInventoryid();
				//restTemplate.put(url,inventory);
			}
		} catch (Exception e) {
			loggingService.logError(e.getMessage());
		}
		return found;
	}
	/**
	 * 
	 * @param inventory
	 */
	private void updateInventory(Inventory inventory) {
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://" + inventoryServiceHost + ":" + inventoryServicePort + "/apis/v1/inventories/"+inventory.getInventoryid();
		restTemplate.put(url,inventory);
	}
	
}


