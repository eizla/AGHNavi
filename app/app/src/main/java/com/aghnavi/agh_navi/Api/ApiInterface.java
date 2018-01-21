package com.aghnavi.agh_navi.Api;


import com.aghnavi.agh_navi.Outdoor.Building;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @Headers({
            "Accept-Encoding: gzip, deflate",
            "Content-Type: application/json;charset=utf-8",
            "Accept: application/json"
    })
    @POST("anyplace/mapping/campus/all_cucode/")
    Call<List<Building>> getBuildings(@Query("cuid") String campusId);
}