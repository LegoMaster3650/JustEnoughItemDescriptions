package io._3650.jeid.config;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;

import io._3650.jeid.JEID;
import net.minecraftforge.fml.loading.FMLPaths;

public class Config {
	
	private static final Path DEFAULT_FOLDER = FMLPaths.CONFIGDIR.get().resolve(JEID.MOD_ID);
	private static final FileFilter JSON_FILTER = file -> {
		return file.isFile() && file.getName().endsWith(".json");
	};
	
	private final ImmutableList<DescriptionConfig> configs;
	
	private Config(Path path) throws NotDirectoryException {
		File folder = path.toFile();
		if (!folder.exists()) folder.mkdir();
		if (!folder.isDirectory()) throw new NotDirectoryException(folder.getAbsolutePath());
		File[] files = folder.listFiles(JSON_FILTER);
		Arrays.sort(files); //For consistency, files will be in alphabetical order

		ImmutableList.Builder<DescriptionConfig> config = ImmutableList.builderWithExpectedSize(files.length);
		for (File file : files) {
			config.add(new DescriptionConfig(file));
		}
		
		this.configs = config.build();
	}
	
	public ImmutableList<DescriptionConfig> getConfigs() {
		return this.configs;
	}
	
//	private static Config instance;
	
	public static Config reload() throws NotDirectoryException {
		JEID.LOGGER.info("Reloading Just Enough Item Descriptions Config");
		return new Config(DEFAULT_FOLDER);
	}
	
}