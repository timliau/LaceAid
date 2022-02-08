package com.sp.laceaid;

public class User {
    // class to group data for storing in realtime database
    private String firstName, lastName, email;

    private User(){
    }

    // to compile the data then push to firebase
    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

}
