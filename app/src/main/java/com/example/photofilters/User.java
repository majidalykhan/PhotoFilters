package com.example.photofilters;

public class User {


    String name;
    String username;
    String email;
    String password;

    public User(){

    }

    public User(String name,
                String username, String email, String password) {

        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getname() {
        return name;
    }

    public String getusername() {
        return name;
    }

    public String getemail() {
        return email;
    }

    public String getpassword() {
        return password;
    }
}
