package com.example.doran;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DdayAdapter extends RecyclerView.Adapter<DdayAdapter.DdayViewHolder> {
    //데이터 삭제
    FirebaseDatabase database;
    String coupleKey;


    public ArrayList<DdayData> DdayDataList;
    Context context;
//    OnDdayItemClickListener listener;


    /*롱클릭*/
    //커스텀 리스너 인터페이스 정의
    public interface OnListItemLongSelectedInterface {
        void onItemLongClick(View v, int position); //메인에서 이 메소드를 오버라이딩
    }

    //리스너 객체를 전달하는 메소드 와 전달될 객체를 저장할 변수 추가(mlistener)
    private OnListItemLongSelectedInterface mLongListener;

//    public void setOnItemLongClickListener(OnItemLongClickListener listener){
//        this.mlonglistener = listener;
//    }

    public DdayAdapter(ArrayList<DdayData> DdayDataList,Context context,OnListItemLongSelectedInterface longListener) {
        this.DdayDataList = DdayDataList;
        this.context = context;
        this.mLongListener = longListener;
    }


    public static class DdayViewHolder extends RecyclerView.ViewHolder{

        TextView Ddaydata_DateName;
        TextView Ddaydata_Date;
        TextView Ddaydata_Dday;
        public DdayViewHolder(@NonNull View itemView, final OnListItemLongSelectedInterface longListener) { // final OnDdayItemClickListener listener
            super(itemView);
            Ddaydata_DateName = (TextView)itemView.findViewById(R.id.Ddaydata_DateName);
            Ddaydata_Date = (TextView)itemView.findViewById(R.id.Ddaydata_Date);
            Ddaydata_Dday = (TextView)itemView.findViewById(R.id.Ddaydata_Dday);

            //나중에 클릭리스너 다는 위치
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int pos = getAdapterPosition(); // 이 뷰홀더에 표시할 아이템이 어댑터에서 몇번째 정보인지 알려줌
//                    if(listener !=null){
//                        listener.onItemClick(DdayViewHolder.this,v,pos);
//                    }
//                }
//            });

            /*롱클릭*/
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();

                    longListener.onItemLongClick(v,pos);

                    return false;
                }
            });

        }
    }

    @NonNull
    @Override
    /*레이아웃 연결*/
    public DdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.d_day_recyledata,parent,false);
        DdayViewHolder ddayViewHolder = new DdayViewHolder(view,mLongListener); //listener
        return ddayViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DdayViewHolder holder, int position) {
        holder.Ddaydata_DateName.setText(DdayDataList.get(position).getDdaydata_DateName());
        holder.Ddaydata_Date.setText(DdayDataList.get(position).getDdaydata_Date());
        holder.Ddaydata_Dday.setText(DdayDataList.get(position).getDdaydata_Dday());

    }



    @Override
    public int getItemCount() {
        return DdayDataList.size();
    }

    /*아이템뷰클릭리스너*/
//    public void onItemClick(DdayViewHolder holder, View view, int position) {
//        //OnItemClick은 뷰홀더 클래스 안에서 뷰가 클릭됬을때 호출되는 메서드
//        // 그런데 밖에서 이벤트처리를 하는 것이 일반적이므로 listener 변수를 하나 선언하고
//        if (listener != null) {
//            listener.onItemClick(holder, view, position);
//        }
//    }
//
//    public void setOnItemClickListener(OnDdayItemClickListener listener){
//        //외부에서 리스너를 설정할 수 있도록 하는 메서드
//        this.listener = listener;
//    }






    /*컨텍스트메뉴*/
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        // 3. 컨텍스트 메뉴를 생성하고 메뉴 항목 선택시 호출되는 리스너를 등록해줍니다. ID 1001, 1002로 어떤 메뉴를 선택했는지 리스너에서 구분하게 됩니다.
//        MenuItem delete = menu.add(Menu.NONE,1001,1,"삭제");
//        delete.setOnMenuItemClickListener(onDeleteMenu);
//
//    }
//
//    private final MenuItem.OnMenuItemClickListener onDeleteMenu = new MenuItem.OnMenuItemClickListener() {
//        @Override
//        public boolean onMenuItemClick(MenuItem item) {
//            if (item.getItemId()==1001){
//
//
//            }
//            return true;
//        }
//
//    };



}
