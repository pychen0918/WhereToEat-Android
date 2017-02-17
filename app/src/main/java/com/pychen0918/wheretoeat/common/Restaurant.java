package com.pychen0918.wheretoeat.common;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pychen0918 on 2016/11/7.
 */
public class Restaurant {
    // The following data can be collected at the first google place search
    private String name;
    private String placeId;
    private List<String> photoIds;
    private List<String> types;
    private double lat;
    private double lon;
    private int priceLevel;
    private float rating;

    // The following data can only be collected from detail query
    private String address;
    private String phoneNumber;
    private String website;
    private String url;
    private List<Review> reviews;
    private List<OpeningHours> openingHours;

    // The following data are extended from the above primitive data
    // We be set when certain data is acquired or action is performed
    private List<Bitmap> photos;
    private long distance;

    // This constructor is used when we do the first query (list 20 restaurants)
    // Only with the info return by place search api
    public Restaurant(String name, String placeId, List<String> photoIds, List<String> types,
                      double lat, double lon, int priceLevel, float rating) {
        this.name = name;
        this.placeId = placeId;
        this.photoIds = photoIds;
        this.types = types;
        this.lat = lat;
        this.lon = lon;
        this.priceLevel = priceLevel;
        this.rating = rating;

        this.address = "";
        this.phoneNumber = "";
        this.website = "";
        this.url = "";
        this.reviews = new ArrayList<>();
        this.openingHours = new ArrayList<>();

        this.photos = new ArrayList<>();
    }

    private Review getReviewInstance(String author, int rating, String text, long time) {
        Review review = new Review();
        review.author = author;
        review.rating = rating;
        review.text = text;
        review.time = time;
        return review;
    }

    private OpeningHours getOpeningHoursInstance(int start, int end) {
        OpeningHours openingHours = new OpeningHours();
        openingHours.start = start;
        openingHours.end = end;
        return openingHours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public List<String> getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(List<String> photoIds) {
        this.photoIds = photoIds;
    }

    public String getPhotoId(int index) {
        if (!this.photoIds.isEmpty() && (index < this.photoIds.size())) {
            return this.photoIds.get(index);
        }
        return "";
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(String author, int rating, String text, long time) {
        this.reviews.add(getReviewInstance(author, rating, text, time));
    }

    public void sortReviewByDate() {
        Collections.sort(this.reviews, new Comparator<Review>() {
            @Override
            public int compare(Review r1, Review r2) {
                return (r1.time == r2.time) ? 0 :
                        ((r1.time > r2.time) ? -1 : 1);
            }
        });
    }

    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<OpeningHours> openingHours) {
        this.openingHours = openingHours;
    }

    public void addOpeningHours(int start, int end) {
        this.openingHours.add(getOpeningHoursInstance(start, end));
    }

    public List<Bitmap> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Bitmap> photos) {
        this.photos = photos;
    }

    public void addPhoto(Bitmap photo) {
        this.photos.add(photo);
    }

    public Bitmap getPhoto(int index) {
        if (!this.photos.isEmpty() && (index < this.photos.size())) {
            return this.photos.get(index);
        }
        return null;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    // Calculate the distance between this restaurant and input location (usually current location)
    public long calculateDistance(double lat1, double lon1) {
        double earthRadius = 6731009;
        double a, b;
        double lat2 = this.lat;
        double lon2 = this.lon;

        a = (lat1 - lat2) * (Math.PI / 180);
        b = Math.cos((lat1 + lat2) * (Math.PI / 180) / 2) * (lon1 - lon2) * (Math.PI / 180);

        return Math.round((earthRadius * Math.sqrt(a * a + b * b)) / 10) * 10;  // roundup the number to 10 meters
    }

    public class Review {
        public String author;
        public int rating;
        public String text;
        public long time;
    }

    public class OpeningHours {
        public int start;
        public int end;
    }
}
