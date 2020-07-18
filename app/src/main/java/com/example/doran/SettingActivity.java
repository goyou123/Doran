package com.example.doran;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    SeekBar Setting_Switch; //자동로그인 시크바
    ImageView iv_Setting_close;
    TextView tv_Setting_Logout,tv_Setting_Signout,tv_Setting_BreakConnect;
    FirebaseDatabase database;
    String shared_Email; // 로그인에서 저장한 이메일 Shared에 저장해놓은거 넣을 변수
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값
    String coupleKey; // A와 B 공유하는 키값
    String anotherName; // 상대방 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        database = FirebaseDatabase.getInstance();

        /*xml연결*/
        //시크바는 어떻게 하는지
        iv_Setting_close = (ImageView)findViewById(R.id.iv_Setting_close); //뒤로가기
        tv_Setting_Logout = (TextView)findViewById(R.id.tv_Setting_Logout); // 로그아웃
        tv_Setting_Signout = (TextView)findViewById(R.id.tv_Setting_Signout); // 회원탈퇴
        tv_Setting_BreakConnect = (TextView)findViewById(R.id.tv_Setting_BreakConnect);//상대방과의 커플 연동 해제

        /*Shared에서 이메일값 불러오기
         * 왜냐하면 이미지를 이메일별로 저장하기 위해서, 한번 시도해보기*/
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

                // 상대방의 이름값 - 다이얼로그에 사용
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
        DatabaseReference ref = database.getReference("User").child(anotherKey); //B의 경로
        ref.addChildEventListener(childEventListener);



        /*뒤로가기*/
        iv_Setting_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*로그아웃*/
        tv_Setting_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // 파이어베이스 로그아웃
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //기존에 쌓여있던 스택을 모두 없앤다.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // task를 새로 생성한다

                SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("Email");

                Toast.makeText(getApplicationContext(),"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show();
                startActivity(intent);



            }
        });


        /* 회훤탈퇴
        *  - 회원탈퇴시에는 */



        /* 상대방과의 연결 끊기
        *  - 연결을 끊으면 상대 ask값을 6으로 줘서 커플 연동이 끊어졌습니다! 라는 메세지를 알려줘야 함
        *  - 그 다이얼로그에서는 이제 ask값을 다시 0으로 만들고 끊어졌으니 다시 요청 보내는 액티비티로 이동*/
        tv_Setting_BreakConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                breakconnectDialog();
                Intent intent = new Intent(getApplicationContext(),ConnectBreakActivity.class);
                startActivity(intent);



            }
        });





    }//OnCreate


    /*상대방과의 연결끊기 다이얼로그*/
    void breakconnectDialog(){

        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY년 MM월 dd일 ");
        calendar.add(Calendar.MONTH,6);
        String strTime = sdf.format(calendar.getTime());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("상대방과의 연결 끊기");
        builder.setMessage(anotherName+"님 과의 연결을 정말로 끊으시겠습니까?\n" +"데이터는 6개월 뒤인 "+strTime+"에 삭제됩니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

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


                        //다시 신청할수 있는 액티비티로 이동 , 로그아웃 X
                        Intent intent = new Intent(SettingActivity.this,Connect_needAskActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(),anotherName+"님 과의 연결이 끊어졌습니다. ㅜㅜ",Toast.LENGTH_LONG).show();



                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }
}
