package com.quebix.bunachat;

public class Users {

    public String name, age, lookingFor, interestedIn, location;
    public String status;
    public String thumb_image;

    public Users() {
    }

    public Users(String name, String image, String status, String thumb_image, String online, String device_token) {
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public Users(String name, String age, String lookingFor, String interestedIn, String location) {
        this.name = name;
        this.age = age;
        this.lookingFor = lookingFor;
        this.interestedIn = interestedIn;
        this.location = location;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getLookingFor() {
        return lookingFor;
    }

    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }

    public String getInterestedIn() {
        return interestedIn;
    }

    public void setInterestedIn(String interestedIn) {
        this.interestedIn = interestedIn;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }
    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

}
