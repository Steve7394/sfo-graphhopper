FROM maven:3.8-openjdk-17 AS build
WORKDIR /app

# copy just project files
COPY ./client-hc ./client-hc
COPY ./core ./core
COPY ./example ./example
COPY ./map-matching ./map-matching
COPY ./navigation ./navigation
COPY ./reader-gtfs ./reader-gtfs
COPY ./sfo ./sfo
COPY ./tools ./tools
COPY ./web ./web
COPY ./web-api ./web-api
COPY ./web-bundle ./web-bundle
COPY ./pom.xml .

COPY ./.m2 /root/.m2

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk
WORKDIR /app

COPY --from=build /app/web/target/graphhopper-*.jar /app/graphhopper.jar
# copy data for offline mode
# custom polygons
# elevation data
# data from last run
# osm file for first run
COPY ./custom-areas ./custom-areas
COPY ./srtmprovider ./srtmprovider
COPY ./graph-cache ./graph-cache
COPY ./iran-latest.osm.pbf .

COPY ./config-example.yml .


EXPOSE 8989
ENTRYPOINT ["java", "-jar", "graphhopper.jar"]
