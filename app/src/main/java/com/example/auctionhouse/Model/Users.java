package com.example.auctionhouse.Model;

import java.util.ArrayList;

public class Users {
    private String name, surname, phone, email, pass, address, image;
    private int boughtListings, uploadedListings;
    private ArrayList<String> productsIDs = new ArrayList<>();

    public Users() {
    }

    public Users(String name, String surname, String phone, String email, String pass, String address, String image) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.pass = pass;
        this.address = address;
        this.image = image;
        this.boughtListings = 0;
        this.uploadedListings = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<String> getProductsIDs() {
        return productsIDs;
    }

    public int getBoughtListings() {
        return boughtListings;
    }

    public void setBoughtListings(int boughtListings) {
        this.boughtListings = boughtListings;
    }

    public int getUploadedListings() {
        return uploadedListings;
    }

    public void setUploadedListings(int uploadedListings) {
        this.uploadedListings = uploadedListings;
    }
}
