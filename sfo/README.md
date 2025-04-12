# SFO Module

# This module contains all rests that needed for sfo project

## Single point reverse geocode
* URL: http://localhost:8989/reverse-geocode
* Method: post
* Content-Type: application/json

* Request Body Example:
```json
{
    "lat": 35.70069889205458,
    "lon": 51.336722455526775,
    "force_edge": false
}
```
-------------------
* lat: latitude of requested point
* lon: longitude of requested point
* force_edge: if equals to true and the point was not near road exception will raise, otherwise the street info will be returned as "missing"
------------------
* Response Body Example:
```json
{
    "street": "فشافویه - چرمشهر",
    "street_type": "SECONDARY",
    "street_max_speed": 60.0,
    "street_osm_id": 544630610,
    "country": "Iran",
    "province": "استان تهران",
    "province_osm_id": 537701,
    "city": "دهستان کلین",
    "city_osm_id": 6558161,
    "county": "شهرستان ری",
    "county_osm_id": 6555053,
    "district": "بخش فشاپویه",
    "district_osm_id": 6555676,
    "village": "missing",
    "village_osm_id": 0,
    "suburb": "missing",
    "suburb_osm_id": 0,
    "subarea": "missing",
    "subarea_osm_id": 0,
    "neighbourhood": "missing",
    "neighbourhood_osm_id": 0,
    "custom_polygon": []
}
```
------------
* street: street name near the requested point
* street_type: the level of street near requested point for more details see https://wiki.openstreetmap.org/wiki/Key:highway
* street_max_speed: the max speed in street near the requested point
* street_osm_id: id of street near the requested point in openstreetmap
* country: country name that the requested point is in (کشور)
* province: province name that the requested point is in (استان)
* province_osm_id: province id in openstreetmap that the requested point is in
* city: city name that the requested point is in (شهر یا دهستان)
* city_osm_id: city id in openstreetmap that the requested point is in
* county: county name that the requested point is in (شهرستان)
* county_osm_id: county id in openstreetmap that the requested point is in
* district: district name that the requested point is in (بخش)
* district_osm_id: district id in openstreetmap that the requested point is in
* village: village name that the requested point is in (روستا)
* village_osm_id: village id in openstreetmap that the requested point is in
* suburb: suburb name that the requested point is in (منطقه شهری)
* suburb_osm_id: suburb id in openstreetmap that the requested point is in
* subarea: subarea name that the requested point is in (ناحیه شهری)
* subarea_osm_id: subarea id in openstreetmap that the requested point is in
* neighbourhood: neighbourhood name that the requested point is in (محله شهری)
* neighbourhood_osm_id: neighbourhood id in openstreetmap that the requested point is in
* custom_polygon: list of id of custom polygons that the user defined 
------------
if there is no data for each of below keys, the default missing value will be returned for that key
* missing value for strings equals to "missing"
* missing value for integer and doubles will be 0
* missing value for lists equals to [] (empty list)
---------
* URL: http://localhost:8989/reverse-geocode/lat=35.71857813726428&lon=51.37114918411147&forceEdge=true
* Method: get
* Content-Type: application/json

---------
    the request and response properties are like above post method.
----------
## Multi point reverse geocode

* URL: http://localhost:8989/reverse-geocode/{coordinatesArray}
* coordinatesArray example: 51.9528487,33.6830178;51.9507366,33.6837469;51.9525818,33.6826412?forceEdge=true
* Method: get
* Content-Type: application/json
---------
 * coordinatesArray: comma seperated (lon,lat) and semicolon seperated points (lon,lat;lon,lat)
 * other the request and response properties are like above post method.
----------

* URL: http://localhost:8989/reverse-geocode/match/{coordinatesArray}
* coordinatesArray example: 51.9528487,33.6830178;51.9507366,33.6837469;51.9525818,33.6826412?forceEdge=true
* Method: get
* Content-Type: application/json

---------
* coordinatesArray: comma seperated (lon,lat) and semicolon seperated points (lon,lat;lon,lat)
----------
* Response Body Example:
```json
{
    "snap_response": {...},
    "distance": 49609.66,
    "time": 1788.36,
    "average_max_speed": 110.32
}
```
---------
* snap_response: like above post method response
* distance: the distance from first point via other node to last node (in route distance)
* time: the time from first point via other node to last node (in route time)
* average_max_speed: the average max speed of matched streets
----------


