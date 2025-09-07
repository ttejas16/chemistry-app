package com.example.chemapp.Utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CompoundLoader {
    public Map<String, Compound> loadCompoundAsMap(Context context, int jsonResourceId){
        Map<String,Compound> compoundsMap = new HashMap<>() ;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try{
            inputStream = context.getResources().openRawResource(jsonResourceId);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            Gson gson = new GsonBuilder().create();
            Type mapType =  new TypeToken<HashMap<String,Compound>>(){}.getType();
            compoundsMap = gson.fromJson(reader,mapType);
            if(compoundsMap != null) {


                for (Map.Entry<String, Compound> entry : compoundsMap.entrySet()) {
                    String compoundName = entry.getKey();
                    Compound compoundData = entry.getValue();

                    if (compoundData != null) {
                        compoundData.setName(compoundName);
                    }
                }

            }
            else {
                throw new Exception("No compounds found");
            }
        }catch (Exception e){
            Log.d("ERROR","" +e );

        }
        return compoundsMap;


    }





}
