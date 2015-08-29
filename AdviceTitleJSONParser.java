package com.cobionecanobi.askme.parsers;

import com.cobionecanobi.askme.model.Advice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//This class first takes the String results from a POST request to a specified url that displaying JSON formatted text containing database information.
//The class then grabs the Inbox database information from the JSON String and saves each message title and id in an Advice object.
//The final List of Advice Objects is then returned

public class AdviceTitleJSONParser {

    public static List<Advice> parseFeed(String content){
        try {
            JSONArray ar = new JSONArray(content);
            List<Advice> adviceList = new ArrayList<>();
            JSONObject error = ar.getJSONObject(0);

            if (!error.getString("error").equals("true")) {  //checks to see if any error occured on the web side when receiving data
                for (int i = 1; i < ar.length(); i++) {

                    JSONObject obj = ar.getJSONObject(i);
                    Advice advice = new Advice();

                    advice.setId(obj.getInt("id"));
                    advice.setTitle(obj.getString("title"));

                    adviceList.add(advice);
                }

                return adviceList; //return all of user's advice after getting  data from JSON and storing it into the a List of Advice objects
            }else {
                return null; //return null if there was a problem
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
