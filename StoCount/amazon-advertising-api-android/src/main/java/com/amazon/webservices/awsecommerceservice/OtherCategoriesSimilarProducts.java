// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.amazon.webservices.awsecommerceservice;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import com.amazon.webservices.awsecommerceservice.othercategoriessimilarproducts.OtherCategoriesSimilarProduct;
import java.util.List;

@RootElement(name = "OtherCategoriesSimilarProducts", namespace = "http://webservices.amazon.com/AWSECommerceService/2011-08-01")
public class OtherCategoriesSimilarProducts implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "OtherCategoriesSimilarProduct")
	@Order(value=0)
	public List<OtherCategoriesSimilarProduct> otherCategoriesSimilarProduct;	
	
    
}