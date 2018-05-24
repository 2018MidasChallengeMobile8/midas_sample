package com.dmedia.dlimited;

/**
 * Created by xema0 on 2016-11-06.
 */

public class Const {
    public final static int LEVEL_LOOKER = 0;
    public final static int LEVEL_PEEKER = 1;
    public final static int LEVEL_GUEST = 2;
    public final static int LEVEL_HOST = 3;
    public final static int LEVEL_DGOD = 10;

    public final static int MAX_CAPACITY = 40;

    public final static int PAGE_SIZE_SMALL_ITEMS = 25;//한페이지에 나타날 리스트 개수(작은 아이템 - 유저 목록 등)
    public final static int PAGE_SIZE_BIG_ITEMS = 15;//한페이지에 나타날 리스트 개수(큰 아이템 - 사진 등)
    public final static int PAGE_SIZE_GALLERY_ITEMS = 36;//갤러리 아이템 -> 3의 배수로
}
