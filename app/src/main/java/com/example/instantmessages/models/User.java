package com.example.instantmessages.models;

import java.util.List;

public class User {
    private String id, name, email, profileURL;
    private boolean isChecked;

    public User(){

    }
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        profileURL = "https://firebasestorage.googleapis.com/v0/b/instantmessages-c52b8.appspot.com/o/images%2Fdefault.png?alt=media&token=2cf93920-b220-4f37-8067-2128c411619e";
    }
    public String toString(){
        return id+" "+name+" "+email;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}
