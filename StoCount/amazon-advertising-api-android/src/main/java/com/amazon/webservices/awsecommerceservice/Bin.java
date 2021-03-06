// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.amazon.webservices.awsecommerceservice;

import com.leansoft.nano.annotation.RootElement;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import java.math.BigInteger;
import com.amazon.webservices.awsecommerceservice.bin.BinParameter;
import java.util.List;

@RootElement(name = "Bin", namespace = "http://webservices.amazon.com/AWSECommerceService/2011-08-01")
public class Bin implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "BinName")
	@Order(value=0)
	public String binName;	
	
	@Element(name = "BinItemCount")
	@Order(value=1)
	public BigInteger binItemCount;	
	
	@Element(name = "BinParameter")
	@Order(value=2)
	public List<BinParameter> binParameter;	
	
    
}