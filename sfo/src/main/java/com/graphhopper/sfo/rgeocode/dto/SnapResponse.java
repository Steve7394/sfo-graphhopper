package com.graphhopper.sfo.rgeocode.dto;

import java.util.List;

public class SnapResponse {
    private String street;
    private String streetType;
    private Double streetMaxSpeed;
    private Integer streetOsmId;


    private String country;

    private String province;
    private Integer provinceOsmId;

    private String city;
    private Integer cityOsmId;

    private String county;
    private Integer countyOsmId;

    private String district;
    private Integer districtOsmId;

    private String village;
    private Integer villageOsmId;

    private String suburb;
    private Integer suburbOsmId;

    private String subarea;
    private Integer subareaOsmId;

    private String neighbourhood;
    private Integer neighbourhoodOsmId;

    private List<Long> customPolygon;

    public SnapResponse(){}

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public Integer getCountyOsmId() {
        return countyOsmId;
    }

    public void setCountyOsmId(Integer countyOsmId) {
        this.countyOsmId = countyOsmId;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Integer getDistrictOsmId() {
        return districtOsmId;
    }

    public void setDistrictOsmId(Integer districtOsmId) {
        this.districtOsmId = districtOsmId;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public Integer getVillageOsmId() {
        return villageOsmId;
    }

    public void setVillageOsmId(Integer villageOsmId) {
        this.villageOsmId = villageOsmId;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public Integer getSuburbOsmId() {
        return suburbOsmId;
    }

    public void setSuburbOsmId(Integer suburbOsmId) {
        this.suburbOsmId = suburbOsmId;
    }

    public String getSubarea() {
        return subarea;
    }

    public void setSubarea(String subarea) {
        this.subarea = subarea;
    }

    public Integer getSubareaOsmId() {
        return subareaOsmId;
    }

    public void setSubareaOsmId(Integer subareaOsmId) {
        this.subareaOsmId = subareaOsmId;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public Integer getNeighbourhoodOsmId() {
        return neighbourhoodOsmId;
    }

    public void setNeighbourhoodOsmId(Integer neighbourhoodOsmId) {
        this.neighbourhoodOsmId = neighbourhoodOsmId;
    }

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

    public List<Long> getCustomPolygon() {
        return customPolygon;
    }

    public void setCustomPolygon(List<Long> customPolygon) {
        this.customPolygon = customPolygon;
    }
}
