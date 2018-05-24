package com.dmedia.dlimited;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jeonghoy on 2016. 7. 27..
 */
// TODO: 2016-10-25 dbox place list가 필요한가? 자신이 입력한게 아니고 스피너 형식으로? - 물어보기
public class CommonData {
    //d-limited
    public static ArrayList<DBoxCategoryData> mDBoxCategoryList;
    public static ArrayList<String> mDBoxCategoryNameList;

    static class DBoxCategoryData {
        public int id;
        public String title;

        public DBoxCategoryData() {
        }

        public DBoxCategoryData(int id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    public static ArrayList<String> mPlaceNameList;
    public static ArrayList<PlaceData> mPlaceList;

    static class PlaceData {
        public int id;
        public String name;
        public String contact;//전화번호
        public String address;

        public PlaceData() {
        }

        public PlaceData(int id, String name, String contact, String address) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.address = address;
        }
    }

    static class LoginUserData {
        public static String loginToken;//session_token
        public static int id;//회원 인덱스
        public static String userId = "";//아이디(전화번호)
        public static String userName = "";//닉네임
        public static String gender = "";//성별
        public static String birthday;//생일
        public static String address;//지역
        public static String comment;//자기소개
        public static String instagramId;//인스타 numeric 아이디
        public static String instagramName;//인스타 아이디
        public static String profileImgUrl;//인스타 프로필 url
        public static int dcodeId;//D-Code 인덱스
        public static int dMoney;//D-Money잔액
        /*level
            0	비회원 (권한검증x) - looker
            1	미인증회원 - peeker
            2	Guest - guest
            3	Host - host
         중간 계층이 생길 수 있음
            10	God (admin)
         */
        public static String level;//레벨
        public static String barcode;//D-Money 바코드
        public static String cDate;//가입 시각
        public static String UDID;//
        public static String instaToken;
    }

    // TODO: 2016-11-07 dbox 배너 리스트에서 image 가 여러개 들어가나?? - 왜 이미지를 array로 넣은건지
    // TODO: 2016-11-07 일단은 처음(0) 이미지만 넣자 - 나중에 검증
    public static ArrayList<DBoxBannerData> mDBoxBannerList;
    static class DBoxBannerData {
        public int id;
        public int dboxId;
        public int imageId;
        public String url;

        public DBoxBannerData(int id, int dboxId, int imageId, String url) {
            this.id = id;
            this.dboxId = dboxId;
            this.imageId = imageId;
            this.url = url;
        }
    }


    public static int page = 0;
}