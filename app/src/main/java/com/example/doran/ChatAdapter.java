package com.example.doran;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<Chatdata> Chatdatalist;
    String shared_Email ;

    public ChatAdapter(ArrayList<Chatdata> chatdatalist, String shared_Email) {
        /*Chatting_Activity에서 어댑터에 이메일값 넣어주고 어댑터 안 생성자에 이메일 추가 */
        Chatdatalist = chatdatalist;

        //어댑터에 매개변수로 넣었던 그 이메일을 이 클래스에서 사용한다 라는 뜻
        this.shared_Email = shared_Email;

    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView Chat_name,Chat_text,Chat_time;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            Chat_name =(TextView)itemView.findViewById(R.id.Chat_name);  // 채팅시 이름
            Chat_text =(TextView)itemView.findViewById(R.id.Chat_text);  // 채팅시 내용
            Chat_time =(TextView)itemView.findViewById(R.id.Chat_time);  // 채팅시 시간
        }
    }

    /*하나의 리사이클러뷰에서 여러개의 아이템 뷰 사용 가능하게 해주는 메소드.*/
    @Override
    public int getItemViewType(int position) {
        Log.d("어댑터안임", "이메일: "+shared_Email);
        //로그인 화면에서 받아온 이메일이 채팅 데이터베이스 안에 들어있는 이메일과 일치 하면 내꺼, 오른쪽에 배치
        if(Chatdatalist.get(position).Chatdata_email.equals(shared_Email)){
            return 1;
        }else {
            //나중에 상대방 이메일도 추가하면 되지 않을까?
            return 2;
        }

    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view,parent,false);
        if (viewType ==1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view2,parent,false);
        }

        ChatViewHolder chatViewHolder = new ChatViewHolder(view);
        return chatViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.Chat_name.setText(Chatdatalist.get(position).getChatdata_name());
        holder.Chat_text.setText(Chatdatalist.get(position).getChatdata_text());
        holder.Chat_time.setText(Chatdatalist.get(position).getChatdata_time());
    }

    @Override
    public int getItemCount() {
        return Chatdatalist.size();
    }

    /*채팅 리사이클러뷰에 . 추가메소드*/
    public void addChat(Chatdata chatdata){
        Chatdatalist.add(chatdata);
        notifyItemInserted(Chatdatalist.size()-1); //갱신용
    }
}

