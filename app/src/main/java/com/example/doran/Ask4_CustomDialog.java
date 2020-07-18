package com.example.doran;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Ask4_CustomDialog {
    private Context context;
    Dialog dialog;
    Button btn_yes;
    FirebaseDatabase database;
    String Email_Key;
    public Ask4_CustomDialog(Context context) {
        this.context = context;
    }

    public void calldialog(){

        /*로그인한 나의 이메일 키값 가져오기 - 로그인에서 이미 변형했음 . >> ,*/
        SharedPreferences sharedPreferences1 = context.getSharedPreferences("USER_EMAIL",context.MODE_PRIVATE);
        Email_Key = sharedPreferences1.getString("MyEmail_Key","이메일없음ㅋ");


        database = FirebaseDatabase.getInstance(); //파이어베이스
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //액티비티 타이틀바 제거
        dialog.setContentView(R.layout.ask4_custom_dialog); // 레이아웃과 연결
        dialog.show(); // 커스텀 다이얼로그 노출

        /*xml*/
        btn_yes = (Button)dialog.findViewById(R.id.btn_yes); // ㅇㅋ 버튼

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*로그인한 나, 수락요청을 처음에 보냈던 나(A)의 ask값을 다시 3으로 바꿈 = 다음 로그인시에 다이얼로그 생략되도록*/
//                DatabaseReference myRef = database.getReference("User").child(Email_Key).child(Email_Key); //데이터베이스경로에
//                HashMap<String,Object> UserProfile = new HashMap<>();
//                UserProfile.put("ask",3);
//                UserProfile.put("ProfileImage","none");
//                myRef.updateChildren(UserProfile); //값을 저장한다.

                Toast.makeText(context,"커플 연결이 완료 되었습니다!",Toast.LENGTH_SHORT).show();

                dialog.dismiss();


            }
        });
    }
}
