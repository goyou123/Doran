package com.example.doran;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MyprofileActivity extends AppCompatActivity {

    ImageView iv_Myprofile_close, iv_Myprofile_man;
    Button btn_Myprofile_edit;
    TextView tv_Myprofile_name, tv_Myprofile_email, tv_Myprofile_phonenum;
    String Shared_Email; // Shared에서 저장된 이메일 값 받아오는 변수
    String email_key; // 그 받아온 이메일 키로 만들변수

    FirebaseDatabase database; //데이터베이스에 접근할 수 있는 진입점 클래스
    int REQUEST_IMAGE_CODE = 1003; // 갤러리 actResult
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1004; // 권한 요청 상수

    private DatabaseReference mDatabase; //데이터베이스의 주소를 저장
    private StorageReference mStorageRef; // Firebase Storage
    File localFile; //Firebase Storage

    String UserUId;
    String getDownlodaUrl; // 프로필 사진을 Storage에 올렸을때 저장된 URI의 값
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        database = FirebaseDatabase.getInstance(); //파이어베이스 데이터베이스 진입
        mStorageRef = FirebaseStorage.getInstance().getReference(); // 파이어베이스 스토리지 진입

//        mDatabase = FirebaseDatabase.getInstance().getReference(); //현재 데이터베이스를 접근할수있는 진입점

//        /*UID받아오기-Shared*/
//        SharedPreferences sharedPreferences1 = getSharedPreferences("유저UID",MODE_PRIVATE);
//        String Shared_userUID = sharedPreferences1.getString("UserUId","");
//        Log.d("UID", "유아이디: "+Shared_userUID);




        /*xml연결*/
        iv_Myprofile_man = (ImageView) findViewById(R.id.iv_Myprofile_man); //내 프로필사진
        iv_Myprofile_close = (ImageView) findViewById(R.id.iv_Myprofile_close); //뒤로가기
        btn_Myprofile_edit = (Button) findViewById(R.id.btn_Myprofile_edit); // 프로필 수정하기
        tv_Myprofile_name = (TextView) findViewById(R.id.tv_Myprofile_name); // 이름
        tv_Myprofile_email = (TextView) findViewById(R.id.tv_Myprofile_email); // 이메일
        tv_Myprofile_phonenum = (TextView) findViewById(R.id.tv_Myprofile_phonenum); // 폰번호



        /*로그인창에서 입력한 이메일 shared로 저장한거 가져오기 */
        SharedPreferences sharedPreferences = getSharedPreferences("Profile_Email", MODE_PRIVATE);
        Shared_Email = sharedPreferences.getString("Email", ""); // 입력 이메일 받아오기
        Log.d("이메일", "onCreate: " + Shared_Email);
        email_key = Shared_Email.replace(".", ","); // 키값으로 만들기


//        /*파이어베이스 스토리지에 있는 메인사진 불러와서 메인에 넣기*/
//        try {
//            localFile = File.createTempFile("images","jpg");
//            StorageReference reference = mStorageRef.child("profileImage").child(Shared_Email+"/profileimg.jpg");
//            reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    Glide.with(getApplicationContext()).load(localFile).into(iv_Myprofile_man);
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//    }


        /* 내  이메일, 이름, 핸드폰번호를 프로필에 등록함*/
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                /*A의 정보*/
                Log.d("프로필에서 데이터", "들어오긴하나 " + dataSnapshot); // 확인
                UserData userData = dataSnapshot.getValue(UserData.class);

                // 가져온 데이터 변수에 담기 (이메일,이름,핸드폰번호)
                String my_email = userData.getMyEmail();
                String my_name = userData.getMyName();
                String my_phonenum = userData.getMyPhonenum();
                Log.d("프로필에서 데이터들", "onChildAdded: " + my_email + my_name + my_phonenum); //확인

                //TextView에 붙히기
                tv_Myprofile_name.setText(my_name);
                tv_Myprofile_email.setText(my_email);
                tv_Myprofile_phonenum.setText(my_phonenum);

                //처음 생성시는 기본 이미지
                String getMyProfileImg = userData.getProfileImage();
                if(getMyProfileImg.equals("none")){
                    iv_Myprofile_man.setImageResource(R.drawable.profile);
                }else{
                    Glide.with(getApplicationContext()).load(getMyProfileImg).into(iv_Myprofile_man);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                /*A의 정보*/
                UserData userData = dataSnapshot.getValue(UserData.class);

                //프로필 이미지 변경됬을때 가져와서 붙히기
//                String getMyProfileImg = userData.getProfileImage();
//                Glide.with(getApplicationContext()).load(getMyProfileImg).into(iv_Myprofile_man);

                //그외 정보들 변경될때 붙히기
                String my_email = userData.getMyEmail();
                String my_name = userData.getMyName();
                String my_phonenum = userData.getMyPhonenum();

                tv_Myprofile_name.setText(my_name);
                tv_Myprofile_email.setText(my_email);
                tv_Myprofile_phonenum.setText(my_phonenum);
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
        //로그인한 유저의 데이터베이스 경로
        DatabaseReference ref = database.getReference("User").child(email_key);
        ref.addChildEventListener(childEventListener);











        /*뒤로가기*/
        iv_Myprofile_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*프로필변경화면으로 이동*/
        btn_Myprofile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChangeprofileActivity
                        .class);
                startActivity(intent);
            }
        });



        /*사진클릭시 갤러리로 이동*/
        iv_Myprofile_man.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 갤러리로 이동
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);

//                /*외부자원 가져올시 권한 요청*/
//                // Here, thisActivity is the current activity
//                if (ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // Permission is not granted
//                    // Should we show an explanation?
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(MyprofileActivity.this,
//                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                        // Show an explanation to the user *asynchronously* -- don't block
//                        // this thread waiting for the user's response! After the user
//                        // sees the explanation, try again to request the permission.
//                    } else {
//                        // No explanation needed; request the permission
//                        ActivityCompat.requestPermissions(MyprofileActivity.this,
//                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                REQUEST_EXTERNAL_STORAGE_PERMISSION);
//
//                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                        // app-defined int constant. The callback method gets the
//                        // result of the request.
//                    }
//                } else {
//                    // Permission has already been granted
//                }

            }
        });

    }//OnCreate

    @Override
    protected void onResume() {
        super.onResume();
//        /*프로필변경된게 finish해도 보이도록 여기서 내 프로필 정보를 불러오기*/
//        /* 내  이메일, 이름, 핸드폰번호를 프로필에 등록함*/
//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//
//                // UID의 유저 하나의 객체의 전체 데이터가 다 불러와짐
//                Log.d("프로필에서 데이터", "들어오긴하나 " + dataSnapshot); // 확인
//                UserData userData = dataSnapshot.getValue(UserData.class);
//
//                // 가져온 데이터 변수에 담기 (이메일,이름,핸드폰번호)
//                String my_email = userData.getMyEmail();
//                String my_name = userData.getMyName();
//                String my_phonenum = userData.getMyPhonenum();
//                Log.d("프로필에서 데이터들", "onChildAdded: " + my_email + my_name + my_phonenum); //확인
//
//                //TextView에 붙히기
//                tv_Myprofile_name.setText(my_name);
//                tv_Myprofile_email.setText(my_email);
//                tv_Myprofile_phonenum.setText(my_phonenum);
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//        //로그인한 유저의 데이터베이스 경로
//        DatabaseReference ref = database.getReference("User").child(email_key);
//        ref.addChildEventListener(childEventListener);


    }



    /*갤러리에서 가져온 이미지를 이미지뷰에 붙히고 Storage에 올려얗함*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //코드가 일치할때 , +예외처리
        if (requestCode == REQUEST_IMAGE_CODE && resultCode == RESULT_OK && data != null) {

            // 이미지 주소를 데이터에서 가져온다
            Uri image = data.getData();

            // DB에 올리기 위해 스트링화시킴
//            String upload_ProfileImage = image.toString();
//            Log.d("내 프로필화면 ", "DB에 저장하고싶은 URI "+image);
//            Log.d("내 프로필화면 ", "DB에 저장하고싶은 URI 스트링"+upload_ProfileImage);
//
//
//            /*가져온 이미지 uri를 내 유저객체의 DB에도 저장하기 -- 후에 상대방 프로필 사진가져오기위해서 */
//            /*멘토에게 질문 >> Uri값을 데이터베이스에 넣을려니까 찾을수 없는 에러가 뜸  >> toString()으로 해결*/

//            //나의 데이터베이스경로에 앨범에서 선택한 사진 경로를 저장
//            /* 그런데 앨범선택사진경로가 아니라 스토리지에 올라간 사진 경로를 저장하는게 낫지 않을까? 고민해보기*/
//            DatabaseReference myRef = database.getReference("User").child(email_key).child(email_key); //나의 데이터베이스경로에
//            HashMap<String,Object> UserProfile = new HashMap<>();
//            UserProfile.put("ProfileImage",upload_ProfileImage); //이때 처음으로 프로필 값을 설정
//            myRef.updateChildren(UserProfile); //변경된 값을 저장한다.


            try {
                /* 앨범에서 가져온 URI를 바로 이미지뷰에 장착
                * 이미지를 이미지뷰에 붙히는 2가지 방법*/

                // 1. 비트맵 - URI를 비트맵으로 전환,비트맵형태로 이미지를 이미지뷰에 올리기
                // 비트맵 아닐경우 try/catch 필요없음 , 왜 비트맵은 try/catch 가 필요할까?
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), image);
//                iv_Myprofile_man.setImageBitmap(bitmap);

                /* 2. 글라이드 이용시*/
                Glide.with(this).load(image).into(iv_Myprofile_man);

            } catch (IOException e) {
                e.printStackTrace();
            }


            /*파이어베이스 Storage에도 올리기
            * 올린 뒤 그 Uri를 내 유저 객체에 추가 - 그래야 상대방도 내 프로필을 볼 수있음
            * >>메인에서 이름 불러올때랑 비슷하게 ㅇㅇ*/

            // 스토리지에 저장하는 경로
            StorageReference reference = mStorageRef.child("profileImage").child(Shared_Email+"/profileimg.jpg");

            // 위 갤러리에서 가져온 이미지 Uri를 가지고 올림
            reference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /*파이어베이스 스토리지에 저장이 됬을 때*/
                    Log.d("내 프로필 화면", "프로필이미지 스토리이제 올라가나: "+taskSnapshot.toString());

                    /*스토리지에 저장된 사진의 URI값 가져오기*/
                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            getDownlodaUrl = uri.toString();
                            Log.d("스토리지에 이미지를 올렸을때", "이미지의 uri가져오기 "+getDownlodaUrl);

                            /* 그런데 앨범선택사진경로가 아니라 스토리지에 올라간 사진 경로를 저장하는게 낫지 않을까? 고민해보기
                            *  내 유저 객체에 Storage의 uri값 저장시키기*/
                            DatabaseReference myRef = database.getReference("User").child(email_key).child(email_key); //나의 데이터베이스경로에
                            HashMap<String,Object> UserProfile = new HashMap<>();
                            UserProfile.put("ProfileImage",getDownlodaUrl); //이때 처음으로 프로필 값을 설정
                            myRef.updateChildren(UserProfile); //변경된 값을 저장한다.

                            //프로필변경 성공 토스트
                            Toast.makeText(MyprofileActivity.this, "프로필이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
            
//            /*저장한 스토리지에서 url가져오기*/
//            String getDownlodaUrl = mStorageRef.child("profileImage").child(Shared_Email+"/profileimg.jpg").getDownloadUrl().getResult().toString();
//            Log.d("저장한 스토리지에서 url가져오기", "onActivityResult: "+getDownlodaUrl);

        }
    }
}


