package com.example.doran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Connect_okAskActivity extends AppCompatActivity {
    TextView tv_whosEmail; //누가 신청했는지에 대한 값
    Button btn_allow,btn_back; //수락하기, 뒤로가기 버튼

    FirebaseDatabase database;
    String Shared_myEmail; // 로그인시 입력한 이메일 (B) 담을 변수
    String MyEmail_key; // B이메일을 key로 만들 변수
    String my_Other; // B의 상대방 (A)의 이메일
    String other_Key; // 상대방 (A)의 이메일을 key로 만들 변수
    String other_name; //상대방 (A) 의 이름

    /*끄적끄적 - ask 값이 2인 유저(요청을 받은 회원)이 로그인했을때 나오는 액티비티
     * 받은 신청 수락하기 / 거절하기 를 조절할 수 있다.
     * 수락 버튼을 눌렀을때 A와 B의 ask값을 둘다 3으로 바꿔줘야 한다.
     * 거절 버튼을 눌렀을때 A의 ask값은 0으로 돌아가서 다시 신청이 가능하게 + Other값 삭제
     *                  B의 ask값 도 0으로 바꾸고 Other값 삭제*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ok_ask);
        database = FirebaseDatabase.getInstance(); //파이어베이스 데이터베이스 참조

        /*xml*/
        tv_whosEmail = (TextView) findViewById(R.id.tv_whosEmail); //나중에 누가 요청했는지 알려줘야되는 이메일
        btn_allow = (Button) findViewById(R.id.btn_allow); // 수락하기 버튼 - 수락시 ask3, 메인액티비티 이동
        btn_back = (Button) findViewById(R.id.btn_back); // 뒤로 가기


        /*<끄적끄적>
        데이터베이스에서 B의 Other값(A의 이메일이겠지)를 가져와서 화면 TextView에 붙혀 누구한테 왔는지 알게 하기
        *
        *  == 지금 현재 상태는 B가 로그인한 상태이다.
        * 그러므로 로그인하는 이메일값을 가져오는 것이 B의 이메일을 가져오는 것이고
        * B의 Other값이 A의 이메일인 것이다.
        * A의 이메일을 가져와서 replace로 바꿔 키로 사용할 수 있게끔 하기*/


        /*로그인한 이메일 가져오기 (이 액티비티에서는 B(신청받은사람)의 이메일)*/
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        Shared_myEmail = sharedPreferences.getString("Email",""); // 로그인시 입력 이메일 받아오기
        Log.d("수락하는 액티비티", "이떄 로그인한 B의 이메일: "+Shared_myEmail); //확인
        MyEmail_key = Shared_myEmail.replace(".",","); // 로그인한 B의 키값
        Log.d("수락하는 액티비티", "로그인한 B의 이메일을 키값으로: "+MyEmail_key); //확인




        /*키값을 활용해 B의 Other값 가져와서 TextView에 붙히기
        * 변경 - 이메일 말고 이름을 불러오기 -- 완료*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                // 로그인한 B의 유저데이터
                UserData userData = dataSnapshot.getValue(UserData.class);
                my_Other = userData.getOther(); // B의 상대방 (A)의 이메일
                other_Key = my_Other.replace(".",","); // B의 상대방 (A)의 이메일을 key로 사용 <A의 키값>
                Log.d("B로 로그인해서 수락하는 화면", "A의 키값은?: "+other_Key);


                /*A의 이메일에 접근해서 이름값 가져오기
                * 이중구조임 ! 주의!*/
                ChildEventListener childEventListener1 = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                        // 상대인 A의 유저 데이터
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        other_name = userData.MyName; // 상대방 (A)의 이름
                        Log.d("요청거절시 상대방 이름", "onChildAdded: "+other_name);
                        /*A 님에게로부터 커플 요청이 왔습니다!*/
                        tv_whosEmail.setText(other_name);
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
                DatabaseReference ref3 = database.getReference("User").child(other_Key); //A의 경로 MyEmail_key
                ref3.addChildEventListener(childEventListener1);


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
        DatabaseReference ref = database.getReference("User").child(MyEmail_key); //로그인한 B의 경로
        ref.addChildEventListener(childEventListener);





        /*수락 버튼을 눌렀을 때
        *
        * 이때 커플만 볼 수 있는 데이터 구조가 생성되야함*/
        btn_allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*B가 커플요청을 수락했을때 결국 커플키를 생성함*/
                String makeCouplekey = MyEmail_key+"@"+other_Key;
                Log.d("커플요청수락시 커플만의키생성해서", "나와 상대방 유저정보에 넣기: "+makeCouplekey);
                /*나(B)의 ask값 을 3으로 변경*/
                int ask = 3;
                DatabaseReference myRef = database.getReference("User").child(MyEmail_key).child(MyEmail_key); //데이터베이스경로에
                HashMap<String,Object> UserProfile = new HashMap<>();
                UserProfile.put("ask",ask); //ask값을 3으로 변경 - 로그인시 메인 액티비티로 이동 가능
                UserProfile.put("CoupleKey",makeCouplekey);
                /*실험용*/
                UserProfile.put("ProfileImage","none");

                myRef.updateChildren(UserProfile); //값을 저장한다.


                /*Other(A)의 ask값을 4로 변경 - 4는 들어가면 커플 연동이 되었다는 다이얼로그가 뜸*/
                int ask2 = 4;
                DatabaseReference myRef2 = database.getReference("User").child(other_Key).child(other_Key); //데이터베이스경로에
                HashMap<String,Object> UserProfile2 = new HashMap<>();
                UserProfile2.put("ask",ask2);
                UserProfile2.put("CoupleKey",makeCouplekey);
                myRef2.updateChildren(UserProfile2); //값을 저장한다.


                /*커플 요청 수락 토스트 메세지 후 메인화면으로 이동하기*/
                Toast.makeText(getApplicationContext(),other_name+"님의 커플 요청을 수락하였습니다.",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });



        /*커플 신청 거절 눌렀을 때*/
        /*거절 했을때 거절당한 상대방은 거절당한지 알아야 한다.*/
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*신청자,Other(A)의 ask값을 0으로 변경하고 상대방 값을 초기화*/
                DatabaseReference myRef2 = database.getReference("User").child(other_Key).child(other_Key); //A의 데이터베이스경로에
                HashMap<String,Object> UserProfile2 = new HashMap<>();
                UserProfile2.put("Other","");
                UserProfile2.put("ask",5); // 수락 거절당한 상태 - PleaseWait액티비티에서 조정
                myRef2.updateChildren(UserProfile2); //변경된 값을 저장한다.

                /* 로그인한 나(B)의 ask값 을 0으로 변경하고 상대방 값을 초기화 - 다시 신청해야함 , 처음 회원가입만 했을때의 상태와 동일*/
                DatabaseReference myRef = database.getReference("User").child(MyEmail_key).child(MyEmail_key); //B의 데이터베이스경로에
                HashMap<String,Object> UserProfile = new HashMap<>();
                UserProfile.put("Other","");
                UserProfile.put("ask",0); // ask값을 0으로
                myRef.updateChildren(UserProfile); //변경된 값을 저장한다.


                /*커플 요청 거절 메인화면으로 이동*/
                Toast.makeText(getApplicationContext(),other_name+"님의 커플 요청을 거절하였습니다. 새로운 상대를 등록해주세요!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),Connect_needAskActivity.class);
                startActivity(intent);
            }
        });





    }//OnCreate
}
