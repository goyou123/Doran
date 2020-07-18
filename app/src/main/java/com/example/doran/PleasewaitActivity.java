package com.example.doran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PleasewaitActivity extends AppCompatActivity {

    Button btn_goLoginActivity;

    FirebaseDatabase database;

    String shared_Email; //로그인시 입력한 내 이메일
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값

    int myask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pleasewait);
        database = FirebaseDatabase.getInstance();


        /*xml*/
        btn_goLoginActivity = (Button) findViewById(R.id.btn_goLoginActivity); // 다시 로그인화면으로 이동


        /*로그인한 내 이메일 가져오기*/
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences.getString("Email",""); // 로그인시 입력 이메일 받아오기



        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences1 = getSharedPreferences(shared_Email,MODE_PRIVATE);
        myKey = sharedPreferences1.getString("A_Key","A키없음");
        anotherKey = sharedPreferences1.getString("B_Key","B키없음");//xml파일로 확인



        /*ondatachange*/
        /* 만약 내 ask값이 4이 되면 자동으로 메인화면으로 넘어가도록
        *  ask값이 5일때 거절당해서 다시 요청할수있는 액티비티로이동*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                UserData userData = dataSnapshot.getValue(UserData.class); //DB데이터를 클래스 객체를 에 넣기
                Log.d("커플요청 하고 수락 대기상태에서", "상대방 데이터 불러오기: "+dataSnapshot); //확인

                /*상대방이 수락하면 내 객체의 ask값이 4로 바뀌고 자동으로 넘어가도록*/
                myask = userData.getAsk();
                Log.d("커플요청 하고 수락 대기상태에서", "onChildChanged: ask값이 4로 바뀌면"+myask);
                if (myask==4){
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Log.d("수락당해서 ask값이 4일때", "4로 바꼈으니"+myask);
                    startActivity(intent);
                } else if (myask==5){

                    /*B에게 요청을 보낸후 기다리다가 상대가 요청을 거절해 A의 ask값이 5가 되어 A가 다시 요청을 보내는 화면으로 이동함 */
                    Intent intent = new Intent(getApplicationContext(),Connect_needAskActivity.class);
                    Log.d("거절당해서 ask값이 5일때", "5로 바꼈으니"+myask);
                    Toast.makeText(PleasewaitActivity.this, "상대방이 나의 요청을 거절하였습니다.ㅠㅠ", Toast.LENGTH_LONG).show();
                    startActivity(intent);

//                    /*그와 동시에 다시 A의 ask값을 0으로 초기화 */
//                    DatabaseReference myRef = database.getReference("User").child(myKey).child(myKey);
//                    HashMap<String,Object> UserProfile = new HashMap<>();
//                    UserProfile.put("ask",0);
//                    myRef.updateChildren(UserProfile);
                }
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
        DatabaseReference ref = database.getReference("User").child(myKey); //현재로그인한 A의 경로
        ref.addChildEventListener(childEventListener);







        btn_goLoginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

    }//OnCreate
}
