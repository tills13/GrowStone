package com.jseb.growstone;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Random;

public class GrowerTask extends BukkitRunnable  {
	private final JavaPlugin plugin;
	private Random rand; 

	public GrowerTask(JavaPlugin plugin) {
		this.plugin = plugin;
		rand = new Random();
	}

	public void run() {
		ArrayList<Block> grow = new ArrayList<Block>();
		ArrayList<Block> remove = new ArrayList<Block>();

		for (Block block : GrowStone.growQueue.getGrowCandidates()) {
			if (block.getType().equals(Material.GLOWSTONE)) {
				if (getClusterSize(block) < GrowStone.config.maxClusterSize) {
					if (rand.nextDouble() < GrowStone.config.growChance) {
						if (!GrowStone.isValid(block)) {
							remove.add(block);
						} else {
							grow.add(block);
						}
					}
				}	
			} else {
				remove.add(block);
			}
		}

		GrowStone.growQueue.removeAll(remove);
		GrowStone.growQueue.growAll(grow);

		if (GrowStone.config.debug) {
			System.out.println("Grew " + grow.size() + " blocks");
			System.out.println("Removed " + remove.size() + " blocks");
			System.out.println("Total blocks tracked: " + GrowQueue.size());
		}
	}

	public int getClusterSize(Block block) {
		int size = 1;
		while (block.getRelative(BlockFace.UP).getType().equals(Material.GLOWSTONE)) {
			block = block.getRelative(BlockFace.UP);
			size++;
		}
		return size;
	}
}