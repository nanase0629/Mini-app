package com.lijiahao.aichat;

public class RobotManager {
    private static String Url="http://api.qingyunke.com/api.php?key=free&appid=0&msg=!!";

    public static String getUrl(String question){
        String real_Url=Url.replace("!!",question);
        return real_Url;
    }
}