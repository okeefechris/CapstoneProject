// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.amazon.webservices.awsecommerceservice;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import com.amazon.webservices.awsecommerceservice.errors.Error;
import java.util.List;

@RootElement(name = "Errors", namespace = "http://webservices.amazon.com/AWSECommerceService/2011-08-01")
public class Errors implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "Error")
	@Order(value=0)
	public List<Error> error;	
	
    
}