package com.hengtan.nanodegreeapp.stocount.api;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.hengtan.nanodegreeapp.stocount.Application;
import com.hengtan.nanodegreeapp.stocount.DetailActivity;
import com.hengtan.nanodegreeapp.stocount.data.Product;
import com.hengtan.nanodegreeapp.stocount.R;
import com.hengtan.nanodegreeapp.stocount.search.SearchSuggestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;
import walmart.webapi.android.WalmartApi;
import walmart.webapi.android.WalmartItemList;
import walmart.webapi.android.WalmartItems;
import walmart.webapi.android.WalmartService;

/**
 * Created by htan on 30/11/2015.
 */
public class WalmartApiCall extends BaseApiCall implements ApiCall {

    public List<SearchSuggestion> GetSuggestedItemName(String query, Context ctx)
    {
        Application.SetAPISearchInProgress(true);

        WalmartApi testApi = new WalmartApi();

        WalmartService testService = testApi.getService();

        Map<String, Object> params = new HashMap<String, Object>();

        Resources res = ctx.getResources();

        params.put("apiKey",res.getString(R.string.walmart_apiKey));
        params.put("format", "json");
        params.put("query", query);

        try {
            WalmartItemList result = testService.searchProduct(params);

            if (result != null && result.items != null && result.items.size() > 0) {

                List<SearchSuggestion> searchResult = new ArrayList<SearchSuggestion>();

                for (WalmartItems s : result.items) {

                    SearchSuggestion ss = new SearchSuggestion();
                    ss.id = s.itemId;
                    ss.name = s.name;
                    ss.additionalInfo = s.categoryPath;

                    searchResult.add(ss);
                }
                Application.SetAPISearchInProgress(false);
                return searchResult;

            } else {
                //Toast.makeText(LoginActivity.this, "Product not found for name: ", Toast.LENGTH_SHORT).show();
            }
            Application.SetAPISearchInProgress(false);
        }
        catch(Exception ex)
        {
            Application.SetAPISearchInProgress(false);
            String err = ex.getMessage();
        }

        return null;
    }

    public void SearchProduct(String barcodeScanResult, String barcodeFormatName, String itemId, final Context ctx)
    {
        Resources res = ctx.getResources();
        final String noBarcodeMatch = res.getString(R.string.api_search_no_product_found_barcode);


        WalmartApi testApi = new WalmartApi();

        WalmartService testService = testApi.getService();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("apiKey", res.getString(R.string.walmart_apiKey));

        if(barcodeScanResult != null && !barcodeScanResult.isEmpty()) {
            params.put("upc", barcodeScanResult);
        }
        else if(itemId != null && !itemId.isEmpty())
        {
            params.put("itemId", itemId);
        }

        final String searchCriteria = (barcodeScanResult != null && !barcodeScanResult.isEmpty()) ? barcodeScanResult : itemId;

        testService.getProduct(params, new retrofit.Callback<WalmartItemList>() {
            @Override
            public void success(final WalmartItemList result, Response response) {

                if (result != null && result.items != null && result.items.size() > 0) {

                    Product prod = new Product(result.items.get(0));

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DetailActivity.PRODUCT_PARCELABLE, prod);
                    Intent intent = new Intent(ctx, DetailActivity.class);
                    intent.putExtra(DetailActivity.PRODUCT_PARCELABLE, bundle);
                    ctx.startActivity(intent);

                } else {
                    DisplayToast(ctx, noBarcodeMatch + searchCriteria);
                }
            }

            @Override
            public void failure(final RetrofitError error) {

                String msg = error.getMessage();

                if(msg.equals("404 Not Found"))
                {
                    msg = noBarcodeMatch + searchCriteria;
                }
                DisplayToast(ctx, msg);
            }
        });
    }
}
