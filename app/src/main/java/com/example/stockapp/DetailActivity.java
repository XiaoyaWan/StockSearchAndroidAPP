package com.example.stockapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailActivity extends AppCompatActivity {
    class InfoHolder{
        String ticker, name, about, price, change;
        float priceNum, changeNum;
        String low,bidPrice,openPrice,mid,high,volume;
    }
    Context context = this;
    private InfoHolder infoHolder;
    private String ticker;
    private TextView textTicker,textName,textPrice,textChange, textAbout, textView, shareText, marketValueText;
    private RecyclerView listView;
    private ArrayList<String[]> newsListInfo;
    private JSONArray chartList;
    private TextView statsPrice,statsLow,statsBidPrice,statsOpenPrice,statsMid,statsHigh,statsVolume;
    private WebView webView;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private View progress;
    private Handler handler;
    private Runnable runnable;

    @SuppressLint({"SetJavaScriptEnabled", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progress = findViewById(R.id.progress_bar);

        ticker = getIntent().getExtras().getString("ticker").toUpperCase();

        textTicker = findViewById(R.id.detail_ticker);
        textName = findViewById(R.id.detail_name);
        textPrice = findViewById(R.id.detail_price);
        textChange = findViewById(R.id.detail_change);
        textAbout = findViewById(R.id.detail_about);

        textView = findViewById(R.id.detail_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if( textView.getText() == "Show More..." ){
                    textAbout.setMaxLines(Integer.MAX_VALUE / 2);
                    textAbout.setText(infoHolder.about);
                    textView.setText("Show Less");
                }else if( textView.getText() == "Show Less" ){
                    textAbout.setMaxLines(2);
                    int lineEndIndex = textAbout.getLayout().getLineEnd(1);
                    String text = textAbout.getText().subSequence(0, lineEndIndex - 5) + "...";
                    textAbout.setText(text);
                    textView.setText("Show More...");
                }
            }
        });
        statsPrice = findViewById(R.id.stats_price);
        statsLow = findViewById(R.id.stats_low);
        statsBidPrice = findViewById(R.id.stats_bidPrice);
        statsOpenPrice = findViewById(R.id.stats_openPrice);
        statsMid = findViewById(R.id.stats_mid);
        statsHigh = findViewById(R.id.stats_high);
        statsVolume = findViewById(R.id.stats_volume);

        //portfolio section

        shareText = findViewById(R.id.detail_share);
        marketValueText = findViewById(R.id.detail_market_value);
        Button tradeButton = findViewById(R.id.detail_trade_button);
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                float ownedShare = fourDigits(pref.getFloat("portfolio_" + ticker, 0 ));
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.trade_dialog);
                TextView title = dialog.findViewById(R.id.trade_title);
                title.setText("Trade " + infoHolder.name +" shares");
                EditText input = dialog.findViewById(R.id.trade_input);
                TextView sum = dialog.findViewById(R.id.trade_sum);
                sum.setText("0.0 x $" + infoHolder.price +"/share = $ 0.0");
                TextView available = dialog.findViewById(R.id.trade_available);
                available.setText("$"+ formatNum(20000 - pref.getFloat("offsetSum",0.00f),true) + " available to buy "+infoHolder.ticker);
                Button buyButton = dialog.findViewById(R.id.trade_buy_button);
                Button sellButton = dialog.findViewById(R.id.trade_sell_button);
                input.addTextChangedListener(new TextWatcher(){
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(s.toString().equals("")){
                            //如果是则直接让成员变量putNumber为0
                            sum.setText("0.0 x $" + infoHolder.price +"/share = $ 0.0");
                        }else{
                            float num = Float.parseFloat(s.toString());
                            sum.setText(fourDigits(num) + " x $" + infoHolder.price +"/share = $ " + twoDigits(num * infoHolder.priceNum));
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        if(s.toString().equals("")){
                            sum.setText("0.0 x $" + infoHolder.price +"/share = $ 0.0");
                        }else{
                            float num = Float.parseFloat(s.toString());
                            sum.setText(fourDigits(num) + " x $" + infoHolder.price +"/share = $ " + twoDigits(num * infoHolder.priceNum));
                            System.out.println("formatnum"+formatNum(num * infoHolder.priceNum,false));
                        }
                    }
                });


                Dialog complete_dialog = new Dialog(context);
                complete_dialog.setContentView(R.layout.trade_complete_dialog);
                TextView text = complete_dialog.findViewById(R.id.trade_complete_text);
                TextView doneButton = complete_dialog.findViewById(R.id.trade_done_button);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pref.getFloat("portfolio_" + ticker, 0.00f ) == 0.00f ) {
                            shareText.setText("You have 0 shares of "+infoHolder.ticker+".");
                            marketValueText.setText("Start trading!");
                        } else {
                            shareText.setText("Shares owned: "+ formatNum( pref.getFloat("portfolio_" + ticker, 0.00f ),false) );
                            marketValueText.setText("Market Value: $"+ formatNum(pref.getFloat("portfolio_" + ticker, 0.00f )*infoHolder.priceNum,true));
                        }
                        complete_dialog.dismiss();
                    }
                });

                buyButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onClick(View v) {
                        if( !isFourDigitsNumeric(input.getText().toString()) || input.getText() == null ){
                            Toast.makeText(context,"Please enter valid amount",Toast.LENGTH_SHORT).show();
                        }else if( fourDigits(20000 - pref.getFloat("offsetSum",0.00f)) < fourDigits( (float) infoHolder.priceNum * Float.parseFloat(input.getText().toString()) ) ){
                            Toast.makeText(context,"Not enough money to buy",Toast.LENGTH_SHORT).show();
                        }else if( Float.parseFloat(input.getText().toString()) > 0 ) {
                            dialog.dismiss();
                            text.setText("You have successfully bought " + input.getText().toString() + " shares of " + ticker);
                            editor.putFloat("portfolio_" + ticker,
                                    fourDigits(pref.getFloat("portfolio_" + ticker, 0.00f) + Float.parseFloat(input.getText().toString()))
                            );
                            editor.putFloat("offsetSum",
                                    fourDigits(pref.getFloat("offsetSum", 0.00f) + infoHolder.priceNum * Float.parseFloat(input.getText().toString()))
                            );
                            editor.apply();
                            System.out.println(pref.getFloat("offsetSum", 0.00f));

                            complete_dialog.show();
                        }else{
                            Toast.makeText(context,"Cannot buy less than 0 shares",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                sellButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onClick(View v) {
                        if( !isFourDigitsNumeric(input.getText().toString()) || input.getText() == null ){
                            Toast.makeText(context,"Please enter valid amount",Toast.LENGTH_SHORT).show();
                        } else if(  Float.parseFloat(input.getText().toString()) > 0 && Float.parseFloat(input.getText().toString()) <= ownedShare){
                            dialog.dismiss();
                            text.setText("You have successfully sold " + input.getText().toString() +" shares of "+ticker);
                            editor.putFloat("portfolio_" + ticker,
                                    fourDigits(ownedShare - Float.parseFloat(input.getText().toString()))
                            );
                            if ( fourDigits(  ownedShare - Float.parseFloat(input.getText().toString()) ) == 0 ) {
                                editor.remove("portfolio_" + ticker);
                            }
                            editor.putFloat("offsetSum",
                                    fourDigits(pref.getFloat("offsetSum" , 0.00f) - infoHolder.priceNum * Float.parseFloat(input.getText().toString()))
                            );
                            editor.commit();
                            System.out.println(pref.getFloat("offsetSum" , 0.00f));
                            complete_dialog.show();
                        }else if(  Float.parseFloat(input.getText().toString()) <= 0 ){
                            Toast.makeText(context,"Cannot sell less than 0 shares",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context,"Not enough shares to sell",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });

        //news section
        listView = findViewById(R.id.news_list);
        listView.setNestedScrollingEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
//        listView.setAdapter(new NewsListAdapter(DetailActivity.this, newsListInfo));

        listView.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), listView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(newsListInfo.get(position)[4]));
                startActivity(intent);
            }
            @Override
            public void onLongClick(View view, int position) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.news_dialog);

                TextView title = dialog.findViewById(R.id.dialog_title);
                title.setText(newsListInfo.get(position)[3]);
                ImageView image = dialog.findViewById(R.id.dialog_image);
                Glide.with(context).load(newsListInfo.get(position)[0]).apply(new RequestOptions().transforms(new CenterCrop())).into(image);

                ImageButton twitterButton = dialog.findViewById(R.id.dialog_twitter);
                ImageButton chromeButton = dialog.findViewById(R.id.dialog_chrome);

                twitterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse("https://twitter.com/intent/tweet?text="+newsListInfo.get(position)[3]+"&url="+newsListInfo.get(position)[4]));
                        startActivity(intent);
                    }
                });
                chromeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse(newsListInfo.get(position)[4]));
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        }));

        infoHolder = new InfoHolder();
        makeOneTimeCall(ticker);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_star:
                if(pref.getString("favorite_" + ticker, "nothing").equals("nothing")){
                    Toast.makeText(context,"\""+infoHolder.ticker + "\" was added to favorites",Toast.LENGTH_SHORT).show();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));
                    editor.putString("favorite_" + ticker, infoHolder.name );
                    editor.commit();
                }else{
                    Toast.makeText(context,"\""+infoHolder.ticker + "\" was removed from favorites",Toast.LENGTH_SHORT).show();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_border_24));
                    editor.remove("favorite_" + ticker);
                    editor.commit();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_toolbar_menu, menu);
        if(pref.getString("favorite_" + ticker, "nothing").equals("nothing")){
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_border_24));
        }else{
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void makeOneTimeCall(String text) {
        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/details?=" + text, new Response.Listener<String>() {
            //        APICall.make(this, "http://10.0.2.2:3000/details?=" + text, new Response.Listener<String>() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
                    JSONObject json = new JSONObject(response);
                    infoHolder.ticker = json.getString("ticker");
                    infoHolder.name = json.getString("name");
                    infoHolder.about = json.getString("description");
                    textTicker.setText(infoHolder.ticker);
                    textName.setText(infoHolder.name);
                    textAbout.setText(infoHolder.about);
                    textAbout.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            if(textAbout.getLineCount() > 2){
                                textAbout.setMaxLines(2);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText("Show More...");
                            }else{
                                textView.setVisibility(View.GONE);
                            }
                        }
                    });
                    if (pref.getFloat("portfolio_" + ticker, 0.00f ) == 0.00f ) {
                        shareText.setText("You have 0 shares of "+infoHolder.ticker+".");
                    } else {
                        shareText.setText("Shares owned: "+ formatNum(pref.getFloat("portfolio_" + ticker, 0 ),false) );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            private String TAG = getClass().getSimpleName();
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error );
            }
        });

        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/news?=" + text, new Response.Listener<String>() {
//        APICall.make(this, "http://10.0.2.2:3000/news?="+text, new Response.Listener<String>() {
            private String TAG = getClass().getSimpleName();
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
//                    Log.d(TAG, response);
                    JSONArray array = new JSONArray(response);
                    newsListInfo = new ArrayList<String[]>();

                    for( int i = 0 ; i < Math.min(array.length(),10) ; i++){
                        String[] temp = new String[5];
                        if(array.getJSONObject(i).getString("urlToImage").equals("null")
                                || array.getJSONObject(i).getString("title").equals("null")
                                || array.getJSONObject(i).getString("url").equals("null")
                                || new JSONObject(array.getJSONObject(i).getString("source")).getString("name").equals("null")
                                || array.getJSONObject(i).getString("publishedAt").equals("null")){
                            continue;
                        }
                        temp[0] = array.getJSONObject(i).getString("urlToImage");
                        temp[1] = new JSONObject(array.getJSONObject(i).getString("source")).getString("name");
                        //calculate difference of date
                        Date now = new Date();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        Date news = format.parse(array.getJSONObject(i).getString("publishedAt"));
                        long day = (now.getTime() - news.getTime())/(1000 * 60 * 60 * 24);
                        temp[2] = day + " days ago";
                        if( day < 1 ){
                            long hour = (now.getTime() - news.getTime())/(1000 * 60 * 60);
                            temp[2] = hour + " hours ago";
                            if( hour <=0 ){
                                long min = (now.getTime() - news.getTime())/(1000 * 60);
                                temp[2] = min + " minutes ago";
                            }
                        }else if( day < 2 ){
                            temp[2] = day + " day ago";
                        }
                        temp[3] = array.getJSONObject(i).getString("title");
                        temp[4] = array.getJSONObject(i).getString("url");

                        newsListInfo.add(temp);
                    }
//                    Log.d(TAG, newsListInfo[0][3]);
                    listView.setAdapter(new NewsListAdapter(DetailActivity.this, newsListInfo));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.w("error in response", "Error: " + error.getMessage()));

        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/detailsrefresh?=" + text, new Response.Listener<String>() {
//        APICall.make(this, "http://10.0.2.2:3000/detailsrefresh?="+text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    infoHolder.price = (object.getString("last").equals("null")) ? "0.0"
                            : object.getString("last");
                    infoHolder.priceNum = (object.getString("last").equals("null")) ? 0.00f
                            : Float.parseFloat(object.getString("last"));
                    infoHolder.changeNum = (object.getString("prevClose").equals("null")) ? infoHolder.priceNum
                            : infoHolder.priceNum - Float.parseFloat(object.getString("prevClose"));
                    infoHolder.change = formatNum(infoHolder.changeNum, true);

                    infoHolder.low = object.getString("low");
                    infoHolder.bidPrice = object.getString("bidPrice");
                    infoHolder.openPrice = object.getString("open");
                    infoHolder.mid = object.getString("mid");
                    infoHolder.high = object.getString("high");
                    infoHolder.volume = object.getString("volume");

                    textPrice.setText("$" + infoHolder.price);
                    if (infoHolder.change.charAt(0) == '-') {
                        textChange.setTextColor(Color.parseColor("#9B4049"));
                        textChange.setText("-$" + infoHolder.change.substring(1));
                    } else {
                        textChange.setText("$" + infoHolder.change);
                        if (infoHolder.changeNum != 0.00f) {
                            textChange.setTextColor(Color.parseColor("#319C5E"));
                        }else{
                            textChange.setTextColor(Color.parseColor("#9C9C9C"));
                        }
                    }

                    infoHolder.low = (infoHolder.low.equals("null") || infoHolder.low.equals("0")) ? "0.0" : infoHolder.low;
                    infoHolder.bidPrice = (infoHolder.bidPrice.equals("null") || infoHolder.bidPrice.equals("0")) ? "0.0" : infoHolder.bidPrice;
                    infoHolder.openPrice = (infoHolder.openPrice.equals("null") || infoHolder.openPrice.equals("0")) ? "0.0" : infoHolder.openPrice;
                    infoHolder.mid = (infoHolder.mid.equals("null") || infoHolder.mid.equals("0")) ? "0.0" : infoHolder.mid;
                    infoHolder.high = (infoHolder.high.equals("null") || infoHolder.high.equals("0")) ? "0.0" : infoHolder.high;
                    infoHolder.volume = (infoHolder.volume.equals("null") || infoHolder.volume.equals("0")) ? "0.0" : infoHolder.volume;
                    statsPrice.setText("Current Price: " + infoHolder.price);
                    statsLow.setText("Low: " + infoHolder.low);
                    statsBidPrice.setText("Bid Price: " + infoHolder.bidPrice);
                    statsOpenPrice.setText("Open Price: " + infoHolder.openPrice);
                    statsMid.setText("Mid: " + infoHolder.mid);
                    statsHigh.setText("High: " + infoHolder.high);
                    statsVolume.setText("Volume: " + formatVolume(Float.parseFloat(infoHolder.volume)));

                    if (pref.getFloat("portfolio_" + ticker, 0.00f) == 0.00f) {
                        marketValueText.setText("Start trading!");
                    } else {
                        marketValueText.setText("Market Value: $" + formatNum(pref.getFloat("portfolio_" + ticker, 0.00f) * infoHolder.priceNum, true));
                    }

                    //refresh every 15 seconds
//                    if (object.getString("market").equals("+")) {
//                        handler.postDelayed(runnable = () -> {
//                            handler.postDelayed(runnable, 15000);
////                            Toast.makeText(DetailActivity.this, ticker+": This method is run every 15 seconds", Toast.LENGTH_SHORT).show();
//                            makeRefreshCall(ticker);
//                        }, 15000);
                    handler = new Handler();
                    String TAG = getClass().getSimpleName();
                    handler.postDelayed(runnable = new Runnable() {
                        public void run() {
                            handler.postDelayed(runnable, 15000);
                            Log.d(TAG, ticker + ": This method is run every 15 seconds");
                            Toast.makeText(DetailActivity.this, ticker+": This method is run every 15 seconds", Toast.LENGTH_SHORT).show();
                            makeRefreshCall(ticker);
                        }
                    }, 15000);
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, error -> {
        });

        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/detailssmachart?=" + text, new Response.Listener<String>() {
//        APICall.make(this, "http://10.0.2.2:3000/detailssmachart?="+text, new Response.Listener<String>() {
            //            private String TAG = getClass().getSimpleName();
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                try {
                    chartList = new JSONArray(response);
                    JSONObject[] ohlc = new JSONObject[chartList.length()];
                    JSONObject[] volume = new JSONObject[chartList.length()];
                    for (int i=0; i < chartList.length(); i += 1) {
                        JSONObject temp = new JSONObject();
                        temp.put("date",chartList.getJSONObject(i).getString("date"));
                        temp.put("open",chartList.getJSONObject(i).getString("open"));
                        temp.put("high",chartList.getJSONObject(i).getString("high"));
                        temp.put("low",chartList.getJSONObject(i).getString("low"));
                        temp.put("close",chartList.getJSONObject(i).getString("close"));
                        ohlc[i] = temp;

                        JSONObject temp2 = new JSONObject();
                        temp2.put("date",chartList.getJSONObject(i).getString("date"));
                        temp2.put("volume",chartList.getJSONObject(i).getString("volume"));
                        volume[i] = temp2;
                    }

                    webView = findViewById(R.id.detail_webview);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl( "file:///android_asset/index.html");
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            if (url.equals("file:///android_asset/index.html")) {
                                webView.loadUrl( "javascript:setData('"+chartList+"')");
                                webView.loadUrl( "javascript:getSMACharts('"+ticker+"')");
                            }
                        }
                    });
                    progress.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.w("error in response", "Error: " + error.getMessage()));
    }

    private void makeRefreshCall(String text) {
        APICall.make(this, "http://xiaoyastockapp.us-east-1.elasticbeanstalk.com/detailsrefresh?=" + text, new Response.Listener<String>() {
//        APICall.make(this, "http://10.0.2.2:3000/detailsrefresh?="+text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    infoHolder.price = (object.getString("last").equals("null")) ? "0.0"
                            : object.getString("last");
                    infoHolder.priceNum = (object.getString("last").equals("null")) ? 0.00f
                            : Float.parseFloat(object.getString("last"));
                    infoHolder.changeNum = (object.getString("prevClose").equals("null")) ? infoHolder.priceNum
                            : infoHolder.priceNum - Float.parseFloat(object.getString("prevClose"));
                    infoHolder.change = formatNum(infoHolder.changeNum,true);

                    infoHolder.low = object.getString("low");
                    infoHolder.bidPrice = object.getString("bidPrice");
                    infoHolder.openPrice = object.getString("open");
                    infoHolder.mid = object.getString("mid");
                    infoHolder.high = object.getString("high");
                    infoHolder.volume = object.getString("volume");

                    textPrice.setText("$"+infoHolder.price);
                    if( infoHolder.change.charAt(0) == '-' ){
                        textChange.setTextColor(Color.parseColor("#9B4049"));
                        textChange.setText("-$" + infoHolder.change.substring(1));
                    }else{
                        textChange.setText("$" + infoHolder.change);
                        if (infoHolder.changeNum != 0.00f) {
                            textChange.setTextColor(Color.parseColor("#319C5E"));
                        }else{
                            textChange.setTextColor(Color.parseColor("#9C9C9C"));
                        }
                    }

                    infoHolder.low = (infoHolder.low.equals("null")||infoHolder.low.equals("0")) ? "0.0" : infoHolder.low;
                    infoHolder.bidPrice = (infoHolder.bidPrice.equals("null")||infoHolder.bidPrice.equals("0")) ? "0.0" : infoHolder.bidPrice;
                    infoHolder.openPrice = (infoHolder.openPrice.equals("null")||infoHolder.openPrice.equals("0")) ? "0.0" : infoHolder.openPrice;
                    infoHolder.mid = (infoHolder.mid.equals("null")||infoHolder.mid.equals("0")) ? "0.0" : infoHolder.mid;
                    infoHolder.high = (infoHolder.high.equals("null")||infoHolder.high.equals("0")) ? "0.0" : infoHolder.high;
                    infoHolder.volume = (infoHolder.volume.equals("null")||infoHolder.volume.equals("0")) ? "0.0" : infoHolder.volume;
                    statsPrice.setText("Current Price: "+infoHolder.price);
                    statsLow.setText("Low: "+infoHolder.low);
                    statsBidPrice.setText("Bid Price: "+infoHolder.bidPrice);
                    statsOpenPrice.setText("Open Price: "+infoHolder.openPrice);
                    statsMid.setText("Mid: "+infoHolder.mid);
                    statsHigh.setText("High: "+infoHolder.high);
                    statsVolume.setText("Volume: "+formatVolume(Float.parseFloat(infoHolder.volume)));

                    if (pref.getFloat("portfolio_" + ticker, 0.00f ) == 0.00f ) {
                        marketValueText.setText("Start trading!");
                    } else {
                        marketValueText.setText("Market Value: $"+ formatNum(pref.getFloat("portfolio_" + ticker, 0.00f )*infoHolder.priceNum,true));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, error -> {
        });
    }

    public boolean isFourDigitsNumeric(String str){
        String reg = "^[0-9]+(.[0-9]{4})?$";
        return str.matches(reg);
    }

    private float fourDigits(float f){
        return new BigDecimal(f).setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private float twoDigits(float f){
        return new BigDecimal(f).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private String formatNum(float f, boolean isTwoDigit) {
        if( isTwoDigit ){
            if( f < 1 ){
                DecimalFormat df = new DecimalFormat("0.00");
                return df.format(f);
            }
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(f);
        }
        if( f < 1 ){
            DecimalFormat df = new DecimalFormat("0.0000");
            return df.format(f);
        }
        DecimalFormat df = new DecimalFormat("#.0000");
        return df.format(f);
    }
    private String formatVolume(float f) {
        if( f < 1 ){
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(f);
        }
        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( runnable != null )
            handler.removeCallbacks(runnable);
    }
}