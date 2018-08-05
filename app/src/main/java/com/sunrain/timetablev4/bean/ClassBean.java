package com.sunrain.timetablev4.bean;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ClassBean {

    public long _id;
    //week + section + time = 周二 + 下午 + 第三节
    public int week;
    public int section;
    public int time;
    public int startWeek;
    public int endWeek;
    public int doubleWeek;//0 未设置或单双周,1 双周,2 单周
    public String course;
    public String classroom;

    public ClassBean() {
    }

    public ClassBean(JSONObject jsonObject) throws JSONException {
        this.week = jsonObject.getInt("a");
        this.section = jsonObject.getInt("b");
        this.time = jsonObject.getInt("c");
        this.startWeek = jsonObject.getInt("d");
        this.endWeek = jsonObject.getInt("e");
        this.doubleWeek = jsonObject.getInt("f");
        this.course = jsonObject.getString("g");
        this.classroom = jsonObject.getString("h");
    }

    private JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", week);
        jsonObject.put("b", section);
        jsonObject.put("c", time);
        jsonObject.put("d", startWeek);
        jsonObject.put("e", endWeek);
        jsonObject.put("f", doubleWeek);
        jsonObject.put("g", course);
        jsonObject.put("h", classroom);
        return jsonObject;
    }

    public static JSONArray toJsonArray(List<ClassBean> list) {

        JSONArray jsonArray = new JSONArray();
        for (ClassBean classBean : list) {
            try {
                jsonArray.put(classBean.toJsonObject());
            } catch (JSONException ignore) {
            }
        }
        return jsonArray;
    }

    public static class Format {

        private static final String[] sWeekArray;
        private static final String[] sSectionArray;
        private static final String[] sTimeArray;
        private static final StringBuilder sSb;

        static {
            Resources resources = MyApplication.sContext.getResources();
            sWeekArray = resources.getStringArray(R.array.week);
            sSectionArray = resources.getStringArray(R.array.section);
            sTimeArray = resources.getStringArray(R.array.time);
            sSb = new StringBuilder();
        }

        public static String getFormatString(@NonNull ClassBean classBean) {
            sSb.setLength(0);
            sSb.append(classBean.course).append("\n");
            sSb.append(classBean.classroom).append("\n");
            sSb.append(sWeekArray[classBean.week]).append(" ");
            sSb.append(sSectionArray[classBean.section]).append(" ");
            sSb.append(sTimeArray[classBean.time]).append("\n");
            sSb.append(classBean.startWeek + 1).append(" ~ ").append(classBean.endWeek + 1).append(" 周");
            if (classBean.doubleWeek == 1) {
                sSb.append(" ").append("双周");
            } else if (classBean.doubleWeek == 2) {
                sSb.append(" ").append("单周");
            }
            return sSb.toString();
        }

        public static String getFormatTime(@NonNull ClassBean classBean) {
            sSb.setLength(0);
            sSb.append(sWeekArray[classBean.week]).append(" ");
            sSb.append(sSectionArray[classBean.section]).append(" ");
            sSb.append(sTimeArray[classBean.time]).append("\n");
            sSb.append(classBean.startWeek + 1).append(" ~ ").append(classBean.endWeek + 1).append(" 周");
            if (classBean.doubleWeek == 1) {
                sSb.append(" ").append("双周");
            } else if (classBean.doubleWeek == 2) {
                sSb.append(" ").append("单周");
            }
            return sSb.toString();
        }

        /**
         * 小控件用
         */
        public static String getFormatTimeInDay(int time) {
            return sTimeArray[time];
        }

        public static String getFormatCourseClassroom(@NonNull ClassBean classBean) {
            sSb.setLength(0);
            sSb.append(classBean.course).append("\n");
            sSb.append(classBean.classroom).append("\n");
            sSb.append(classBean.startWeek + 1).append(" ~ ").append(classBean.endWeek + 1).append(" 周");
            if (classBean.doubleWeek == 1) {
                sSb.append(" ").append("双周");
            } else if (classBean.doubleWeek == 2) {
                sSb.append(" ").append("单周");
            }
            return sSb.toString();
        }

        /**
         * 小控件用
         */
        public static String getFormatCourseClassroomInDay(@NonNull ClassBean classBean) {
            sSb.setLength(0);
            sSb.append(classBean.course).append(" ");
            sSb.append(classBean.classroom).append("\n");
            return sSb.toString();
        }
    }
}
