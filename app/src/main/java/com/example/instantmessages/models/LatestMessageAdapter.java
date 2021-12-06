package com.example.instantmessages.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.instantmessages.R;
import com.example.instantmessages.messaging.ChatLogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LatestMessageAdapter extends RecyclerView.Adapter<LatestMessageAdapter.ViewHolder> {
    private List<Message> latestMessages;
    private Context context;

    public LatestMessageAdapter(List latestMessages, Context context){
        this.context = context;
        this.latestMessages = latestMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context cnt = parent.getContext();;
        LayoutInflater inflater = LayoutInflater.from(cnt);
        View messageView = inflater.inflate(R.layout.lastest_message_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(messageView);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String myID = FirebaseAuth.getInstance().getUid();
        Message message = latestMessages.get(position);
        if(message.getType().equals("singleChat")){
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChatLogActivity.class);
                    intent.putExtra("ID",message.getReceiveId());
                    intent.putExtra("chatType","users");
                    context.startActivity(intent);
                }
            });
            String thisPersonId = message.getFrom().equals(myID) ? message.getReceiveId() : message.getFrom();
            FirebaseDatabase.getInstance(context.getResources().getString(R.string.databaseURL))
                    .getReference()
                    .child("users").child(thisPersonId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            holder.name.setText(user.getName());
                            Glide.with(context).load(user.getProfileURL())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                                    .into(holder.img);
                            if(message.isAttached()){
                                holder.msg.setText("[Hình ảnh]");
                            }else{
                                holder.msg.setText(message.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else{
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChatLogActivity.class);
                    intent.putExtra("ID",message.getReceiveId());
                    intent.putExtra("chatType","groups");
                    context.startActivity(intent);
                }
            });
            String thisGroupId = message.getReceiveId();
            FirebaseDatabase.getInstance(context.getResources().getString(R.string.databaseURL))
                    .getReference().child("groups").child(thisGroupId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Group group = snapshot.getValue(Group.class);
                            holder.name.setText(group.getGroupName());
                            Glide.with(context).load(group.getGroupImageURL())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                                    .into(holder.img);
                            if(message.isAttached()){
                                holder.msg.setText("[Hình ảnh]");
                            }
                            else{
                                holder.msg.setText(message.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return latestMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView img;
        private TextView name;
        private TextView msg;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            img = (ImageView) view.findViewById(R.id.avatar_img_view_ltmsg_item);
            name = (TextView) view.findViewById(R.id.name_text_view_ltmsg_item);
            msg = (TextView) view.findViewById(R.id.message_text_view_ltmsg_item);
        }
    }
}
