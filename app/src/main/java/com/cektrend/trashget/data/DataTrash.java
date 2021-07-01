package com.cektrend.trashget.data;

public class DataTrash {
    private String id;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer organicCapacity;
    private Integer anorganicCapacity;
    private Long kadarGas;
    private Boolean isFire;
    private Boolean isNotif;
    private Boolean fireNotif;
    private Boolean isChecked;
    private Integer tinggiBak;

    public DataTrash() {
    }

    public DataTrash(Double latitude, Double longitude, Integer organicCapacity, Integer anorganicCapacity, Long kadarGas, Boolean isFire, String location, Boolean isNotif, Boolean fireNotif, Boolean isChecked, Integer tinggiBak) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.organicCapacity = organicCapacity;
        this.anorganicCapacity = anorganicCapacity;
        this.kadarGas = kadarGas;
        this.isFire = isFire;
        this.location = location;
        this.isNotif = isNotif;
        this.fireNotif = fireNotif;
        this.isChecked = isChecked;
        this.tinggiBak = tinggiBak;
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

    public Long getKadarGas() {
        return kadarGas;
    }

    public void setKadarGas(Long kadarGas) {
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

    public Boolean getNotif() {
        return isNotif;
    }

    public void setNotif(Boolean notif) {
        isNotif = notif;
    }

    public Boolean getFireNotif() {
        return fireNotif;
    }

    public void setFireNotif(Boolean fireNotif) {
        this.fireNotif = fireNotif;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }

    public Integer getTinggiBak() {
        return tinggiBak;
    }

    public void setTinggiBak(Integer tinggiBak) {
        this.tinggiBak = tinggiBak;
    }
}
