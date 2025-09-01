package com.example.chemapp;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ElementMapLoader {
    public HashMap<String, Element> loadElementAsMap(Context context, int jsonResourceId) {
        HashMap<String, Element> elementMap = new HashMap<>();

        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getResources().openRawResource(jsonResourceId);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            Gson gson = new GsonBuilder().create();
            Type mapType = new TypeToken<HashMap<String, Element>>() {
            }.getType();
            elementMap = gson.fromJson(reader, mapType);
            if (elementMap != null) {
                return elementMap;
            }

        } catch (Exception e) {
            Log.d("ERROR", "" + e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                Log.d("ERROR", "" + e);
            }

        }
        return null;
    }
}


