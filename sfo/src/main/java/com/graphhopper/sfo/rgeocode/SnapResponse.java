package com.graphhopper.sfo.rgeocode;

public class SnapResponse {
    private String country;
    private String province;
    private String city;
    private String street;
    private String streetType;
    private Integer streetOsmId;
    private Integer provinceOsmId;
    private Integer cityOsmId;
    private Double streetMaxSpeed;

    public SnapResponse(){}

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public Integer getStreetOsmId() {
        return streetOsmId;
    }

    public void setStreetWayId(Integer streetWayId) {
        this.streetOsmId = streetWayId;
    }

    public Double getStreetMaxSpeed() {
        return streetMaxSpeed;
    }

    public void setStreetMaxSpeed(Double streetMaxSpeed) {
        this.streetMaxSpeed = streetMaxSpeed;
    }

    public void setStreetOsmId(Integer streetOsmId) {
        this.streetOsmId = streetOsmId;
    }

    public Integer getProvinceOsmId() {
        return provinceOsmId;
    }

    public void setProvinceOsmId(Integer provinceOsmId) {
        this.provinceOsmId = provinceOsmId;
    }

    public Integer getCityOsmId() {
        return cityOsmId;
    }

    public void setCityOsmId(Integer cityOsmId) {
        this.cityOsmId = cityOsmId;
    }
}
