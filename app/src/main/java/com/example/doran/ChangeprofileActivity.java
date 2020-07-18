package com.example.doran;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ChangeprofileActivity extends AppCompatActivity {
    Button btn_Changeprofile_back,btn_Changeprofile_Save;
    ImageView iv_Changeprofile_close;
    EditText et_Changeprofile_name,et_Changeprofile_phonenum;

    FirebaseDatabase database;
    String shared_Email; //로그인시 입력한 내 이메일
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값

    String updateName;
    String updatePhonenum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeprofile);
        database = FirebaseDatabase.getInstance();


        /*xml연결*/
        btn_Changeprofile_back = (Button)findViewById(R.id.btn_Changeprofile_back); // 하단 뒤로가기버튼
        btn_Changeprofile_Save = (Button)findViewById(R.id.btn_Changeprofile_Save); // 저장하기 버튼
        iv_Changeprofile_close = (ImageView)findViewById(R.id.iv_Changeprofile_close); // 상단 뒤로가기 버튼
        et_Changeprofile_name = (EditText)findViewById(R.id.et_Changeprofile_name); // 변경할 이름 입력
        et_Changeprofile_phonenum = (EditText)findViewById(R.id.et_Changeprofile_phonenum); // 변경된 핸드폰번호 입력


        /*로그인한 내 이메일 가져오기*/
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences.getString("Email",""); // 로그인시 입력 이메일 받아오기


        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences1 = getSharedPreferences(shared_Email,MODE_PRIVATE);
        myKey = sharedPreferences1.getString("A_Key","A키없음");
        anotherKey = sharedPreferences1.getString("B_Key","B키없음");//xml파일로 확인


        /*저장하기 버튼 눌렀을때 이름과 전화번호 변경된 값이 내 DB에 저장되도록 하기*/
        btn_Changeprofile_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 변경화면에서 입력한 이름,핸드폰 번호 값
                updateName = et_Changeprofile_name.getText().toString();
                updatePhonenum = et_Changeprofile_phonenum.getText().toString();


                /*DB에 로그인한유저(A)의 이름과 핸드폰 번호를 업데이트*/
                DatabaseReference myRef = database.getReference("User").child(myKey).child(myKey); //B의 데이터베이스경로에
                HashMap<String,Object> UserProfile = new HashMap<>();
                UserProfile.put("MyName",updateName);
                UserProfile.put("MyPhonenum",updatePhonenum);
                myRef.updateChildren(UserProfile); //변경된 값을 저장한다.

                finish();


            }
        });




        /*뒤로가기상단*/
        iv_Changeprofile_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*뒤로가기하단버튼*/
        btn_Changeprofile_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
