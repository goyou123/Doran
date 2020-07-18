package com.example.doran;

import android.view.View;

public interface OnDdayItemClickListener {
    public void onItemClick(DdayAdapter.DdayViewHolder holder, View view, int position);
    //onItemClick()메소드가 실행될때마다 파라미터로 뷰홀더 객체, 뷰 객체, 뷰의 포지션정보가 전달되도록
}
