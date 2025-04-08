package com.graphhopper.sfo.rgeocode.dto;

import java.util.List;

public class SnapResponse {
    private final static String MISSING_STRING = "missing";
    private final static int MISSING_INTEGER = 0;
    private String street = MISSING_STRING;
    private String streetType = MISSING_STRING;
    private Double streetMaxSpeed = MISSING_INTEGER * .0;
    private Integer streetOsmId = MISSING_INTEGER;


    private String country = MISSING_STRING;

    private String province = MISSING_STRING;
    private Integer provinceOsmId = MISSING_INTEGER;

    private String city = MISSING_STRING;
    private Integer cityOsmId = MISSING_INTEGER;

    private String county = MISSING_STRING;
    private Integer countyOsmId = MISSING_INTEGER;

    private String district = MISSING_STRING;
    private Integer districtOsmId = MISSING_INTEGER;

    private String village = MISSING_STRING;
    private Integer villageOsmId = MISSING_INTEGER;

    private String suburb = MISSING_STRING;
    private Integer suburbOsmId = MISSING_INTEGER;

    private String subarea = MISSING_STRING;
    private Integer subareaOsmId = MISSING_INTEGER;

    private String neighbourhood = MISSING_STRING;
    private Integer neighbourhoodOsmId = MISSING_INTEGER;

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
