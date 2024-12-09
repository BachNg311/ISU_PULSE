package com.coms309.isu_pulse_frontend.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.coms309.isu_pulse_frontend.R;
import com.coms309.isu_pulse_frontend.api.UpdateAccount;
import com.coms309.isu_pulse_frontend.chat_system.ChatActivity;
import com.coms309.isu_pulse_frontend.chat_system.ChatMessage;
import com.coms309.isu_pulse_frontend.chat_system.GroupChatActivity;
import com.coms309.isu_pulse_frontend.loginsignup.UserSession;
import com.coms309.isu_pulse_frontend.model.Profile;
import com.coms309.isu_pulse_frontend.ui.ask_ai.AskAiActivity;

import java.util.List;

public class AskAiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ONE_ON_ONE = 1;

    private List<ChatMessage> chatMessages;

    public AskAiAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ONE_ON_ONE; // Always one-on-one for AI chat
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new AskAiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        if (holder instanceof AskAiViewHolder) {
            AskAiViewHolder viewHolder = (AskAiViewHolder) holder;
            viewHolder.textViewName.setText("ChatGPT");
            viewHolder.textViewMessage.setText(chatMessage.getMessage());
            viewHolder.textViewTimestamp.setText(chatMessage.getTimestamp());
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.chatgpt_100)
                    .into(viewHolder.imageViewProfile);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class AskAiViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewMessage;
        ImageView imageViewProfile;
        TextView textViewTimestamp;

        public AskAiViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.name);
            textViewMessage = itemView.findViewById(R.id.last_message);
            imageViewProfile = itemView.findViewById(R.id.profile_image);
            textViewTimestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}

