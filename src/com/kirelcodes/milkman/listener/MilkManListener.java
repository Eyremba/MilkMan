package com.kirelcodes.milkman.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class MilkManListener implements Listener{
	public MilkManListener(Plugin plug){
		Bukkit.getPluginManager().registerEvents(this, plug);
	}
}
