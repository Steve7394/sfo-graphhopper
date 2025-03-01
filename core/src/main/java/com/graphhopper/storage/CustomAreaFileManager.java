package com.graphhopper.storage;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.util.JsonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomAreaFileManager {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path directory;
    private final String suffix;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAreaFileManager.class);
    public CustomAreaFileManager(String directory, String suffix) {
        this.objectMapper.registerModule(new JtsModule());
        this.directory = Paths.get(directory);
        this.suffix = suffix;
    }

    public void delete(String name){
        Path path = directory.resolve(name + "." + suffix);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CustomArea read(String name){
        Path path = directory.resolve(name + "." + suffix);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonFeature jsonFeature = objectMapper.readValue(reader, JsonFeature.class);
            return CustomArea.fromJsonFeature(jsonFeature);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(String name, CustomArea area) {
        Path path = directory.resolve(name + "." + suffix);
        JsonFeature jsonFeature = new JsonFeature(area.getProperties().get("id").toString(), "POLYGON", null, area.getBorders().get(0), area.getProperties());
        try {
            this.objectMapper.writeValue(path.toFile(), jsonFeature);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<CustomArea> getAllCustomAreas(){
        List<JsonFeature> jsonFeatures = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.directory, "*." + this.suffix)) {
            for (Path borderFile : stream) {
                try (BufferedReader reader = Files.newBufferedReader(borderFile, StandardCharsets.UTF_8)) {
                    JsonFeature jsonFeature = objectMapper.readValue(reader, JsonFeature.class);
                    jsonFeatures.add(jsonFeature);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return jsonFeatures.stream()
                .map(CustomArea::fromJsonFeature)
                .collect(Collectors.toList());
    }
}
