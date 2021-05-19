package com.cektrend.trashget.collector;

public class TruckList {
    public String head, body;

    public TruckList() {
    }

    public TruckList(String head, String body) {
        this.head = head;
        this.body = body;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}