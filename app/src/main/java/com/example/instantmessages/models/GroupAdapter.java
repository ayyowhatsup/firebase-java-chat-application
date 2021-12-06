package com.example.instantmessages.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instantmessages.R;
import com.example.instantmessages.messaging.ChatLogActivity;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    @NonNull
    private List<Group> groupList;
    private Context context;

    public GroupAdapter(List list,Context context){
        this.groupList=list;
        this.context=context;
    }


    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context cnt = parent.getContext();;
        LayoutInflater inflater = LayoutInflater.from(cnt);

        View groupView = inflater.inflate(R.layout.group_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(groupView);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group =  groupList.get(position);
        holder.nameDisplay.setText(group.getGroupName());
        Glide.with(context)
                .load(group.getGroupImageURL())
                .into(holder.imgDisplay);
        holder.nameDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatLogActivity.class);
                intent.putExtra("ID",group.getGroupID());
                intent.putExtra("chatType","groups");
                view.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class  ViewHolder extends RecyclerView.ViewHolder{
        private Button nameDisplay;
        private ImageView imgDisplay;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            nameDisplay = (Button) view.findViewById(R.id.group_name_display);
            imgDisplay = (ImageView) view.findViewById(R.id.group_image_display);
        }
    }
}
