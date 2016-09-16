package com.kirelcodes.milkman.listener;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.kirelcodes.milkman.MilkMan;
import com.kirelcodes.miniaturepets.api.events.MobDeathEvent;

public class MilkManListener implements Listener{
	public MilkManListener(Plugin plug){
		Bukkit.getPluginManager().registerEvents(this, plug);
	}
	
	private boolean spawnMilkMan(){
		Random rand = new Random();
		int randN = rand.nextInt(10);
		return randN == 9;
	}
	
	protected Location getRandomNear(Location loc){
		Location newLoc = loc.clone();
		Random rand = new Random();
		int x = rand.nextInt(6);
		int z = rand.nextInt(6);
		int y = loc.getWorld().getHighestBlockYAt(x, z);
		newLoc.setX(x + loc.getBlockX());
		newLoc.setY(y+2);
		newLoc.setZ(z + loc.getBlockZ());
		return newLoc;
	}
	
	@EventHandler
	public void cowSpawn(EntitySpawnEvent e){
		if(!(e.getEntity() instanceof Cow))
			return;
		if(spawnMilkMan())
			MilkMan.getMilkMan().spawnMob(e.getEntity().getLocation());
		
	}
	
	@EventHandler
	public void chunkLoad(ChunkLoadEvent e){
		for(Entity en : e.getChunk().getEntities()){
			if(!(en instanceof Cow))
				continue;
			if(spawnMilkMan())
				MilkMan.getMilkMan().spawnMob(en.getLocation());
		}
	}
	@EventHandler
	public void mobDeathEvent(MobDeathEvent e){
		if(!"MilkMan".equalsIgnoreCase(e.getMob().getName()))
			return;
		e.getDrops().clear();
		for(ItemStack item : e.getMob().getInventory().getContents())
			e.getDrops().add(item);
	}
	
}
