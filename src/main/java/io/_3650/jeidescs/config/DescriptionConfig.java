package io._3650.jeidescs.config;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io._3650.jeidescs.JEIDescs;

public class DescriptionConfig {
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().registerTypeAdapter(DescriptionJson.class, new DescriptionJson.Serializer()).create();
	
	private final String name;
	private final DescriptionJson data;
	
	public DescriptionConfig(File file) {
		this.name = file.getName();
		this.data = this.readData(file);
	}
	
	private DescriptionJson readData(File file) {
		if (file.exists()) {
			try {
				return GSON.fromJson(FileUtils.readFileToString(file, Charset.defaultCharset()), DescriptionJson.class);
			} catch (Exception e) {
				JEIDescs.LOGGER.error("Config read failed for File: " + file, e);
			}
		}
		
		return new DescriptionJson();
	}
	
	public String getName() {
		return this.name;
	}
	
	public DescriptionJson getData() {
		return this.data;
	}
	
}