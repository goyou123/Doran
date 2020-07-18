package com.example.doran;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Mypromise_CustomDialog {
    private Context context;
    Dialog dialog;
    EditText Dialg_message;
    Button Dialg_okButton,Dialg_cancelButton;

    public Mypromise_CustomDialog(Context context) {
        this.context = context;
    }



    /* 호출할 다이얼로그 메소드 */
    public void callFuntion(final TextView tv_main_promise){

        dialog = new Dialog(context); //다이얼로그 클래스 생성
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //액티비티 타이틀바 제거
        dialog.setContentView(R.layout.custom_dialog); // 레이아웃과 연결
        dialog.show(); // 커스텀 다이얼로그 노출

        /*xml연결*/
        Dialg_message = (EditText)dialog.findViewById(R.id.Dialg_message); // 입력
        Dialg_okButton = (Button)dialog.findViewById(R.id.Dialg_okButton); // 확인버튼
        Dialg_cancelButton = (Button)dialog.findViewById(R.id.Dialg_cancelButton); // 취소버튼


        /*확인버튼*/
        Dialg_okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String promise = Dialg_message.getText().toString(); //입력한값 가져오기
                if (promise.length()>0 && promise.length()<=20 ){ // 글자 수 20자 이하일때
                    tv_main_promise.setText(promise); // 메인액티비티의 텍스트에 붙히기
                    //Shared에 작성한 다짐 저장
                    SharedPreferences sharedPreferences = context.getSharedPreferences("다이얼로그",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("promise",promise);
                    editor.commit();

                }else {
                    Toast.makeText(context,"20자 이내로 작성해주세요!",Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(context,"다짐을 작성하였습니다.",Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // 다이얼로그 종료

            }
        });


        /*취소버튼*/
        Dialg_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"작성을 취소합니다.",Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // 다이얼로그 종료
            }
        });
    }
}
