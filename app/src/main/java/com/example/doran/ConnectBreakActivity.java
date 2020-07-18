package com.example.doran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class ConnectBreakActivity extends AppCompatActivity {

    TextView tv_Firstdate,tv_Break_BackUpDay,tv_Break_lastask;
    CheckBox checkbox_break;
    Button btn_BreakButton;

    FirebaseDatabase database;
    String shared_Email; // 로그인에서 저장한 이메일 Shared에 저장해놓은거 넣을 변수
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값
    String coupleKey; // A와 B 공유하는 키값
    String anotherName; // 상대방 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_break);
        database = FirebaseDatabase.getInstance();

        /*xml연결*/
        tv_Firstdate = (TextView) findViewById(R.id.tv_Firstdate); //사귄기간 표시 (만난날 ~ 오늘날)
        tv_Break_BackUpDay = (TextView) findViewById(R.id.tv_Break_BackUpDay); // 복구가능기간 (오늘~한달뒤)
        tv_Break_lastask = (TextView) findViewById(R.id.tv_Break_lastask); // 00님과의 연결을 끊으시겠어요?
        checkbox_break = (CheckBox) findViewById(R.id.checkbox_break); //체크박스
        btn_BreakButton = (Button) findViewById(R.id.btn_BreakButton); // 연결끊기버튼

        /*로그인창에서 입력한 이메일 shared로 저장한거 가져오기 */
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences.getString("Email",""); // 입력 이메일 받아오기
        Log.d("이메일 스토리쓰는곳", "onCreate: "+shared_Email);


        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences1 = getSharedPreferences(shared_Email,MODE_PRIVATE);
        myKey = sharedPreferences1.getString("A_Key","A키없음");
        anotherKey = sharedPreferences1.getString("B_Key","B키없음");//xml파일로 확인


        /*A와 B 공유하는 키값 가져오기 - 스토리 저장 데이터베이스 경로로 사용할것*/
        SharedPreferences sharedPreferences2 = getSharedPreferences("Couplekey",MODE_PRIVATE);
        coupleKey = sharedPreferences2.getString("getCouplekey","커플공유키값없음~~~");
        Log.d("스토리 작성 저장시", "커플2명만의 키: "+coupleKey); // 확인




        /*상대방(B) 정보 불러와서 연결끊기시 상대방이름으로 사용*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                //유저데이터 확인
                UserData userData = dataSnapshot.getValue(UserData.class);
                Log.d("상대프로필화면에서 OnCreate", "상대방 데이터 불러오기: " + dataSnapshot); //확인

                // 상대방의 이름값
                anotherName = userData.getMyName();
                tv_Break_lastask.setText(anotherName+"님 과의 연결을 정말로 끊으시겠어요?");
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
        DatabaseReference ref = database.getReference("User").child(anotherKey); //B의 경로
        ref.addChildEventListener(childEventListener);




        /*사귄날짜 불러오기 - 우리가 함께한 기간*/
        ChildEventListener childEventListener0 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                DdayPickData ddayPickData = dataSnapshot.getValue(DdayPickData.class);
                Log.d("디데이화면들어가서", "만난날, 사귄일수: "+dataSnapshot);
                String firstDay = ddayPickData.getFirstday();

                // 처음 만난 날 ~ 오늘 날
                Calendar calendar = new GregorianCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY년 MM월 dd일 ");
                String strTime = sdf.format(calendar.getTime());

                //사귄기간
                tv_Firstdate.setText(firstDay+" ~ "+strTime);

                //복구가능 날 // 오늘날 ~ 60일 뒤
                Calendar calendar1 = new GregorianCalendar();
                calendar1.add(Calendar.DAY_OF_MONTH,60);
                String backupday = sdf.format(calendar1.getTime());
                tv_Break_BackUpDay.setText(strTime+" ~ "+backupday);

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

        // 파이어베이스 스토리 데이터 불러오기
        DatabaseReference ref0 = database.getReference(coupleKey).child("PickDday");//.child("PickDday");
        ref0.addChildEventListener(childEventListener0);


        btn_BreakButton.setEnabled(false);
        /*체크박스에 체크 해야 연결끊기버튼 활성화되게끔 */
        checkbox_break.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkbox_break.isChecked()){
                    btn_BreakButton.setEnabled(true);
                    btn_BreakButton.setBackgroundColor(Color.parseColor("#FFEA4444"));
                }else{
                    btn_BreakButton.setEnabled(false);
                    btn_BreakButton.setBackgroundColor(Color.parseColor("#FFF39393"));
                }
            }
        });



        /*연결끊기버튼 클릭*/
        btn_BreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*연결 끊을시*/
                Log.d("세팅화면", "상대방과의 연결끊키시 커플키: "+coupleKey); // 확인
                //나의 유저데이터 정보를 초기화하고 커플요청할수 있는 액티비티로 이동
                DatabaseReference myRef = database.getReference("User").child(myKey).child(myKey); // 나의 경로
                HashMap<String,Object> UserProfile = new HashMap<>();
                UserProfile.put("Other","");
                UserProfile.put("ask",0);
                myRef.updateChildren(UserProfile); //값을 저장한다.


                /*상대방의 ask값을 6으로 바꿈*/
                //상대방의 유저데이터를 초기화하고 커플요청할수있는 액티비티로 이동,
                DatabaseReference myRef1 = database.getReference("User").child(anotherKey).child(anotherKey); // 상대의 경로
                HashMap<String,Object> UserProfile1 = new HashMap<>();
                UserProfile1.put("Other","");
                UserProfile1.put("ask",6);
                myRef1.updateChildren(UserProfile1); //값을 저장한다.



//                        //DB에서 커플키생성된데이터 아예 지워버리기
//                        DatabaseReference myRef2 = database.getReference(coupleKey); // 상대의 경로
//                        myRef2.setValue(null);

//                /*회원탈퇴시 */
//                SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.remove("Email");
//                editor.commit();




                //다시 신청할수 있는 액티비티로 이동 , 로그아웃 X
                Intent intent = new Intent(ConnectBreakActivity.this,Connect_needAskActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),anotherName+"님 과의 연결을 끊었습니다.",Toast.LENGTH_LONG).show();

            }
        });


    }
}
