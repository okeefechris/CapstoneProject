package com.hengtan.nanodegreeapp.stocount;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;
import walmart.webapi.android.ItemList;
import walmart.webapi.android.Items;
import walmart.webapi.android.WalmartApi;
import walmart.webapi.android.WalmartService;

/**
 * Created by htan on 06/11/2015.
 */
public class SearchByNameSuggestionProvider  extends ContentProvider {
    private static final String tag = "SuggestUrlProvider";
    public static String AUTHORITY = "com.hengtan.nanodegreeapp.stocount.SearchByNameSuggestionProvider";

    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    List<SearchSuggestion> searchResult = null;

    private static final String[] COLUMNS = {
            "_id",  // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID
    };

    private static UriMatcher buildUriMatcher()
    {
        UriMatcher matcher =
                new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY,
                SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY +
                        "/*",
                SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_SHORTCUT,
                SHORTCUT_REFRESH);
        matcher.addURI(AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_SHORTCUT +
                        "/*",
                SHORTCUT_REFRESH);
        return matcher;
    }

    @Override
    public boolean onCreate() {

        //lets not do anything in particular
        Log.d(tag, "onCreate called");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder)
    {
        Log.d(tag,"query called with uri:" + uri);
        Log.d(tag,"selection:" + selection);

        String query = selectionArgs[0];
        Log.d(tag,"query:" + query);

        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                Log.d(tag,"search suggest called");
                return getSuggestions(query);
            case SHORTCUT_REFRESH:
                Log.d(tag,"shortcut refresh called");
                return null;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    private Cursor getSuggestions(String query)
    {
        //List<StockCountItemSearchSuggestion> result;
        List<String> result = new ArrayList<String>();

        if (query == null) return null;
        else if(query.length() <= 2) {
            return null;
        }
        else
        {
            //result = getSuggestedItemNameFromDB(query);
            getSuggestedItemNameFromAPI(query);
            //result.add("Orange juice");
            //result.add("Apple juice");
            //result.add("Mango juice");
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);

        if(result != null) {
            for(SearchSuggestion s : searchResult) {
                cursor.addRow(createRow(s));
            }
        }


        return cursor;
    }
    private Object[] createRow(SearchSuggestion suggestion)
    {
        return columnValuesOfQuery(suggestion.id, //suggestion.SiteItemId,
                "android.intent.action.VIEW",
                "http://com.hengtan.nanodegreeapp.stocount.SearchByNameSuggestionProvider/" +suggestion.id, //suggestion.SiteItemId,
                suggestion.name,
                suggestion.category);
    }

    private Object[] columnValuesOfQuery(int siteItemId,
                                         String intentAction,
                                         String url,
                                         String text1,
                                         String text2)
    {
        String[] colValues = new String[] {
                Integer.toString(siteItemId),     // _id
                text1,     // text1
                text2,     // text2
                url,
                // intent_data (included when clicking on item)
                intentAction, //action
                SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT
        };

        return colValues;
    }

    private Cursor refreshShortcut(String shortcutId,
                                   String[] projection) {
        return null;
    }

    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SHORTCUT_REFRESH:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw
                        new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public int delete(Uri uri, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public int update(Uri uri, ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private void getSuggestedItemNameFromAPI(String query)
    {
        WalmartApi testApi = new WalmartApi();

        WalmartService testService = testApi.getService();

        Map<String, Object> params = new HashMap<String, Object>();

        //Resources res = getResources();

        //params.put("apiKey",res.getString(R.string.apiKey));
        params.put("apiKey","");
        params.put("format", "json");
        params.put("query", query);

        testService.searchProduct(params, new retrofit.Callback<ItemList>() {
            @Override
            public void success(final ItemList result, Response response) {

                if (result != null && result.items != null && result.items.size() > 0) {

                    searchResult = new ArrayList<SearchSuggestion>();

                    for (Items s : result.items) {

                        SearchSuggestion ss = new SearchSuggestion();
                        ss.id = s.itemId;
                        ss.name = s.name;
                        ss.category = s.categoryPath;

                        searchResult.add(ss);
                    }

                } else {
                    //Toast.makeText(LoginActivity.this, "Product not found for name: ", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void failure(final RetrofitError error) {

                String msg = error.getMessage();

            }
        });

    }
}

class SearchSuggestion
{
    String name;
    String category;
    int id;
}
