package com.tl.film.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tl.film.R;
import com.tl.film.model.FirstFilms_Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiaoping on 2017/3/28.
 */

public class RecyclerCoverFlow_Adapter extends RecyclerView.Adapter<RecyclerCoverFlow_Adapter.ViewHolder> {

    private Context mContext;

    List<FirstFilms_Model.DataBean>  dataBeans = new ArrayList<>();

    public void setDataBeans(List<FirstFilms_Model.DataBean> dataBeans) {
        this.dataBeans = dataBeans;
    }

    private ItemClick clickCb;

    public RecyclerCoverFlow_Adapter(Context c) {
        mContext = c;
    }

    public RecyclerCoverFlow_Adapter(Context c, ItemClick cb) {
        mContext = c;
        clickCb = cb;
    }

    public void setOnClickLstn(ItemClick cb) {
        this.clickCb = cb;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FirstFilms_Model.DataBean bean = dataBeans.get(position);
        Glide.with(mContext).load(bean.getBgImage()).into(holder.img);
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(mContext, "点击了：" + position, Toast.LENGTH_SHORT).show();
            if (clickCb != null) {
                clickCb.clickItem(position);
            }
        });
        holder.img.setTag(position);

    }

    @Override
    public int getItemCount() {
        return dataBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            //item可以获得焦点，需要设置这个属性。
//            itemView.setFocusable(true);
//            itemView.setFocusableInTouchMode(true);
        }

    }

    interface ItemClick {
        void clickItem(int pos);
    }
}