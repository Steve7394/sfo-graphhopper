# SFO Module

# this module contains all rests that needed for sfo project

## single point reverse geocode
* URL: http://localhost:8989/reverse-geocode
* Method: post
* Content-Type: application/json

* Request Body Example:
```json
{
  "lat": 35.70069889205458,
  "lon": 51.336722455526775
}
```

* Response Body Example:
```json
{
    "car_subnetwork": "false",
    "road_class_link": "false",
    "road_access": "yes",
    "road_environment": "road",
    "road_class": "trunk",
    "max_speed": "30.0 | 30.0",
    "car_average_speed": "28.0 | 28.0",
    "car_access": "true | false",
    "ferry_speed": "0.0",
    "street_name": "میدان آزادی",
    "roundabout": "true"
}
```
* URL: http://localhost:8989/reverse-geocode/lat=35.71857813726428&lon=51.37114918411147
* Method: get
* Content-Type: application/json
```

* Response Body Example:
```json
{
    "car_subnetwork": "false",
    "road_class_link": "false",
    "road_access": "yes",
    "road_environment": "road",
    "road_class": "trunk",
    "max_speed": "30.0 | 30.0",
    "car_average_speed": "28.0 | 28.0",
    "car_access": "true | false",
    "ferry_speed": "0.0",
    "street_name": "میدان آزادی",
    "roundabout": "true"
}
```
## multi point reverse geocode

* URL: http://localhost:8989/reverse-geocode/{coordinatesArray}
* coordinatesArray example: 51.9528487,33.6830178;51.9507366,33.6837469;51.9525818,33.6826412
* Method: get
* Content-Type: application/json
```

* Response Body Example:
```json
{
    "car_subnetwork": "false",
    "road_class_link": "false",
    "road_access": "yes",
    "road_environment": "road",
    "road_class": "trunk",
    "max_speed": "30.0 | 30.0",
    "car_average_speed": "28.0 | 28.0",
    "car_access": "true | false",
    "ferry_speed": "0.0",
    "street_name": "میدان آزادی",
    "roundabout": "true"
}
```


