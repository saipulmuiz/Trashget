package com.cektrend.trashget.data;

public class DataTrash {
    private String id;
    private String location;
    private  Double latitude;
    private Double longitude;
    private Integer organicCapacity;
    private Integer anorganicCapacity;
    private Integer kadarGas;
    private Boolean isFire;

    public DataTrash(){
    }

    public DataTrash(Double latitude, Double longitude, Integer organicCapacity, Integer anorganicCapacity, Integer kadarGas, Boolean isFire, String location) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.organicCapacity = organicCapacity;
        this.anorganicCapacity = anorganicCapacity;
        this.kadarGas = kadarGas;
        this.isFire = isFire;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getOrganicCapacity() {
        return organicCapacity;
    }

    public void setOrganicCapacity(Integer organicCapacity) {
        this.organicCapacity = organicCapacity;
    }

    public Integer getAnorganicCapacity() {
        return anorganicCapacity;
    }

    public void setAnorganicCapacity(Integer anorganicCapacity) {
        this.anorganicCapacity = anorganicCapacity;
    }

    public Integer getKadarGas() {
        return kadarGas;
    }

    public void setKadarGas(Integer kadarGas) {
        this.kadarGas = kadarGas;
    }

    public Boolean getFire() {
        return isFire;
    }

    public void setFire(Boolean fire) {
        isFire = fire;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
