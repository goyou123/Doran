package com.example.doran;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    EditText et_Login_Email,et_Login_Password;
    Button btn_Login,btn_Signup,btn_Google_login;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    DatabaseReference databaseReference;
    String stemail; // 입력 이메일값 Shared에 저장
    String useUid; // 로그인시 입력되는 UID받기

    String Key_Email;
    int ask2;
    ArrayList<CoupleListdata> coupleListdataArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        /*xml연결*/
        btn_Login = (Button)findViewById(R.id.btn_Login); // 로그인버튼
        btn_Signup = (Button)findViewById(R.id.btn_Signup); // 회원가입 버튼
        btn_Google_login = (Button)findViewById(R.id.btn_Google_login); // 구글로그인 버튼
        et_Login_Email = (EditText) findViewById(R.id.et_Login_Email); //이메일 입력
        et_Login_Password = (EditText) findViewById(R.id.et_Login_Password); //비밀번호 입력


//        /*커플 이메일 검사 - */
//        DatabaseReference ref2 = database.getReference("CoupleList");
//        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                coupleListdataArrayList = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 DB에 있는데이터 리스트를 추출
//                    CoupleListdata coupleListdata = snapshot.getValue(CoupleListdata.class); // 만들어논 스토리데이터 객체에 데이터를 담는다.
//                    coupleListdataArrayList.add(coupleListdata); // 담은 데이터들을 리스트에 넣고 리사이클러뷰로 보낼 준비
//                    Log.d("로그인버튼눌렀을대", "커플리스트 전체 나오는지 검사: "+snapshot);
//                    Log.d("로그인액티비티 켜켜지자마자 ", "커플 이메일 검사: "+coupleListdataArrayList.size());
////                    for (CoupleListdata value : coupleListdataArrayList){
////                        Log.d("리스트내용확인", "onDataChange: "+value);
////                    }
//                    //리스트 내용이 정확한건지 다시한번 확인.
//                    // 그리고 그 리스트 안에있는 이메일과 내가 입력하는 이메일이 일치하면.
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//


        /*로그인 버튼 눌렀을 때*/
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stemail = et_Login_Email.getText().toString(); // 이메일입력
                String stpassword = et_Login_Password.getText().toString(); //비밀번호입력
                Key_Email = stemail.replace(".",","); // . 를 , 으로 바꾼 입력 이메일 - DBkey값

                /*SharedPreference에 입력 이메일값 저장 -- 식별용도로 계속 씀 (프로필, 추가)*/
                SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Email",stemail);
                Log.d("Sharedpreferences에 저장", "입력한 이메일: "+stemail);
                editor.commit();



                /*이메일 공백검사*/
                if(stemail.isEmpty()){
                    Toast.makeText(LoginActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
//                if(android.util.Patterns.EMAIL_ADDRESS.matcher(stemail).matches()){
//                    Toast.makeText(LoginActivity.this, "이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                if(stemail.isEmpty()){
                    Toast.makeText(LoginActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }


                if(stpassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                /*Firebase - Auth, 로그인 */
                mAuth.signInWithEmailAndPassword(stemail, stpassword)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    /*로그인 성공 했을 때 FCM토큰 값을 갱신해주어야 한다.*/

                                    /*로그인이 성공 했을 때*/
                                    Log.d("로그인", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    //프로필에 Child값
                                    useUid = user.getUid(); //현재 로그인한 상태의 UID - 필요없음
                                    Log.d("로그인", "로그인시 이메일에 맞는 UID키값 (나,A의 UId) line133:  "+useUid);


                                    /*로그인 했을때 로그인하는 유저의 ask값에 따른 액티비티 변화*/
                                    /*===============================================================================*/
                                    ChildEventListener childEventListener = new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                                            /*
                                        ========================== < Ask값 정리 > ===========================
                                        - ask = 0 | 회원가입시 | 막 회원가입한 상태 (A,B) | Connect_needAskActivity (로그인시 커플 요청을 보낼 수 있는 액티비티)
                                        - ask = 1 | 요청 보냈을때 | 상대에게 요청을 보낸 상태 (A) | PleasewaitActivity (로그인시 상대의 수락을 기다려달라는 액티비티)
                                        - ask = 2 | 요청 보냈을때 | 상대에게 요청을 받은 상태 (B) | Connect_okAskActivity (로그인시 신청을 수락할 수 있는 액티비티)
                                        - ask = 3 | 수락 했을때(★) | 내가 수락 한 상태 (A,B) | MainActivity ( 메인으로 이동)
                                        - ask = 4 | 수락 했을때 | 상대가 수락한 상태 (A) | MainActivity ( 메인, 수락을 축하하는 다이얼로그 - 다시 ask값이 3이됨)
                                        - ask = 5 | 연결을 끊었을 때 | 상대로부터 연결이 끊킨 상태(B) | MainActivity ( 연결이 끊켰다는 다이얼로그 - ask값이 0이됨)
                                        ======================================================================
                                        */
                                            String datakey = dataSnapshot.getKey();
                                            Log.d("로그인시 받아온 전체 데이터", "dataSnapshot.key: "+datakey); //확인
                                            UserData userData = dataSnapshot.getValue(UserData.class);
                                            Log.d("로그인시 받아온 데이터", "데이터객체자체 "+dataSnapshot);//확인
                                            ask2 = userData.getAsk();
                                            Log.d("로그인시 받아온 전체 데이터", "에스크값: "+ask2);//확인

                                            if(ask2 == 0 ) {
                                                //A가 요청을 안보냈을때 (회원가입만 했을때의 상태)
                                                Intent intent = new Intent(getApplicationContext(),Connect_needAskActivity.class);
                                                intent.putExtra("email",stemail); // 로그인한 A의 이메일을 받아서 B의 Other에 추가하기 위해
                                                startActivity(intent);
                                            } else if (ask2 == 1 ){
                                                //A가 요청을 보냈을때 요청 대기 상태 (상대방에게 요청을 보낸 상태)
                                                Intent intent = new Intent(getApplicationContext(),PleasewaitActivity.class);
                                                startActivity(intent);
                                            } else if (ask2 == 2 ){
                                                //들어온 요청이 있는 상태
                                                Intent intent = new Intent(getApplicationContext(),Connect_okAskActivity.class);
                                                startActivity(intent);
                                            } else if ( ask2 == 3 || ask2 == 4 ){
                                                // 둘다 수락인 상태 (수락해서 이제 메인액티비티로 가는 상태)
                                                // ask가 4이면 다이얼로그 띄움
                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                intent.putExtra("email",stemail); // 후에 채팅방에서 내 이메일을 오른쪾에 배치하기위해
                                                startActivity(intent);
                                            } else if (ask2 ==5){
                                                // 상대가 내 요청을 거절했을때의 상태
                                                Intent intent = new Intent(getApplicationContext(),Connect_needAskActivity.class);
                                                startActivity(intent);
                                            } else if(ask2 ==6){
                                                // 상대가 연결을 끊어버렸을때의 상태
                                                Intent intent = new Intent(getApplicationContext(),Connect_needAskActivity.class);
                                                startActivity(intent);
                                            }

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
                                    DatabaseReference ref = database.getReference("User").child(Key_Email);
                                    ref.addChildEventListener(childEventListener);



                                    /*===============================================================================*/
//                                    /*커플 이메일 검사*/
//                                    DatabaseReference ref2 = database.getReference("CoupleList");
//                                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                            ArrayList<CoupleListdata> coupleListdataArrayList = new ArrayList<>();
//                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 DB에 있는데이터 리스트를 추출
//                                                CoupleListdata coupleListdata = snapshot.getValue(CoupleListdata.class); // 만들어논 스토리데이터 객체에 데이터를 담는다.
//                                                coupleListdataArrayList.add(coupleListdata); // 담은 데이터들을 리스트에 넣고 리사이클러뷰로 보낼 준비
//                                                Log.d("로그인버튼눌렀을대", "커플리스트 전체 나오는지 검사: "+snapshot);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                        }
//                                    });




                                    /*===============================================================================*/

                                    /*로그인하는 이메일에 맞는 "나"의 UID를 여기서 셰어드로 저장한다음에 쭉 사용하기
                                    * 어디어디서 사용하는가? -- 사용안해도됨 */
                                    SharedPreferences sharedPreferences2 = getSharedPreferences("USER_UID",MODE_PRIVATE);
                                    SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                                    editor2.putString("UID",useUid);
                                    editor2.commit();



                                    /*로그인하는 이메일에 맞는 "나"의 이메일을 여기서 셰어드로 저장한다음에 쭉 사용하기
                                    * 키값으로 사용하기 위해 "." >> "," 으,로 변경한 뒤 저장
                                    *
                                    *>> 최종적으로 그냥 메인에서 내꺼랑 상대방 키 shared에 저장함 */
                                    SharedPreferences sharedPreferences3 = getSharedPreferences("USER_EMAIL",MODE_PRIVATE);
                                    SharedPreferences.Editor editor3 = sharedPreferences3.edit();
                                    editor3.putString("MyEmail_Key",Key_Email);
                                    editor3.commit();
                                    Log.d("Shared에 UID저장하기(로그인시)", "로그인,97 "+Key_Email);

                                    Toast.makeText(LoginActivity.this, "로그인 되었습니다.",
                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(user);


                                    /*===============================================================================*/



                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("로그인", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "이메일 혹은 비밀번호를 확인해주세요.",
                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(null);
                                }

                            }
                        });

            }
        });




        /*회원가입으로 이동*/
        btn_Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Signup_meActivity.class);
                startActivity(intent);
            }
        });
    }
}
