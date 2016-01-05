package com.hengtan.nanodegreeapp.stocount;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hengtan.nanodegreeapp.stocount.api.ApiCall;
import com.hengtan.nanodegreeapp.stocount.data.DBAsyncTask;
import com.hengtan.nanodegreeapp.stocount.data.Product;
import com.hengtan.nanodegreeapp.stocount.data.ProductCount;
import com.hengtan.nanodegreeapp.stocount.data.StoCountContract;
import com.hengtan.nanodegreeapp.stocount.data.StockPeriod;
import com.hudomju.swipe.OnItemClickListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import android.app.LoaderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductListActivity extends AppCompatActivity implements SearchView.OnSuggestionListener, LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = ProductListActivity.class.getSimpleName();

    @InjectView(R.id.famProductListButton)
    protected FloatingActionsMenu famProductListButton;

    @InjectView(R.id.fabManualButton)
    protected FloatingActionButton fabManualButton;

    @InjectView(R.id.fabSearchButton)
    protected FloatingActionButton fabSearchButton;

    @InjectView(R.id.fabBarcodeButton)
    protected FloatingActionButton fabBarcodeButton;

    @InjectView(R.id.stockPeriodSpinner)
    protected Spinner mStockPeriodSpinner;

    @InjectView(R.id.recycler_view)
    protected RecyclerView mRecyclerView;


    private SearchView searchView;

    public static final int RESULT_SETTINGS = 1;

    private ProductListAdapter adapter;

    private StockPeriodSpinnerAdapter spinnerAdapter;

    // Identifies a particular Loader being used in this component
    private static final int PRODUCT_LOADER = 0;

    private static final int PREVIOUS_STOCK_LOADER = 1;

    private GoogleApiClient mGoogleApiClient;

    private String mApiCode;

    private ApiCall mApiCall;

    private StockPeriod mCurrentStockPeriod;

    private StockPeriod mSelectedStockPeriod;

    private MenuItem mSearchItem;

    private static final int EMPTY_VIEW = 10;

    private String mErrorBarcodeStr;
    private String mCancelledBarcodeStr;
    private String mScannedBarcodeStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_list_activity);
        ButterKnife.inject(this);
        mApiCode = Application.GetApiCodeFromPreference();
        mApiCall = Application.GetApiCallFromPreference(mApiCode);
        mCurrentStockPeriod = Application.getCurrentStockPeriod();

        init();

        hideShowFab();

        getLoaderManager().restartLoader(PREVIOUS_STOCK_LOADER, null, this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Resources res = getResources();

        mErrorBarcodeStr = res.getString(R.string.error_barcode_text);
        mCancelledBarcodeStr = res.getString(R.string.cancelled_barcode_text);
        mScannedBarcodeStr = res.getString(R.string.scanned_barcode_log_text);
    }

    @OnClick(R.id.fabManualButton)
    public void onManualClick(View v) {
        Product prod = new Product(getResources());
        Bundle bundle = new Bundle();
        bundle.putParcelable(DetailActivity.PRODUCT_PARCELABLE, prod);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.PRODUCT_PARCELABLE, bundle);
        this.startActivity(intent);
    }

    @OnClick(R.id.fabSearchButton)
    public void onSearchClick(View v) {
        famProductListButton.collapse();

        View actionSearch = findViewById(R.id.action_search);

        if(actionSearch instanceof ActionMenuItemView)
        {
            ((ActionMenuItemView)actionSearch).callOnClick();
        }
    }

    @OnClick(R.id.fabBarcodeButton)
    public void onBarcodeClick(View v) {
        famProductListButton.collapse();
        try {
            IntentIntegrator intentIntegrator = new IntentIntegrator(ProductListActivity.this);
            //intentIntegrator.setOrientationLocked(false);
            intentIntegrator.initiateScan();
        } catch (Exception ex) {
            Log.e(TAG, mErrorBarcodeStr + ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mSearchItem = menu.findItem(R.id.action_search);

        if(!Utilities.IsConnectedToInternet(this))
        {
            mSearchItem.setVisible(false);
        }

        searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //searchView.setIconified(false);

        searchView.setOnSuggestionListener(this);
        /*searchView.setOnQueryTextListener(
                new

        );*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_logout:
                Application.Logout(mGoogleApiClient, this);
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            final String barcodeScanResult = result.getContents();

            if (barcodeScanResult == null) {
                Log.d(TAG, mCancelledBarcodeStr);
                Toast.makeText(this, mCancelledBarcodeStr, Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, mScannedBarcodeStr + barcodeScanResult);

                String formatNameResult = result.getFormatName();
                String formatTypeResult = result.getType();

                mApiCall.SearchProduct(barcodeScanResult, formatNameResult, null, this);

                //SearchProductFromAmazonApi(barcodeScanResult, formatNameResult, formatTypeResult);
                //SearchProductFromWalmartAPI(barcodeScanResult,null);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        hideShowFab();
        RefreshApiPreference();
        getLoaderManager().restartLoader(PRODUCT_LOADER, null, this);
    }
    
    private void init() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new ProductListAdapter(this, mCurrentStockPeriod);
        mRecyclerView.setAdapter(adapter);
        final SwipeToDismissTouchListener<RecyclerViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new RecyclerViewAdapter(mRecyclerView),
                        new SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(RecyclerViewAdapter view, int position) {

                                adapter.remove(position);

                            }
                        });

        mRecyclerView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mRecyclerView.setOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
        mRecyclerView.addOnItemTouchListener(new SwipeableItemClickListener(this,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (view.getId() == R.id.txt_delete) {
                            touchListener.processPendingDismisses();
                        } else if (view.getId() == R.id.txt_undo) {
                            touchListener.undoPendingDismiss();
                        } else { // R.id.txt_data
                            adapter.onItemclicked(position);
                        }
                    }
                }));

        spinnerAdapter = new StockPeriodSpinnerAdapter(this, mCurrentStockPeriod);

        mStockPeriodSpinner.setAdapter(spinnerAdapter);

        mStockPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Called when a new item was selected (in the Spinner)
             */
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                mSelectedStockPeriod = (StockPeriod) parent.getItemAtPosition(pos);

                //inAnimation = new AlphaAnimation(0f, 1f);
                //inAnimation.setDuration(200);
                //progressBarHolder.setAnimation(inAnimation);
                //progressBarHolder.setVisibility(View.VISIBLE);
                getLoaderManager().restartLoader(PRODUCT_LOADER, null, ProductListActivity.this);
            }


            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }
        });



    }

    private void hideShowFab()
    {
        if(Utilities.IsConnectedToInternet(this))
        {
            fabSearchButton.setVisibility(View.VISIBLE);
            fabBarcodeButton.setVisibility(View.VISIBLE);

            if(mSearchItem != null)
            {
                mSearchItem.setVisible(true);
            }
        }
        else
        {
            fabSearchButton.setVisibility(View.GONE);
            fabBarcodeButton.setVisibility(View.GONE);

            if(mSearchItem != null)
            {
                mSearchItem.setVisible(false);
            }
        }
    }

    private void RefreshApiPreference()
    {
        String tmpApiPreferenceCode = Application.GetApiCodeFromPreference();

        if(!mApiCode.equals(tmpApiPreferenceCode))
        {
            mApiCode = tmpApiPreferenceCode;
            mApiCall = Application.GetApiCallFromPreference(tmpApiPreferenceCode);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

     /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (id) {
            case PRODUCT_LOADER:
                // Returns a new CursorLoader

                if(mSelectedStockPeriod == null)
                {
                    mSelectedStockPeriod = Application.getCurrentStockPeriod();
                }

                if(mCurrentStockPeriod.getStockPeriodId() == mSelectedStockPeriod.getStockPeriodId()) {
                    return new CursorLoader(
                            this,
                            StoCountContract.ProductEntry.buildCurrentProductUri(mSelectedStockPeriod.getStockPeriodId()),
                            null,
                            null,
                            null,
                            null
                    );
                }
                else
                {
                    return new CursorLoader(
                            this,
                            StoCountContract.ProductEntry.buildPreviousProductUri(mSelectedStockPeriod.getStockPeriodId()),
                            null,
                            null,
                            null,
                            null
                    );
                }

            case PREVIOUS_STOCK_LOADER:
                return new CursorLoader(
                        this,
                        StoCountContract.StockPeriodEntry.PREVIOUS_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId())
        {
            case PRODUCT_LOADER:
                adapter.swapCursor(data, mSelectedStockPeriod);
                adapter.notifyDataSetChanged();
                break;
            case PREVIOUS_STOCK_LOADER:
                spinnerAdapter.swapCursor(data);
                spinnerAdapter.notifyDataSetChanged();
                mStockPeriodSpinner.setSelection(spinnerAdapter.getCount()-1);
                break;
        }

        //if (position != ListView.INVALID_POSITION) {
        //    bookList.smoothScrollToPosition(position);
        //}
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //adapter.swapCursor(null);
    }


    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        if(searchView != null) {
            CursorAdapter c = searchView.getSuggestionsAdapter();

            Cursor cur = c.getCursor();
            cur.moveToPosition(position);
            int suggestionItemId = cur.getInt(0);

            mApiCall.SearchProduct(null, null, Integer.toString(suggestionItemId), this);
            //SearchProductFromWalmartAPI(null, Integer.toString(suggestionItemId));

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    static class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Cursor mProductCursor;
        private Context mContext;
        private StockPeriod mCurrentStockPeriod;
        private StockPeriod mSelectedStockPeriod;


        ProductListAdapter(Context context, StockPeriod stockPeriod) {
            this.mProductCursor = null;
            this.mContext = context;
            this.mCurrentStockPeriod = stockPeriod;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {

            View v;

            if (position == EMPTY_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
                EmptyViewHolder evh = new EmptyViewHolder(v);
                return evh;
            }

            return new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if(mProductCursor != null && holder instanceof MyViewHolder) {

                MyViewHolder myViewHolder = (MyViewHolder) holder;

                mProductCursor.moveToPosition(position);
                myViewHolder.dataTextView.setText(mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductEntry.PRODUCT_NAME)));
                myViewHolder.dataTextView.setContentDescription(mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductEntry.PRODUCT_NAME)));

                myViewHolder.dataTextInfoView.setText(mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductEntry.ADDITIONAL_INFO)));
                myViewHolder.dataTextInfoView.setContentDescription(mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductEntry.ADDITIONAL_INFO)));

                myViewHolder.dataTextCount.setText(mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductCountEntry.QUANTITY)));
                myViewHolder.dataTextCount.setContentDescription(mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductCountEntry.QUANTITY)));

                String imageUrl = mProductCursor.getString(mProductCursor.getColumnIndex(StoCountContract.ProductEntry.THUMBNAIL_IMAGE));

                if(imageUrl.isEmpty())
                {
                    Glide.with(this.mContext).load(android.R.drawable.ic_menu_gallery).fitCenter().into(myViewHolder.dataImageView);
                }
                else {
                    Glide.with(this.mContext).load(imageUrl).fitCenter().into(myViewHolder.dataImageView);
                }
            }
        }

        @Override
        public int getItemCount() {
            if(mProductCursor != null)
            {
                if(mProductCursor.getCount() == 0)
                    return 1;
                else
                    return mProductCursor.getCount();
            }
            else
                return 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (mProductCursor != null && mProductCursor.getCount() == 0) {
                return EMPTY_VIEW;
            }
            return super.getItemViewType(position);
        }

        public void remove(int position) {

            if(mCurrentStockPeriod.getStockPeriodId() == mSelectedStockPeriod.getStockPeriodId()) {
                mProductCursor.moveToPosition(position);

                Product prod = new Product(mProductCursor);

                DBAsyncTask deleteAsyncTask = new DBAsyncTask(mContext.getContentResolver(), DBAsyncTask.ObjectType.PRODUCT, DBAsyncTask.OperationType.DELETE, null);
                deleteAsyncTask.execute(prod);

                ((Activity) mContext).getLoaderManager().restartLoader(PRODUCT_LOADER, null, (ProductListActivity) mContext);

                notifyItemRemoved(position);
            }
            else
            {
                Toast.makeText(mContext, "Unable delete product for previous stock period.", Toast.LENGTH_LONG).show();
            }

        }

        public void swapCursor(Cursor cursor, StockPeriod stockPeriod)
        {
            this.mProductCursor = cursor;
            this.mSelectedStockPeriod = stockPeriod;
        }

        public void onItemclicked(int position)
        {
            if(mProductCursor != null) {
                mProductCursor.moveToPosition(position);

                Product prod = new Product(mProductCursor);
                ProductCount prodCount = new ProductCount(mProductCursor);

                Bundle bundle = new Bundle();
                bundle.putParcelable(DetailActivity.PRODUCT_PARCELABLE, prod);

                if(prodCount.getProductCountId() != null)
                    bundle.putParcelable(DetailActivity.PRODUCT_COUNT_PARCELABLE, prodCount);

                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra(DetailActivity.PRODUCT_PARCELABLE, bundle);


                if(mCurrentStockPeriod.getStockPeriodId() != mSelectedStockPeriod.getStockPeriodId()) {
                    intent.putExtra(DetailActivity.IS_PREVIOUS_STOCK_PERIOD, true);
                }
                else
                {
                    intent.putExtra(DetailActivity.IS_PREVIOUS_STOCK_PERIOD, false);
                }

                mContext.startActivity(intent);

            }
        }

        static class EmptyViewHolder extends RecyclerView.ViewHolder {
            public EmptyViewHolder(View itemView) {
                super(itemView);
            }
        }

        static class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView dataImageView;
            TextView dataTextView;
            TextView dataTextInfoView;
            TextView dataTextCount;

            MyViewHolder(View view) {
                super(view);
                dataTextView = ((TextView) view.findViewById(R.id.txt_data));
                dataTextInfoView = ((TextView) view.findViewById(R.id.txt_datainfo));
                dataTextCount = ((TextView) view.findViewById(R.id.txt_datacount));
                dataImageView = ((ImageView) view.findViewById(R.id.img_data));
            }
        }
    }

}


class StockPeriodSpinnerAdapter extends BaseAdapter
{
    private Cursor mStockPeriodCursor;
    private Context mContext;
    private StockPeriod mCurrentPeriod;
    private LayoutInflater mInflater;

    StockPeriodSpinnerAdapter(Context context, StockPeriod stockPeriod) {
        this.mStockPeriodCursor = null;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);

        try {
            this.mCurrentPeriod = (StockPeriod) stockPeriod.clone();
        } catch (CloneNotSupportedException ex) {

        }
    }

    public void swapCursor(Cursor cursor)
    {
        this.mStockPeriodCursor = cursor;
    }


    @Override
    public int getCount() {
        if(mStockPeriodCursor != null)
        {
            return mStockPeriodCursor.getCount() + 1;
        }
        else
            return 1;
    }

    @Override
    public Object getItem(int i) {

        if (mStockPeriodCursor != null) {

            if(mStockPeriodCursor.getCount() > i) {
                mStockPeriodCursor.moveToPosition(i);
                return new StockPeriod(mStockPeriodCursor);
            }
            else
            {
                return mCurrentPeriod;
            }
        }
        else
            return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder=null;

        if(view==null ||  view.getTag() == null ) {
            view = mInflater.inflate(R.layout.stock_period_spinner_item,viewGroup,false);

            holder = new ViewHolder();

            holder.txtStockPeriodDate=(TextView)view.findViewById(R.id.stockPeriodSpinnerItemDate);
            holder.txtStockPeriodId=(TextView)view.findViewById(R.id.stockPeriodSpinnerItemId);

            if(i==0)
            {
                view.setTag(holder);
            }

        }
        else
        {
            holder = (ViewHolder) view.getTag();
        }

        if (mStockPeriodCursor != null) {
            if(mStockPeriodCursor.getCount() > i) {
                mStockPeriodCursor.moveToPosition(i);
                StockPeriod sp = new StockPeriod(mStockPeriodCursor);
                holder.txtStockPeriodDate.setText(sp.DateFormat.format(sp.getStartDate()) + " - " + sp.DateFormat.format(sp.getEndDate()));
                holder.txtStockPeriodId.setText(sp.getStockPeriodId().toString());
            }
            else
            {
                holder.txtStockPeriodDate.setText("Current Period Starting: "+mCurrentPeriod.DateFormat.format(mCurrentPeriod.getStartDate()));
                holder.txtStockPeriodId.setText(mCurrentPeriod.getStockPeriodId().toString());
            }
        }

        return view;
    }
}

class ViewHolder
{
    TextView txtStockPeriodDate;

    TextView txtStockPeriodId;
}
