package com.jseb.growstone;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;


import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;

public class GrowQueue {
	private static File saveFile;
	private static LinkedHashMap<Block, Long> blockqueue;
	private static GrowStone plugin;
	private static Random rand; 

	public GrowQueue(JavaPlugin plugin, File file) {
		this.saveFile = file;
		this.blockqueue = new LinkedHashMap<Block, Long>();
		this.plugin = (GrowStone) plugin;
		this.rand = new Random();
	}

	public static void save() throws IOException {
		BufferedWriter br;
		br  = new BufferedWriter(new FileWriter(saveFile));

		for (Block block : blockqueue.keySet()) {
			br.write(block.getWorld() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
			br.newLine();
			br.flush();
		}
		
		br.close();

		if (plugin.config.debug) {
			System.out.println("Data successfully saved.");
		}
	}

	public static void load() throws IOException {
		BufferedReader br;
		br  = new BufferedReader(new FileReader(saveFile));

		String s = br.readLine();

		while (s != null) {
			blockqueue.put(loadParser(s), generateTimeDelay(false));
			s = br.readLine();
		}

		br.close();
		
		if (plugin.config.debug) {
			System.out.println("Data successfully loaded.");
		}
	}

	public static Block loadParser(String string) {
		String list[] = string.split(" ");
		World world = plugin.getServer().getWorld(list[0].substring(list[0].indexOf("=") + 1, list[0].length() - 1));
		return (world.getBlockAt(Integer.parseInt(list[1]), Integer.parseInt(list[2]), Integer.parseInt(list[3])));
	}

	public static void clean() {
		for (Block block : blockqueue.keySet()) {
			if (!block.getType().equals(Material.GLOWSTONE)) {
				blockqueue.remove(block);
			}
		}
	}

	public static int size() {
		return blockqueue.size();
	}

	public void clear() {
		this.blockqueue.clear();
	}

	public void remove(Block block) {
		blockqueue.remove(block);
	}

	public void removeAll(List<Block> locations) {
		for (Block block : locations) {
			blockqueue.remove(block);
		}
	}

	public void growAll(List<Block> locations) {
		for (Block block : locations) {
			for (int i = 1; i < plugin.config.maxClusterSize; i++) {
				World world = block.getWorld();
				Material temp = world.getBlockAt(block.getX(), block.getY() + i, block.getZ()).getType();
				if (temp.equals(Material.AIR)) {
					world.getBlockAt(block.getX(), block.getY() + i, block.getZ()).setType(Material.GLOWSTONE);
					updateTime(block, false);
					break;
				} else if (temp.equals(Material.GLOWSTONE)) { 
					continue;
				} else {
					if (!plugin.config.growAround) {
						break;
					}

					updateTime(block, true);
				}
			}
		}
	}

	public static Long generateTimeDelay(boolean penalty) {
		if (penalty) {
			return(System.currentTimeMillis() + (rand.nextInt(rand.nextInt(10) + 1) * (plugin.config.growTimeDelayPenalty)));
		} else {
			return(System.currentTimeMillis() + (rand.nextInt(rand.nextInt(10) + 1) * (plugin.config.growTimeDelay)));
		}
	}

	public boolean containsBlock(Block block2) {
		for (Block block : blockqueue.keySet()) {
			if (block.getLocation().equals(block2.getLocation())) {
				return true;
			}
		}
		return false;
	}

	public void updateTime(Block block, boolean penalty) {
		blockqueue.put(block, generateTimeDelay(penalty)); //overwrite old value
	}

	public void addBlock(Block block) {
		blockqueue.put(block, generateTimeDelay(false));
	}

	public boolean checkBlock(Block block) {
		return block.getType().equals(Material.GLOWSTONE);
	}

	public ArrayList<Block> getGrowCandidates() {
		ArrayList<Block> candidates = new ArrayList<Block>();
		ArrayList<Block> remove = new ArrayList<Block>();
		long currentTime = System.currentTimeMillis();

		for (Entry<Block, Long> entry : blockqueue.entrySet()) {
			if (!checkBlock(entry.getKey())) {
				remove.add(entry.getKey());
				continue;
			}

			if (currentTime >= entry.getValue()) {
				candidates.add(entry.getKey());
			}
		}

		removeAll(remove);
		return candidates;
	}
}