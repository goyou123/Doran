package com.example.doran;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class StoryclickActivity extends AppCompatActivity {

    //xml
    Button btn_Storyclick_edit,btn_Storyclick_delete;
    ImageView iv_Storyclick_image;
    TextView tv_Storyclick_name,tv_Storyclick_content,tv_Storyclick_date;

    //전달받은 인텐트데이터 받을 변수
    String intentName,intentContent,intentImage,intentDate,intentPushKey;
    int intentPosition;

    //파이어베이스
    FirebaseDatabase database;

    String coupleKey;
    StoryAdapter storyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storyclick);

        database = FirebaseDatabase.getInstance();

        /*xml연결*/
        btn_Storyclick_edit = (Button)findViewById(R.id.btn_Storyclick_edit); // 글 수정 버튼
        btn_Storyclick_delete = (Button)findViewById(R.id.btn_Storyclick_delete); // 글 삭제 버튼
        iv_Storyclick_image = (ImageView)findViewById(R.id.iv_Storyclick_image); // 스토리 상세 이미지
        tv_Storyclick_name = (TextView)findViewById(R.id.tv_Storyclick_name); // 스토리 상세 글 제목
        tv_Storyclick_content = (TextView)findViewById(R.id.tv_Storyclick_content); // 스토리 상세 글 내용
        tv_Storyclick_date = (TextView)findViewById(R.id.tv_Storyclick_date); // 스토리 상세 글 날짜

        /*A와 B 공유하는 키값 가져오기 - 스토리 저장 데이터베이스 경로로 사용할것*/
        SharedPreferences sharedPreferences2 = getSharedPreferences("Couplekey",MODE_PRIVATE);
        coupleKey = sharedPreferences2.getString("getCouplekey","커플공유키값없음~~~");
        Log.d("디데이화면들어가자마자", "커플2명만의 키: "+coupleKey); // 확인

        /*리사이클러뷰에서 인텐트로 넘긴 데이터 받기*/
        Intent intent = getIntent(); //받는 인텐트 선언
        intentName = intent.getStringExtra("intent_name"); //제목 받기
        intentContent = intent.getStringExtra("intent_content");//내용 받기
        intentImage = intent.getStringExtra("intent_image");//이미지 받기
        intentDate = intent.getStringExtra("intent_date");//날짜 받기
        intentPushKey = intent.getStringExtra("intent_pushkey");// 푸시키 받기
        intentPosition = intent.getIntExtra("position",0); // 포지션값 받기
        Log.d("클릭해서 들어온리사이클러뷰에서", "데이터 잘 받는지: /"+intentName+"/"+intentContent+"/"+intentImage+"/"+intentDate+"/"+intentPosition);
        Log.d("클릭해서 들어온리사이클러뷰에서", "포시젼값: /"+intentPosition);
        //로그 확인 완료


        /*스토리 화면에서 인텐트로 전달받은 데이터들 배치
        * 상세화면 완료*/
        tv_Storyclick_name.setText(intentName);
        tv_Storyclick_content.setText(intentContent);
        tv_Storyclick_date.setText(intentDate);
        Glide.with(getApplicationContext()).load(intentImage).into(iv_Storyclick_image); //글라이드로 사진 넣기


        /*상세페이지에서 스토리 삭제하기*/
        btn_Storyclick_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*어떻게 각 데이터마다의 push키값을 가져올 것인가 - 후에 생각 , position값에 맞는 push키
                * 그uid값을 저장시키고 인텐트로 넘김*/
                Log.d("스토리삭제버튼클릭시", "그 게시글에 맞는 푸시키 인텐트로받았: "+intentPushKey);
                Log.d("스토리삭제버튼클릭시", "그 게시글에 맞는 커플키 쉐어드로받았: "+coupleKey);
                dialogshow(); // 다이얼로그 호출

//                DatabaseReference ref = database.getReference(coupleKey).child("Story").child(intentPushKey);
//                ref.removeValue();

//                Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();

            }
        });




        /*스토리 수정하기 버튼 클릭시*/
        btn_Storyclick_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //글 작성화면으로 게시글 값데이터, 푸시키, 포시션값 전달하기!
                Intent intent = new Intent(getApplicationContext(),WritestoryActivity.class);
                intent.putExtra("수정할제목",intentName);
                intent.putExtra("수정할내용",intentContent);
                intent.putExtra("수정할사진",intentImage);
                intent.putExtra("수정할날짜",intentDate);
                intent.putExtra("수정푸시키",intentPushKey);
                intent.putExtra("수정포지션",intentPosition);
                startActivity(intent);
                Log.d("스토리 상세화면에서 수정화면으로", "인텐트로 데이터 전달: "+intentName+intentContent+intentDate);
                Log.d("스토리 상세화면에서 수정화면으로", "인텐트로 데이터 전달: "+intentImage);
                Log.d("스토리 상세화면에서 수정화면으로", "인텐트로 데이터 전달: "+intentPushKey);
                Log.d("스토리 상세화면에서 수정화면으로", "인텐트로 데이터 전달: "+intentPosition);

//                DatabaseReference ref = database.getReference(coupleKey).child("Story").child(intentPushKey);


            }
        });




    }//OnCreate

    /*삭제시 예 or 아니오 다이얼로그 호출하는 메소드*/
    void dialogshow(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); //다이얼로그 빌드 객체 생성
        builder.setTitle("삭제");
        builder.setMessage("소중한 추억을 정말 삭제하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        /*리사이클러뷰 삭제하기 추가 */
                        DatabaseReference ref = database.getReference(coupleKey).child("Story").child(intentPushKey);
                        ref.removeValue();
                        Toast.makeText(getApplicationContext(),"삭제가 완료되었습니다.",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"삭제가 취소되었습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }
}
