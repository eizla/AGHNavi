package com.aghnavi.agh_navi.Places;

public enum EPlaceType {

    BAKERY("BAKERY", 7),
    BAR("BAR",9 ),
    CAFE("CAFE", 15),
    STORE("STORE",88 ),
    RESTAURANT("RESTAURANT", 79);

    private String mName;
    private int mNum;

    EPlaceType(String name, int i) {

        this.mName = name;
        this.mNum = i;
    }

    public String getType(){
        return mName;
    }

    public int getNum(){
        return  mNum;
    }



}
