package com.example.doran;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

public class DdayActivity extends AppCompatActivity implements DdayAdapter.OnListItemLongSelectedInterface{
    TextView tv_Dday_please_Choosedate,tv_Dday_Showdate;
    Button btn_Dday_datepicker;
    ImageView iv_Add_Dday,iv_Dday_close;


    ArrayList<DdayPickData> ddayPickDataArrayList;
    //리사이클러뷰
    ArrayList<DdayData> ddayDataArrayList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DdayAdapter ddayAdapter;

    FirebaseDatabase database;
    //저장
    String shared_Email; // 로그인에서 저장한 이메일 Shared에 저장해놓은거 넣을 변수
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값
    String coupleKey; // A와 B 공유하는 키값

    String D_DAY;
    String pickDday;
    //캘린더
    Calendar mCalendar;
    final int ONE_DAY = 24*60*60*1000; //Millisecond형태의 하루(24시간)

    /*DATEPICKER 리스너*/
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        //리스너를 통해 datepicker에서 선택한 날짜를 텍스트뷰에 붙힌다.
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // MONTH는 0월~11월로 계산되므로 픽할때 +1 해줘야 한다.
//            int month1 = mCalendar.get(Calendar.MONTH)+1; >> 6월로 고정됨
            int month1 = month+1;
            database = FirebaseDatabase.getInstance();


            //달력에서 선택한 사귄 날짜
            pickDday = String.format(year+"년 "+month1+"월 "+dayOfMonth+"일"); //픽하는 날짜
            tv_Dday_Showdate.setText("우리가 사귀기 시작한 날 : " + pickDday);
            Log.d("데이트피커", "PickDdaty: "+pickDday);
            Log.d("데이트피커", "텍스트뷰에 붙는 내가 선택한 날짜: "+year+"--"+month1+"--"+dayOfMonth);

            //오늘날짜 - 선택한 날짜의 차이값 = 현재까지 사귄 날짜 (밑에서 계산)
            D_DAY = getDday(year,month,dayOfMonth); // 디 데이 출력 값
            Log.d("디데이계산", "계산값: "+D_DAY);
            tv_Dday_please_Choosedate.setText(D_DAY); // 맨위 텍스트에 붙힘


            /*이전 디데이데이터 지우기*/
            DatabaseReference ref11 = database.getReference(coupleKey).child("Dday");
            ref11.removeValue();
            ddayAdapter.notifyDataSetChanged();



            /*선택한날짜, 여태사귄날짜 따로 저장*/
            DatabaseReference myRef0 = database.getReference(coupleKey).child("PickDday").child("PickDday");
            HashMap<String,String> Ddays0 = new HashMap<>();
            Ddays0.put("Firstday",pickDday);
            Ddays0.put("Ourdate",D_DAY);
            myRef0.setValue(Ddays0); //fireDB에 데이터 넣기



            //100일 후 구하기
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal100 = Calendar.getInstance();

            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,100);
            String day_100 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "100일 후 계산완료"+day_100);
//            tv_Dday_please_Choosedate.setText(day_100);

            //100일까지는 얼마나 남았는지
            final long dday100 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday100 = dday100-today;
            Log.d("데이트피커", "100일까진 몇일 남았는지: "+resultday100);

            final String strFormat;
            if (resultday100 > 0) {
                strFormat = "D-"+resultday100;
            } else if (resultday100 == 0) {
                strFormat = "D-Day";
            } else {
                resultday100 *= -1;
                strFormat = "D+"+resultday100;
            }
            final String Ddaydata_Date100 = (String.format(strFormat, resultday100));
            Log.d("데이트피커", "100일까진 몇일 남았는지: 결과" +Ddaydata_Date100);






            //200일 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,200);
            String day_200 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "200일 후 계산완료"+day_200);

            final long dday200 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today1 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday200 = dday200-today1;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday200);

            final String strFormat1;
            if (resultday200 > 0) {
                strFormat1 = "D-"+resultday200;
            } else if (resultday200 == 0) {
                strFormat1 = "D-Day";
            } else {
                resultday200 *= -1;
                strFormat1 = "D+"+resultday200;
            }
            final String Ddaydata_Date200 = (String.format(strFormat1, resultday200));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date200);



            //300일 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,300);
            String day_300 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "300일 후 계산완료"+day_300);

            final long dday300 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today300 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday300 = dday300-today300;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday300);
//
            final String strFormat300;
            if (resultday300 > 0) {
                strFormat300 = "D-"+resultday300;
            } else if (resultday300 == 0) {
                strFormat300 = "D-Day";
            } else {
                resultday300 *= -1;
                strFormat300 = "D+ "+resultday300;
            }
            final String Ddaydata_Date300 = (String.format(strFormat300, resultday300));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date300);



            //1년 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,365);
            String day_365 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "1년 후 계산완료"+day_365);

            final long dday365 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today365 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday365 = dday365-today365;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday365);
//
            final String strFormat365;
            if (resultday365 > 0) {
                strFormat365 = "D-"+resultday365;
            } else if (resultday365 == 0) {
                strFormat365 = "D-Day";
            } else {
                resultday365 *= -1;
                strFormat365 = "D+ "+resultday365;
            }
            final String Ddaydata_Date365 = (String.format(strFormat365, resultday365));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date365);







            //400일 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,400);
            String day_400 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "400일 후 계산완료"+day_400);

            final long dday400 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today400 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday400 = dday400-today400;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday400);
//
            final String strFormat400;
            if (resultday400 > 0) {
                strFormat400 = "D-"+resultday400;
            } else if (resultday400 == 0) {
                strFormat400 = "D-Day";
            } else {
                resultday400 *= -1;
                strFormat400 = "D+ "+resultday400;
            }
            final String Ddaydata_Date400 = (String.format(strFormat400, resultday400));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date400);





            //500일 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,500);
            String day_500 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "400일 후 계산완료"+day_500);

            final long dday500 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today500 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday500 = dday500-today500;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday500);
//
            final String strFormat500;
            if (resultday500 > 0) {
                strFormat500 = "D-"+resultday500;
            } else if (resultday500 == 0) {
                strFormat500 = "D-Day";
            } else {
                resultday500 *= -1;
                strFormat500 = "D+ "+resultday500;
            }
            final String Ddaydata_Date500 = (String.format(strFormat500, resultday500));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date500);





            //600일 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,600);
            String day_600 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "400일 후 계산완료"+day_600);

            final long dday600 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today600 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday600 = dday600-today600;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday600);
//
            final String strFormat600;
            if (resultday600 > 0) {
                strFormat600 = "D-"+resultday600;
            } else if (resultday600 == 0) {
                strFormat600 = "D-Day";
            } else {
                resultday600 *= -1;
                strFormat600 = "D+ "+resultday600;
            }
            final String Ddaydata_Date600 = (String.format(strFormat600, resultday600));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date600);




            //700일 후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,700);
            String day_700 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "400일 후 계산완료"+day_700);

            final long dday700 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today700 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday700 = dday700-today700;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday700);
//
            final String strFormat700;
            if (resultday700 > 0) {
                strFormat700 = "D-"+resultday700;
            } else if (resultday700 == 0) {
                strFormat700 = "D-Day";
            } else {
                resultday700 *= -1;
                strFormat700 = "D+ "+resultday700;
            }
            final String Ddaydata_Date700 = (String.format(strFormat700, resultday700));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date700);






            //2년후 구하기
            cal100.set(year,month,dayOfMonth);
            cal100.add(Calendar.DAY_OF_MONTH,730);
            String day_730 = dateFormat.format(cal100.getTime());
            Log.d("데이트피커", "400일 후 계산완료"+day_730);

            final long dday730 = cal100.getTimeInMillis() /ONE_DAY ;
            final long today730 = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
            long resultday730 = dday730-today730;
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +resultday730);
//
            final String strFormat730;
            if (resultday730 > 0) {
                strFormat730 = "D-"+resultday730;
            } else if (resultday730 == 0) {
                strFormat730 = "D-Day";
            } else {
                resultday730 *= -1;
                strFormat730 = "D+ "+resultday730;
            }
            final String Ddaydata_Date730 = (String.format(strFormat730, resultday730));
            Log.d("데이트피커", "200일까진 몇일 남았는지: 결과" +Ddaydata_Date730);












            /*DB에 저장*/
            /* 채팅내용 파이어베이스 실시간 DB에 추가하기*/
            DatabaseReference myRef = database.getReference(coupleKey).child("Dday").child(day_100); //100
             /* 100일*/
            HashMap<String,String> Ddays = new HashMap<>();

            String Ddaydata_Firstday = pickDday;
            String Ourdate = D_DAY;
            Ddays.put("Ddaydata_DateName","100일");
            Ddays.put("Ddaydata_Date",day_100);
            Ddays.put("Ddaydata_Dday",Ddaydata_Date100);
            Ddays.put("Ddaydata_Firstday",Ddaydata_Firstday);
            Ddays.put("Ourdate",Ourdate);
            myRef.setValue(Ddays); //fireDB에 데이터 넣기

            /*200일*/
            DatabaseReference myRef1 = database.getReference(coupleKey).child("Dday").child(day_200); //200
            HashMap<String,String> Ddays1 = new HashMap<>();
            Ddays1.put("Ddaydata_DateName","200일");
            Ddays1.put("Ddaydata_Date",day_200);
            Ddays1.put("Ddaydata_Dday",Ddaydata_Date200);
            Ddays1.put("Ddaydata_Firstday",pickDday);
            Ddays1.put("Ourdate",D_DAY);
            myRef1.setValue(Ddays1); //fireDB에 데이터 넣기


            /*300일*/
            DatabaseReference myRef300 = database.getReference(coupleKey).child("Dday").child(day_300); //300
            HashMap<String,String> Ddays300 = new HashMap<>();
            Ddays300.put("Ddaydata_DateName","300일");
            Ddays300.put("Ddaydata_Date",day_300);
            Ddays300.put("Ddaydata_Dday",Ddaydata_Date300);
            Ddays300.put("Ddaydata_Firstday",pickDday);
            Ddays300.put("Ourdate",D_DAY);
            myRef300.setValue(Ddays300); //fireDB에 데이터 넣기


            /*365일*/
            DatabaseReference myRef365 = database.getReference(coupleKey).child("Dday").child(day_365); //365
            HashMap<String,String> Ddays365 = new HashMap<>();
            Ddays365.put("Ddaydata_DateName","1년");
            Ddays365.put("Ddaydata_Date",day_365);
            Ddays365.put("Ddaydata_Dday",Ddaydata_Date365);
            Ddays365.put("Ddaydata_Firstday",pickDday);
            Ddays365.put("Ourdate",D_DAY);
            myRef365.setValue(Ddays365); //fireDB에 데이터 넣기


            /*400일*/
            DatabaseReference myRef400 = database.getReference(coupleKey).child("Dday").child(day_400); //400
            HashMap<String,String> Ddays400 = new HashMap<>();
            Ddays400.put("Ddaydata_DateName","400일");
            Ddays400.put("Ddaydata_Date",day_400);
            Ddays400.put("Ddaydata_Dday",Ddaydata_Date400);
            Ddays400.put("Ddaydata_Firstday",pickDday);
            Ddays400.put("Ourdate",D_DAY);
            myRef400.setValue(Ddays400); //fireDB에 데이터 넣기


            /*500일*/
            DatabaseReference myRef500 = database.getReference(coupleKey).child("Dday").child(day_500); //500
            HashMap<String,String> Ddays500 = new HashMap<>();
            Ddays500.put("Ddaydata_DateName","500일");
            Ddays500.put("Ddaydata_Date",day_500);
            Ddays500.put("Ddaydata_Dday",Ddaydata_Date500);
            Ddays500.put("Ddaydata_Firstday",pickDday);
            Ddays500.put("Ourdate",D_DAY);
            myRef500.setValue(Ddays500); //fireDB에 데이터 넣기


            /*600일*/
            DatabaseReference myRef600 = database.getReference(coupleKey).child("Dday").child(day_600); //600
            HashMap<String,String> Ddays600 = new HashMap<>();
            Ddays600.put("Ddaydata_DateName","600일");
            Ddays600.put("Ddaydata_Date",day_600);
            Ddays600.put("Ddaydata_Dday",Ddaydata_Date600);
            Ddays600.put("Ddaydata_Firstday",pickDday);
            Ddays600.put("Ourdate",D_DAY);
            myRef600.setValue(Ddays600); //fireDB에 데이터 넣기


            /*700일*/
            DatabaseReference myRef700 = database.getReference(coupleKey).child("Dday").child(day_700); //700
            HashMap<String,String> Ddays700 = new HashMap<>();
            Ddays700.put("Ddaydata_DateName","700일");
            Ddays700.put("Ddaydata_Date",day_700);
            Ddays700.put("Ddaydata_Dday",Ddaydata_Date700);
            Ddays700.put("Ddaydata_Firstday",pickDday);
            Ddays700.put("Ourdate",D_DAY);
            myRef700.setValue(Ddays700); //fireDB에 데이터 넣기


            /*2년*/
            DatabaseReference myRef730 = database.getReference(coupleKey).child("Dday").child(day_730); //730
            HashMap<String,String> Ddays730 = new HashMap<>();
            Ddays730.put("Ddaydata_DateName","2년");
            Ddays730.put("Ddaydata_Date",day_730);
            Ddays730.put("Ddaydata_Dday",Ddaydata_Date730);
            Ddays730.put("Ddaydata_Firstday",pickDday);
            Ddays730.put("Ourdate",D_DAY);
            myRef730.setValue(Ddays730); //fireDB에 데이터 넣기






            //날짜선택시 리스트를 비운 뒤에 새로운 날짜에 대한 리사이클러뷰를 새로 불러옴
            ddayDataArrayList.clear();

        }






    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dday);
        database = FirebaseDatabase.getInstance();
        /*xml연결*/
        tv_Dday_please_Choosedate = (TextView) findViewById(R.id.tv_Dday_please_Choosedate); // 날짜를선택해주세요
        tv_Dday_Showdate = (TextView) findViewById(R.id.tv_Dday_Showdate); // 선택한 날짜 보여주는 텍스트
        btn_Dday_datepicker = (Button) findViewById(R.id.btn_Dday_datepicker); // 날짜 선택 (datepicker)
        iv_Add_Dday = (ImageView) findViewById(R.id.iv_Add_Dday); // 기념일 추가 버튼
        iv_Dday_close = (ImageView) findViewById(R.id.iv_Dday_close); // 뒤로가기버튼



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
        Log.d("디데이화면들어가자마자", "커플2명만의 키: "+coupleKey); // 확인


        /*맨위 사귄날짜, 몇일사겼나 정보 DB에 있는정보 가져와서 붙히기*/
        /*데이트피커에서 날짜 선택시 DB에 있는 데이터 불러오기*/
        ChildEventListener childEventListener0 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                DdayPickData ddayPickData = dataSnapshot.getValue(DdayPickData.class);
                Log.d("디데이화면들어가서", "만난날, 사귄일수: "+dataSnapshot);
                String firstDay = ddayPickData.getFirstday();
                String ourdate = ddayPickData.getOurdate();
                tv_Dday_Showdate.setText("우리가 사귀기 시작한날 : " + firstDay);
                tv_Dday_please_Choosedate.setText(ourdate); // 맨위 텍스트에 붙힘

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                DdayPickData ddayPickData = dataSnapshot.getValue(DdayPickData.class);
                Log.d("디데이화면들어가서", "만난날, 사귄일수: "+dataSnapshot);
                String firstDay = ddayPickData.getFirstday();
                String ourdate = ddayPickData.getOurdate();
                tv_Dday_Showdate.setText("사귄 날 : " + firstDay);
                tv_Dday_please_Choosedate.setText(ourdate); // 맨위 텍스트에 붙힘
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




        /*리사이클러뷰*/
        recyclerView = findViewById(R.id.RcView_Dday); // 리사이클러뷰 연결
        recyclerView.setHasFixedSize(true); //사이즈조정
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager); // 레이아웃매니저 장착

        ddayDataArrayList = new ArrayList<DdayData>(); // 데이터리스트 객체 생성
//        ddayDataArrayList.add(new DdayData("100일",pickDday+100,"100","500")); //하드코딩
//        ddayDataArrayList.add(new DdayData()); //하드코딩


        ddayAdapter = new DdayAdapter(ddayDataArrayList,this,this); // 어댑터 안에 리스트
        recyclerView.setAdapter(ddayAdapter); // 리사이클러뷰에 어댑터 장착


//        /*디데이 리사이클러뷰 아이템 클릭리스너 추가*/
//        ddayAdapter.setOnItemClickListener(new OnDdayItemClickListener() {
//            @Override
//            public void onItemClick(DdayAdapter.DdayViewHolder holder, View view, int position) {
//                Toast.makeText(DdayActivity.this, position+"번 위치 클릭", Toast.LENGTH_SHORT).show();
//            }
//        });





//        /*addValueEventListener*/
//        DatabaseReference ref = database.getReference(coupleKey).child("Dday");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                ddayDataArrayList.clear();
//                Log.d("디데이 벨류이벤트리스너", "onDataChange: "+dataSnapshot); // ㅇㅋ
//
//                for (DataSnapshot snapshott : dataSnapshot.getChildren()){
//                    String keys = snapshott.getKey();
//                    Log.d("디데이 벨류이벤트", "키: "+keys);
//                    DdayData ddayData = dataSnapshot.getValue(DdayData.class);
//                    ddayDataArrayList.add(ddayData);
//                    Log.d("디데이 벨류이벤트", "리스트: "+ddayDataArrayList);
//                    //키값만 다 가져옴
//                }
//
//                ddayAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });



        /*데이트피커에서 날짜 선택시 DB에 있는 데이터 리스트로 불러오기*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                //처음 데이터 불러올때
                DdayData ddayData = dataSnapshot.getValue(DdayData.class);

                /*데이터 읽어오는거 확인*/
                Log.d("디데이화면", "디데이데이터: "+dataSnapshot); //확인
                ddayDataArrayList.add(ddayData);
                ddayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

//
//                Log.d("디데이화면", "onchanged메소드실행되나: ");
//                //데이터가 변경됬을 때
//                Log.d("디데이화면 onchanged", "클리어: "+ddayDataArrayList);
//                DdayData ddayData = dataSnapshot.getValue(DdayData.class);
//                Log.d("디데이화면", "ddayData: "+ddayData);
//                ddayDataArrayList.add(ddayData); // 다시 추가가 아니고 변경하는 방법?
//                Log.d("디데이화면", "add한 뒤: "+ddayDataArrayList);
//
//                /*데이터 읽어오는거 확인*/
//                Log.d("디데이화면 onchanged", "다시추가: "+ddayDataArrayList.size());
//                ddayAdapter.notifyDataSetChanged();
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
        DatabaseReference ref = database.getReference(coupleKey).child("Dday");
        ref.addChildEventListener(childEventListener);








        /*뒤로가기*/
        iv_Dday_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*디데이추가*/
        iv_Add_Dday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AddDdayActivity.class);
                startActivity(intent);
            }
        });


        /*데이트피커*/
        Locale.setDefault(Locale.KOREAN); // 한국어 설정
        mCalendar = new GregorianCalendar(); // 현재 날짜를 알기 위해 사용
        btn_Dday_datepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"캘린더나옴",Toast.LENGTH_SHORT).show();
                mCalendar = new GregorianCalendar();
                int year = mCalendar.get(Calendar.YEAR);
                final int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DATE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(DdayActivity.this,onDateSetListener, year,month,day);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); //오늘 날짜까지만 선택 가능하도록 제한
                datePickerDialog.show();
                Log.d("디데이데이트피커", "datepicker: "+year+"/"+month+"/"+day);
            }
        });
    }//OnCreate



    /* D-day 반환해주는 */
    private String getDday(int a_year,int a_month ,int a_day){

        final  Calendar ddayCalendar = Calendar.getInstance(); //현재 시간을 받아옴
        ddayCalendar.set(a_year, a_month, a_day); //현재 시간을 년도, 달, 일까지 계산

        // D-day 를 구하기 위해 millisecond 으로 환산하여 d-day 에서 today 의 차를 구한다.
        final long dday = ddayCalendar.getTimeInMillis() /ONE_DAY ;
        final long today = Calendar.getInstance().getTimeInMillis()/ONE_DAY ; //오늘날
//        final Date dday = ddayCalendar.getTime();
//        final Date today = Calendar.getInstance().getTime();//오늘날
        Log.d("디데이계산", "dday: "+dday);
        Log.d("디데이계산", "today: "+today);
        long result = dday - today;
        Log.d("디데이계산", "날짜뺀값 "+result);

        // 출력 시 d-day 에 맞게 표시
        final String strFormat;
        if (result > 0) {
            strFormat = "D-"+result;
        } else if (result == 0) {
            strFormat = "♥ 사랑한지 0일 째 ♥";
        } else {
            result *= -1;
            strFormat = "♥ 사랑한지 "+ result + "일 째 ♥";
        }

        final String strCount = (String.format(strFormat, result));
        return strCount;
    }


    /*리사이클러뷰 롱클릭시*/
    @Override
    public void onItemLongClick(View v, final int position) {
        Toast.makeText(this, "롱클릭성공"+position, Toast.LENGTH_SHORT).show();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); //다이얼로그 빌드 객체 생성
        builder.setTitle("삭제");
        builder.setMessage("기념일을 삭제하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d("포지션", "onClick: "+position);
                /*리사이클러뷰 삭제하기*/
                String datekeys = ddayDataArrayList.get(position).getDdaydata_Date();
                // 리스트를 가져온다
                // 리스트 안 하나의 객체를 가져온다
                // 그 객체로부터 키값을 추출한다.
                // 키값 삭제 + 리스트에서도 삭제
                ddayDataArrayList.remove(position);
                DatabaseReference ref = database.getReference(coupleKey).child("Dday").child(datekeys);
                ref.removeValue();
                Toast.makeText(getApplicationContext(),"삭제가 완료되었습니다.",Toast.LENGTH_LONG).show();
                ddayAdapter.notifyDataSetChanged();

            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }
}
