# 안드로이드 메신저 with 급식 알리미 기능

## C-talk
전국 고등학교 급식 식단과 일정을 채팅 앱 내에서 보여주는 고등학생을 위한 채팅 앱
현재 위치로 자신의 고등학교를 쉽게 찾을 수 있는 기능을 가집니다.

### 1. 개발자 정보

> 고은진 aroneia : ppt, 최종발표 준비, github에 라이센스 추가
>
> 김보경 bokyungkim : 데이터베이스 담당, 파이어베이스 연동, 기본 채팅앱 실행, readme작성
>
> 김예림 yerim2 : 급식 메뉴 api 코드 작성, 채팅앱과 연동, Maps1Activity 코드 작성
>
> 서주원 chws : 사용자 위치를 받아와 전달해 주변학교 검색 코드 작성, readme 정리
>
> 안건희 MaryAhn : 구글 맵 api를 이용하여 현재 위치 받아오는 코드 작성

### 2. 앱 설치 방법 및 사용법

> 2-1. 사용자에게 apk파일을 배부하면 사용자는 이를 설치하여 구글 아이디를 이용하여 로그인한다.
>
> 2-2. 로그인 후, 앱에 로그인한 사람들과 채팅을 할 수 있다. 
>
> 2-3. 오늘의 일정과 식단을 보고 싶다면, 초기 지도 화면으로 이동한다. 초기 지도 화면에서는 현재 위치를 받아 주변의 학교들을 띄워준다.
>
> 2-4. 그 중 한 학교를 선택하면 사용자는 그날의 학사일정과 급식 메뉴를 볼 수 있다.


### 3. 주요 기능 및 관련 API
> 3-1. 채팅 기능 : android firebase 기반의 채팅 기능 ( https://github.com/firebase/friendlychat-android)
>//메세지 작성
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        //메세지 및 이미지 전송
        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new
                        FriendlyMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        null /* no image */);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                        .push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });
        
        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        //지도 액티비티로 전환
        Button mapsB = (Button) findViewById(R.id.mapsButton);
        mapsB.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, Maps1Activity.class);
                startActivity(intent1);
            }
        });

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");


    }
> 
> 3-2. 급식 기능 : 김급식 API (https://github.com/agemor/school-api)
>
> 3-2. 코드
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

> 3-3. 학교 검색 기능 : google maps SDK for android, 교육청에서 제공하는 초중등학교 현황 data (google maps SDK for android: https://console.developers.google.com/apis/library/maps-android-backend.googleapis.com?q=maps%20&id=01d8f5af-dc9a-4b12-af6f-37029d8e3e71&project=maptest-225305, 초중등학교 현황: https://www.data.go.kr/dataset/15021920/fileData.do)
>
>3-3. 코드
//-------------------------학교위도, 경도, 코드 정보를 저장------------------------------------------------
        List<String> index = new ArrayList<String>(); //순서 번호 저장
        List<String> code_list = new ArrayList<String>(); //학교 코드 저장
        List<String> latitude = new ArrayList<String>(); //위도 저장
        List<String> longitude = new ArrayList<String>(); //경도 저장


        try {
            CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("schools.csv")));

            String[] nextLine; //한줄씩 읽기

            while ((nextLine = reader.readNext()) != null) {
                index.add(nextLine[0]);
                code_list.add(nextLine[1]);
                latitude.add(nextLine[3]);
                longitude.add(nextLine[4]);
                //name_list.add(nextLine[2]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //------------------------------저장한 위도, 경도 정보로 마커 생성---------------------------------------

        for (int i = 1; i < 321; i++) {
            LatLng latLng
                    = new LatLng(Double.parseDouble(latitude.get(i))
                    , Double.parseDouble(longitude.get(i))); //위도경도를 Double로 바꾸어 위치에 추가
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("메뉴보기");
            mGoogleMap.addMarker(markerOptions).showInfoWindow();

        }
        
        
 ### 4. 프로젝트 실행 방법
 1. api키 받기: google developers console에서 https://console.developers.google.com/ 에서 api 발급받기 --> 패키지 이름(com.google.firebase.codelab.friendlychat;) 지정, 사용자의 pc sha-1을 입력.

1-1 SHA-1 찾기: 명령 프롬프트에  
"C:\Program Files\Android\Android Studio\jre\bin\keytool" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android –keypass android 입력 

2. 구글에서 제공받은 API 키를 프로젝트의 App>manifests>AndroidManifest.xml 파일의 메타데이터에 삽입. (YOUR API KEY 부분에 삽입)

3. Firebase에서 프로젝트 생성 및 json file 생성 후 다운로드 후 프로젝트 src에 추가.


 ### 5. 오류 발생 시 해결 방법
 
1. api 키가 옳은지 확인
2. 프로젝트의 gradle file에 있는 모든 dependencies의 버전이 각각 호환되는지 확인

 > 프로젝트의 gradle version = 4.4, Android Plugin Version 3.1.2


 ### 6. 라이센스
 > 이 소프트웨어는 MIT 라이센스를 따라 자유롭게 이용하실 수 있습니다.
 
 
