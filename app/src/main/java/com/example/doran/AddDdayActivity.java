package com.example.doran;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class AddDdayActivity extends AppCompatActivity {
    Button btn_addDday_datepicker;
    TextView tv_addDday_Showdate;
    EditText et_addDday_NewDateName;
    Button btn_addDay_back,btn_addDay_Add;
    ImageView iv_addDday_close;

    //파이어베이스
    FirebaseDatabase database;
    //이메일&키
    String coupleKey;

    //캘린더
    Calendar mCalendar;
    String pickday;
    final int ONE_DAY = 24*60*60*1000; //Millisecond형태의 하루(24시간)
    String Ddaydata_Date;


    /*DATEPICKER 리스너*/
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        //리스너를 통해 datepicker에서 선택한 날짜를 텍스트뷰에 붙힌다.
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            //월에다가는 +1 해줘야함 , 월은 0~11로 취급
            int month1 = month+1;

            // DB에 순서를 맞추기 위해 month1 과 day 앞에 0 을 붙혀줌
            String plusZero_month1 = String.format("%02d",month1);
            String plusZero2_dayOfMonth = String.format("%02d",dayOfMonth);
            Log.d("앞에0붙히기", "onDateSet: " + plusZero_month1+plusZero2_dayOfMonth); //확인

            //내가 선택한 날짜 텍스트뷰에 붙히기
            pickday = String.format(year+"-"+plusZero_month1+"-"+plusZero2_dayOfMonth); // 이 변수는 데이터 저장시 활용
            tv_addDday_Showdate.setText(pickday);
            Log.d("데이트피커",  "텍스트뷰에 붙는 내가 선택한 날짜: "+year+"--"+plusZero_month1+"--"+plusZero2_dayOfMonth); //확인



            /*내가 선택한 날짜가 오늘로부터 얼마나 남았는지 구하기*/
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();

            cal.set(year,month,dayOfMonth);
            String dayday = dateFormat.format(cal.getTime());
            Log.d("데이트피커", "선택한날짜 계산완료"+dayday);

            final long pickdate = cal.getTimeInMillis() /ONE_DAY ;
            final long today = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday = pickdate-today;
            Log.d("데이트피커", "선택한날짜는 오늘로부터 몇일 남았는지: "+resultday);

            final String strFormat;
            if (resultday > 0) {
                strFormat = "D-"+resultday;
            } else if (resultday == 0) {
                strFormat = "D-Day";
            } else {
                resultday *= -1;
                strFormat = "D+"+resultday;
            }
            Ddaydata_Date = (String.format(strFormat, resultday));
            Log.d("데이트피커", "선택한날짜는 오늘로부터 몇일 남았는지: 결과" +Ddaydata_Date);




        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dday);
        database = FirebaseDatabase.getInstance();

        //디데이 계산 여기서 해서 넘겨버리기
        /*xml*/
        iv_addDday_close = (ImageView)findViewById(R.id.iv_addDday_close); //뒤로가기(상단)
        btn_addDday_datepicker = (Button)findViewById(R.id.btn_addDday_datepicker); // 날짜선택(datepicker)
        tv_addDday_Showdate = (TextView)findViewById(R.id.tv_addDday_Showdate); // 선택한날짜 보여주는 텍스트
        et_addDday_NewDateName = (EditText)findViewById(R.id.et_addDday_NewDateName); // 새로운 기념일 이름 입력
        btn_addDay_back = (Button)findViewById(R.id.btn_addDay_back); // 이전 버튼
        btn_addDay_Add = (Button)findViewById(R.id.btn_addDay_Add); // 저장 버튼


        /*A와 B 공유하는 키값 가져오기 - 스토리 저장 데이터베이스 경로로 사용할것*/
        SharedPreferences sharedPreferences2 = getSharedPreferences("Couplekey",MODE_PRIVATE);
        coupleKey = sharedPreferences2.getString("getCouplekey","커플공유키값없음~~~");
        Log.d("디데이화면들어가자마자", "커플2명만의 키: "+coupleKey); // 확인


        /*DatePicker 해서 날짜 칸에 날짜 붙히기*/
        btn_addDday_datepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCalendar = new GregorianCalendar();
                int year = mCalendar.get(Calendar.YEAR);
                final int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DATE);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddDdayActivity.this,onDateSetListener,
                        year,month,day);
                datePickerDialog.show();
                Log.d("데이트피커", "datepicker: "+year+"/"+month+"/"+day);
            }
        });



        /*기념일 추가하기*/
        btn_addDay_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("기념일추가버튼 클릭", "픽데이 - DBkey값: "+pickday); // 확인
                Log.d("기념일추가버튼 클릭", "디데이값 : "+Ddaydata_Date); // 확인

                //작성한 기념일 제목
                String add_Dday_name = et_addDday_NewDateName.getText().toString();

                //공백 예외 처리
                if(add_Dday_name==null){
                    Toast.makeText(AddDdayActivity.this, "기념일 제목은 필수로 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pickday==null){
                    Toast.makeText(AddDdayActivity.this, "날짜는 필수로 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //DB 저장 경로
                DatabaseReference myRef = database.getReference(coupleKey).child("Dday").child(pickday);

                /* 내가 선택한 기념일 데이터베이스에 추가*/
                HashMap<String,String> Ddays = new HashMap<>();
                Ddays.put("Ddaydata_DateName",add_Dday_name);
                Ddays.put("Ddaydata_Date",pickday);
                Ddays.put("Ddaydata_Dday",Ddaydata_Date);
                Ddays.put("Ddaydata_Firstday","none");
                Ddays.put("Ourdate","none");
                myRef.setValue(Ddays); //fireDB에 데이터 넣기

                Intent intent = new Intent(getApplicationContext(),DdayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });







        /*뒤로가기*/
        iv_addDday_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*이전버튼*/
        btn_addDay_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });








    }//OnCreate
}
