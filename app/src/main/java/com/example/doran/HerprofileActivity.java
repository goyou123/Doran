package com.example.doran;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HerprofileActivity extends AppCompatActivity {
    ImageView iv_Herprofile_girl,iv_Herprofile_close;
    Button btn_Herprofile_Chatting,btn_Herprofile_Call;
    TextView tv_Herprofile_name,tv_Herprofile_email,tv_Herprofile_phonenum;

    FirebaseDatabase database;

    String shared_Email; //로그인시 입력한 내 이메일
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값

    String herName; //상대방 프로필 - 이름
    String herEmail; // 상대방 프로필 - 이메일
    public String herPhonenum; // 상대방 프로필 - 폰번호



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herprofile);
        database = FirebaseDatabase.getInstance();

        /*xml연결*/
        iv_Herprofile_close = (ImageView)findViewById(R.id.iv_Herprofile_close); // 뒤로가기
        iv_Herprofile_girl = (ImageView)findViewById(R.id.iv_Herprofile_girl); // 상대방 프로필사진
        btn_Herprofile_Chatting =(Button)findViewById(R.id.btn_Herprofile_Chatting); // 채팅버튼
        btn_Herprofile_Call =(Button)findViewById(R.id.btn_Herprofile_Call); // 전화버튼
        tv_Herprofile_name =(TextView) findViewById(R.id.tv_Herprofile_name); // 상대이름
        tv_Herprofile_email =(TextView)findViewById(R.id.tv_Herprofile_email); // 상대이메일
        tv_Herprofile_phonenum =(TextView)findViewById(R.id.tv_Herprofile_phonenum); // 상대폰번호


        /*로그인한 내 이메일 가져오기*/
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences.getString("Email",""); // 로그인시 입력 이메일 받아오기


        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences1 = getSharedPreferences(shared_Email,MODE_PRIVATE);
        myKey = sharedPreferences1.getString("A_Key","A키없음");
        anotherKey = sharedPreferences1.getString("B_Key","B키없음");//xml파일로 확인



        /*상대방(B) 정보 불러와서 상대 프로필에 적용시키기 - 처음 생성될때*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                UserData userData = dataSnapshot.getValue(UserData.class); //DB데이터를 클래스 객체를 에 넣기
                Log.d("상대프로필화면에서 OnCreate", "상대방 데이터 불러오기: "+dataSnapshot); //확인

                // 상대 객체 정보 불러오기 (이름, 이메일 , 폰번호)
                herName = userData.getMyName();
                herEmail = userData.getMyEmail();
                herPhonenum = userData.getMyPhonenum();
                Log.d("상대프로필화면에서", "상대방 정보 3개 : " +herEmail+herName+herPhonenum); //확인

                // 상대 객체 정보 상대 프로필에 붙히기
                tv_Herprofile_name.setText(herName);
                tv_Herprofile_email.setText(herEmail);
                tv_Herprofile_phonenum.setText(herPhonenum);


                //전화걸기
                btn_Herprofile_Call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // 후에 프로필 설정값으로 바꾸기
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ herPhonenum)); //상대방폰번호
                        startActivity(intent);
                    }
                });


               /*상대 URI 가져와서 프로필사진에 적용시키기*/
                String herProfileUri = userData.getProfileImage();
                if (herProfileUri.equals("none")){
                    iv_Herprofile_girl.setImageResource(R.drawable.profile);
                }else {
                    Glide.with(HerprofileActivity.this).load(herProfileUri).into(iv_Herprofile_girl);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                /*B의 정보*/
                /*상대방 데이터가 변경됬을때*/
                UserData userData = dataSnapshot.getValue(UserData.class); //DB데이터를 클래스 객체를 에 넣기
                Log.d("상대프로필화면에서 OnCreate", "상대방 데이터 불러오기: "+dataSnapshot); //확인

                /*상대 객체 정보 불러오기*/
                herName = userData.getMyName();
                herEmail = userData.getMyEmail();
                herPhonenum = userData.getMyPhonenum();
                Log.d("상대프로필화면에서", "상대방 정보 3개 : " +herEmail+herName+herPhonenum); //확인

                /*상대 객체 정보 상대 프로필에 붙히기*/
                tv_Herprofile_name.setText(herName);
                tv_Herprofile_email.setText(herEmail);
                tv_Herprofile_phonenum.setText(herPhonenum);


                /*전화걸기*/
                btn_Herprofile_Call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // 후에 프로필 설정값으로 바꾸기
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ herPhonenum)); //상대방폰번호
                        startActivity(intent);
                    }
                });

                /*상대 URI 가져와서 프로필사진에 적용시키기 - 변경됬을때*/
//                String herProfileUri = userData.getProfileImage();
//                Glide.with(HerprofileActivity.this).load(herProfileUri).into(iv_Herprofile_girl);
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




        /*뒤로가기*/
        iv_Herprofile_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*채팅으로 이동*/
        btn_Herprofile_Chatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ChattingActivity.class);
                startActivity(intent);
            }
        });




    }//OnCreate
}
