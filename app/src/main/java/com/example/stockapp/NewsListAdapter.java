package com.example.stockapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;

public class NewsListAdapter extends SectionedRecyclerViewAdapter< NewsListAdapter.HeaderViewHolder, NewsListAdapter.ItemViewHolder, RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;

    private ArrayList<String[]> info;

    class ItemViewHolder extends RecyclerView.ViewHolder{
        public TextView title, date, publisher;
        public ImageView img;
        public ItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.news_title);
            date = view.findViewById(R.id.news_date);
            publisher = view.findViewById(R.id.news_publisher);
            img = view.findViewById(R.id.news_image);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder{
        public TextView title, date, publisher;
        public ImageView img;
        public HeaderViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.news_first_title);
            date = view.findViewById(R.id.news_first_date);
            publisher = view.findViewById(R.id.news_first_publisher);
            img = view.findViewById(R.id.news_first_image);
        }
    }

    public NewsListAdapter(Context context, ArrayList<String[]> array) {
        this.context = context;
        this.info = array;
        inflater = LayoutInflater.from(context);
    }

    @Override
    protected int getSectionCount() {
        return 1;
    }

    @Override
    protected int getItemCountForSection(int section) {
//        return (section==0) ? 1 : info.size()-1 ;
        return info.size();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected NewsListAdapter.HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.news_header, parent, false);
        return new  NewsListAdapter.HeaderViewHolder(view);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected NewsListAdapter.ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.news_card, parent, false);
        return new  NewsListAdapter.ItemViewHolder(view);
    }

    @Override
    protected void onBindSectionHeaderViewHolder(NewsListAdapter.HeaderViewHolder holder, int section) {
        Glide.with(context).load(info.get(section)[0]).apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(40))).into(holder.img);
        holder.publisher.setText(info.get(section)[1]);
        holder.date.setText(info.get(section)[2]);
        holder.title.setText(info.get(section)[3]);
    }

    @Override
    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder viewHolder, int section) {
    }

    @Override
    protected void onBindItemViewHolder(NewsListAdapter.ItemViewHolder holder, int section, int position) {
        Glide.with(context).load(info.get(position+1)[0]).apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(40))).into(holder.img);
        holder.publisher.setText(info.get(position+1)[1]);
        holder.date.setText(info.get(position+1)[2]);
        holder.title.setText(info.get(position+1)[3]);
    }

    @Override
    public int getItemCount() {
        return info.size();
    }
}
