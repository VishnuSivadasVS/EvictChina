package com.vishnusivadas.evictchina.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vishnusivadas.evictchina.R;
import com.vishnusivadas.evictchina.listner.OnItemClickListener;
import com.vishnusivadas.evictchina.model.AppInfo;
import java.util.List;

public class ItemlistAdapter extends RecyclerView.Adapter<ItemlistAdapter.ViewHolder> {
    private List<AppInfo> appInfos;
    private final OnItemClickListener onItemClickListener;

    public ItemlistAdapter(List<AppInfo> list, OnItemClickListener onItemClickListener2) {
        this.appInfos = list;
        this.onItemClickListener = onItemClickListener2;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_item, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final AppInfo item = getItem(i);
        viewHolder.appsName.setText(item.appName);
        viewHolder.size.setText(item.size);
        viewHolder.img.setImageDrawable(item.icon);
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                onItemClickListener.onItemClick(i);
            }
        });
    }

    private AppInfo getItem(int i) {
        return this.appInfos.get(i);
    }

    public int getItemCount() {
        return this.appInfos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView appsName;
        final ImageView delete;
        final ImageView img;
        final TextView size;

        ViewHolder(View view) {
            super(view);
            this.appsName = (TextView) view.findViewById(R.id.appsName);
            this.size = (TextView) view.findViewById(R.id.size);
            this.img = (ImageView) view.findViewById(R.id.img);
            this.delete = (ImageView) view.findViewById(R.id.delete);
        }
    }
}