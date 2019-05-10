package com.okan.chatdigitus.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.okan.chatdigitus.MessageActivity;
import com.okan.chatdigitus.Model.Chat;
import com.okan.chatdigitus.Model.User;
import com.okan.chatdigitus.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;



    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent,int viewType)
    {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder (@NonNull final MessageAdapter.ViewHolder holder, final int position){
        Chat chat = mChat.get(position);


        if(chat.getType().equals("text")) {
            holder.show_message.setText(chat.getMessage());

        }
        else if(chat.getType().equals("image")){
            holder.show_image.setImageResource(R.mipmap.image);
            holder.show_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(mChat.get(position).getMessage())));
                    holder.show_image.getContext().startActivity(intent);
                }
            });

        }
        else if(chat.getType().equals("PDF")){
            holder.show_image.setImageResource(R.mipmap.documentation);
            holder.show_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(mChat.get(position).getMessage())));
                    holder.show_image.getContext().startActivity(intent);
                }
            });
        }
        else{
            chat.getType().equals("video");
            holder.show_image.setImageResource(R.mipmap.video);
            holder.show_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(mChat.get(position).getMessage())));
                    holder.show_image.getContext().startActivity(intent);
                }
            });
        }



    }

    @Override
    public int getItemCount () {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView show_image;


        public ViewHolder(View itemView) {
            super(itemView);
            show_image = itemView.findViewById(R.id.show_image);
            show_message = itemView.findViewById(R.id.show_message);

        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }
}