package kr.co.nextcore.taglocationandgwcollector.common;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Util {
    private static Gson gson = new Gson();

    private Util(){
        //no instance
        }

    public static Map<String,Object> jsonToMap(String jsonStr){
        return gson.fromJson(jsonStr, Map.class);
    }

    public static String mapToJson(Map<String,Object> map){
        return gson.toJson(map);
    }

    public static String dateFormat(String date, String format){
        LocalDateTime getDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return getDate.format(DateTimeFormatter.ofPattern(format));
    }
}
