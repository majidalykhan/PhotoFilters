package com.example.photofilters;

public class User {


    String userName;
    String userUsername;
    String userEmail;
    String userPassword;

    public User(){

    }

    public User(String userName,
                String userUsername, String userEmail, String userPassword) {

        this.userName = userName;
        this.userUsername = userUsername;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
