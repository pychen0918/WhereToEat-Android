package com.pychen0918.wheretoeat.common;

import com.google.gson.Gson;
import com.pychen0918.wheretoeat.googleplace.GooglePlaceDetailJsonContainer;
import com.pychen0918.wheretoeat.googleplace.GooglePlaceJsonContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by pychen0918 on 2016/11/14.
 */

public class QueryResults {
    private List<Restaurant> restaurantData;
    private IndexedHashMap typeMap;
    private IndexedHashMap distanceSiMap;
    private IndexedHashMap distanceIuMap;
    private IndexedHashMap openNowMap;
    private long lastQueryTime;
    private boolean isDirty;
    private boolean isQueryInProgress;
    private String localeLanguage;

    public QueryResults() {
        restaurantData = new ArrayList<>();
        typeMap = new IndexedHashMap();
        lastQueryTime = 0;
        isDirty = false;
        isQueryInProgress = false;
        localeLanguage = "en";
    }

    public List<Restaurant> getRestaurantData() {
        return restaurantData;
    }

    public void setRestaurantData(List<Restaurant> restaurantData) {
        this.restaurantData = restaurantData;
    }

    public int getRestaurantCount() {
        return restaurantData.size();
    }

    public void clearRestaurantData() {
        restaurantData.clear();
    }

    public void addOneRestaurant(Restaurant restaurant) {
        restaurantData.add(restaurant);
    }

    public Restaurant getOneRestaurant(int index) {
        Restaurant ret = null;
        int count = restaurantData.size();
        if (count > 0 && index >= 0 && index < count) {
            ret = restaurantData.get(index);
        }
        return ret;
    }

    public IndexedHashMap getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(IndexedHashMap typeMap) {
        this.typeMap = typeMap;
    }

    public IndexedHashMap getDistanceSiMap() {
        return distanceSiMap;
    }

    public void setDistanceSiMap(IndexedHashMap distanceSiMap) {
        this.distanceSiMap = distanceSiMap;
    }

    public IndexedHashMap getDistanceIuMap() {
        return distanceIuMap;
    }

    public void setDistanceIuMap(IndexedHashMap distanceIuMap) {
        this.distanceIuMap = distanceIuMap;
    }

    public IndexedHashMap getOpenNowMap() {
        return openNowMap;
    }

    public void setOpenNowMap(IndexedHashMap openNowMap) {
        this.openNowMap = openNowMap;
    }

    public String restaurantTypeTranslate(List<String> types) {
        String result = "";
        IndexedHashMap typeMap = this.typeMap;
        for (String type : types) {
            if (typeMap.containsKey(type)) {
                result = result + typeMap.get(type) + ", ";
            }
        }
        if (result.length() >= 2)
            result = result.substring(0, result.length() - 2); // remove the last ", "

        return result;
    }

    public long getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(long lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public void setLastQueryTimeToNow() {
        this.lastQueryTime = System.currentTimeMillis();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void markAsDirty() {
        this.isDirty = true;
    }

    public void markAsClean() {
        this.isDirty = false;
    }

    public boolean isQueryInProgress() {
        return isQueryInProgress;
    }

    public void setQueryInProgress(boolean queryInProgress) {
        isQueryInProgress = queryInProgress;
    }

    public void initLocalLanguage() {
        String language = Locale.getDefault().getLanguage();
        String region = Locale.getDefault().getCountry();
        if (region != null && !region.isEmpty()) {
            this.setLocaleLanguage(language + "-" + region);
        } else {
            this.setLocaleLanguage(language);
        }
    }

    public String getLocaleLanguage() {
        return localeLanguage;
    }

    public void setLocaleLanguage(String localeLanguage) {
        this.localeLanguage = localeLanguage;
    }

    // Check if the current restaurant data is valid
    // 1. recently queried, 2. not marked as dirty
    // Empty restaurant list is considered as valid
    public boolean isDataValid() {
        return ((System.currentTimeMillis() - lastQueryTime) < Constants.queryInterval &&
                !isDirty
        );
    }

    public boolean parseListResult(String jsonString) {
        GooglePlaceJsonContainer container;
        Gson gson = new Gson();

        try {
            container = gson.fromJson(jsonString, GooglePlaceJsonContainer.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        if (!this.restaurantData.isEmpty())
            this.restaurantData.clear();

        for (int i = 0; i < container.results.size(); i++) {
            GooglePlaceJsonContainer.GooglePlaceJsonResults item = container.results.get(i);
            GooglePlaceJsonContainer.GooglePlaceJsonResults.Photos photos[] = container.results.get(i).photos;
            List<String> photoIds = new ArrayList<>();
            if (photos != null && photos.length > 0) {
                for (GooglePlaceJsonContainer.GooglePlaceJsonResults.Photos photo : photos) {
                    photoIds.add(photo.photo_reference);
                }
            }
            this.addOneRestaurant(new Restaurant(
                    stringSafeStore(item.name),
                    stringSafeStore(item.place_id),
                    photoIds,
                    item.types,
                    item.geometry.location.lat,
                    item.geometry.location.lng,
                    item.price_level,
                    item.rating
            ));
        }

        return true;
    }

    public boolean parseDetailResult(int index, String jsonString) {
        GooglePlaceDetailJsonContainer container;
        Gson gson = new Gson();

        try {
            container = gson.fromJson(jsonString, GooglePlaceDetailJsonContainer.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        Restaurant restaurant = this.getOneRestaurant(index);
        GooglePlaceDetailJsonContainer.Result resultContainer = container.result;

        if (restaurant != null) {
            restaurant.setPhoneNumber(stringSafeStore(resultContainer.formatted_phone_number));
            restaurant.setAddress(stringSafeStore(resultContainer.formatted_address));
            restaurant.setWebsite(stringSafeStore(resultContainer.website));
            restaurant.setUrl(stringSafeStore(resultContainer.url));
            if (resultContainer.reviews != null && !resultContainer.reviews.isEmpty()) {
                for (GooglePlaceDetailJsonContainer.Reviews review : resultContainer.reviews) {
                    restaurant.addReview(review.author_name, review.rating, review.text, review.time);
                }
                restaurant.sortReviewByDate();
            }
            if (resultContainer.opening_hours != null) {
                for (GooglePlaceDetailJsonContainer.Periods period : resultContainer.opening_hours.periods) {
                    if (period.open != null && period.close != null) {
                        int start = period.open.day * 1440 + convertTimeString(period.open.time);
                        int end = period.close.day * 1440 + convertTimeString(period.close.time);
                        restaurant.addOpeningHours(start, end);
                    } else {
                        // this restaurant never close
                        restaurant.addOpeningHours(0, 10800);
                        break;
                    }
                }
            }
        } else {
            // TODO
        }

        return true;
    }

    private String stringSafeStore(String input){
        return (input == null)?"":input;
    }

    private int convertTimeString(String time) {
        // format: 0800
        int hour = Integer.parseInt(time.substring(0, 2));
        int min = Integer.parseInt(time.substring(2));
        return hour * 60 + min;
    }

    public void sortByRating() {
        Collections.sort(restaurantData, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                return (r1.getRating() == r2.getRating()) ? 0 :
                        ((r1.getRating() > r2.getRating()) ? -1 : 1);
            }
        });
    }

    public void sortByDistance() {
        Collections.sort(restaurantData, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                return (r1.getDistance() == r2.getDistance()) ? 0 :
                        ((r1.getDistance() > r2.getDistance()) ? 1 : -1);
            }
        });
    }

    public void updateRestaurantDistance(double lat, double lon) {
        for (Restaurant restaurant : this.restaurantData) {
            restaurant.setDistance(restaurant.calculateDistance(lat, lon));
        }
    }

    public int getRandomIndex() {
        Random random = new Random();
        if (this.getRestaurantCount() > 0)
            return random.nextInt(this.getRestaurantCount());
        else
            return -1;
    }
}
