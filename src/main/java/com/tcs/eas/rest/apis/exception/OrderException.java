package com.tcs.eas.rest.apis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * @author 44745
 *
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND,reason="Order does not exist",value=HttpStatus.NOT_FOUND)
public class OrderException extends RuntimeException{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2608652732129687374L;

	/**
	 * 
	 * @param message
	 */
	public OrderException(String message) {
		super(message);
	}
}
