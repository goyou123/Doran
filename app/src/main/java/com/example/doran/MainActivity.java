package com.example.doran;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Context context;
    TextView tv_DAY,tv_main_promise,tv_Myname,tv_Hername; //일 수
    ImageView iv_Myprofile,iv_Herprofile,iv_main_image,iv_setting_icon,iv_main_imageClick; //내 프로필, 상대프로필, 메인이미지, 세팅아이콘
    Button btn_main_Dday,btn_main_Chat,btn_main_Story,btn_main_Datepick; // 메인메뉴 4개
    String myemail; // 로그인시 내 이메일 인텐트로 받는 변수
    String shared_Email; // 로그인시 내 이메일 shared로 받는 변수
    String myEmail_Key; // shared로 받은 이메일 키로 만들 변수
    String another_email; // 내 객체에 있는 상대 이메일
    String anotherEmail_Key; // 그 상대이메일을 가지고 상대방에게 접근하기 위한 키
    String coupleKey;
    int REQUEST_IMAGE_CODE = 1001; // 갤러리 actResult
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    int loginUserAsk; // 로그인한 유저의 ask값;

    private StorageReference mStorageRef; // Firebase Storage
    File localFile; //Firebase Storage / 임시로 담을 데이터
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef = FirebaseStorage.getInstance().getReference(); // Firebase Storage
        database = FirebaseDatabase.getInstance(); //데이터베이스 참조



        /*xml연결*/
        tv_DAY = (TextView) findViewById(R.id.tv_DAY); //일수
        tv_main_promise = (TextView) findViewById(R.id.tv_main_promise); // 나의 다짐
        tv_Myname= (TextView) findViewById(R.id.tv_Myname); // 프로필 밑 내 이름
        tv_Hername = (TextView) findViewById(R.id.tv_Hername); //프로필 밑 상대 이름
        iv_Myprofile = (ImageView) findViewById(R.id.iv_Myprofile); //내 프로필
        iv_Herprofile = (ImageView) findViewById(R.id.iv_Herprofile); //상대 프로필
        iv_main_image = (ImageView) findViewById(R.id.iv_main_image); // 메인이미지
        iv_setting_icon =(ImageView) findViewById(R.id.iv_setting_icon); // 세팅아이콘
        iv_main_imageClick = (ImageView) findViewById(R.id.iv_main_imageClick); // 메인사진 투명도
        btn_main_Dday = (Button) findViewById(R.id.btn_main_Dday); // 기념일버튼
        btn_main_Chat = (Button) findViewById(R.id.btn_main_Chat); // 채팅버튼
        btn_main_Story = (Button) findViewById(R.id.btn_main_Story); // 스토리 버튼
        btn_main_Datepick = (Button) findViewById(R.id.btn_main_Datepick);// 데이트추천버튼




        /*파이어베이스 Storage 있는 사진(메인화면 배경이미지) 불러와서 메인이미지뷰에 넣기*/
        try {
            //일지적인 로컬파일을 생성해서 그 로컬파일에 이미지 저장하기
            localFile = File.createTempFile("images", "jpg");

            //스토리지에 저장했던 파일 위치 그대로 가져오기 (올리는 위치 == 받는 위치)
            StorageReference riversRef = mStorageRef.child("mainimage/coupleA.jpg");

            //레퍼런스에서 데이터를 받아와서 localFile에 저장하겠다
            riversRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            /*localFile로 받아온 이미지를 비트맵으로 전환하기
                             * 1. 비트맵으로 변환해서 사용하는 방법*/
//                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath()); // 전환
//                            iv_main_image.setImageBitmap(bitmap);

                            /* 2. Glide방법 - 이미지뷰에 uri을 사용해 쉽게 넣기*/
                            Glide.with(getApplicationContext()).load(localFile).into(iv_main_image);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }



        /*로그인시 입력한 이메일 Shared로 꺼내와서 + 내 데이터베이스에 접근할 키로 반들기*/
        SharedPreferences sharedPreferences5 = getSharedPreferences("Profile_Email",MODE_PRIVATE);
        shared_Email = sharedPreferences5.getString("Email","값없음ㅋㅋ");
        myEmail_Key = shared_Email.replace(".",","); // 로그인한 A의 키값.
        Log.d("메인화면에서 onCreate", "일때 로그인때입력한 이메일 키값만들기: "+myEmail_Key); // 확인



        /*A와 B 공유하는 키값 가져오기 - 스토리 저장 데이터베이스 경로로 사용할것*/
        SharedPreferences sharedPreferences2 = getSharedPreferences("Couplekey",MODE_PRIVATE);
        coupleKey = sharedPreferences2.getString("getCouplekey","커플공유키값없음~~~");
        Log.d("디데이화면들어가자마자", "커플2명만의 키: "+coupleKey); // 확인


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




        /* 끄적끄적
         * 만약 로그인하는 객체의 ask가 4인 경우 (요청했고 상대가 수락 해준 경우)
         * 들어가자마자 @@님이 수락했다는 다이얼로그 띄어준다.
         * 그리고 이 메세지는 한번만 띄우면 되므로 다이얼로그를 닫을 때 다시 ask의 값을 3으로 변경한다. >> 다이얼로그때 바꾸면 이미지가 이상하게 적용됨
         * */


        /*로그인한 객체의 ask값이 4이면 커플요청이 다이얼로그를 띄움*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                /*A의 정보*/
                // 로그인한 A의 유저 데이터 불러와 데이터클래스에 넣기
                UserData userData = dataSnapshot.getValue(UserData.class);
                Log.d("메인화면에서 OnCreate", "일때 로그인한유저데이터불러오기: "+dataSnapshot); //확인

                /*메인화면 진입시 만약 ask가 4인 유저(요청을 수락받은 유저)면 다이얼로그 띄어줌*/
                loginUserAsk = userData.getAsk(); // 로그인한 나의 ask값 = 4
                if (loginUserAsk == 4){
                    Ask4_CustomDialog dialog = new Ask4_CustomDialog(MainActivity.this);
                    dialog.calldialog(); // 이 함수 안에서 ask값을 다시 3으로 바꿈 >> onChildAdded 마지막에 ask값을 3으로 바꿈

                }
//                else if(loginUserAsk == 6){
//                    /*상대방이 연결을 끊어버린 상태 */
//                    Intent intent = new Intent(MainActivity.this,Connect_needAskActivity.class);
//                    startActivity(intent);
//                }



                /* 이런 키값들을 shared에 저장해서 계속 쓰면 어떨까? */
                another_email = userData.getOther(); // 로그인한 나의 상대 이메일값
                anotherEmail_Key = another_email.replace(".",","); // 상대의 키값
                Log.d("메인,로그인한유저의 상대방이메일을", "키값으로: "+anotherEmail_Key); //확인
                Log.d("메인,로그인한유저의 ask값", "처음엔 4 여야함: "+loginUserAsk); //확인


                /* 데이터베이스에 접근할 A와 B의 <KEY> 를 shared에 저장
                 * 로그인할때 입력하는 A 이메일이 키값이 되어 안에 A와 B 키값이 저장됨
                 * 다른액티비티에서 불러올라면 shared에 저장된 로그인시 입력한 이메일을 먼저 가져오고, 그 다음 키값을 뺌
                 * --상대방 프로필 화면에서 예시로 사용해보기    -- 사용 완료*/
                SharedPreferences sharedPreferences = getSharedPreferences(shared_Email,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("B_Key",anotherEmail_Key);
                editor.putString("A_Key",myEmail_Key);
                editor.commit(); //저장된거 xml파일로 확인했음


                // A의 이름값만 가져오기
                String getMyname = userData.getMyName();
                tv_Myname.setText(getMyname); //프사밑에 내 이름 붙히기 완료


                //처음에 생성했을때 프로필사진에 내 사진 불러오기
                /*처음 메인화면 생성시 내 유저정보에 url이 없으면 기본이미지, 있으면 url불러오기*/
                String getProfileUri = userData.getProfileImage();
                Log.d("메인화면생성시 내유저정보에 url", "ㅇ "+getProfileUri);
                if (getProfileUri.equals("none") ){
                    Log.d("메인화면생성시 내유저정보에 url", "NULL일때: "+getProfileUri);
                    iv_Myprofile.setImageResource(R.drawable.profile2);
                } else {
                    Log.d("메인화면생성시 내유저정보에 url", "존재할때: "+getProfileUri);
                    Glide.with(MainActivity.this).load(getProfileUri).into(iv_Myprofile);
                }



//                // A의 ask값, 상대이메일값 불러오기
//                /* 1. A의 ask값 =ask값을 구별해 다이얼로그 호출을 위해 */
//                /* 2. 상대 이메일값을 이용해 상대데이터에 접근할 키로 만들기 위해 */
//                loginUserAsk = userData.getAsk();
//                another_email = userData.getOther(); // 로그인한 나의 상대 이메일값


                /*메인화면으로 오면 B가 수락할때 생성되는 커플 키를 추출하여 sharedpreference에 저장해 채팅과 스토리에서 사용한다.*/
                String getCouplekey = userData.getCoupleKey();
                SharedPreferences sharedPreferences11 = getSharedPreferences("Couplekey",MODE_PRIVATE);
                SharedPreferences.Editor editor11 = sharedPreferences11.edit();
                editor11.putString("getCouplekey",getCouplekey);
                editor11.commit();
                Log.d("메인화면에서", "커플수락시 만든 커플키값 저장, 후에 데이터베이스접근 경로로 사용: "+getCouplekey);




                /*=========================================================================================================*/
                /*내꺼(A)의 OnchildAdd메소드의 마지막에 ask값을 3으로 변경한다 == 그래야 이 후 업데이트가 안되서 메인화면 프로필기본사진이 남아있음*/
                DatabaseReference myRef = database.getReference("User").child(myEmail_Key).child(myEmail_Key); //데이터베이스경로에
                HashMap<String,Object> UserProfile = new HashMap<>();
                UserProfile.put("ask",3);
                myRef.updateChildren(UserProfile); //값을 저장한다.
                /*=========================================================================================================*/




                /*상대 유저 이름을 불러와서 메인화면의 상대방 프로필 이름 밑에 저장 <B의 정보>*/
                ChildEventListener childEventListener1 = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                        /*B의 정보*/
                        /*처음에 메인화면 생성될 때 상대 프로필, 이름 불러오기*/
                        // 상대 (B)의 유저 데이터 불러오기
                        UserData userData1 = dataSnapshot.getValue(UserData.class);
                            Log.d("메인,상대방 유저객체", "전제데이터: "+dataSnapshot); //확인

                        // 상대방 유저 이름 가져오기
                        String herName = userData1.getMyName();
                            Log.d("메인,상대방 유저객체", "상대방이름: "+herName); //확인

                        // 상대 프사 밑에 상대이름 붙히기 완료
                        tv_Hername.setText(herName);

                        // 상대 프로필 이미지 가져오기
                        String herProfileUri = userData1.getProfileImage();


//                         메인액티비티 - 상대 프사에 상대 이미지를 붙힌다
                        if (herProfileUri.equals("none")){
                            // 처음에 이미지 없을시에는 기본 이미지
                            iv_Herprofile.setImageResource(R.drawable.profile);
                        }else{
                            // 이미지가 있으면 그걸로 붙히기
                            Glide.with(MainActivity.this).load(herProfileUri).into(iv_Herprofile);
                        }


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                        /*B의 정보*/
                        /*상대방 프사가 변경되었을 때 내 화면에서 자동으로 상대프로필 ,이름 변경적용되도록*/
                        // 상대 (B)의 유저 데이터 불러오기
                        UserData userData = dataSnapshot.getValue(UserData.class);

                        // 상대 프로필 이미지 가져오기
                        String herProfileUri = userData.getProfileImage();

                        if (herProfileUri.equals("none")){
                            iv_Herprofile.setImageResource(R.drawable.profile);
                        }else{
                            Glide.with(MainActivity.this).load(herProfileUri).into(iv_Herprofile);
                        }


                        // 상대방 유저 이름 가져오기
                        String herName = userData.getMyName();
                        Log.d("메인,상대방 유저객체", "상대방이름: "+herName); //확인
                        tv_Hername.setText(herName);


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
                // B의 경로
                DatabaseReference ref1 = database.getReference("User").child(anotherEmail_Key);
                ref1.addChildEventListener(childEventListener1);



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                /*A의 정보*/
                UserData userData = dataSnapshot.getValue(UserData.class);
                int changeAsk = userData.getAsk();
                 if(changeAsk == 6){
                    /*상대방이 연결을 끊어버린 상태*/
                     //요청보내는화면으로 이동하고
                     Intent intent = new Intent(MainActivity.this,Connect_needAskActivity.class);
                     startActivity(intent);
                     Toast.makeText(MainActivity.this, "상대방이 연결을 끊었습니다..힘내세요", Toast.LENGTH_LONG).show();
                     // 다시 내 유저객체의 값을 0으로 바꿈
//                     DatabaseReference myRef1 = database.getReference("User").child(myEmail_Key).child(myEmail_Key); // 상대의 경로
//                     HashMap<String,Object> UserProfile1 = new HashMap<>();
//                     UserProfile1.put("Other","");
//                     UserProfile1.put("ask",0);
//                     myRef1.updateChildren(UserProfile1); //값을 저장한다.

                }

                // 내가 내 프로필 이름 변경했을때 메인화면에서 바로 반영되도록
                String getMyname = userData.getMyName();
                tv_Myname.setText(getMyname); //프사밑에 내 이름 붙히기 완료

                // 내가 내 프로필 사진 변경했을때 메인화면에서 바로 반영되도록
                String getProfileUri = userData.getProfileImage();
                if (getProfileUri.equals("none") ){
                    iv_Myprofile.setImageResource(R.drawable.profile2);
                } else {
                    Glide.with(MainActivity.this).load(getProfileUri).into(iv_Myprofile);

                }

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
        // 현재 로그인한 A의 경로
        DatabaseReference ref = database.getReference("User").child(myEmail_Key);
        ref.addChildEventListener(childEventListener);









        /*메인화면에 얼마나 사겼는지 정보를 DB에서 꺼내와 배치시키기*/
        /*데이트피커에서 날짜 선택시 DB에 있는 데이터 불러오기*/
        ChildEventListener childEventListener0 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                DdayPickData ddayPickData = dataSnapshot.getValue(DdayPickData.class);
                Log.d("디데이화면들어가서", "만난날, 사귄일수: "+dataSnapshot);
                String firstDay = ddayPickData.getFirstday();
                String ourdate = ddayPickData.getOurdate();
                tv_DAY.setText(ourdate);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                DdayPickData ddayPickData = dataSnapshot.getValue(DdayPickData.class);
                Log.d("디데이화면들어가서", "만난날, 사귄일수: "+dataSnapshot);
                String firstDay = ddayPickData.getFirstday();
                String ourdate = ddayPickData.getOurdate();
                tv_DAY.setText(ourdate);
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










        /*기념일로 이동*/
        btn_main_Dday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),DdayActivity.class);
                startActivity(intent);
            }
        });

        /*내 프로필로 이동*/
        iv_Myprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MyprofileActivity.class);
                startActivity(intent);
            }
        });

        /*상대 프로필로 이동*/
        iv_Herprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),HerprofileActivity.class);
                startActivity(intent);
            }
        });

        /*채팅으로 이동*/
        btn_main_Chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ChattingActivity.class);

                //로그인 - 메인 받은 이메일 넘기기 = 넘길필요없음
                intent.putExtra("email",myemail);
                startActivity(intent);
                Log.d("메인에서 받은 이메일", "채팅으로 보내기"+myemail); // 확인
            }
        });

        /*스토리로 이동*/
        btn_main_Story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
                startActivity(intent);
            }
        });

        /*환경설정으로 이동*/
        iv_setting_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });

        /*데이트룰렛으로 이동*/
        btn_main_Datepick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RandomDateActivity.class);
                startActivity(intent);
            }
        });

        /*나의 다짐 다이얼로그*/
        tv_main_promise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Mypromise_CustomDialog mypromise_customDialog = new Mypromise_CustomDialog(MainActivity.this); // 커스텀다이얼로그 생성

                // callFuntion이라는 메소드는 커스텀 다이얼로그에서 생성
                // 커스텀 다이얼로그의 결과를 출력할 Textview를 매개변수로 넘겨줌
                mypromise_customDialog.callFuntion(tv_main_promise);

                /* + onResume에서 setText(); */
            }
        });




        /*메인이미지 변경*/
        iv_main_imageClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //갤러리로 이동
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,REQUEST_IMAGE_CODE);

                /*외부자원 가져올시 권한 요청 -- 이 부분 공부 필요*/
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_EXTERNAL_STORAGE_PERMISSION);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted
                }

            }
        });

    } //OnCreate


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*메인화면 배경 이미지를 위해 갤러리에서 사진을 가져온 후 사진을 이미지뷰에 넣고 Firebase Storage에도 올리기*/
        //코드가 일치할때 , +예외처리
        if (requestCode ==REQUEST_IMAGE_CODE && resultCode ==RESULT_OK && data!=null){

            // 이미지 주소를 데이터에서 가져온다
            Uri image = data.getData();
            try {
                // 이미지 데이터를 비트맵으로 전환,비트맵형태로 바꿔줘야 이미지뷰에 넣을 수 있음. or 글라이더 사용해도 됨
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),image);

                // 이미지뷰에 장착
                iv_main_image.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }


            /*파이어베이스 Storage에 사진 올리기*/
//            Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg")); 이미 위에 받아옴

            // 스토리지의 저장경로
            StorageReference riversRef = mStorageRef.child("mainimage/coupleA.jpg");

            riversRef.putFile(image)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("메인 파이어 스토리지 사진", "잘 올라가나: "+taskSnapshot.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });

        }
    } // OnActivityResult


    /*권한 요청 체크하기 - 167Line , 공부필요*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onResume() {
        super.onResume();


        /*나의 다짐 다이얼로그에서 작성한 내용 텍스트뷰에 붙히기*/
        SharedPreferences sharedPreferences = getSharedPreferences("다이얼로그",MODE_PRIVATE);
        String promise1 = sharedPreferences.getString("promise","클릭하여 나의 다짐을 입력해주세요!");
        tv_main_promise.setText(promise1);


    } // OnResume

}
