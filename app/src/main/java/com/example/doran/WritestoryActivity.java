package com.example.doran;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Objects;

public class WritestoryActivity extends AppCompatActivity {
    public int REQUEST_IMAGE_CODE = 100; // 갤러리 연동시 상수값
    ImageView iv_WirteStory_close,iv_WriteStory_AddImage,iv_WirteStory_Image;
    EditText et_WriteStory_Name,et_WriteStory_Content;
    TextView tv_WriteStory_addStory,tv_WriteStory_DatePicker;
    ProgressBar progressBar;

    public static Context mcontext;
    String pushkey;

    //전역변수
    String story_name,story_content,story_date,story_image,story_who;
    Uri getimage;
    FirebaseDatabase database;
    Calendar calendar;
    Calendar mCalendar;

    String shared_Email; // 로그인에서 저장한 이메일 Shared에 저장해놓은거 넣을 변수
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값
    String coupleKey; // A와 B 공유하는 키값

    //Storage관련
    public String getDownlodaStoryUrl; // 글 작성시 스토리지에 저장한 url주소
    private StorageReference mStorageRef; // Firebase Storage
    File localFile; //Firebase Storage

    //수정화면에서 필요한 인텐트로 전달받은 데이터
    String intentName,intentContent,intentImage,intentDate,intentPushKey,intentPosition;

    /*DATEPICKER 리스너*/
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        //리스너를 통해 datepicker에서 선택한 날짜를 텍스트뷰에 붙힌다.
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//            int month1 = mCalendar.get(Calendar.MONTH)+1;
            int month1 = month+1;
            tv_WriteStory_DatePicker.setText(String.format(year+"."+month1+"."+dayOfMonth));
            Log.d("데이트피커",  "텍스트뷰에 붙는 내가 선택한 날짜: "+year+"--"+month1+"--"+dayOfMonth);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writestory);
        database = FirebaseDatabase.getInstance(); // DB의 인스턴스 받아옴 + 전역변수로 선언해 write&read둘다 가능하게
        mStorageRef = FirebaseStorage.getInstance().getReference(); // Firebase Storage
        mcontext = this;


        /*xml연결*/
        iv_WirteStory_close = (ImageView)findViewById(R.id.iv_WirteStory_close); // 뒤로가기
        iv_WriteStory_AddImage = (ImageView)findViewById(R.id.iv_WriteStory_AddImage); //이미지 추가 버튼
        iv_WirteStory_Image= (ImageView)findViewById(R.id.iv_WirteStory_Image); //이미지 장착하는 뷰
        et_WriteStory_Name = (EditText)findViewById(R.id.et_WriteStory_Name); // 글 제목
        et_WriteStory_Content = (EditText)findViewById(R.id.et_WriteStory_Content); // 글 내용
        tv_WriteStory_addStory = (TextView)findViewById(R.id.tv_WriteStory_addStory); // 글 저장하기 버튼
        tv_WriteStory_DatePicker = (TextView) findViewById(R.id.tv_WriteStory_DatePicker); // 날짜선택버튼
        progressBar = (ProgressBar)findViewById(R.id.WriteStory_Progressbar);

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

        /*이미지뷰에 처음이미지*/
        Drawable drawable = getResources().getDrawable(R.drawable.defaultimg);
        Glide.with(WritestoryActivity.this).load(drawable).into(iv_WirteStory_Image);


        /*게시글 수정하기로 들어와서 인텐트로 받는 데이터들*/
        Intent intent = getIntent();
        intentName = intent.getStringExtra("수정할제목");
        intentContent = intent.getStringExtra("수정할내용");
        intentImage = intent.getStringExtra("수정할사진");
        intentDate = intent.getStringExtra("수정할날짜");
        intentPushKey = intent.getStringExtra("수정푸시키");
        intentPosition = intent.getStringExtra("수정포지션");

        Log.d("글 수정시", "인텐트로받음 제목"+intentName);
        Log.d("글 수정시", "인텐트로받음 내용"+intentContent);
        Log.d("글 수정시", "인텐트로받음 사진"+intentImage);
        Log.d("글 수정시", "인텐트로받음 날짜"+intentDate);
        Log.d("글 수정시", "인텐트로받음 푸시키"+intentPushKey);
        Log.d("글 수정시", "인텐트로받음 포지션"+intentPosition); //null


        //수정화면 만들기 - 데이터 붙히기
        et_WriteStory_Name.setText(intentName);
        et_WriteStory_Content.setText(intentContent);
        Glide.with(WritestoryActivity.this).load(intentImage).into(iv_WirteStory_Image);
        tv_WriteStory_DatePicker.setText(intentDate);






        /*로그인한 유저 객체의 이름만 가져오기 (글쓴이 확인을 위해)*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                UserData userData = dataSnapshot.getValue(UserData.class);

                //후에 글 작성자로 남겨둠
                story_who = userData.getMyName();

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
        //로그인한 유저의 데이터베이스 경로
        DatabaseReference ref = database.getReference("User").child(myKey);
        ref.addChildEventListener(childEventListener);






        /*처음 들어가면 날짜텍스트뷰에 오늘 날짜 표시*/
         calendar = Calendar.getInstance();  // 현재의 날짜와 시간의 객체를 받아옴
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd"); // 날짜의 형식을 지정
        String datetime = dateFormat.format(calendar.getTime()); // 캘린더를 통한 현재 날짜와 시간의 객체를 위 형식에 맞게 담아줌
        tv_WriteStory_DatePicker.setText(datetime); // 텍스트뷰에 현재 날짜 올리기
        Log.d("데이트피커", "현재날짜 "+datetime);

        

        /*DatePicker 해서 날짜 칸에 날짜 붙히기*/
        tv_WriteStory_DatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar = new GregorianCalendar();
                 int year = mCalendar.get(Calendar.YEAR);
                 final int month = mCalendar.get(Calendar.MONTH);
                 int day = mCalendar.get(Calendar.DATE);
//                 Date time = calendar.getTime();
                Log.d("데이트피커", "MONTH+1한값 "+month);
                DatePickerDialog datePickerDialog = new DatePickerDialog(WritestoryActivity.this,onDateSetListener,
                        year,month,day);

                datePickerDialog.show();
                Log.d("데이트피커", "datepicker: "+year+"/"+month+"/"+day);
            }
        });




        /*저장하기 눌렀을때 파이어베이스에 데이터 추가 - FireBase Write*/
        tv_WriteStory_addStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("수정으로 들어와서 ", "인텐트제목"+intentName);
                Log.d("수정으로 들어와서 ", "인텐트제목"+intentImage);
                progressBar.setVisibility(View.VISIBLE);
                /*게시글 추가*/
                //입력한 값 가져오기
                story_name = et_WriteStory_Name.getText().toString();
                story_content = et_WriteStory_Content.getText().toString();
                story_date = tv_WriteStory_DatePicker.getText().toString();







                /*파이어베이스 Storage에 사진 올리기*/
                // 순차저장을 위해 고유한 키값으로 현재시간을 사용
                Calendar calendar = Calendar.getInstance();  // 현재의 날짜와 시간의 객체를 받아옴
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 날짜의 형식을 지정
                String datetime = dateFormat.format(calendar.getTime()); // 캘린더를 통한 현재 날짜와 시간의 객체를 위 형식에 맞게 담아줌

                // 파이어베이스 스토리지 <스토리> 글 사진 저장 경로
                StorageReference riversRef = mStorageRef.child("storyimage").child(coupleKey).child(datetime+".jpg"); //storage사진경로

                /*만약 갤러리에서 불러온 사진이 없다면 기본이미지 세팅*/
                if(getimage == null){ /*갤러리에서 사진 선택 X시(기본글쓰기)*/
                    if (intentImage == null){ /*인텐트로 불러온 사진이 있을때 (기본글쓰기)*/
                        Log.d("가져온사진urL이 없으면", "기본사진으로 저장: ");
                        DatabaseReference myRef3 = database.getReference(coupleKey).child("Story").push(); //스토리 저장 경로
                        pushkey = myRef3.getKey(); // push키 값 받아오기 - 나중에 게시글 수정, 삭제 시

                        //이 기본이미지는 파이어베이스 스토리지에 따로 저장되있음
                        String basicImage = "https://firebasestorage.googleapis.com/v0/b/doran-5661b.appspot.com/o/defaulfimages%2Fdefaultimg.png?alt=media&token=add21f62-e074-4968-82fc-4aa3e0039ccf";
                        HashMap<String, String> Storys3 = new HashMap<String, String>();
                        Storys3.put("Storydata_name",story_name);
                        Storys3.put("Storydata_context",story_content);
                        Storys3.put("Storydata_date",story_date);
                        Storys3.put("Storydata_who",story_who); // 작성자
                        Storys3.put("pushkey",pushkey);
                        Storys3.put("Storydata_image",basicImage);
                        myRef3.setValue(Storys3); //fireDB에 데이터 넣기

                        Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }else if(intentImage != null){ /*갤러리에서 사진 X 시 + 인텐트로 불러온 사진이 있을때 == (수정) */
                        Log.d("갤러리에서 가져온 사진 없을때", "인텐트제목 != null이라서 수정시 실행: ");
                        DatabaseReference myRef4 = database.getReference(coupleKey).child("Story").child(intentPushKey); //스토리 저장 경로
                        pushkey = myRef4.getKey(); // push키 값 받아오기 - 나중에 게시글 수정, 삭제 시
                        /*수정화면 저장하기*/
                        HashMap<String, Object> Storys4 = new HashMap<>();
                        Storys4.put("Storydata_name",story_name);
                        Storys4.put("Storydata_context",story_content);
                        Storys4.put("Storydata_date",story_date);
                        Storys4.put("Storydata_who",story_who); // 작성자
                        Storys4.put("pushkey",pushkey);

                        if (getDownlodaStoryUrl==null){
                            //만약 갤러리에서 사진 선택 안했을때 인텐트로 넘어온 이미지 추가
                            Storys4.put("Storydata_image",intentImage);

                        } else if (getDownlodaStoryUrl!=null){
                            //갤러리에서 불러온 사진이 있으면 그걸로 저장
                            Storys4.put("Storydata_image",getDownlodaStoryUrl); /*스토리지의 url로 저장하기*/
                            Log.d("갤러리에서선택함", "onClick: "+getDownlodaStoryUrl);
                        }

                        /*업데이트칠드런으로 수정만하기*/
                        myRef4.updateChildren(Storys4);

                        Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }



                /*갤러리에서 불러온 사진이 있을 시에 스토리지에 먼저 사진을 저장하고 그 url을 사용해 게시글 객체에 저장한다.*/
                }else if(getimage!=null){ /*갤러리에서 불러온 사진이 있을 시 (기본글쓰기)*/

                    //파이어베이스 Storage에 Uri데이터를 올리는 메소드 (putfile)
                    riversRef.putFile(getimage)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    /*저장버튼 클릭시  스토리지에 올린 사진의 URI경로 뽑아오기 == 게시글 객체에 저장*/
                                    Log.d("파이어 스토리지 사진", "잘 올라가나: "+taskSnapshot.toString());
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            //스토리지에 저장이 성공했을때 스토리지폴더에 저장된 URI를 가져옴
                                            getDownlodaStoryUrl = uri.toString();
                                            Log.d("스토리작성시 갤러리에서 가져온 이미지", "의 스토리지에 저장된 URI: "+getDownlodaStoryUrl);
                                            Toast.makeText(mcontext, "이미지가 스토리지에 저장되었습니다.", Toast.LENGTH_SHORT).show();

//                                            //프로그래스바
//                                            progressBar.setVisibility(View.VISIBLE);
//
                                            //스토리지에 저장 된 후 저장되게끔
                                            /*파이어베이스DB에 내가 쓴 스토리데이터 저장하기*/

                                            /*일반적으로 글 추가시, 수정화면에서 글 추가시*/
                                            Log.d("수정할때 인텐트이름 없나 있나 체크", "있으면 수정, 없으면 글추가: "+intentName);
                                            if(intentName==null){
                                                DatabaseReference myRef = database.getReference(coupleKey).child("Story").push(); //스토리 저장 경로
                                                pushkey = myRef.getKey(); // push키 값 받아오기
                                                Log.d("일반저장", "인텐트제목이 null이라 일반저장실행: "); // 확인완료
                                                HashMap<String, String> Storys = new HashMap<String, String>();
                                                Storys.put("Storydata_name",story_name);
                                                Storys.put("Storydata_context",story_content);
                                                Storys.put("Storydata_date",story_date);
                                                Storys.put("Storydata_who",story_who); // 작성자
                                                Storys.put("pushkey",pushkey);
                                                if (getDownlodaStoryUrl==null){
                                                    //만약 갤러리에서 사진 선택 안했을때 기본이미지 셋팅
                                                    Storys.put("Storydata_image","https://firebasestorage.googleapis.com/v0/b/doran-5661b.appspot.com/o/defaulfimages%2Fdefaultimg.png?alt=media&token=add21f62-e074-4968-82fc-4aa3e0039ccf"); // 기본 이미지추가
                                                    Log.d("갤러리에서선택안함", "onClick: "+getDownlodaStoryUrl);
                                                } else if (getDownlodaStoryUrl!=null){
                                                    //갤러리에서 불러온 사진이 있으면 그걸로 저장
                                                    Storys.put("Storydata_image",getDownlodaStoryUrl); /*스토리지의 url로 저장하기*/
                                                    Log.d("갤러리에서선택함", "onClick: "+getDownlodaStoryUrl);
                                                }
                                                myRef.setValue(Storys); //fireDB에 데이터 넣기

                                                progressBar.setVisibility(View.GONE);
                                                Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);

                                            }
                                            else if(intentName!=null){ /*갤러리에서 불러온 사진이 있고 인텐트 제목이 있을떄 == (수정)*/
                                                DatabaseReference myRef1 = database.getReference(coupleKey).child("Story").child(intentPushKey); //스토리 저장 경로
                                                pushkey = myRef1.getKey(); // push키 값 받아오기 - 나중에 게시글 수정, 삭제 시
                                                Log.d("수정", "인텐트제목 != null이라서 수정시 실행: ");

                                                /*수정화면 저장하기*/
                                                HashMap<String, Object> Storys1 = new HashMap<>();
                                                Storys1.put("Storydata_name",story_name);
                                                Storys1.put("Storydata_context",story_content);
                                                Storys1.put("Storydata_date",story_date);
                                                Storys1.put("Storydata_who",story_who); // 작성자
                                                Storys1.put("pushkey",pushkey);
                                                if (getDownlodaStoryUrl==null){
                                                    //만약 갤러리에서 사진 선택 안했을때 기본이미지 셋팅
                                                    Storys1.put("Storydata_image","https://firebasestorage.googleapis.com/v0/b/doran-5661b.appspot.com/o/defaulfimages%2Fdefaultimg.png?alt=media&token=add21f62-e074-4968-82fc-4aa3e0039ccf"); // 기본 이미지추가
                                                    Log.d("갤러리에서선택안함", "onClick: "+getDownlodaStoryUrl);
                                                } else if (getDownlodaStoryUrl!=null){
                                                    //갤러리에서 불러온 사진이 있으면 그걸로 저장
                                                    Storys1.put("Storydata_image",getDownlodaStoryUrl); /*스토리지의 url로 저장하기*/
                                                    Log.d("갤러리에서선택함", "onClick: "+getDownlodaStoryUrl);
                                                }

                                                /*업데이트칠드런으로 변경만하기*/
                                                myRef1.updateChildren(Storys1);

                                                Intent intent = new Intent(getApplicationContext(),StoryActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);


                                            }//else if


                                        }
                                    });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //불러오기 실패했을때
                                    Log.d("스토리지에서 사진불러오기 실패했을때", "onFailure: ");
                                    //스토리지에 사진이 없을때는 아예 실행이 안됨
                                }
                            });
                }//else if (getimage!=null)

            }
        });







        /*뒤로가기*/
        iv_WirteStory_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*이미지추가버튼*/
        iv_WriteStory_AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "앨범들어가기", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,REQUEST_IMAGE_CODE);
            }
        });
    }//OnCreate


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*갤러리에서 받아온 사진 이미지뷰에 붙히기*/
        if (requestCode ==REQUEST_IMAGE_CODE && resultCode ==RESULT_OK && data!=null){

            // 이미지 주소를 데이터에서 가져온다 (URL)
            getimage = data.getData();
            // 이미지를 스토리작성화면 이미지뷰에 붙힌다.
            Glide.with(getApplicationContext()).load(getimage).into(iv_WirteStory_Image);

//
//            /*파이어베이스 Storage에 사진 올리기*/
//            //순차저장을 위해 고유한 키값으로 현재시간을 사용
//            Calendar calendar = Calendar.getInstance();  // 현재의 날짜와 시간의 객체를 받아옴
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 날짜의 형식을 지정
//            String datetime = dateFormat.format(calendar.getTime()); // 캘린더를 통한 현재 날짜와 시간의 객체를 위 형식에 맞게 담아줌
//
//            /*파이어베이스 스토리지 <스토리> 글 사진 저장 경로*/
//            StorageReference riversRef = mStorageRef.child("storyimage").child(coupleKey).child(datetime+".jpg"); //storage사진경로
//            riversRef.putFile(getimage)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                            /*글 작성시 갤러리에서 가져온 사진을  스토리지에 올린 뒤에 바로 URI경로 뽑아오기*/
//                            Log.d("파이어 스토리지 사진", "잘 올라가나: "+taskSnapshot.toString());
//                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
//                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//
//                                    //스토리지에 저장이 성공했을때 스토리지폴더에 저장된 URI를 가져옴
//                                    getDownlodaStoryUrl = uri.toString();
//                                    Log.d("스토리작성시 갤러리에서 가져온 이미지", "의 스토리지에 저장된 URI: "+getDownlodaStoryUrl);
//                                    Toast.makeText(mcontext, "이미지가 스토리지에 저장되었습니다.", Toast.LENGTH_SHORT).show();
////                                    /*파이어베이스 스토리데이터 저장하기*/
////                                    DatabaseReference myRef = database.getReference(coupleKey).child("Story").push(); //스토리 저장 경로
////                                    pushkey = myRef.getKey(); // push키 값 받아오기
////                                    Hashtable<String, String> Storys = new Hashtable<String, String>();
////                                    Storys.put("Storydata_image",getDownlodaStoryUrl); /*스토리지의 url로 저장하기*/
////                                    myRef.setValue(Storys); //fireDB에 데이터 넣기
//
//                                }
//                            });
//
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//
//                        }
//                    });


        }
    }
}
