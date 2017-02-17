package com.pychen0918.wheretoeat.googleplace;

import java.util.List;

/**
 * Created by pychen0918 on 2016/11/7.
 */

public class GooglePlaceJsonContainer {
    public String status;
    public List<GooglePlaceJsonResults> results;

    public GooglePlaceJsonContainer() {}

    public class GooglePlaceJsonResults {
        public String name;
        public String place_id;
        public int price_level;
        public float rating;
        public Geometry geometry;
        public List<String> types;
        public Photos[] photos;

        public class Geometry {
            public Location location;
            public class Location{
                public Double lat;
                public Double lng;
            }
        }

        public class Photos {
            public String photo_reference;
        }
    }
}
