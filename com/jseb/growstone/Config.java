package com.jseb.growstone;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.World;

import java.io.*;

public class Config {
	public FileConfiguration config; 
	public static String configPath;
	public GrowStone plugin;

	public static double growChance;
	public static int growTimeDelay;
	public static int growTimeDelayPenalty;
	public static boolean debug;
	public static boolean growAround;
	public static String growsOn;
	public static String requires;
	public static int maxClusterSize;
	public static String world; 

	public Config(String filePath, FileConfiguration config, GrowStone plugin) {
		//get fileconfig
		this.config = config;
		this.configPath = filePath + File.separator + "config.yml";
		this.plugin = plugin;

		init(filePath);
	}

	public void init(String filePath) {
		File growStoneConfig = new File(filePath + File.separator + "config.yml");
		if (growStoneConfig.exists()) {
			try {
				config.load(this.configPath);
				refreshConfig();
			} catch (Exception e) {

			}
		} else {
			try {
				growStoneConfig.createNewFile();
				defaults();
				config.save(this.configPath);
			} catch (IOException e) {
				System.out.println("Error creating config file.");
				e.printStackTrace();
			}
		}
	}

	public void defaults() {
		config.set("general.growChance", 0.65);
		config.set("general.growTimeDelay", 100000);
		config.set("general.growTimeDelayPenalty", 150000);
		config.set("general.debug", false);
		config.set("general.growAround", false);
		config.set("general.requires", "stationary_water");
		config.set("general.growsOn", "dirt, grass");
		config.set("general.maxClusterSize", 3);
		config.set("general.world", "overworld, nether");

		refreshConfig();
	}

	public boolean refreshConfig() {
		try {
			config.load(this.configPath);
		} catch (Exception e) {
			return false;
		}

		this.requires = null;
		this.growsOn = null;

		this.growChance = config.getDouble("general.growChance");
		this.growTimeDelay = config.getInt("general.growTimeDelay");
		this.growTimeDelayPenalty = config.getInt("general.growTimeDelayPenalty");
		this.debug = config.getBoolean("general.debug");
		this.growAround = config.getBoolean("general.growAround");
		this.requires = config.getString("general.requires");
		this.growsOn = config.getString("general.growsOn");
		this.maxClusterSize = config.getInt("general.maxClusterSize");
		this.world = config.getString("general.world");

		if (debug) {
			printDebug();
		}

		return true;
	} 

	public boolean saveConfig() {
		config.set("general.growChance", this.growChance);
		config.set("general.growTimeDelay", this.growTimeDelay);
		config.set("general.growTimeDelayPenalty", this.growTimeDelayPenalty);
		config.set("general.debug", this.debug);
		config.set("general.growAround", this.growAround);
		config.set("general.requires", this.requires);
		config.set("general.growsOn", this.growsOn);
		config.set("general.maxClusterSize", this.maxClusterSize);
		config.set("general.world", this.world);

		if (debug) {
			printDebug();
		}

		try {
			config.save(this.configPath);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public void printDebug() {
		System.out.println("growChance: " + config.getDouble("general.growChance"));
		System.out.println("growTimeDelay: " + config.getInt("general.growTimeDelay"));
		System.out.println("growTimeDelayPenalty: " + config.getInt("general.growTimeDelayPenalty"));
		System.out.println("debug: " + config.getBoolean("general.debug"));
		System.out.println("growAround: " + config.getBoolean("general.growAround"));
		System.out.println("requires: " + config.getString("general.requires"));
		System.out.println("growsOn: " + config.getString("general.growsOn"));
		System.out.println("maxClusterSize: " + config.getInt("general.maxClusterSize"));
		System.out.println("world: " + config.getString("general.world"));
	}
}