package com.pychen0918.wheretoeat.googleplace;

import java.util.List;

/**
 * Created by pychen0918 on 2016/11/14.
 */

public class GooglePlaceDetailJsonContainer {
    public String status;
    public Result result;

    public class Result{
        public String formatted_address;
        public String formatted_phone_number;
        public String website;
        public String url;
        public List<Reviews> reviews;
        public OpeningHours opening_hours;
    }

    public class Reviews{
        public String author_name;
        public String text;
        public int rating;
        public long time;
    }

    public class OpeningHours{
        public List<Periods> periods;
    }

    public class Periods{
        public HoursItem open;
        public HoursItem close;
    }

    public class HoursItem{
        public int day;
        public String time;
    }
}
