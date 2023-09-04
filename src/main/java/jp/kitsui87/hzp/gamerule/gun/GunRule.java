package jp.kitsui87.hzp.gamerule.gun;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.NBTTagCompound;

public class GunRule {
	
	private static GunRule rule;
	private final Map<UUID, GunBase> guns = new HashMap<UUID, GunBase>();
	
	private GunRule() {
		
	}
	
	public static void setupGunRule() {
		if (rule == null) rule = new GunRule();
	}
	
	public static GunRule getGunRule() {
		return rule;
	}
	
	public static void disableGunRule() {
		
	}
	
	/**
	 * Registers a new Gun 
	 * @param gun
	 */
	public void registerGun(Player owner, int slot, GunType gunType) {
		
		GunBase gun = GunBase.createGun(owner, slot, gunType);
		gun.updateGun();
		
		String uuid = CraftItemStack.asNMSCopy(owner.getInventory().getItem(slot)).getTag().getString("gunID");
		this.guns.put(UUID.fromString(uuid), gun);
		
	}
	
	public void removeRegisteredGun(GunBase gun) {
		
	}
	
	
	public GunBase getGunObj(ItemStack itemGun) {
		NBTTagCompound nbt = CraftItemStack.asNMSCopy(itemGun).getTag();
		if (nbt == null || !nbt.hasKey("gunType")) 
			return null;
		return this.guns.get(UUID.fromString(nbt.getString("gunID")));
	}
	
}
