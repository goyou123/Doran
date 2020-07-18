package com.example.doran;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingActivity extends AppCompatActivity {
    private static final String TAG = "ChattingActivity";
    ImageView iv_Chatting_close;
    EditText et_Chatting_input;
    Button btn_Chatting_send;


    String shared_Email; //로그인시 입력한 내 이메일
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값
    String coupleKey;
    String getMyName;
    /*채팅리사이클러뷰*/
    ArrayList<Chatdata> chatdataArrayList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ChatAdapter chatAdapter;
    FirebaseDatabase database;

    Chatdata chatdata;


    //뒤로가기 버튼 이벤트 - FCM을 위해
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        database = FirebaseDatabase.getInstance(); // DB의 인스턴스 받아옴 + 전역변수로 선언해 write&read둘다 가능하게

        /*xml연결*/
        iv_Chatting_close =(ImageView)findViewById(R.id.iv_Chatting_close); // 뒤로가기
        et_Chatting_input =(EditText)findViewById(R.id.et_Chatting_input); // 채팅입력
        btn_Chatting_send =(Button)findViewById(R.id.btn_Chatting_send); // 채팅전송


        /*현재 날짜 , 시간 리사이클러뷰에 붙히기*/
        Calendar calendar = Calendar.getInstance();  // 현재의 날짜와 시간의 객체를 받아옴
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 날짜의 형식을 지정
        String datetime = dateFormat.format(calendar.getTime()); // 캘린더를 통한 현재 날짜와 시간의 객체를 위 형식에 맞게 담아줌


        /*로그인한 이메일 shared*/
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences.getString("Email",""); // 로그인시 입력 이메일 받아오기


        /*리사이러뷰*/
        recyclerView = findViewById(R.id.RcView_Chatting); // xml과 리사이클러뷰 연결
        recyclerView.setHasFixedSize(true); //사이즈조정
        layoutManager = new LinearLayoutManager(this); //레이아웃매니저 객체선언
        recyclerView.setLayoutManager(layoutManager); // 리사이클러뷰객체에 레이아웃매니저 추가
        chatdataArrayList = new ArrayList<Chatdata>(); // 채팅목록리스트 객체선언

        //하드코딩1개
//        chatdataArrayList.add(new Chatdata("실종","고은찬","안녕하세요",datetime));

        chatAdapter = new ChatAdapter(chatdataArrayList,shared_Email); //어댑터에 로그인때부터 받아온 이메일 넣기 >> 어댑터에서 내 이메일인지 검사
        recyclerView.setAdapter(chatAdapter);



        /*내 키값과 상대방 키값 불러오기*/
        SharedPreferences sharedPreferences1 = getSharedPreferences(shared_Email,MODE_PRIVATE);
        myKey = sharedPreferences1.getString("A_Key","A키없음");
        anotherKey = sharedPreferences1.getString("B_Key","B키없음");//xml파일로 확인


        /*A와 B 공유하는 키값 가져오기 - 스토리 저장 데이터베이스 경로로 사용할것*/
        SharedPreferences sharedPreferences2 = getSharedPreferences("Couplekey",MODE_PRIVATE);
        coupleKey = sharedPreferences2.getString("getCouplekey","커플공유키값없음~~~");
        Log.d("스토리 작성 저장시", "커플2명만의 키: "+coupleKey); // 확인


        /*뒤로가기*/
        iv_Chatting_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*내 이름 가져와 후에 채팅할때 붙히기*/
        ChildEventListener childEventListener2 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                //로그인한 사용자의 이름 가져오기
                UserData userData = dataSnapshot.getValue(UserData.class);
                getMyName = userData.getMyName();
                //붙히는건 DB에 데이터 올릴때

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
        DatabaseReference ref2 = database.getReference("User").child(myKey);
        ref2.addChildEventListener(childEventListener2);


        /*파이어베이스 Read===================================================================
        * 파이어베이스 DB에 저장된 형식이 메세지-(하위 이메일,텍스트 이기 때문에) 하위 값,하위이벤트(이메일,텍스트)만
        * 가져오기 위해서 하위이벤트 메소드를 사용해야 한다.
        * 실시간 DB여서 DB가 수정 될 때마다 아래 메소드들이 실행 됨*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "시간날짜?:" + dataSnapshot.getKey());

                Chatdata chat = dataSnapshot.getValue(Chatdata.class); //파이어베이스 DB에 있는 스토리 내용 불러오기
                chatAdapter.addChat(chat); // 불러와서 리사이클러뷰에 추가

                /*데이터 읽어오는거 확인*/
                String sttEmail = chat.getChatdata_email();
                String stttext = chat.getChatdata_text();
                String sttname = chat.getChatdata_name();
                String stttime = chat.getChatdata_time();
                Log.d(TAG, "onChildAdded1: "+sttEmail);
                Log.d(TAG, "onChildAdded2: "+stttext);
                Log.d(TAG, "onChildAdded3: "+sttname);
                Log.d(TAG, "onChildAdded4: "+stttime);
//                chatdataArrayList.add(chat); /*데이터베이스에 있는 내용을 불러오기*/
                chatAdapter.notifyDataSetChanged();

                /*채팅 전송하구 리사이클러뷰의 맨아래 부분이 보이도록*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(chatAdapter.getItemCount()-1);
                    }
                },10);


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
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(ChattingActivity.this, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        DatabaseReference ref = database.getReference(coupleKey).child("Message"); //추가
        ref.addChildEventListener(childEventListener);

        /*파이어베이스 READ END===========================================================*/











        /*채팅 SEND 버튼 눌렀을 때 파이어베이스에 Write=============================================*/
        btn_Chatting_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ChatText = et_Chatting_input.getText().toString(); // 내가 쓰는글
                Toast.makeText(getApplicationContext(),ChatText,Toast.LENGTH_SHORT).show(); // 토스트로 보여주기

                /*실제 채팅 전송했을 때 메세지 옆에 붙는 시간 (분단위)*/
                Calendar calendar = Calendar.getInstance();  // 현재의 날짜와 시간의 객체를 받아옴
                SimpleDateFormat dateFormat = new SimpleDateFormat("a hh:mm"); // 채팅 옆에 붙는 형식
                String datetime1 = dateFormat.format(calendar.getTime()); // 캘린더를 통한 현재 날짜와 시간의 객체를 위 형식에 맞게 담아줌

                /*파이어베이스 DB에 정렬될 시간순서 (초단위)*/
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 데이터베이스 순차적으로 나열
                String datetime2 = dateFormat2.format(calendar.getTime());

                /* 채팅내용 파이어베이스 실시간 DB에 추가하기*/
                DatabaseReference myRef = database.getReference(coupleKey).child("Message").child(datetime2);

                /*2. HashTable 공부
                * firebase 실시간 데이터 베이스에 들어갈 구조 */
                Hashtable<String,String> Chattings = new Hashtable<String, String>();
                Chattings.put("Chatdata_email",shared_Email);
                Chattings.put("Chatdata_name",getMyName);
                Chattings.put("Chatdata_text",ChatText);
                Chattings.put("Chatdata_time",datetime1);
                myRef.setValue(Chattings); //fireDB에 데이터 넣기

                // 전송버튼 눌렀을때 다시 빈칸되도록 설정
                et_Chatting_input.setText(null);



                /*FCM전송*/
                sendPostToFCM(chatdata,ChatText);















            }
        });
        /*Firebase Write End===========================================================================*/


    }//OnCreate



    private void sendPostToFCM(Chatdata chatdata, final String message){

        final String SERVER_KEY = "AAAAo4_UtdM:APA91bFS3wpZ48VgSqj0lRuW_UpyP8xJ0_8fN_9Hsb67NFcWd-rGgpQ8YP6Gc5I6zPLlAIIwAEXIvREXO6jdFuK6TXj8M4_iiu2rxhaORADJJVbAxfmHnntnfSQt7p3VJoHQCSdVOcjp";
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                /*상대의 토큰정보 빼오기 */
                //상대방의 FCM토큰은 회원가입할때 생성 및 저장된다.
                UserData userData = dataSnapshot.getValue(UserData.class);
                final String getToken = userData.getFcmToken();
                final String getname = userData.getMyName();
                Log.d("토큰토큰", "상대 토큰: "+getToken); // 이상하게 상대방토큰이 내꺼에 저장됬음
                Log.d("토큰토큰", "이름: "+getname);
                Log.d("토큰토큰", "키: "+dataSnapshot.getKey());
                Log.d("토큰토큰", "mykey: "+myKey);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //메세지 가공
                            JsonObject jsonObj = new JsonObject();

                            //토큰
                            Gson gson = new Gson();
                            JsonElement jsonElement = gson.toJsonTree(getToken); //상대방 기기 토큰
                            jsonObj.add("to", jsonElement); //상대방에게!
                            Log.d(TAG, "jsonElement: "+jsonElement); //상대방 토큰 확인
                            Log.d(TAG, "//jsonElement: "+jsonObj);

                            //노티피케이션
                            JsonObject notification = new JsonObject();
//                            notification.addProperty("title", getString(R.string.app_name)); //노티피케이션 제목 앱 이름

                            notification.addProperty("title", getname);
                            notification.addProperty("body", message); //채팅내용
                            notification.addProperty("click_action", "OPEN_ACTIVITY"); /*노티피케이션 눌렀을때 채팅으로 이동 > */
                            jsonObj.add("notification", notification);


                            Log.d(TAG, "notification jsonObj(노티피케이션add됬음): "+jsonObj);
                            Log.d(TAG, "notification title: "+getString(R.string.app_name));
                            Log.d(TAG, "notification body: "+message);
                            Log.d(TAG, "notification : "+notification);


                            //FCM서버로 전송하기 - 기존 Http API 방식
                            final MediaType mediaType = MediaType.parse("application/json");
                            OkHttpClient httpClient = new OkHttpClient();
                            try {

                                Request request = new Request.Builder()
                                        .url("https://fcm.googleapis.com/fcm/send")
                                        .addHeader("Content-Type", "application/json; UTF-8")
                                        .addHeader("Authorization", "key=" + SERVER_KEY)
                                        .post(RequestBody.create(mediaType, jsonObj.toString())).build();
                                Response response = httpClient.newCall(request).execute(); // 발송

                                String res = response.body().string(); //, 요걸 리턴함

                                Log.d(TAG, "notification response okhttp:전송 리퀘스트 "+request);
                                Log.d(TAG, "notification response okhttp:전송 리스폰스"+response);
                                Log.d(TAG, "notification response okhttp:전송확인"+res);
                               /*에러 코드별로 로그에 찍힘 - ex)CODE:400 */
                            } catch (IOException e) {
                                Log.d(TAG, "Error in sending message to FCM server: "+e);
//                                logger.info("Error in sending message to FCM server " + e);
                            }





//
//                            //FCM메세지 생성
//                            JSONObject jsonObject = new JSONObject();
//                            JSONObject notification = new JSONObject();
//                            notification.put("body",message);
//                            notification.put("title", getString(R.string.app_name));
//                            jsonObject.put("notification", notification);
//                            jsonObject.put("to", getToken);
//
//                            Log.d(TAG, "FCM스레드 메세지: "+message); //ㅇㅋ
//                            Log.d(TAG, "FCM스레드 앱네임: "+getString(R.string.app_name)); //ㅇㅋ
//                            Log.d(TAG, "FCM스레드 노티피케이션: "+notification);
//                            Log.d(TAG, "FCM스레드 토큰: "+getToken);
//                            //FCM메세시 끝
//                            FCM서버로 전송하기 - 어려운방식
//                            URL Url = new URL("https://fcm.googleapis.com/fcm/send ");
//                            HttpURLConnection connection = (HttpURLConnection) Url.openConnection();
//                            connection.setRequestMethod("POST");
//                            connection.setDoOutput(true);
//                            connection.setDoInput(true);
//                            connection.addRequestProperty("Authorization", "key=" + SERVER_KEY);
//                            connection.setRequestProperty("Accept", "application/json");
//                            connection.setRequestProperty("Content-type", "application/json");
//                            OutputStream os = connection.getOutputStream();
//                            os.write(jsonObject.toString().getBytes("utf-8"));
//                            os.flush();
//                            connection.getResponseCode();

                        }catch (Exception e){

                        }
                    }
                }).start();


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
        //상대의 정보
        DatabaseReference ref = database.getReference("User").child(anotherKey);
        ref.addChildEventListener(childEventListener);
    }






}
