package com.example.instantmessages.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instantmessages.R;
import com.example.instantmessages.messaging.CreateNewGroupActivity;

import java.util.ArrayList;
import java.util.List;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.ViewHolder> {
    private Context context;
    private List<User> mList;

    public UserSelectionAdapter(Context context, List<User> mList) {
        this.context = context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context cnt = parent.getContext();;
        LayoutInflater inflater = LayoutInflater.from(cnt);

        View userView = inflater.inflate(R.layout.user_select_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(userView);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mList.get(position);
        holder.name.setText(user.getName());
        Glide.with(context)
                .load(user.getProfileURL())
                .into(holder.profImg);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setChecked(!user.isChecked());
                holder.check.setVisibility(user.isChecked()?View.VISIBLE:View.INVISIBLE);
            }
        });
    }
    public List<User> getSelectedUsers(){
        List<User> t = new ArrayList<>();
        for(User user : mList){
            if(user.isChecked()){
                t.add(user);
            }
        }
        return t;
    }
    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView check,profImg;
        private TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view= itemView;
            check = (ImageView) view.findViewById(R.id.selected_or_not_new_group);
            profImg = (ImageView) view.findViewById(R.id.prof_img_new_group);
            name = (TextView) view.findViewById(R.id.name_user_new_group);
            check.setVisibility(View.INVISIBLE);
        }
    }
}
