package com.jseb.growstone;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.World.Environment;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

public class GrowStone extends JavaPlugin implements Listener {
	boolean debug;
	public static GrowQueue growQueue;
	public BukkitScheduler scheduler;
	public static Config config;
	public boolean blockChooser;

	public void onEnable() {
		//get folders/files ready
		File baseDirectory = this.getDataFolder();
		String filePath = baseDirectory.getAbsolutePath();

		if (!baseDirectory.exists()){
			baseDirectory.mkdirs();
		}

		//load config
		FileConfiguration fileConfig = getConfig();
		config = new Config(filePath, fileConfig, this);

		//load up the grow queue
		growQueue = new GrowQueue(this, new File(filePath + File.separator + "grow-locations.bin"));

		try {
			growQueue.load();
			//growQueue.clean();
		} catch (IOException e) {
			
		}

		//set up schedulers
		scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new GrowerTask(this), 20, 100);

		getServer().getPluginManager().registerEvents(this, this);
		blockChooser = false;
	}
	
	public void onDisable() {
		//populate file with sources
		try {
			growQueue.save();
		} catch (IOException e) {
			
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Block block = e.getBlockPlaced();
		Player player = e.getPlayer();
		boolean nether = false, overworld = false, theEnd = false;
		try {
			nether = config.world.contains("nether");
			overworld = config.world.contains("overworld");
			theEnd = config.world.contains("end");
		} catch (NullPointerException exeption) {
			// if the string is blank...
			nether = true;
			overworld = true;
			theEnd = true;
		}
		

		if (block.getType().equals(Material.GLOWSTONE)) {
			if (validBlockBelow(block) && blockRequirement(block)) {
				if ((nether && block.getWorld().getEnvironment().equals(Environment.NETHER)) 
				|| (overworld && block.getWorld().getEnvironment().equals(Environment.NORMAL))
				|| (theEnd && block.getWorld().getEnvironment().equals(Environment.THE_END))) {
					//register for growing.
					if (config.debug) {
						System.out.println("valid block below, block requirement met");
					}
					growQueue.addBlock(block);
				}	
			} 
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Block block = e.getBlock();
		Player player = e.getPlayer();
		if (block.getType().equals(Material.GLOWSTONE)) {
			if (growQueue.containsBlock(block)) {
				for (int i = 1; i < config.maxClusterSize; i++) {
					Block temp = block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ());
					if (temp.getType().equals(Material.GLOWSTONE)) {
						if (player.getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
							temp.setType(Material.AIR);
							Location location = temp.getLocation();
							ItemStack drop = new ItemStack(Material.GLOWSTONE);
							temp.getWorld().dropItem(location, drop);
						} else {
							temp.breakNaturally();
						}
					}
				}
			} else {
				for (int i = 1; i <= config.maxClusterSize; i++) {
					Block temp = block.getWorld().getBlockAt(block.getX(), block.getY() - i, block.getZ());
					if (growQueue.containsBlock(temp)) {
						for (int j = 0; j <= config.maxClusterSize - (i + 1); j++) {
							temp = block.getWorld().getBlockAt(block.getX(), block.getY() + j, block.getZ());
							if (temp.getType().equals(Material.GLOWSTONE)) {
								if (player.getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
									temp.setType(Material.AIR);
									Location location = temp.getLocation();
									ItemStack drop = new ItemStack(Material.GLOWSTONE);
									temp.getWorld().dropItem(location, drop);
								} else {
									temp.breakNaturally();
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (blockChooser)) {
			if (config.debug) {
				System.out.println("Choose a block.");
			}
			Block block = e.getClickedBlock();
			if ((block.equals(Material.GLOWSTONE)) && (isValid(block))) {
				if (growQueue.containsBlock(block)) {
					growQueue.remove(block);
				} else {
					growQueue.addBlock(block);
				} else if (e.get action().equals(Action.
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("growstone")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (args[1].equalsIgnoreCase("config")) {
						if (config.refreshConfig()) {
							sender.sendMessage("Config successfully reloaded.");
						}
					}
				} else if (args[0].equalsIgnoreCase("setflag")) {

				} else if (args[0].equalsIgnoreCase("chooseblock")) {
					blockChooser = !blockChooser;
					sender.sendMessage("Block chooser set to " + blockChooser);
				}
			}
		} 
		return true;
	}

	public static boolean isValid(Block block) {
		if (blockRequirement(block) && validBlockBelow(block)) {
			return true;
		}

		return false;
	}

	public static boolean blockRequirement(Block block) {
		if (config.requires == null) {
			return true;
		}

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				for (int y = -1; y <= 0; y++) {
					Material material = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z).getType();
					if (!config.requires.contains(",")) {
						Material temp = Material.getMaterial(config.requires.trim().toUpperCase());
						if (temp != null) {
							if (temp.equals(material)) {
								return true;
							}
						} 
						continue;
					}

					for (String mat : config.requires.split(",")) {
						Material temp = Material.getMaterial(mat.trim().toUpperCase());
						System.out.println(temp + " " + material);
						if (temp != null) {
							if (temp.equals(material)) {
								return true;
							}
						}
					}
				}
			}
		}
		if (config.debug) { 
			System.out.println("block requirement fail");
		}
		return false;
	}

	public static boolean validBlockBelow(Block block) {
		if (config.growsOn == null) {
			return true; 
		}

		Material below = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ()).getType();

		if (!config.growsOn.contains(",")) {
			Material temp = Material.getMaterial(config.growsOn.trim().toUpperCase());
			if (temp != null) {
				if (temp.equals(below)) {
					return true;
				}
			} 

			if (config.debug) { 
				System.out.println("block below fail");
			}
			return false;
		}

		for (String material : config.growsOn.split(",")) {
			Material temp = Material.getMaterial(material.trim().toUpperCase());
			if (temp != null) {
				if (temp.equals(below)) {
					return true;
				}
			}
		}

		if (config.debug) { 
			System.out.println("block below fail");
		}
		return false;
	}

	public static boolean validWorld(Block block) {
		return true;
	}
}