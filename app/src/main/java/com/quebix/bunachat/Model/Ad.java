package com.quebix.bunachat.Model;

public class Ad {
    private String image, name;
    private int testImage;

    public Ad() {
    }

    public Ad(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public Ad(String name, int testImage) {
        this.name = name;
        this.testImage = testImage;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTestImage() {
        return testImage;
    }

    public void setTestImage(int testImage) {
        this.testImage = testImage;
    }
}
