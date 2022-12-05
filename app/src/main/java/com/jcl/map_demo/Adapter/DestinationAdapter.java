package com.jcl.map_demo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcl.map_demo.R;
import com.jcl.map_demo.Search_result_Activity;
import com.jcl.map_demo.pojo.DestinationInfo;
import com.mapbox.geojson.Point;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private List<DestinationInfo> list;
    private Context context;

    public DestinationAdapter(List<DestinationInfo> list, Context context) {
        this.list = list;
        this.context = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_name;
        private TextView tv_address;
        private TextView tv_distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_address = itemView.findViewById(R.id.tv_address);
            tv_distance = itemView.findViewById(R.id.tv_distance);


        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DestinationInfo destinationInfo = list.get(position);
        String address = destinationInfo.getAddress();
        String distance = destinationInfo.getDistance();
        Point point = destinationInfo.getPoint();
        String name = destinationInfo.getName();
        holder.tv_address.setText(address);
        holder.tv_distance.setText(distance);
        holder.tv_name.setText(name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Search_result_Activity.class);
                intent.putExtra("longitude",String.valueOf(point.longitude()));
                intent.putExtra("latitude",String.valueOf(point.latitude()));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
