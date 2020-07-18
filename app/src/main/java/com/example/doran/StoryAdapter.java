package com.example.doran;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> implements OnStoryItemClickListener {
    public ArrayList<StoryData> StoryDataList;
    Context context;
    OnStoryItemClickListener listener;
    // 어댑터에서 액티비티 액션을 가져올때 context가 필요한데 어댑터에는 context가 없어서
    //선택한 액티비티에 대한 context를 가져올때 필요하다.
    public StoryAdapter(ArrayList<StoryData> storyDataList,Context context) {
        this.StoryDataList = storyDataList;
        this.context = context;
    }


    static class StoryViewHolder extends RecyclerView.ViewHolder{
        ImageView Storydata_Image;
        TextView Storydata_Date;
        TextView Storydata_Name;
        TextView Storydata_Content;

        public StoryViewHolder(@NonNull View itemView, final OnStoryItemClickListener listener) {
            // 1. 뷰홀더 객체의 생성자가 호출될 때 리스너 객체가 파라미터로 전달되로록 추가,
            // 뷰가 클릭되었을 때 리스너객체의 OnItemclick 이벤트 호출
            super(itemView);
            Storydata_Image = (ImageView)itemView.findViewById(R.id.Storydata_Image);
            Storydata_Date = (TextView)itemView.findViewById(R.id.Storydata_Date);
            Storydata_Name = (TextView)itemView.findViewById(R.id.Storydata_Name);
            Storydata_Content = (TextView)itemView.findViewById(R.id.Storydata_Content);

            //2. 뷰홀더에 클릭리스너 달기, 파라미터에 리스너 추가
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition(); // 이 뷰홀더에 표시할 아이템이 어댑터에서 몇번째 정보인지 알려줌
                    if(listener !=null){
                        listener.onItemClick(StoryViewHolder.this,v, pos);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_recycledata,parent,false);
        StoryViewHolder storyViewHolder = new StoryViewHolder(view,listener);
        return storyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.Storydata_Date.setText(StoryDataList.get(position).getStorydata_date());
        holder.Storydata_Name.setText(StoryDataList.get(position).getStorydata_name());
        holder.Storydata_Content.setText(StoryDataList.get(position).getStorydata_context());
        Glide.with(holder.itemView).load(StoryDataList.get(position).getStorydata_image()).fitCenter()
                .into(holder.Storydata_Image); // 이미지 로드 가능

        //혹시 인트형을 넣는일이 있으면
        //holder.password(텍스트뷰).setText(String.valueOf(데이터리스트.get(position).getter메소드))
        //이렇게 String값으로 변환해서 넣음
    }

    @Override
    public int getItemCount() {
        return (StoryDataList!=null ? StoryDataList.size():0);
        //StoryDatalist가 null이 아니면 size를 가져오고 아니면 0을 가져온다.
    }



    @Override
    public void onItemClick(StoryViewHolder holder, View view, int position) {
        //OnItemClick은 뷰홀더 클래스 안에서 뷰가 클릭됬을때 호출되는 메서드
        // 그런데 밖에서 이벤트처리를 하는 것이 일반적이므로 listener 변수를 하나 선언하고
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClickListener(OnStoryItemClickListener listener){
        //외부에서 리스너를 설정할 수 있도록 하는 메서드
        this.listener = listener;
    }
}
