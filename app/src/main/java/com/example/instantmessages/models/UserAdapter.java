package com.example.instantmessages.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instantmessages.R;
import com.example.instantmessages.messaging.ChatLogActivity;
import com.google.firebase.auth.FirebaseAuth;


import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    @NonNull
    private List userlist;
    private int type;
    private Context context;
    private OnClickSet onClick;

    public UserAdapter(List list,Context context,int type){
        this.userlist=list;
        this.context=context;
        this.type = type;
    }


    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context cnt = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(cnt);
        View userView = inflater.inflate(R.layout.user_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(userView);
        return  viewHolder;
    }

    public void addInterface(OnClickSet onClick){
        this.onClick = onClick;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = (User) userlist.get(position);
        holder.nameDisplay.setText(user.getName());
        Glide.with(context)
                .load(user.getProfileURL())
                .into(holder.imgDisplay);
        holder.nameDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatLogActivity.class);
                intent.putExtra("ID",user.getId());
                intent.putExtra("chatType","users");
                view.getContext().startActivity(intent);
            }
        });
        if(user.getId().equals(FirebaseAuth.getInstance().getUid())){
            holder.menu.setVisibility(View.GONE);
        }
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(R.menu.member_manage_edit_group_menu,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id){
                            case R.id.give_admin_permission:
                                onClick.giveGroupKey(user);
                                break;
                            case R.id.delete_member:
                                onClick.deleteUser(user);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return userlist.size();
    }


    class  ViewHolder extends RecyclerView.ViewHolder{
        private Button nameDisplay;
        private ImageView imgDisplay;
        private ImageView menu;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            nameDisplay = (Button) view.findViewById(R.id.usernameDisplay);
            imgDisplay = (ImageView) view.findViewById(R.id.userprofileImg);
            menu = (ImageView) view.findViewById(R.id.user_item_menu_group);
            menu.setVisibility(type==2?View.VISIBLE:View.GONE);
            //Nếu mà type = 2 thì adapter sử dụng trong cái edit group (hiện ra cái menu mời làm admin/ xóa khỏi nhóm), type = 1
            //thì dùng trong hiển thị danh sách thành viên, danh sách người, ẩn cái menu này đi
        }
    }
    public interface OnClickSet{
        void giveGroupKey(User user);
        void deleteUser(User user);
    }
}
