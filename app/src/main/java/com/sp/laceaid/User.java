package com.sp.laceaid;

public class User {
    // class to group data for storing in realtime database
    private String name, email;

    private User(){
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
