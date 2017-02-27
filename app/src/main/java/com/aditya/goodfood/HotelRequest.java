package com.aditya.goodfood;

/**
 * Created by Aditya PC on 2/26/2017.
 */

public class HotelRequest {
    private String name, lat, lon, url, rating, imageUrl;

    public HotelRequest(String name, String lat, String lon, String url, String rating, String imageUrl) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.url = url;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
