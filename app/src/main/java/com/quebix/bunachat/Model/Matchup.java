package com.quebix.bunachat.Model;

public class Matchup{
    private String image, fullName, age, location, date, userId;
    private Boolean flag;

    public Matchup() {}

    public Matchup(String userId, String image, String fullName, String age, String location) {
        this.userId = userId;
        this.image = image;
        this.fullName = fullName;
        this.age = age;
        this.location = location;
    }

    public Matchup(String fullName, String age, String location) {
        this.fullName = fullName;
        this.age = age;
        this.location = location;
    }

    public Matchup(String fullName, Boolean flag) {
        this.fullName = fullName;
        this.flag = flag;
    }

    public Matchup(String fullName, String date) {
        this.fullName = fullName;
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
