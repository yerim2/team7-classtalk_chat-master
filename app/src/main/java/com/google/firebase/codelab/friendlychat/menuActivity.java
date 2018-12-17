package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import com.opencsv.CSVReader;
import org.hyunjun.school.School;
import org.hyunjun.school.SchoolException;
import org.hyunjun.school.SchoolMenu;
import org.hyunjun.school.SchoolSchedule;
import java.io.FileNotFoundException;
import java.util.List;


//파일을 읽기 위함
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date; //현재 날짜 불러오기 위함

public class menuActivity extends AppCompatActivity {

    private TextView menuview; // menu를 위한 텍스트뷰 생성
    private TextView todoview; // 학사일정을 위한 텍스트뷰 생성
    String A="";
    String B="";
    String code="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //-------------------오류시 추가-----------------------------------------------------------
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //---------------------------------------------------------------------------------------


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        String menu=getmenu();   //메뉴를 저장할 변수
        String todo=gettodo();   //학사일정을 저장할 변수

        menuview = (TextView) this.findViewById(R.id.textView);  //메뉴를 출력할 텍스트뷰
        menuview.setText(menu);      //메뉴를 텍스트뷰에 넣기

        todoview = (TextView) this.findViewById(R.id.textView2);  //학사일정를 출력할 텍스트뷰
        todoview.setText(todo);      //학사일정을 텍스트뷰에 넣기

        //------------------네트워크 처리를 위한 쓰레드-------------------------------------------------
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                String menu = getmenu(); //getmenu함수를 실행시켜 리턴된 점심메뉴를 menu에 저장
                String todo = gettodo(); //gettodo함수를 실행시켜 리턴된 학사일정을 todo에 저장

                Bundle bun = new Bundle();
                bun.putString("HTML_DATA", menu);

                Message msg = handler.obtainMessage();
                msg.setData(bun);
                handler.sendMessage(msg);
            }
        });thread.start();

//-----------------------------------------뒤로가기 버튼---------------
        Button back = (Button) findViewById(R.id.back2);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//-------------------------------------------------------------------
        Bundle extras = getIntent().getExtras();

        A = extras.getString("A");
        B = extras.getString("B");


    } //onCreate끝

    //---------------------핸들러에서 getString 실행--------------------------------------------------
    Handler handler = new Handler() {
        public void handleMassage(Message msg) {
            Bundle bun = msg.getData();
            String menu = bun.getString("HTML_DATA");
        }
    };
    //---------------------------------------------------------------------------------------------


    //-------------------[순서번호,학교코드,학교이름] 이 저장된 csv파일을 읽어서 arraylist에 저장하는 코드
    List<String> index = new ArrayList<String>(); //순서 번호 저장
    List<String> code_list = new ArrayList<String>(); //학교 코드 저장
    List<String> name_list = new ArrayList<String>(); //학교 이름 저장
    List<String> lat = new ArrayList<String>(); //위도 저장
    List<String> lon = new ArrayList<String>(); //경도 저장

    public void parsing_data(String file_name){

        try{
            CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open(file_name)));

            String [] nextLine; //한줄씩 읽기

            while ((nextLine = reader.readNext()) != null){
                index.add(nextLine[0]);
                code_list.add(nextLine[1]);
                //name_list.add(nextLine[2]);
                lat.add(nextLine[3]);
                lon.add(nextLine[4]);
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    //---------------------------------------------------------------------------------------

    //오늘의 년,월,일을 저장

    Date today = new Date();
    int year = today.getYear() + 1900; //년
    int month = today.getMonth()+1;  //월
    int todayDate = today.getDate(); //일

    //Intent intent3 =getIntent();
    //String A = intent3.getStringExtra("A");
    //String B = intent3.getStringExtra("B");
    //String code="";



    //------------------------api 코드를 이용하여 점심 메뉴를 return하는 함수-----------------------
    private String getmenu() {


        parsing_data("schools.csv");

        for(int i=1;i<320;i++){
            if(A.equals(lat.get(i)) && B.equals(lon.get(i))){
                code = code_list.get(i);
            }
        }


        String lunch = "";
        String school_code = "";
        school_code = code_list.get(278); //1번 인덱스 학교의 메뉴를 가져옴

        School api = new School(School.Type.HIGH, School.Region.SEOUL, code);
        try {
            List<SchoolMenu> menu = api.getMonthlyMenu(year, month);
            lunch = menu.get(todayDate-3).lunch; //lunch에 점심메뉴만 저장
            // 21을 넣으면 22일 메뉴가 뜸(api인덱스 문제)
        } catch (SchoolException e) {
            e.printStackTrace();
        }
        return lunch;
    }


    //----------------------api코드를 이용하여 학사일정을 return하는 함수-------------------------------
    private String gettodo() {
        parsing_data("schools.csv");





        String todolist ="";
        String school_code = "";
        school_code = code_list.get(278); //1번 인덱스 학교의 메뉴를 가져옴

        School api = new School(School.Type.HIGH, School.Region.SEOUL, code);
        try {

            List<SchoolSchedule> schedule = api.getMonthlySchedule(year,month);
            todolist = schedule.get(todayDate-2).toString();


        } catch (SchoolException e) {
            e.printStackTrace();
        }
        return todolist ;
    }


    //----------------------------------------------------------------------------------------


}
