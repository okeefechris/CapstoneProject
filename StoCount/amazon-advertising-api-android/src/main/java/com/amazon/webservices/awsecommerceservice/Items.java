// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.amazon.webservices.awsecommerceservice;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import java.math.BigInteger;
import java.util.List;

@RootElement(name = "Items", namespace = "http://webservices.amazon.com/AWSECommerceService/2011-08-01")
public class Items implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "Request")
	@Order(value=0)
	public Request request;	
	
	@Element(name = "CorrectedQuery")
	@Order(value=1)
	public CorrectedQuery correctedQuery;	
	
	@Element(name = "Qid")
	@Order(value=2)
	public String qid;	
	
	@Element(name = "EngineQuery")
	@Order(value=3)
	public String engineQuery;	
	
	@Element(name = "TotalResults")
	@Order(value=4)
	public BigInteger totalResults;	
	
	@Element(name = "TotalPages")
	@Order(value=5)
	public BigInteger totalPages;	
	
	@Element(name = "MoreSearchResultsUrl")
	@Order(value=6)
	public String moreSearchResultsUrl;	
	
	@Element(name = "SearchResultsMap")
	@Order(value=7)
	public SearchResultsMap searchResultsMap;	
	
	@Element(name = "Item")
	@Order(value=8)
	public List<Item> item;	
	
	@Element(name = "SearchBinSets")
	@Order(value=9)
	public SearchBinSets searchBinSets;	
	
    
}