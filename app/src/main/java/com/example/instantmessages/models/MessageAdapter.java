package com.example.instantmessages.models;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter{
    private List<Message> msgList;
    private Context context;
    private String adapterType;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public MessageAdapter(List msgList,Context context,String adapterType){
        this.msgList=msgList;
        this.context=context;
        this.adapterType = adapterType;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if( viewType==0 || viewType == 2) {
            View view = inflater.inflate(R.layout.message_item_from_me, parent, false);
            ViewHolderMe viewHolderMe = new ViewHolderMe(view);
            return viewHolderMe;
        }
        View view1 = inflater.inflate(R.layout.message_item_from_user,parent,false);
        ViewHolderUser viewHolderUser = new ViewHolderUser(view1);
        return viewHolderUser;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = msgList.get(position);
        DatabaseReference ref = FirebaseDatabase.getInstance(context.getResources().getString(R.string.databaseURL)).getReference();
        ref.child("users").child(msg.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(msg.getFrom().equals(mAuth.getUid())){
                    ViewHolderMe viewHolderMe = (ViewHolderMe) holder;
                    Glide.with(context)
                            .load(user.getProfileURL())
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                            .into(viewHolderMe.img);
                    if(msg.isAttached()){
                        viewHolderMe.msg.setVisibility(View.GONE);
                        Glide.with(context)
                                .load(msg.getImageUrl())
                                .apply(new RequestOptions().override(500,500))
                                .into(viewHolderMe.msgImg);
                    }else{
                        viewHolderMe.msg.setText(msg.getMessage());
                    }
                }else{
                    ViewHolderUser viewHolderUser = (ViewHolderUser) holder;
                    viewHolderUser.name.setText(user.getName());
                    Glide.with(context)
                            .load(user.getProfileURL())
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                            .into(viewHolderUser.img);
                    if(msg.isAttached()){
                        viewHolderUser.msg.setVisibility(View.GONE);
                        Glide.with(context)
                                .load(msg.getImageUrl())
                                .apply(new RequestOptions().override(500,500))
                                .into(viewHolderUser.msgImg);
                    }else{
                        viewHolderUser.msg.setText(msg.getMessage());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public int getItemCount() {
        return msgList.size();
    }
    @Override
    public int getItemViewType(int position) {
        Message msg = msgList.get(position);
        if (msg.isAttached()) {
            if (msg.getFrom().equals(mAuth.getUid())) {
                return 2;
            }
            return 3;
        } else if (!(msg.isAttached())) {
            if (msg.getFrom().equals(mAuth.getUid())) {
                return 0;
            }
            return 1;
        }
        return 0;
    }

    class ViewHolderMe extends RecyclerView.ViewHolder{
        private ImageView img,msgImg;
        private TextView msg;
        private View view;

        public ViewHolderMe(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            msgImg = (ImageView) view.findViewById(R.id.msg_img_from_me);
            img = (ImageView) view.findViewById(R.id.img_me_from_me);
            msg = (TextView) view.findViewById(R.id.msg_from_me);
        }
    }
    class ViewHolderUser extends RecyclerView.ViewHolder{
        private ImageView img,msgImg;
        private TextView msg,name;
        private View view;

        public ViewHolderUser(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            msgImg =(ImageView) view.findViewById(R.id.msg_img_user_from_user);
            img = (ImageView) view.findViewById(R.id.img_user_from_user);
            msg = (TextView) view.findViewById(R.id.msg_user_from_user);
            name = (TextView) view.findViewById(R.id.name_user_from_user);
            if(!adapterType.equals("groupChat")){
                name.setVisibility(View.GONE);
            }
        }
    }
}
