package com.example.auctionhouse.Model;

public class Products {
    private String name, description, price, image, dateDown, dateUp, timeUp, timeDown, pid, userLastBid, userUpload, currency;

    public  Products() {

    }

    public Products(String name, String description, String price, String image, String dateDown, String dateUp, String timeUp, String timeDown, String pid, String userLastBid, String userUpload, String currency) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.dateDown = dateDown;
        this.dateUp = dateUp;
        this.timeUp = timeUp;
        this.timeDown = timeDown;
        this.pid = pid;
        this.userLastBid = userLastBid;
        this.userUpload = userUpload;
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDateDown() {
        return dateDown;
    }

    public void setDateDown(String dateDown) {
        this.dateDown = dateDown;
    }

    public String getDateUp() {
        return dateUp;
    }

    public void setDateUp(String dateUp) {
        this.dateUp = dateUp;
    }

    public String getTimeUp() {
        return timeUp;
    }

    public void setTimeUp(String timeUp) {
        this.timeUp = timeUp;
    }

    public String getTimeDown() {
        return timeDown;
    }

    public void setTimeDown(String timeDown) {
        this.timeDown = timeDown;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUserLastBid() {
        return userLastBid;
    }

    public void setUserLastBid(String userLastBid) {
        this.userLastBid = userLastBid;
    }

    public String getUserUpload() {
        return userUpload;
    }

    public void setUserUpload(String userUpload) {
        this.userUpload = userUpload;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
