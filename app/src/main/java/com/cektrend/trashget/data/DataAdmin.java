package com.cektrend.trashget.data;

public class DataAdmin {
    private String username;
    private String name;
    private String password;
    private String phone;
    private String alamat;

    public DataAdmin(String username, String name, String password, String phone, String alamat) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.alamat = alamat;
    }

    public DataAdmin() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
