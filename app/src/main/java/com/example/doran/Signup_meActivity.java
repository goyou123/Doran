package com.example.doran;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Pattern;


public class Signup_meActivity extends AppCompatActivity {

    EditText et_signupMe_name,et_signupMe_email,et_signupMe_pw,et_signupMe_pwcheck,et_signupMe_phonenum; //xml
    Button btn_signupMe_back,btn_signupMe_next; //xml
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    String MyName,MyEmail,MyPassword,MyPWCheck,MyPhonenum; //실제 입력하는 회원정보들
    ImageView iv_PwCheckImage;
    String UserUId;
    String CoupleKey;
    String token;

    String key_Email; // . 을 , 으로 바꾼 이메일 값 - DB키
//    String emailValidation = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"; // 이메일 체크 정규식

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_me);
        mAuth = FirebaseAuth.getInstance(); // 파이어베이스 인스턴스 가져오기
        database = FirebaseDatabase.getInstance(); // DB의 인스턴스 받아옴 + 전역변수로 선언해 write&read둘다 가능하게
        context = this;
        /*xml연결*/
        btn_signupMe_next = (Button) findViewById(R.id.et_signupMe_next); // 상대방과 연결으로 가는 버튼
        btn_signupMe_back = (Button) findViewById(R.id.et_signupMe_back); // 뒤로가기 버튼
        et_signupMe_name = (EditText) findViewById(R.id.et_signupMe_name); // 이름 입력
        et_signupMe_email = (EditText) findViewById(R.id.et_signupMe_email); // 이메일 입력
        et_signupMe_pw = (EditText) findViewById(R.id.et_signupMe_pw); // 비밀번호 입력
        et_signupMe_pwcheck = (EditText) findViewById(R.id.et_signupMe_pwcheck); // 비밀번호 재입력
        et_signupMe_phonenum = (EditText) findViewById(R.id.et_signupMe_phonenum); // 핸드폰번호 입력
        iv_PwCheckImage = (ImageView) findViewById(R.id.iv_PwCheckImage); // 비번중복 X/O




        /*비밀번호 중복 체크
        * 텍스트 변화감지*/
        et_signupMe_pwcheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { //텍스트가 바뀔떄
                String Pw = et_signupMe_pw.getText().toString(); // 비밀번호 입력값
                String pwCheck = et_signupMe_pwcheck.getText().toString(); //비밀번호 재입력값
                if (Pw.equals(pwCheck)){ // 서로 같으면 체크
                    iv_PwCheckImage.setImageResource(R.drawable.check);
                } else if(pwCheck.isEmpty()){ // 비밀번호재입력값이 없으면 null
                    iv_PwCheckImage.setImageResource(0);
                }else{ // 틀리면 X
                    iv_PwCheckImage.setImageResource(R.drawable.xxxxx);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });







        /*다음단계 버튼 눌렀을때*/
        btn_signupMe_next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { //콜백메소드

                 MyName = et_signupMe_name.getText().toString(); //입력이름
                 MyEmail = et_signupMe_email.getText().toString(); // 입력이메일
                 MyPassword = et_signupMe_pw.getText().toString(); // 입력비밀번호
                 MyPWCheck = et_signupMe_pwcheck.getText().toString(); // 입력비밀번호체크
                 MyPhonenum = et_signupMe_phonenum.getText().toString(); // 입력폰번호



                /*DB에 저장하기 위한 Key값 이메일 */
                key_Email = MyEmail.replace(".",",");
                Log.d("회원가입", "새로운 이메일 키: "+key_Email);

//                /*회원가입 시 예외처리*/
//                //이름 공백 입력
//                if(MyName.isEmpty()){
//                    Toast.makeText(Signup_meActivity.this,"이름을 입력해주세요",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //이메일 공백 입력
//                if(MyEmail.isEmpty()){
//                    Toast.makeText(Signup_meActivity.this,"이메일을 입력해주세요",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //이메일 유효성 검사
//                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(MyEmail).matches()){
//                    Toast.makeText(Signup_meActivity.this, "이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //비밀번호 공백 입력
//                if(MyPassword.isEmpty()){
//                    Toast.makeText(Signup_meActivity.this,"비밀번호를 입력해주세요",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //비밀번호 글자 수 6자로 이상으로 제한
//                if(MyPassword.length()<6){
//                    Toast.makeText(Signup_meActivity.this,"비밀번호는 6자 이상 입력해야 합니다.",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //비밀번호 중복체크 공백시
//                if(MyPWCheck.isEmpty()){
//                    Toast.makeText(Signup_meActivity.this,"비밀번호를 재입력해주세요",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //핸드폰번호 공백
//                if(MyPhonenum.isEmpty()){
//                    Toast.makeText(Signup_meActivity.this,"핸드폰번호를 입력해주세요",Toast.LENGTH_SHORT).show();
//                    return;
//                }
////                  핸드폰 번호 유효성 검사
////                else if(!Pattern.matches("^01(?:0|1|[6-9]) - (?:\\d{3}|\\d{4}) - \\d{4}$",MyPhonenum)){
////                    Toast.makeText(Signup_meActivity.this,"올바른 형식의 핸드폰 번호를 입력해주세요",Toast.LENGTH_SHORT).show();
////                    return;
////                }
//
//
//
//
//
                /*파이어베이스 (회원가입 버튼 클릭 안임) */
                /*입력한 이메일, 비밀번호를 사용해 파이어베이스Auth에 신규회원데이터 추가,저장하기*/
                mAuth.createUserWithEmailAndPassword(MyEmail, MyPassword)
                        .addOnCompleteListener(Signup_meActivity.this, new OnCompleteListener<AuthResult>() {
//리스너가 언제 호출되는지, 콜백 메서드 이해 필요 (누가 호출해줘야함) / 리스너
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) { // 회원가입 성공시

                                if (task.isSuccessful()) {
                                   /*회원가입이 성공했을때*/

                                    /*회원가입하는 이메일의 UID 받아오기*/
                                    Log.d("회원가입시", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser(); //Auth에서 현재유저 받아옴
                                    Log.d("회원가입시user", "의 정보: "+user);
                                    UserUId = user.getUid(); //회원가입 성공했을시 그 아이디(이메일의 UID받아옴)
                                    Log.d("회원가입시", "유저의 UID: "+UserUId); // Auth의 UID랑 비교해보기


                                    /*FCM 토큰*/
                                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w("토큰토큰", "getInstanceId failed", task.getException());
                                                return;
                                            }
                                        //서비스에서 생성된 토큰 가져오기

                                            /*FCM끄적끄적
                                            * 토큰 == 기기식별번호이므로 회원가입시 폰과 로그인시 폰이 다를 수 있다. 그러므로 로그인시에도 토큰을 갱신해주어야 한다.*/
                                            token = task.getResult().getToken();
                                            Log.d("토큰토큰", "onComplete: 토큰토큰"+token);

                                            /*파이어베이스 */
                                            /*파이어베이스 데이터베이스에 나머지 회원정보 추가,저장하기*/
                                            int ask = 0;// 요청 했을때, 요청 안했을때, 수락시(3이면 메인으로 이동가능) 처음 기본 0으로 시작
                                            String Other = "null"; //상대방 없음
                                            String ProfileImage = "none"; //회원가입시 유저 프로필이미지 없음
                                            CoupleKey = "null";
                                            DatabaseReference myRef = database.getReference("User").child(key_Email).child(key_Email);
                                            HashMap<String,Object> UserProfile = new HashMap<>();
                                            UserProfile.put("MyName",MyName);
                                            UserProfile.put("MyEmail",MyEmail);
                                            UserProfile.put("MyPhonenum",MyPhonenum);
                                            UserProfile.put("ProfileImage",ProfileImage);
                                            UserProfile.put("ask",ask); // 요청 했을때, 요청 안했을때, 수락시(3이면 메인으로 이동가능)
                                            UserProfile.put("Other",Other); // 상대방
                                            UserProfile.put("MyUid",UserUId); // 누가 나에게 신청했는지 이메일 -- 없어도됨
                                            UserProfile.put("CoupleKey",CoupleKey); // 커플 키값 처음엔 X -- 수락시 생성
                                            UserProfile.put("fcmToken",token); // FCM 토큰 값
                                            Log.d("토큰토큰", "유저객체에저장: "+token);
                                            //누가 나랑 커플인지도 필요한가?
                                            myRef.setValue(UserProfile); //fireDB에 데이터 넣기
                                            Log.d("회원가입시", "onComplete: 파이어베이스 DB에 "+myRef +"로 데이터 올라갔음");
                                            Log.d("회원가입시", "파이어베이스에"+Other+UserUId);


                                            /*쉐어드로 이름만 저장해서 메인에서 내 이름 보여주기*/
                                            SharedPreferences sharedPreferences = getSharedPreferences("유저이메일별이름",MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            //키 = 회원가입 시 입력한 이메일 // 밸류 = 회원가입시 입력한 이름
                                            editor.putString(MyEmail,MyName);
                                            editor.commit();
                                            Log.d("회원가입Shared이름", "onComplete: "+ editor.putString("이름",MyName));
                                            Log.d("회원가입Shared이름", "onComplete: "+ MyName);

                                            Toast.makeText(Signup_meActivity.this, "회원가입에 성공하였습니다.",
                                                    Toast.LENGTH_SHORT).show();


                                        }
                                    });



                                    /*로그인화면으로 이동하기*/
                                    Intent intent = new Intent(Signup_meActivity.this,LoginActivity.class);
                                    startActivity(intent);

//                                    updateUI(user);
                                } else {
                                    // 회원가입 실패시 메세지
                                    Log.w("드디어", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Signup_meActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(null);

                                }


                            }
                        });



            }
        });


        /*뒤로가기*/
        btn_signupMe_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }//OnCreate




    /*로그인 여부를 처음에 검색해줌 - firebase*/
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        UserUId = currentUser.getUid();
//        Log.d("회원가입,OnStart", "UserUId: "+UserUId);
            //로그인되어있는 UID
    }

}
