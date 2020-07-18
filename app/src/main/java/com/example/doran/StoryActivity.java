package com.example.doran;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StoryActivity extends AppCompatActivity  {
    ImageView iv_Story_close,iv_Story_AddStory;
    private static final String TAG = "스토리액티비티";
    /*스로리 리사이클러뷰 변수*/
    ArrayList<StoryData> storyDataArrayList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    StoryAdapter storyAdapter;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    Context context;

    String shared_Email; // 로그인에서 저장한 이메일 Shared에 저장해놓은거 넣을 변수
    String myKey; // 내 정보에 접근하는 키값
    String anotherKey; // 상대방 키값
    String coupleKey; // A와 B 공유하는 키값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        database = FirebaseDatabase.getInstance(); //파이어베이스 데이터베이스 연동


        recyclerView = findViewById(R.id.RcView_Story); // xml , 리사이클러뷰 연결
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        storyDataArrayList = new ArrayList<>();

        storyAdapter = new StoryAdapter(storyDataArrayList,this ); // 스토리어댑터
        recyclerView.setAdapter(storyAdapter); // 리사이클러뷰에 어댑터 연결


        /*xml연결*/
        iv_Story_close = (ImageView) findViewById(R.id.iv_Story_close); // 뒤로가기
        iv_Story_AddStory = (ImageView) findViewById(R.id.iv_Story_AddStory); // 스토리 추가버튼

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


        storyDataArrayList.clear();

        /*파이어베이스 스토리 데이터 베이스 읽어오기*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    //파이어베이스 DB에서 스토리 데이터 리스트 추출
                    StoryData storydata = dataSnapshot.getValue(StoryData.class);

                    /*데이터 읽어오는거 확인*/
                    Log.d(TAG, "스토리데이터들: "+dataSnapshot); //확인
                    storyDataArrayList.add(storydata);
                    storyAdapter.notifyDataSetChanged();

                    /*리사이클러뷰 맨 아래 보이게 하기*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(storyAdapter.getItemCount()-1);
                    }
                },10);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                //여기서 리스트를 clear()해버리면 마지막하나만 나옴
                StoryData storydata = dataSnapshot.getValue(StoryData.class);
                /*데이터 읽어오는거 확인*/
                Log.d(TAG, "스토리데이터들: "+dataSnapshot); //확인
                storyDataArrayList.add(storydata);
                storyAdapter.notifyDataSetChanged();

                /*리사이클러뷰 맨 아래 보이게 하기*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(storyAdapter.getItemCount()-1);
                    }
                },10);
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
        DatabaseReference ref = database.getReference(coupleKey).child("Story");
        ref.addChildEventListener(childEventListener);

        /*파이어베이스 스토리 READ END===========================================================*/


        /* 스토리 리사이클러뷰 클릭 리스너 추가하기*/
        storyAdapter.setOnItemClickListener(new OnStoryItemClickListener() {
            @Override
            public void onItemClick(StoryAdapter.StoryViewHolder holder, View view, int position) {
                Toast.makeText(getApplicationContext(),"아이템 선택",Toast.LENGTH_SHORT).show();

                //클릭시 상세 페이지로 이동하기, 인텐트로 데이터정보 + 포지션값 넘겨서 상세페이지에서 받기
                String intent_name = storyDataArrayList.get(position).getStorydata_name();
                String intent_content = storyDataArrayList.get(position).getStorydata_context();
                String intent_date = storyDataArrayList.get(position).getStorydata_date();
                String intent_image = storyDataArrayList.get(position).getStorydata_image();
                String intent_pushkey = storyDataArrayList.get(position).getPushkey();
                //여기서 각 스토리에 맞는 push키값을 같이 넘겨주면 좋을거 같음


                //인텐트로 클릭했을때 나오는 상세 페이지에 데이터 전달
                Intent intent = new Intent(getApplicationContext(),StoryclickActivity.class);
                intent.putExtra("intent_name",intent_name); // 스토리 제목 인텐트로 보내기
                intent.putExtra("intent_content",intent_content); // 스토리 내용 인텐트로 보내기
                intent.putExtra("intent_date",intent_date); // 스토리 날짜 인텐트로 보내기
                intent.putExtra("intent_image",intent_image); // 스토리 사진 인텐트로 보내기
                intent.putExtra("intent_pushkey",intent_pushkey); // 스토리 푸시키 인텐트로 보내기
                intent.putExtra("position",position); //포지션값도 인텐트로 보내기
                Log.d("리사이클러뷰클릭시", "이미지 "+intent_image+intent_name+intent_content+intent_date); //확인완료
                Log.d("리사이클러뷰클릭시", "푸시키 "+intent_pushkey); //확인완료
                startActivity(intent);
            }
        });





        /*뒤로가기*/
        iv_Story_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*스토리추가*/
        iv_Story_AddStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),WritestoryActivity.class);
                startActivity(intent);
            }
        });
    }//OnCreate

    @Override
    protected void onResume() {
        super.onResume();


    }
}
