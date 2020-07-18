package com.example.doran;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    String shared_Email; // 로그인에서 저장한 이메일 Shared에 저장해놓은거 넣을 변수
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        database = FirebaseDatabase.getInstance();

        /*Shared에서 이메일값 불러오기
         * 왜냐하면 이미지를 이메일별로 저장하기 위해서, 한번 시도해보기*/
        /*로그인창에서 입력한 이메일 shared로 저장한거 가져오기 */
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences.getString("Email",null); // 입력 이메일 받아오기
        Log.d("이메일 스토리쓰는곳", "onCreate: "+shared_Email);


        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences1 = getSharedPreferences(shared_Email,MODE_PRIVATE);
        myKey = sharedPreferences1.getString("A_Key","A키없음");
        anotherKey = sharedPreferences1.getString("B_Key","B키없음");//xml파일로 확인
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                /*A의 정보*/
                UserData userData = dataSnapshot.getValue(UserData.class);
                String my_name = userData.getMyName();
                Toast.makeText(SplashActivity.this, my_name+"님 자동로그인 되었습니다.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        //로그인한 유저의 데이터베이스 경로
        DatabaseReference ref = database.getReference("User").child(myKey);
        ref.addChildEventListener(childEventListener);






        startLoading();


    }


    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                /*자동로그인*/
                if (shared_Email!=null) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }
//
//                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
//                startActivity(intent);
            }
        },2000);

    }
}
