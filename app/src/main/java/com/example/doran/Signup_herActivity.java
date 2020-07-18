package com.example.doran;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Signup_herActivity extends AppCompatActivity {

    EditText et_signupHer_name,et_signupHer_email,et_signupHer_phonenum;
    Button btn_signupHer_back,btn_signupHer_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_her);


        /*xml연결*/
        et_signupHer_name = (EditText) findViewById(R.id.et_signupHer_name); // 상대이름작성
        et_signupHer_email = (EditText) findViewById(R.id.et_signupHer_email); // 상대이메일작성
        et_signupHer_phonenum = (EditText) findViewById(R.id.et_signupHer_phonenum); // 상대폰번호작성 >> 인텐트로 넘겨서 상대프로필전화까지
        btn_signupHer_connect =(Button) findViewById(R.id.btn_signupHer_connect); // 회원가입완료, 로그인화면으로 가는 버튼
        btn_signupHer_back = (Button)findViewById(R.id.btn_signupHer_back); // 뒤로가기 버튼


        /*로그인화면으로 이동*/
        btn_signupHer_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });


        /*뒤로가기*/

    }//Oncreate
}
