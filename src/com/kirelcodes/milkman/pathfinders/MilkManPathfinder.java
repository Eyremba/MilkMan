package com.kirelcodes.milkman.pathfinders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.kirelcodes.milkman.MilkMan;
import com.kirelcodes.milkman.utils.NMSClassInteracter;
import com.kirelcodes.miniaturepets.api.pathfinding.Pathfinder;
import com.kirelcodes.miniaturepets.api.pets.APIMob;

public class MilkManPathfinder extends Pathfinder {
	private APIMob mob = null;
	private Map<Cow, Integer> cooldownMap;
	private int timeOnTarget, timeOnLocationTarget;
	private Item targetItem;
	private Cow targetCow;

	private enum TargetType {
		COW, INGOT, UNKNOWN;
	}

	public MilkManPathfinder(APIMob mob) {
		if (!"MilkMan".equalsIgnoreCase(mob.getName())) {
			MilkMan.getInstance()
					.getLogger()
					.warning(
							"Someone is trying to use a milk man pathfinder on a non milkman mob");
			return;
		}
		this.mob = mob;
	}

	@Override
	public void onStart() {
		this.timeOnTarget = -1;
		this.timeOnLocationTarget = -1;
		this.targetItem = null;
		this.targetCow = null;
		this.cooldownMap = new HashMap<>();
	}

	@Override
	public boolean shouldStart() {
		return this.mob != null;
	}

	@Override
	public void afterTask() {
		List<Cow> removeCows = new ArrayList<>();
		for (Entry<Cow, Integer> entry : cooldownMap.entrySet()) {
			cooldownMap.replace(entry.getKey(), entry.getValue() - 1);
			if (cooldownMap.get(entry.getKey()) < 0)
				removeCows.add(entry.getKey());
		}
		for(Cow cow : removeCows)
			cooldownMap.remove(cow);
		if (hasTarget()) {
			timeOnTarget++;
			if (timeOnTarget >= 20 * 5) {
				timeOnTarget = -1;
				targetCow = null;
				targetItem = null;
				stopPathFinding();
			}
		}
		if (hasTargetLocation()) {
			timeOnLocationTarget++;
			if (timeOnLocationTarget >= 20 * 5) {
				timeOnLocationTarget = -1;
				targetCow = null;
				targetItem = null;
				stopPathFinding();
			}
		}
	}

	@Override
	public void updateTask() {
		if (!hasTarget()) {
			try {
				mob.stopPathfinding();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (scanForIngot())
				return;
			if (scanForCows())
				return;
		}
		switch (getTargetType()) {
		case INGOT:
			handleIngot();
			break;
		case COW:
			handleCow();
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}
	}

	private boolean hasTarget() {
		if (targetCow != null) {
			if (targetCow.isDead())
				return false;
			return true;
		}
		if (targetItem != null) {
			if (targetItem.isDead())
				return false;
			return true;
		}
		return false;
	}

	private boolean hasTargetLocation() {
		return mob.getTargetLocation() != null;
	}

	private boolean onTargetLocation() {
		return mob.onTargetLocation();
	}

	private void handleIngot() {
		if (!hasTargetLocation()) {
			try {
				if (!mob.setTargetLocation(targetItem.getLocation()))
					this.targetItem = null;
			} catch (Exception e) {
				e.printStackTrace();
				this.targetItem = null;
			}
			return;
		}
		if (onTargetLocation()) {
			for (ItemStack item : mob.getInventory().getContents()) {
				if (item == null)
					continue;
				if (item.getType() != Material.MILK_BUCKET)
					continue;
				mob.getLocation()
						.getWorld()
						.dropItemNaturally(
								mob.getLocation().add(
										mob.getLocation().getDirection()
												.multiply(1.2)), item);
				mob.getInventory().remove(item);
			}
			mob.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET));
			targetItem.remove();
			this.targetItem = null;
			return;
		}
	}

	private void handleCow() {
		if (!hasTargetLocation()) {
			try {
				if (!mob.setTargetLocation(targetCow.getLocation()))
					this.targetCow = null;
			} catch (Exception e) {
				this.targetCow = null;
				e.printStackTrace();
			}
			return;
		}
		if (onTargetLocation()) {
			mob.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));
			startCooldown(targetCow);
			this.targetCow = null;
		}
	}

	private void startCooldown(Cow cow) {
		cooldownMap.put(cow, 75 * 2);
	}

	private boolean onCooldown(Cow cow) {
		return cooldownMap.containsKey(cow);
	}

	private boolean scanForIngot() {
		for (Entity e : mob.getNavigator().getNearbyEntities(10, 10, 10)) {
			if (!(e instanceof Item))
				continue;
			if (!e.isOnGround())
				continue;
			Item item = (Item) e;
			if (item.getItemStack().getType() == Material.GOLD_INGOT) {
				this.targetItem = item;
				return true;
			}
		}
		return false;
	}

	private boolean scanForCows() {
		for (Entity e : mob.getNavigator().getNearbyEntities(10, 10, 10)) {
			if (!(e instanceof Cow))
				continue;
			if (!e.isOnGround())
				continue;
			if (onCooldown((Cow) e))
				continue;
			this.targetCow = (Cow) e;
			return true;
		}
		return false;
	}

	private TargetType getTargetType() {
		if (!hasTarget())
			return TargetType.UNKNOWN;
		if (targetItem != null)
			return TargetType.INGOT;
		if (targetCow != null)
			return TargetType.COW;
		return TargetType.UNKNOWN;
	}

	private void stopPathFinding() {
		try {
			mob.stopPathfinding();
			NMSClassInteracter.setDeclaredField(mob, "targetLocation", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
