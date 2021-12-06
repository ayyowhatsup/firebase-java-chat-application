package com.example.instantmessages.models;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String createdBy,groupName,groupID,groupImageURL;
    private Long createdOn;
    private List<String> members = new ArrayList<>();

    public Group(){

    }
    public Group(String groupID,String groupName,List<String> members){
        this.createdBy = FirebaseAuth.getInstance().getUid();
        this.groupName = groupName;
        this.groupID = groupID;
        this.createdOn = System.currentTimeMillis() /1000;
        this.members = members;
        this.groupImageURL = "https://firebasestorage.googleapis.com/v0/b/instantmessages-c52b8.appspot.com/o/images%2Fdefault_group.png?alt=media&token=8eb300ce-2f64-4c32-9446-46ef56871166";
    }

    public List<String> getMembers() {
        return members;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getGroupImageURL() {
        return groupImageURL;
    }
}
