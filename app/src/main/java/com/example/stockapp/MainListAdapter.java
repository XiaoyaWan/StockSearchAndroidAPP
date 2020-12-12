package com.example.stockapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class MainListAdapter extends SectionedRecyclerViewAdapter< MainListAdapter.HeaderViewHolder, MainListAdapter.ItemViewHolder, RecyclerView.ViewHolder> implements MyItemTouchHelperCallback.ItemTouchHelperAdapter{

    private Context context;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String[]> favoriteList;
    private ArrayList<String[]> portfolioList;
    private String netWorth;
    private String date;
    private ItemClickListener mItemClickListener;
//    private ItemClickListener[] fItemClickListener;

    public MainListAdapter(Context context, ArrayList<String[]> favoriteList, ArrayList<String[]> portfolioList, float netWorth, String date) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.portfolioList = portfolioList;
        float worth = 0;
        for( int i = 0; i < this.portfolioList.size(); i++ ){
            worth += Float.parseFloat(this.portfolioList.get(i)[2]) * Float.parseFloat(this.portfolioList.get(i)[1]);
        }
        this.netWorth = formatNum(worth + netWorth);
        this.date = date;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCountForSection(int section) {
        return ( section == 0 ) ? portfolioList.size() : favoriteList.size();
    }

    @Override
    protected int getSectionCount() {
        return 2;
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    protected LayoutInflater getLayoutInflater(){
        return LayoutInflater.from(context);
    }

    @Override
    protected MainListAdapter.HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.list_item_banner_main, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected MainListAdapter.ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.list_item_main, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindSectionHeaderViewHolder(MainListAdapter.HeaderViewHolder holder, int section) {
        switch (section){
            case 0:
                holder.date.setText(date);
                holder.banner.setText("PORTFOLIO");
                holder.title.setText("Net Worth");
                holder.netWorth.setText(netWorth);
                break;
            case 1:
                holder.date.setVisibility(View.GONE);
                holder.banner.setText("FAVORITE");
                holder.netWorthSection.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {
    }

    public void addItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    protected void onBindItemViewHolder(MainListAdapter.ItemViewHolder holder, int section, int position) {
        switch (section){
            case 0:
                holder.ticker.setText(portfolioList.get(position)[0]);
                holder.subticker.setText(formatNum(Float.parseFloat(portfolioList.get(position)[1]))+" shares");
                holder.price.setText(portfolioList.get(position)[2]);
                holder.change.setText(portfolioList.get(position)[3]);
                if(portfolioList.get(position)[4].equals("+")){
                    holder.trend.setVisibility(View.VISIBLE);
//                    Picasso.with(context).load(R.drawable.ic_twotone_trending_up_24).into(holder.trend);
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.ic_twotone_trending_up_24)).into(holder.trend);
                    holder.change.setTextColor(Color.parseColor("#319C5E"));
                }else if(portfolioList.get(position)[4].equals("-")){
                    holder.trend.setVisibility(View.VISIBLE);
                    holder.change.setText(portfolioList.get(position)[3].substring(1));
//                    Picasso.with(context).load(R.drawable.ic_baseline_trending_down_24).into(holder.trend);
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.ic_baseline_trending_down_24)).into(holder.trend);
                    holder.change.setTextColor(Color.parseColor("#9B4049"));
                }else{
                    holder.change.setText(portfolioList.get(position)[3]);
                    holder.trend.setVisibility(View.INVISIBLE);
                    holder.change.setTextColor(Color.parseColor("#9C9C9C"));
                }
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemClick(portfolioList.get(position)[0]);
                        }
                    }
                });
                break;
            case 1:
                holder.ticker.setText(favoriteList.get(position)[0]);
                if( isNumeric(favoriteList.get(position)[1]) ){
                    holder.subticker.setText(formatNum(Float.parseFloat(favoriteList.get(position)[1]))+" shares");
                }else{
                    holder.subticker.setText(favoriteList.get(position)[1]);
                }
                holder.price.setText(favoriteList.get(position)[2]);
                holder.change.setText(favoriteList.get(position)[3]);
                if(favoriteList.get(position)[4].equals("+")){
                    holder.trend.setVisibility(View.VISIBLE);
//                    Picasso.with(context).load(R.drawable.ic_twotone_trending_up_24).into(holder.trend);
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.ic_twotone_trending_up_24)).into(holder.trend);
                    holder.change.setTextColor(Color.parseColor("#319C5E"));
                }else if(favoriteList.get(position)[4].equals("-")){
                    holder.trend.setVisibility(View.VISIBLE);
                    holder.change.setText(favoriteList.get(position)[3].substring(1));
//                    Picasso.with(context).load(R.drawable.ic_baseline_trending_down_24).into(holder.trend);
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.ic_baseline_trending_down_24)).into(holder.trend);
                    holder.change.setTextColor(Color.parseColor("#9B4049"));
                }else{
                    holder.change.setText(portfolioList.get(position)[3]);
                    holder.trend.setVisibility(View.INVISIBLE);
                    holder.change.setTextColor(Color.parseColor("#9C9C9C"));
                }
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemClick(favoriteList.get(position)[0]);
                        }
                    }
                });
                break;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder{
        public TextView banner, title, netWorth, date;
        public View netWorthSection;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.list_banner);
            title = itemView.findViewById(R.id.list_title);
            netWorth = itemView.findViewById(R.id.list_net_worth);
            date = itemView.findViewById(R.id.list_date);
            netWorthSection = itemView.findViewById(R.id.list_net_worth_section);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        public TextView ticker, subticker, price, change, button;
        public ImageView trend;
        public View itemView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.list_ticker);
            subticker = itemView.findViewById(R.id.list_subticker);
            price = itemView.findViewById(R.id.list_price);
            change = itemView.findViewById(R.id.list_change);
            trend = itemView.findViewById(R.id.list_trend);
            button = itemView.findViewById(R.id.list_button);
            this.itemView = itemView;
        }
        public int getSection(int section) {
            return ( section == 0 ) ? portfolioList.size() : favoriteList.size();
        }
    }

    @Override
    public void onItemDismiss(int position) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("favorite_"+favoriteList.get(position - getItemCountForSection(0) - 2)[0]);
        editor.apply();
        favoriteList.remove(position - getItemCountForSection(0) - 2);
        notifyItemRemoved(position);
    }

    @Override
    public void onClearView(ItemViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
    }

    @Override
    public void onSelectView(ItemViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.parseColor("#ECECEC"));
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition, int section) {
        int section1 = getItemCountForSection(0);
        int section2 = getItemCountForSection(1);

        switch ( section ) {
            case 0:
                if ( ( toPosition < section1 + 1 ) ) {
                    if ( toPosition < 1 ) {
                        toPosition = 1;
                    } else {
                        if (fromPosition < toPosition) {
                            for (int i = fromPosition; i < toPosition; i++) {
                                Collections.swap(portfolioList, i - 1 , i - 1 + 1);
                            }
                        } else {
                            for (int i = fromPosition; i > toPosition; i--) {
                                Collections.swap(portfolioList, i - 1 , i - 1 - 1);
                            }
                        }
                    }
                    notifyItemMoved(fromPosition, toPosition);
                }
                break;
            case 1:
                if ( ( toPosition < section1 + section2 + 2 ) ){
                    if ( toPosition < section1 + 2 ) {
                        toPosition = section1 + 2;
                    }else{
                        if (fromPosition < toPosition) {
                            for (int i = fromPosition; i < toPosition; i++) {
                                Collections.swap(favoriteList, i - section1 - 2 , i - section1 - 2 + 1);

                            }
                        } else {
                            for (int i = fromPosition; i > toPosition; i--) {
                                Collections.swap(favoriteList, i - section1 - 2 , i - section1 - 2 - 1);
                            }
                        }
                    }
                    notifyItemMoved(fromPosition, toPosition);
                }
                break;
        }
    }

    public ArrayList<String[]> getPortfolioList() {
        return portfolioList;
    }

    public ArrayList<String[]> getFavoriteList() {
        return favoriteList;
    }

    private String formatNum(float f) {
        if( f < 1 ){
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(f);
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(f);
    }
    public boolean isNumeric(String str){
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }

    public interface ItemClickListener {
        void onItemClick(String query);
    }
}
