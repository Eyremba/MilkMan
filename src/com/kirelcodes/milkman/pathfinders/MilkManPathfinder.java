package com.kirelcodes.milkman.pathfinders;

import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.kirelcodes.milkman.MilkMan;
import com.kirelcodes.miniaturepets.api.pathfinding.Pathfinder;
import com.kirelcodes.miniaturepets.api.pets.APIMob;

public class MilkManPathfinder extends Pathfinder {
	private APIMob mob = null;
	private int cooldown;
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
		this.cooldown = -1;
		this.targetItem = null;
		this.targetCow = null;
	}

	@Override
	public boolean shouldStart() {
		return this.mob != null;
	}

	@Override
	public void afterTask() {
		if(onCooldown())
			cooldown--;
	}
	
	@Override
	public void updateTask() {
		if (!hasTarget()) {
			try{
				mob.stopPathfinding();
			}catch(Exception e){
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
			if (!onCooldown())
				handleCow();
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}
	}

	private boolean hasTarget() {
		return targetCow != null || targetItem != null;
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
			}
			return;
		}
		if (onTargetLocation()) {
			for (ItemStack item : mob.getInventory().getContents()) {
				mob.getLocation()
						.getWorld()
						.dropItemNaturally(
								mob.getLocation().add(
										mob.getLocation().getDirection()
												.multiply(1.2)), item);
				mob.getInventory().remove(item);
			}
			mob.getInventory().addItem(this.targetItem.getItemStack());
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
				e.printStackTrace();
			}
			return;
		}
		if (onTargetLocation()) {
			for (ItemStack item : mob.getInventory()
					.addItem(new ItemStack(Material.MILK_BUCKET)).values())
				mob.getLocation().getWorld()
						.dropItemNaturally(this.targetCow.getLocation(), item);
			this.targetCow = null;
			startCooldown();
		}
	}

	private void startCooldown() {
		this.cooldown = 5 * 20;
	}

	private boolean onCooldown() {
		return this.cooldown != -1;
	}

	private boolean scanForIngot() {
		for (Entity e : mob.getNavigator().getNearbyEntities(10, 10, 10)) {
			if (!(e instanceof Item))
				continue;
			Item item = (Item) e;
			if (item.getItemStack().getType() == Material.GOLD_NUGGET) {
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

}
