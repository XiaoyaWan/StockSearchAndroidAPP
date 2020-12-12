package com.example.stockapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MainListAdapter.ItemClickListener {

    private RecyclerView mainList;
    private MainListAdapter adapter;
//    private boolean isRefresh;
//    private RecyclerView portfolioView;
//    private RecyclerView favoriteView;
//    private PortfolioListAdapter pAdapter;
//    private FavoriteListAdapter fAdapter;

    private SearchAutocompleteAdapter autoSearchAdapter;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 500;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private final ArrayList<String[]> portfolioArray = new ArrayList<String[]>();
    private final ArrayList<String[]> favoriteArray = new ArrayList<String[]>();
    private final ArrayList<String[]> portfolioOrderedArray = new ArrayList<String[]>();
    private final ArrayList<String[]> favoriteOrderedArray = new ArrayList<String[]>();
    private HashMap<String,String> favoriteList;
    private HashMap<String,Float> portfolioList;
    private String query;
//    private final ArrayList<String> portfolioOrderList = new ArrayList<String>();
//    private final ArrayList<String> favoriteOrderList = new ArrayList<String>();


    private View progress;
    private SearchView.SearchAutoComplete searchAutoComplete;
    private Handler handler;
    private Handler refreshhandler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
//        editor.clear();
//        editor.commit();

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_StockAPP);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = findViewById(R.id.progress_bar);


        favoriteList = new HashMap<String, String>();
        portfolioList = new HashMap<String, Float>();
        Map<String, ?> allEntries = pref.getAll();
        if ( allEntries != null ){
            for (Map.Entry<String, ?> entry : allEntries.entrySet() ) {
                Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                if( entry.getKey().charAt(0) == 'f'){
                    favoriteList.put(entry.getKey().toString().split("_")[1],entry.getValue().toString());
                }else if( entry.getKey().charAt(0) == 'p'){
                    portfolioList.put(entry.getKey().toString().split("_")[1],Float.parseFloat(entry.getValue().toString()));
                }
//                else if( entry.getKey().charAt(0) == 'F'){
//                    favoriteOrderList.add(entry.getValue().toString());
//                }else if( entry.getKey().charAt(0) == 'P'){
//                    portfolioOrderList.add(entry.getValue().toString());
//                }
            }
        }


        mainList = findViewById(R.id.main_List);
        mainList.setNestedScrollingEnabled(false);

//        portfolioView = findViewById(R.id.portfolio_List);
//        portfolioView.setNestedScrollingEnabled(false);
//        LinearLayoutManager pLayoutManager = new LinearLayoutManager(MainActivity.this);
//        DividerItemDecoration pDividerItemDecoration = new DividerItemDecoration(portfolioView.getContext(), pLayoutManager.getOrientation());
//        portfolioView.addItemDecoration(pDividerItemDecoration);
//        portfolioView.setLayoutManager(pLayoutManager);
//
//        favoriteView = findViewById(R.id.favorite_List);
//        favoriteView.setNestedScrollingEnabled(false);
//        LinearLayoutManager fLayoutManager = new LinearLayoutManager(MainActivity.this);
//        DividerItemDecoration fDividerItemDecoration = new DividerItemDecoration(favoriteView.getContext(), fLayoutManager.getOrientation());
//        favoriteView.addItemDecoration(fDividerItemDecoration);
//        favoriteView.setLayoutManager(fLayoutManager);

        makeListCall(portfolioList , favoriteList);



        TextView footer = findViewById(R.id.main_footer);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse("https://www.tiingo.com/"));
                startActivity(intent);
            }
        });

    }

    private void makeApiCall(String text) {
        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/search?="+ text, new Response.Listener<String>() {
//        APICall.make(this, "http://10.0.2.2:3000/search?="+text, new Response.Listener<String>() {
            private String TAG = getClass().getSimpleName();
            @Override
            public void onResponse(String response) {
                List<String> stringList = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        stringList.add(row.getString("ticker")+" - "+row.getString("name"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                autoSearchAdapter.setData(stringList);
//                searchAutoComplete.setAdapter(autoSearchAdapter);
                autoSearchAdapter.notifyDataSetChanged();
            }
        }, error -> {
        });
    }

    @SuppressLint("DefaultLocale")
    private void makeListCall(HashMap<String,Float> pList, HashMap<String,String> fList) {
        query = "";
        for (Map.Entry<String, Float> entry : pList.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            query += entry.getKey()+",";
        }
        for (Map.Entry<String, String> entry : fList.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            query += entry.getKey()+",";
        }
        if(query.length()>1){
            query.substring(0,query.length()-1);
        }

        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/simpledata?="+ query, response -> {
//        APICall.make(this, "http://10.0.2.2:3000/simpledata?="+ query, response -> {
            try {
                JSONArray array = new JSONArray(response);
                for( int i = 0 ; i < array.length() ; i++){
                    String[] temp = new String[5];
                    temp[0] = array.getJSONObject(i).getString("ticker");
                    temp[2] = array.getJSONObject(i).getString("last");
                    temp[3] = array.getJSONObject(i).getString("change");
                    temp[4] = array.getJSONObject(i).getString("trend");
                    if( pList.get(temp[0]) != null){
                        temp[1] = String.valueOf(pList.get(temp[0]));
                        if( fList.get(temp[0]) != null){
                            favoriteArray.add(temp);
                        }
                        portfolioArray.add(temp);
                    }else{
                        temp[1] =  fList.get(temp[0]);
                        favoriteArray.add(temp);
                    }
                }

                setOrder();

                Date now = new Date();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
                adapter = new MainListAdapter(MainActivity.this, favoriteOrderedArray, portfolioOrderedArray, 20000 - pref.getFloat("offsetSum", 0.00f), format.format(now) );
                mainList.setAdapter( adapter );
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
                DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mainList.getContext(), mLayoutManager.getOrientation());
                mainList.addItemDecoration(mDividerItemDecoration);
                mainList.setLayoutManager(mLayoutManager);
                adapter.addItemClickListener(this);

                ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback( this, adapter );
                ItemTouchHelper touchHelper = new ItemTouchHelper( callback );
                touchHelper.attachToRecyclerView( mainList );
                progress.setVisibility(View.GONE);

//                pAdapter = new PortfolioListAdapter(MainActivity.this, portfolioArray, formatNum( 20000 - pref.getFloat("offsetSum", 0.00f)) );
//                fAdapter = new FavoriteListAdapter(MainActivity.this, favoriteArray );
//                portfolioView.setAdapter( pAdapter );
//                favoriteView.setAdapter( fAdapter );
//                ItemTouchHelper.Callback pCallback = new PortfolioItemTouchHelperCallback( this, pAdapter );
//                ItemTouchHelper pTouchHelper = new ItemTouchHelper( pCallback );
//                pTouchHelper.attachToRecyclerView( portfolioView );
//                ItemTouchHelper.Callback fCallback = new FavoriteItemTouchHelperCallback( this, fAdapter );
//                ItemTouchHelper fTouchHelper = new ItemTouchHelper( fCallback );
//                fTouchHelper.attachToRecyclerView( favoriteView );


                //refresh every 15s
//                for( int i = 0 ; i < array.length() ; i++) {
//                    isRefresh = false;
//                    if (array.getJSONObject(i).getString("market").equals("+") ) {
//                        isRefresh = true;
                        refreshhandler = new Handler();
                        String TAG = getClass().getSimpleName();
                        refreshhandler.postDelayed(runnable = new Runnable() {
                            public void run() {
                                refreshhandler.postDelayed(runnable, 15000);
                                Log.d(TAG, "Main: This method is run every 15 seconds");
                                Toast.makeText(MainActivity.this, "Main: This method is run every 15 seconds", Toast.LENGTH_SHORT).show();
//                                System.out.println("main run every 15 s");
                                makeRefreshListCall(query);
                            }
                        }, 15000);
//                        break;
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> {
        });
    }

    private void makeRefreshListCall(String query) {
        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/simpledata?="+ query, response -> {
//        APICall.make(this, "http://10.0.2.2:3000/simpledata?="+query, response -> {
            try {
                JSONArray array = new JSONArray(response);
                for( int i = 0 ; i < array.length() ; i++){
                    String[] temp = new String[5];
                    temp[0] = array.getJSONObject(i).getString("ticker");
                    temp[1] = null;
                    temp[2] = array.getJSONObject(i).getString("last");
                    temp[3] = array.getJSONObject(i).getString("change");
                    temp[4] = array.getJSONObject(i).getString("trend");

                    for( int j = 0 ; j < portfolioOrderedArray.size(); j++ ){
                        if( portfolioOrderedArray.get(j)[0].equals(temp[0]) ){
                            portfolioOrderedArray.get(j)[2] = temp[2];
                            portfolioOrderedArray.get(j)[3] = temp[3];
                            portfolioOrderedArray.get(j)[4] = temp[4];
                        }
                    }
                    for( int j = 0 ; j < favoriteOrderedArray.size(); j++ ){
                        if( favoriteOrderedArray.get(j)[0].equals(temp[0]) ){
                            favoriteOrderedArray.get(j)[2] = temp[2];
                            favoriteOrderedArray.get(j)[3] = temp[3];
                            favoriteOrderedArray.get(j)[4] = temp[4];
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> {
        });

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        autoSearchAdapter = new SearchAutocompleteAdapter(this, android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setThreshold(3);
        searchAutoComplete.setAdapter(autoSearchAdapter);
        searchAutoComplete.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchAutoComplete.setText(autoSearchAdapter.getObject(position));
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("ticker",autoSearchAdapter.getObject(position).split(" ")[0]);
                startActivity(intent);
            }
        });
        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        makeApiCall(searchAutoComplete.getText().toString());
                    }
                }
                return false;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private String TAG = getClass().getSimpleName();
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
            @Override
            public boolean onQueryTextSubmit(String queryText) {
//                Log.d(TAG, "onQueryTextSubmit = " + queryText);
//                if (searchView != null) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    if (imm != null) {
//                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
////                        searchView.clearFocus();
//                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//                        intent.putExtra("ticker",queryText.split(" ")[0]);
//                        startActivity(intent);
//                        return true;
//                    }
//                }
                return false;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

//        if( isRefresh ){
            refreshhandler = new Handler();
            String TAG = getClass().getSimpleName();
            refreshhandler.postDelayed(runnable = new Runnable() {
                public void run() {
                    refreshhandler.postDelayed(runnable, 15000);
                    Log.d(TAG, "Main: This method is run every 15 seconds");
                    Toast.makeText(MainActivity.this, "Main: This method is run every 15 seconds", Toast.LENGTH_SHORT).show();
//                    System.out.println("main run every 15 s");
                    makeRefreshListCall(query);
                }
            }, 15000);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        ArrayList<String[]> ptemp = adapter.getPortfolioList();
        ArrayList<String[]> ftemp = adapter.getFavoriteList();

        String porder = "";
        for ( int i = 0 ; i < ptemp.size() ; i++ ) {
            porder += ptemp.get(i)[0] + "_" ;

        }
        editor.putString("OrderPortfolio", porder);
        System.out.println("OrderPortfolio : "+ ptemp.size() +" : "+ porder);
        String forder = "";
        for ( int i = 0 ; i < ftemp.size() ; i++ ) {
            forder += ftemp.get(i)[0] + "_" ;
        }
        editor.putString("OrderFavorite", forder);
        System.out.println("OrderFavorite :" + forder);
        editor.commit();

        if( runnable != null )
            refreshhandler.removeCallbacks(runnable);
    }

    protected void setOrder() {
        System.out.println( "OrderPortfolio:  "+pref.getString("OrderPortfolio", "null"));
        System.out.println( "OrderFavorite:  "+pref.getString("OrderFavorite", "null"));
        String[] portfolioOrder = pref.getString("OrderPortfolio", "null").split("_");
        String[] favoriteOrder = pref.getString("OrderFavorite", "null").split("_");
        int i = 0;
        while( i < portfolioOrder.length && portfolioOrder[i] != null ){
            for ( int j = 0; j < portfolioArray.size() ; j++ ) {
                if ( portfolioArray.get(j)[0].equals(portfolioOrder[i]) ) {
                    portfolioOrderedArray.add(portfolioArray.get(j));
                    portfolioArray.remove(j);
                    break;
                }
            }
            i++;
        }
        for ( int j = 0; j < portfolioArray.size() ; j++ ) {
                portfolioOrderedArray.add(portfolioArray.get(j));
        }
        i = 0;
        while( i < favoriteOrder.length && favoriteOrder[i] != null ){
            for ( int j = 0; j < favoriteArray.size() ; j++ ) {
                if ( favoriteArray.get(j)[0].equals(favoriteOrder[i]) ) {
                    favoriteOrderedArray.add(favoriteArray.get(j));
                    favoriteArray.remove(j);
                    break;
                }
            }
            i++;
        }
        for ( int j = 0; j < favoriteArray.size() ; j++ ) {
                favoriteOrderedArray.add(favoriteArray.get(j));
        }
    }

    @Override
    public void onItemClick(String query) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("ticker", query);
        startActivity(intent);
    }
}