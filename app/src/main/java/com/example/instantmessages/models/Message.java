package com.example.instantmessages.models;

public class Message implements Comparable<Message>{
    private String from,receiveId,message,type;
    private long time;
    private boolean attached;
    private String imageUrl;
    public Message(){

    }
    public Message(String message,String from, String receiveId, String type){
        this.from=from;
        this.receiveId = receiveId;
        this.type = type;
        this.message=message;
        this.time=System.currentTimeMillis()/1000;
        attached = false;
        
    }
    public Message(boolean attached, String imageUrl, String from, String receiveId, String type){
        this.attached = attached;
        this.imageUrl= imageUrl;
        this.from = from;
        this.time = System.currentTimeMillis()/1000;
        this.receiveId = receiveId;
        this.type = type;
    }

    public String getReceiveId() {
        return receiveId;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAttached() {
        return attached;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public int compareTo(Message message) {
        return message.getTime() > time ? 1 : -1;
    }
}
