package com.kirelcodes.milkman;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import com.kirelcodes.milkman.listener.MilkManListener;
import com.kirelcodes.milkman.pathfinders.MilkManPathfinder;
import com.kirelcodes.miniaturepets.api.APIUtils;
import com.kirelcodes.miniaturepets.api.pets.APIMob;
import com.kirelcodes.miniaturepets.api.pets.APIMobContainer;
import com.kirelcodes.miniaturepets.api.pets.MobSpawnAction;

public class MilkMan extends JavaPlugin {
	private static APIMobContainer milkMan = null;
	private static MilkMan instance = null;

	@Override
	public void onEnable() {
		instance = this;
		if (!intMilkMan()) {
			getLogger().warning(
					"Couldnt intsilaize the milk man disabling the plugin");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		new MilkManListener(this);
	}

	private static boolean intMilkMan() {
		File model = null;
		try {
			model = APIUtils.loadModelByName("Milker", getInstance());
		} catch (IOException e) {
			return false;
		}
		milkMan = new APIMobContainer(model, "MilkMan", 20, 0.17D,
				EntityType.CHICKEN, "Chicken");
		milkMan.addSpawnAction(new MobSpawnAction() {

			@Override
			public void spawnMob(APIMob mob, Location loc) {
				try {
					mob.clearAI();
				} catch (Exception e) {
					getInstance()
							.getLogger()
							.warning(
									"Couldnt intsilaize the milk man disabling the plugin");
					Bukkit.getPluginManager().disablePlugin(getInstance());
					return;
				}
				mob.getPathManager().addPathfinder(new MilkManPathfinder(mob));
			}
		});
		return true;
	}

	public static MilkMan getInstance() {
		return instance;
	}

	public static APIMobContainer getMilkMan() {
		return milkMan;
	}

}
