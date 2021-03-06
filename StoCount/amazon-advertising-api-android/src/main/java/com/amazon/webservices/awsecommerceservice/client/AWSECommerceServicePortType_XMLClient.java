// Generated by wsdl compiler for android/java
// DO NOT CHANGE!
package com.amazon.webservices.awsecommerceservice.client;


import com.leansoft.nano.ws.XMLServiceCallback;
import com.leansoft.nano.ws.NanoXMLClient;
import com.amazon.webservices.awsecommerceservice.CartClear;
import com.amazon.webservices.awsecommerceservice.CartModify;
import com.amazon.webservices.awsecommerceservice.SimilarityLookup;
import com.amazon.webservices.awsecommerceservice.SimilarityLookupResponse;
import com.amazon.webservices.awsecommerceservice.ItemSearch;
import com.amazon.webservices.awsecommerceservice.ItemSearchResponse;
import com.amazon.webservices.awsecommerceservice.CartClearResponse;
import com.amazon.webservices.awsecommerceservice.CartGet;
import com.amazon.webservices.awsecommerceservice.ItemLookup;
import com.amazon.webservices.awsecommerceservice.BrowseNodeLookup;
import com.amazon.webservices.awsecommerceservice.ItemLookupResponse;
import com.amazon.webservices.awsecommerceservice.CartAdd;
import com.amazon.webservices.awsecommerceservice.CartAddResponse;
import com.amazon.webservices.awsecommerceservice.CartGetResponse;
import com.amazon.webservices.awsecommerceservice.CartCreate;
import com.amazon.webservices.awsecommerceservice.BrowseNodeLookupResponse;
import com.amazon.webservices.awsecommerceservice.CartCreateResponse;
import com.amazon.webservices.awsecommerceservice.CartModifyResponse;


/**
 This class is the XML client to the AWSECommerceServicePortType Web Service.
*/ 
public class AWSECommerceServicePortType_XMLClient extends NanoXMLClient {


    /**
     public method
    */
    public void itemSearch(ItemSearch requestObject, XMLServiceCallback<ItemSearchResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, ItemSearchResponse.class);
    }

    /**
     public method
    */
    public void itemLookup(ItemLookup requestObject, XMLServiceCallback<ItemLookupResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, ItemLookupResponse.class);
    }

    /**
     public method
    */
    public void browseNodeLookup(BrowseNodeLookup requestObject, XMLServiceCallback<BrowseNodeLookupResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, BrowseNodeLookupResponse.class);
    }

    /**
     public method
    */
    public void similarityLookup(SimilarityLookup requestObject, XMLServiceCallback<SimilarityLookupResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, SimilarityLookupResponse.class);
    }

    /**
     public method
    */
    public void cartGet(CartGet requestObject, XMLServiceCallback<CartGetResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, CartGetResponse.class);
    }

    /**
     public method
    */
    public void cartCreate(CartCreate requestObject, XMLServiceCallback<CartCreateResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, CartCreateResponse.class);
    }

    /**
     public method
    */
    public void cartAdd(CartAdd requestObject, XMLServiceCallback<CartAddResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, CartAddResponse.class);
    }

    /**
     public method
    */
    public void cartModify(CartModify requestObject, XMLServiceCallback<CartModifyResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, CartModifyResponse.class);
    }

    /**
     public method
    */
    public void cartClear(CartClear requestObject, XMLServiceCallback<CartClearResponse> serviceCallback) {
       
        
        super.invoke(requestObject, serviceCallback, CartClearResponse.class);
    }


}