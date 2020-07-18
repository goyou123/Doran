package com.example.doran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RandomDateActivity extends AppCompatActivity {

    TextView tv_handler_whatdate,tv_handler_whatfood,tv_handler_whatgift;
    Button btn_start_whatdate,btn_start_whatfood,btn_start_whatgift,btn_stop_whatdate,btn_stop_whatfood,btn_stop_whatgift;
    ImageView iv_Randomdate_close;

    ArrayList<String> raindateList = new ArrayList<>();
    ArrayList<String> dateList = new ArrayList<>();
    ArrayList<String> foodList = new ArrayList<>();
    ArrayList<String> giftList = new ArrayList<>();
    Handler raindateHandler = new Handler();
    Handler dateHandler= new Handler();;
    Handler foodHandler= new Handler();;
    Handler giftHandler= new Handler();;
    RainDateThread rainDateThread;
    DateThread dateThread;
    FoodThread foodThread;
    GiftThread giftThread;

    //날씨 관련 변수들
    EditText cityName;
    Button searchButton;
    TextView weatherResult;


    String main; // 메인 온도
    String cName; // 도시이름
    /*OpenWeatherMap API 사용하기
    * 백그라운드에서 실행되어 데이터가 들어있는 url에서 데이터를 받아옴*/
    class Weather extends AsyncTask<String,Void,String > {

        @Override
        protected String doInBackground(String... address) {
            //주소와 연결 설정
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Log.d("커넥션", "doInBackground: "+connection); // 날씨 데이터 url로 잘 감


                //retrieve data from url
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                Log.d("is", "doInBackground: "+is);


                //데이터 되찾아와 / 검색하여 String으로 반환
                int data = isr.read();
                Log.d("데이터", "doInBackground: "+data);
                String content = "";
                char ch;
                while (data != -1) {
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
                Log.i("Content", content);
                return content;



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }


    public void search(View view){
        cityName = findViewById(R.id.cityName);
        searchButton = findViewById(R.id.searchButton);
        weatherResult = findViewById(R.id.weatherResult);

        cName = cityName.getText().toString(); // 나라이름 입력
        if (cName.isEmpty()){
            Toast.makeText(this, "도시 이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String content; //날씨데이터 반환
        Weather weather = new Weather();

        try {
            //이 url안에 들어있는 json파일을 가져와서 content에 담아라
            content = weather.execute("http://api.openweathermap.org/data/2.5/weather?q="
                    +cName +"&appid=3082f0abfd79b0b8cf559098df2b927a").get();

            //JSON
            //JSON 데이터 안의 weather 항목안에있는 것을 다 가져옴 //여기서 값 변경
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            String mainTemperature = jsonObject.getString("main"); // 메인 온도 가져오기
            Double visibility;
            Log.i("JSON날씨정보",weatherData);

            //날씨 데이터를 어레이리스트 안에 넣기
            JSONArray array = new JSONArray(weatherData);

            //날씨 JSON데이터 안에 들어있는 key값 과 일치해야함
            main ="";
            String description="";
            long temperature = 0;

            for (int i = 0; i<array.length(); i++) {
                JSONObject weatherPart = array.getJSONObject(i);
                main = weatherPart.getString("main");
                description = weatherPart.getString("description"); // 이건
            }

                //메인 온도 가져오기
                JSONObject mainPart = new JSONObject(mainTemperature); //94Line
                temperature = mainPart.getLong("temp");
                visibility = Double.parseDouble(jsonObject.getString("visibility"));
                int visibleKM = (int) (visibility/1000);

                //텍스트뷰에 붙히기
                weatherResult.setText("도시 : " +cName+
                        "\n날씨: "+main+
                        "\n온도: "+(temperature-273)+"*C");
//                "\n비지블 : "+visibleKM)
//                "\nDescription : "+description +







        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_date);


        /*xml연결*/
        iv_Randomdate_close = (ImageView)findViewById(R.id.iv_Randomdate_close); //뒤로가기

        tv_handler_whatdate = (TextView)findViewById(R.id.tv_handler_whatdate); // 데이트추천쓰레드
        tv_handler_whatfood = (TextView)findViewById(R.id.tv_handler_whatfood); // 음식추천쓰레드
        tv_handler_whatgift = (TextView)findViewById(R.id.tv_handler_whatgift); // 선물추천쓰레드

        btn_start_whatdate = (Button)findViewById(R.id.btn_start_whatdate); // 데이트추천쓰레드 시작 버튼
        btn_start_whatfood = (Button)findViewById(R.id.btn_start_whatfood); // 음식추천쓰레드 시작 버튼
        btn_start_whatgift = (Button)findViewById(R.id.btn_start_whatgift); // 선물추천쓰레드 시작 버튼
        btn_stop_whatdate = (Button)findViewById(R.id.btn_stop_whatdate); // 데이트추천쓰레드 스탑 버튼
        btn_stop_whatfood = (Button)findViewById(R.id.btn_stop_whatfood); // 음식추천쓰레드 스탑 버튼
        btn_stop_whatgift = (Button)findViewById(R.id.btn_stop_whatgift); // 선물추천쓰레드 스탑 버튼


        /*데이트 추천 버튼*/
        btn_start_whatdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /*만약 비가 올때는 실내데이트 추천*/
                Log.d("비올때", "onClick: "+main); // rain

                //처음에 null인지 아닌지 부터 체크해야 한다.
                if (main==null){
                    Toast.makeText(RandomDateActivity.this, "데이트하는 지역을 먼저 설정해주세요!", Toast.LENGTH_SHORT).show();
                    return;
//                    Log.d("날씨선택안하고", "==null: ");
//                    dateList.add("한강데이트");
//                    dateList.add("보드게임카페");
//                    dateList.add("등산");
//                    dateList.add("영화보기");
//                    dateList.add("만화카페");
//                    dateList.add("자전거타기");
//                    dateList.add("애견카페");
//                    dateList.add("연극");
//                    dateList.add("공원산책");
//                    dateThread = new DateThread();
//                    dateThread.start();
//                    btn_start_whatdate.setClickable(false);
//                    Toast.makeText(RandomDateActivity.this, "추천", Toast.LENGTH_SHORT).show();

                }else if(main.equals("Rain")){
                    Log.d("비올때쓰레드", "==Rain: ");
                    raindateList.add("보드게임카페");
                    raindateList.add("연극");
                    raindateList.add("PC방");
                    raindateList.add("커플스파");
                    raindateList.add("아쿠아리움");
                    raindateList.add("찜질방데이트");
                    raindateList.add("애견카페");
                    raindateList.add("드라이브");
//                    raindateList.add("파전골목");
                    rainDateThread = new RainDateThread();
                    rainDateThread.start();
                }else{
                    Log.d("비올때쓰레드", "비 말고 다른 날씨일때 else: ");
                    dateList.add("보드게임카페");
                    dateList.add("등산");
                    dateList.add("영화관");
                    dateList.add("만화카페");
                    dateList.add("자전거데이트");
                    dateList.add("애견카페");
                    dateList.add("연극");
                    dateList.add("공원산책");
//                    dateList.add("111");
//                    dateList.add("222");
//                    dateList.add("333");
//                    dateList.add("444");
//                    dateList.add("555");
//                    dateList.add("666");
//                    dateList.add("777");
//                    dateList.add("888");
                    dateThread = new DateThread();
                    dateThread.start();
                    btn_start_whatdate.setClickable(false);
                    Toast.makeText(RandomDateActivity.this, "추천", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*음식 추천 버튼*/
        btn_start_whatfood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(main==null){
                    Toast.makeText(RandomDateActivity.this, "데이트하는 지역을 먼저 설정해주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                foodList.add("치킨");
                foodList.add("파스타");
                foodList.add("피자");
                foodList.add("곱창");
                foodList.add("국밥");
                foodList.add("보쌈");
                foodList.add("짜장");
                foodList.add("고기");
                foodList.add("양꼬치");
                foodList.add("마라탕");

                foodThread = new FoodThread();
                foodThread.start();
                btn_start_whatfood.setClickable(false);
                Toast.makeText(RandomDateActivity.this, "음식추천", Toast.LENGTH_SHORT).show();
            }
        });


        /*선물 추천 버튼*/
        btn_start_whatgift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giftList.add("목걸이");
                giftList.add("반지");
                giftList.add("향수");
                giftList.add("과자");
                giftList.add("지갑");
                giftList.add("손편지");
                giftThread = new GiftThread();
                giftThread.start();
                btn_start_whatgift.setClickable(false);
                Toast.makeText(RandomDateActivity.this, "음식추천", Toast.LENGTH_SHORT).show();
            }
        });


        /*데이트추천 링크로 이동*/
        tv_handler_whatdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // 아무것도 없을때
                Log.d("데이트추천 텍스트 클릭", "멈췄을때 텍스트: "+tv_handler_whatdate.getText().toString());
                Log.d("//데이트추천 텍스트 클릭", "현재날씨: "+main);
                if(dateList.isEmpty()) {
                    return;
                }
                if(raindateList.isEmpty()) {
                    return;
                }

                //비 안올때 리스트
                if(main==null){
                    Log.d("도시선택안했을때데이트추천" ,"");
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(0))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(0));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(1))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(1));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(2))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(2));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(3))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(3));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(4))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(4));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(5))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(5));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(6))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(6));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(7))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(7));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(8))){
                        Log.d("도시선택안했을때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(8));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                        startActivity(intent);

                    }
                }else if(main.equals("Rain")) {
                    //비 올떄 리스트
                    Log.d("비올때데이트추천", "");
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(0))) { //보드게임
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(0));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%EB%B3%B4%EB%93%9C%EA%B2%8C%EC%9E%84%EC%B9%B4%ED%8E%98&oquery=%EC%84%9C%EC%9A%B8+%EB%B3%B4%EB%93%9C%EA%B2%8C%EC%9E%84%EC%B9%B4%ED%8E%98&tqi=UXmyqsp0JXossjkFKYRssssssww-212998"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(1))) { //연극
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(1));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%EC%97%B0%EA%B7%B9&oquery=" + cName + "%EB%B3%B4%EB%93%9C%EA%B2%8C%EC%9E%84%EC%B9%B4%ED%8E%98&tqi=UXm0dwp0YiRssjXEcMZssssst6o-210265"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(2))) { //피시방
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(2));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%ED%94%BC%EC%8B%9C%EB%B0%A9&oquery=" + cName + "%ED%94%BC%EC%8B%9C%EB%B0%A9&tqi=UXm1blp0YiRssjwmdMdsssssst0-182592"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(3))) { //커플스파
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(3));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%EC%BB%A4%ED%94%8C%EC%8A%A4%ED%8C%8C&oquery=" + cName + "%ED%94%BC%EC%8B%9C%EB%B0%A9&tqi=UXm1Lsp0J1ZssDRzmPdssssssMl-070730"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(4))) { //아쿠아리움
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(4));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%EC%95%84%EC%BF%A0%EC%95%84%EB%A6%AC%EC%9B%80&oquery=" + cName + "%EC%BB%A4%ED%94%8C%EC%8A%A4%ED%8C%8C&tqi=UXm1Uwp0YidssbvaMZwssssstJG-378098"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(5))) { //찜질방
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(5));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%EC%B0%9C%EC%A7%88%EB%B0%A9&oquery=" + cName + "%EC%95%84%EC%BF%A0%EC%95%84%EB%A6%AC%EC%9B%80&tqi=UXm1Pwp0Jy0ssUmKvJlssssstu4-267646"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(6))) { //애견카페
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(6));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=" + cName + "%EC%95%A0%EA%B2%AC%EC%B9%B4%ED%8E%98&oquery=" + cName + "%EC%B0%9C%EC%A7%88%EB%B0%A9&tqi=UXm1Esp0JXVssagHG58ssssssQh-470275"));
                        startActivity(intent);
                    }
                    if (tv_handler_whatdate.getText().toString().equals(raindateList.get(7))) { //드라이브
                        Log.d("비올때데이트추천", "멈췄을때 데이트리스트: " + raindateList.get(7));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EB%93%9C%EB%9D%BC%EC%9D%B4%EB%B8%8C&oquery=" + cName + "%EC%95%A0%EA%B2%AC%EC%B9%B4%ED%8E%98&tqi=UXm1rsprvN8ssMK6c58ssssstaG-503985"));
                        startActivity(intent);
                    }
                }else{
                    //다른날씨일떄
                    Log.d("다른날씨일때데이트추천", "");
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(0))){ //보드게임카페
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(0));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EB%B3%B4%EB%93%9C%EA%B2%8C%EC%9E%84%EC%B9%B4%ED%8E%98&oquery=%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C+%EB%92%A4%EB%A1%9C%EA%B0%80%EA%B8%B0+%EB%B2%84%ED%8A%BC+%EC%9D%B4%EB%B2%A4%ED%8A%B8&tqi=UXBlAsprvmsssZ1Q7s8sssssskG-207756"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(1))){ //등산
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(1));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EB%93%B1%EC%82%B0&oquery="+cName+"%EB%B3%B4%EB%93%9C%EA%B2%8C%EC%9E%84%EC%B9%B4%ED%8E%98&tqi=UXBB8dprvxZssgQILdhssssssw8-403314"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(2))){ //영화관
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(2));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%98%81%ED%99%94%EA%B4%80&oquery="+cName+"%EB%93%B1%EC%82%B0&tqi=UXBCdwprvN8ssBJvBAwssssssuZ-226867"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(3))){ //만화카페
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(3));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EB%A7%8C%ED%99%94%EC%B9%B4%ED%8E%98&oquery="+cName+"%EC%98%81%ED%99%94%EA%B4%80&tqi=UXBC9wprvN8ssCd7RtRssssstRd-190515"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(4))){ //자전거데이트
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(4));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%9E%90%EC%A0%84%EA%B1%B0%EB%8D%B0%EC%9D%B4%ED%8A%B8&oquery="+cName+"%EB%A7%8C%ED%99%94%EC%B9%B4%ED%8E%98&tqi=UXBDZwprvTVssS5T4sGssssss68-193123"));
                        startActivity(intent);

                    } if (tv_handler_whatdate.getText().toString().equals(dateList.get(5))){ //애견카페
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(5));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%95%A0%EA%B2%AC%EC%B9%B4%ED%8E%98&oquery="+cName+"%EC%9E%90%EC%A0%84%EA%B1%B0%EB%8D%B0%EC%9D%B4%ED%8A%B8&tqi=UXBD6sprvmZssNSWiNdssssstzV-082819"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(6))){ //연극
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(6));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%97%B0%EA%B7%B9&oquery="+cName+"%EC%95%A0%EA%B2%AC%EC%B9%B4%ED%8E%98&tqi=UXBDTlprvxsssslgcmhssssssB0-322486"));
                        startActivity(intent);

                    }
                    if (tv_handler_whatdate.getText().toString().equals(dateList.get(7))){ //공원산책
                        Log.d("다른날씨일때데이트추천", "멈췄을때 데이트리스트: "+dateList.get(7));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EA%B3%B5%EC%9B%90%EC%82%B0%EC%B1%85&oquery="+cName+"%EC%97%B0%EA%B7%B9&tqi=UXBDklprvOsss7PEMqsssssstpd-350018"));
                        startActivity(intent);

                    }


                }
            }
        });


        /*음식추천 링크로 이동*/
        tv_handler_whatfood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 아무것도 없을때
                Log.d("음식추천", "멈췄을때 텍스트: "+tv_handler_whatfood.getText().toString());
                if(foodList.isEmpty()) {
                    return;
                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(0))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(0));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%B9%98%ED%82%A8&oquery=%EB%B6%80%EC%82%B0+%EC%B9%98%ED%82%A8&tqi=UXBUHsp0J1ssssX4SuossssssLw-345771"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(1))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(1));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%ED%8C%8C%EC%8A%A4%ED%83%80&oquery="+cName+"%EC%B9%98%ED%82%A8&tqi=UXBhklp0YihssSX4I5sssssstoZ-069474"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(2))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(2));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%ED%94%BC%EC%9E%90&oquery="+cName+"%ED%94%BC%EC%9E%90&tqi=UXBhqsp0YihssSGsar0ssssstbK-267102"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(3))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(3));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EA%B3%B1%EC%B0%BD&oquery="+cName+"%ED%94%BC%EC%9E%90&tqi=UXBhrlp0YidssfJ%2BMmdssssstdC-270415"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(4))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(4));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EA%B5%AD%EB%B0%A5&oquery="+cName+"%EA%B5%AD%EB%B0%A5&tqi=UXBh8sp0Jy0ssBOYCq0ssssstDo-148656"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(5))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(5));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EB%B3%B4%EC%8C%88&oquery="+cName+"%EA%B5%AD%EB%B0%A5&tqi=UXBissp0JXossFsl9x8ssssst%2BC-216111"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(6))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(6));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%A7%9C%EC%9E%A5&oquery="+cName+"%EB%B3%B4%EC%8C%88&tqi=UXBiblp0J1Zss6KQa6dssssstrG-236757"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(7))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(7));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EA%B3%A0%EA%B8%B0&oquery="+cName+"%EC%A7%9C%EC%9E%A5&tqi=UXBiKlp0Jy0ssBV57KVssssst1R-317118"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(8))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(8));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EC%96%91%EA%BC%AC%EC%B9%98&oquery="+cName+"%EA%B3%A0%EA%B8%B0&tqi=UXBielp0J1Zss5IRZmdssssstiw-420385"));
                    startActivity(intent);

                }
                else if (tv_handler_whatfood.getText().toString().equals(foodList.get(9))){
                    Log.d("음식추천", "멈췄을때 데이트리스트: "+foodList.get(9));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query="+cName+"%EB%A7%88%EB%9D%BC%ED%83%95&oquery="+cName+"%EC%96%91%EA%BC%AC%EC%B9%98&tqi=UXBihdp0JywssvOJ4GGssssstPC-091108"));
                    startActivity(intent);

                }
            }
        });



        /*선물추천 링크로 이동*/
        tv_handler_whatgift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 아무것도 없을때
                Log.d("데이트추천", "멈췄을때 텍스트: "+tv_handler_whatgift.getText().toString());
                if(giftList.isEmpty()) {
                    return;
                }

                if (tv_handler_whatgift.getText().toString().equals(giftList.get(0))){
                    Log.d("데이트추천", "멈췄을때 데이트리스트: "+giftList.get(0));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                    startActivity(intent);

                }
                if (tv_handler_whatgift.getText().toString().equals(giftList.get(1))){
                    Log.d("데이트추천", "멈췄을때 데이트리스트: "+giftList.get(1));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                    startActivity(intent);

                }
                if (tv_handler_whatgift.getText().toString().equals(giftList.get(2))){
                    Log.d("데이트추천", "멈췄을때 데이트리스트: "+giftList.get(2));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                    startActivity(intent);

                }
                if (tv_handler_whatgift.getText().toString().equals(giftList.get(3))){
                    Log.d("데이트추천", "멈췄을때 데이트리스트: "+giftList.get(3));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                    startActivity(intent);

                }
                if (tv_handler_whatgift.getText().toString().equals(giftList.get(4))){
                    Log.d("데이트추천", "멈췄을때 데이트리스트: "+giftList.get(4));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                    startActivity(intent);

                }
                if (tv_handler_whatgift.getText().toString().equals(giftList.get(5))){
                    Log.d("데이트추천", "멈췄을때 데이트리스트: "+giftList.get(5));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                    startActivity(intent);

                }
            }
        });




        /*데이트 스탑 버튼*/
        btn_stop_whatdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main==null){
                    dateThread.setRunning(false);
                    dateThread.isInterrupted();
                    Toast.makeText(RandomDateActivity.this, "데이트추천멈춤", Toast.LENGTH_SHORT).show();
                    btn_start_whatdate.setClickable(true);
                }else if(main.equals("Rain")){
                    rainDateThread.setRunning(false);
                    rainDateThread.isInterrupted();
                    Toast.makeText(RandomDateActivity.this, "Rain데이트추천멈춤", Toast.LENGTH_SHORT).show();
                    btn_start_whatdate.setClickable(true);
                }else{
                    dateThread.setRunning(false);
                    dateThread.isInterrupted();
                    Toast.makeText(RandomDateActivity.this, "데이트추천멈춤", Toast.LENGTH_SHORT).show();
                    btn_start_whatdate.setClickable(true);
                }


            }
        });


        /*음식 스탑 버튼*/
        btn_stop_whatfood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodThread.setRunning(false);
                foodThread.isInterrupted();
                Toast.makeText(RandomDateActivity.this, "음식추천멈춤", Toast.LENGTH_SHORT).show();
                btn_start_whatfood.setClickable(true);
            }
        });



        /*선물 스탑 버튼*/
        btn_stop_whatgift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giftThread.setRunning(false);
                giftThread.isInterrupted();
                Toast.makeText(RandomDateActivity.this, "선물추천멈춤", Toast.LENGTH_SHORT).show();
                btn_start_whatgift.setClickable(true);
            }
        });


        /*뒤로가기*/
        iv_Randomdate_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }//Oncreate

    /*데이트추천쓰레드*/
    class RainDateThread extends Thread {
        private boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            int dateindex = 0;
            while (isRunning){

                final String datelists = raindateList.get(dateindex);
                dateindex += 1;
                if(dateindex == raindateList.size()){
                    dateindex = dateindex-raindateList.size();
                }
                raindateHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_handler_whatdate.setText(datelists);
                    }
                });
                /////try
                try {
                    Thread.sleep(100);
                }catch (Exception e){

                }
                /////catch
            }//while
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }




    /*데이트추천쓰레드*/
    class DateThread extends Thread {
        private boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            int dateindex = 0;
            while (isRunning){

                final String datelists = dateList.get(dateindex);
                dateindex += 1;
                if(dateindex == dateList.size()){
                    dateindex = dateindex-dateList.size();
                }
                dateHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_handler_whatdate.setText(datelists);
                    }
                });
                /////try
                try {
                    Thread.sleep(100);
                }catch (Exception e){

                }
                /////catch
            }//while
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }


    /*음식쓰레드*/
    class FoodThread extends Thread {
        private boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            int dateindex = 0;
            while (isRunning){
                final String foodlists = foodList.get(dateindex);
                dateindex += 1;
                if(dateindex == foodList.size()){
                    dateindex = dateindex-foodList.size();
                }
                foodHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_handler_whatfood.setText(foodlists);
                    }
                });
                /////try
                try {
                    Thread.sleep(100);
                }catch (Exception e){

                }
                /////catch
            }//while
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }


    /*선물 쓰레드*/
    class GiftThread extends Thread {
        private boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            int index = 0;
            while (isRunning){
                final String giftlists = giftList.get(index);
                index += 1;
                if(index == giftList.size()){
                    index = index-giftList.size();
                }
                giftHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_handler_whatgift.setText(giftlists);
                    }
                });
                /////try
                try {
                    Thread.sleep(100);
                }catch (Exception e){

                }
                /////catch
            }//while
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }


}

