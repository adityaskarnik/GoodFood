package com.aditya.goodfood;

import org.json.JSONObject;

/**
 * Created by Aditya PC on 2/25/2017.
 */

interface AsyncResult
{
    void onResult(JSONObject object);
}