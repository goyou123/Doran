package com.example.doran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;

public class Connect_needAskActivity extends AppCompatActivity {
        TextView tv_Askvalue_ChangeMessage;
        EditText et_Connect_Email;
        Button btn_back,btn_Connect;
        String intent_Myemail;
        String Shared_myEmail;
        String anotherEmail; // 입력하는 상대방 이메일
        String shaerd_UID; // 로그인에서 입력하는 이메일 값의 UID , 로그인하는 사용자의 UID - Shared
        String Email_Key; // 로그인하는 "나"의 이메일을 키값으로 바꾼 값
        String Another_Email_Key; // 상대방 이메일 입력하는내용을 "," 으로 바꿈

        String myKey;
        String anotherKey;
        String anotherName;


        FirebaseDatabase database;
        FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_need_ask);
        mAuth = FirebaseAuth.getInstance(); // 파이어베이스 인증 참조
        database = FirebaseDatabase.getInstance(); //파이어베이스

        /*xml연결*/
        tv_Askvalue_ChangeMessage = (TextView) findViewById(R.id.tv_Askvalue_ChangeMessage); //거절,연결해제 당했을 시 텍스트 추가될 곳
        et_Connect_Email = (EditText) findViewById(R.id.et_Connect_Email); //연결하고싶은 상대방 이메일 입력하는 곳
        btn_Connect = (Button) findViewById(R.id.btn_Connect); //상대방과 연결하는 버튼
        btn_back = (Button) findViewById(R.id.btn_back); // 뒤로가기 버튼

        /*로그인시 저장되는 나의 이메일 값*/
        SharedPreferences sharedPreferences2 = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        Shared_myEmail = sharedPreferences2.getString("Email",""); // 로그인시 입력 이메일 받아오기


        /*로그인시 SHared에 저장되는 "나"의 UID값을 가져와서 사용*/
        SharedPreferences sharedPreferences = getSharedPreferences("USER_UID",MODE_PRIVATE);
        shaerd_UID = sharedPreferences.getString("UID","UID 값 없음ㅋ");


        /*로그인시 Sharedpreferences에 저장되는 "나"의 이메일을 가져와서 < DB 키값으로 사용> */
        SharedPreferences sharedPreferences1 = getSharedPreferences("USER_EMAIL",MODE_PRIVATE);
        Email_Key = sharedPreferences1.getString("MyEmail_Key","이메일없음ㅋ");


        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences4 = getSharedPreferences(Shared_myEmail,MODE_PRIVATE);
        myKey = sharedPreferences4.getString("A_Key","A키없음");
        anotherKey = sharedPreferences4.getString("B_Key","B키없음");//xml파일로 확인


        ChildEventListener childEventListener1 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                //상대방 이름만 가져오기 - 누가 거절했다
                UserData userData = dataSnapshot.getValue(UserData.class);
                anotherName = userData.getMyName();
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
        DatabaseReference ref2 = database.getReference("User").child(anotherKey);
        ref2.addChildEventListener(childEventListener1);










        /* - 커플 신청 거절시 A와 B 둘다 로그인된상태 + 둘 다 이 액티비티인 상황 .. 여기서 안되면 아래 신청 버튼 눌렀을때로 옮겨보기 - */
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

//                // 로그인한 B의 유저데이터 그냥 로그인한애
                UserData userData = dataSnapshot.getValue(UserData.class);
                int askvalue = userData.getAsk();


                //상대가 요청을 거절한 상태일때 나에게 알려주기
                if(askvalue == 5){
                    Log.d("거절당했을때 에스크값", "5여야됨 일단: "+askvalue);
                    tv_Askvalue_ChangeMessage.setText("※ "+anotherName+"님이 요청을 거절하였습니다.");
                    /*그와 동시에 다시 A의 ask값을 0으로 초기화 */
                    DatabaseReference myRef = database.getReference("User").child(Email_Key).child(Email_Key);
                    HashMap<String,Object> UserProfile = new HashMap<>();
                    UserProfile.put("ask",0);
                    myRef.updateChildren(UserProfile);

                }else if(askvalue==6){
                    // 상대가 연결을 끊었을때 나에게 알려주기
                    tv_Askvalue_ChangeMessage.setText("※ "+anotherName+"님이 연결을 끊었습니다");
                    /*그와 동시에 다시 A의 ask값을 0으로 초기화 */
                    DatabaseReference myRef = database.getReference("User").child(Email_Key).child(Email_Key);
                    HashMap<String,Object> UserProfile = new HashMap<>();
                    UserProfile.put("ask",0);
                    myRef.updateChildren(UserProfile);
                }



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

                // 로그인한 B의 유저데이터 그냥 로그인한애
                UserData userData = dataSnapshot.getValue(UserData.class);
                int askvalue = userData.getAsk();
                Log.d("커플요청을 보내는 액티비티", "만약 ASK값이 2로바뀌면: "+askvalue);
                if (askvalue==2){
                    //들어온 요청이 있는 상태
                    Intent intent = new Intent(getApplicationContext(),Connect_okAskActivity.class);
                    startActivity(intent);
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
        DatabaseReference ref = database.getReference("User").child(Email_Key);
        ref.addChildEventListener(childEventListener);












        /*신청 버튼 눌렀을때 내 유저객체에 상대방 이메일 정보 올리기*/
        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                anotherEmail = et_Connect_Email.getText().toString(); //상대방 등록할 이메일
                Another_Email_Key = anotherEmail.replace(".",","); // 상대방 데이터베이스 접근할 키
                int ask=1;
                /* !!! 등록한 이메일이 회원인지 아닌지 검사 필요함 !!!
                * !!! 입력하는 이메일이 내 이메일이 아닌지도 검사 필요*/

                // 입력하는 상대 이메일이 내가 로그인한 이메일인지 아닌지 체크하고 입력값 지우기
                if(anotherEmail.equals(Shared_myEmail)){
                    Toast.makeText(Connect_needAskActivity.this, "현재 로그인되있는 이메일에는 신청이 불가능합니다.", Toast.LENGTH_SHORT).show();
                    et_Connect_Email.setText(null);
                    return;
                }

                /*DB에 로그인한 회원객체(A)의 Other에 등록하는상대 이메일(B)을 추가하고 , ask값을 1로 변경한다.*/
                DatabaseReference myRef = database.getReference("User").child(Email_Key).child(Email_Key); //데이터베이스경로에
                HashMap<String,Object> UserProfile = new HashMap<>();
                UserProfile.put("Other",anotherEmail); //연결하고픈 상대방 이메일을 회원가입한 유저 객체에 저장
                UserProfile.put("ask",ask); //ask값을 1로 변경
                myRef.updateChildren(UserProfile); //값을 저장한다.
                Log.d("anotherEmail", "onClick: "+anotherEmail+ask); //확인완료




                /* 등록하는 이메일에 대한 UID값을 알아서(B의 유저 객체) 그 유저의 ask값을 2로(수락ok할수있는 화면) 바꾸고 로그인하는 A의 이메일을
                *  B 유저의 Other에 등록함
                *  - 등록하는 이메일 (B)의 ask값을 2로 바꾸기
                *  - A의 이메일값을 B 의 Other로 등록하기
                */


                /*A로 로그인한 상태로 B의 상대, B의 ask값 바꾸기*/
                int ask2 = 2;
                DatabaseReference myRef2 = database.getReference("User").child(Another_Email_Key).child(Another_Email_Key); //데이터베이스경로에
                HashMap<String,Object> UserProfile2 = new HashMap<>();
                UserProfile2.put("Other",Shared_myEmail); // 로그인한 나의 이메일을 상대방 B의 이메일에 등록
                UserProfile2.put("ask",ask2); // ask값을 2로 변경
                myRef2.updateChildren(UserProfile2); // 변경한 값을 저장한다.






                /*"신청 버튼눌렀을때임. 기다려주세요 라는 화면으로 이동하기"*/
                Intent intent1 = new Intent(getApplicationContext(),PleasewaitActivity.class);
                startActivity(intent1);
            }
        });






    }//OnCreate
}
