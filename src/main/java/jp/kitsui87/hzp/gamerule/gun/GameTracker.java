package jp.kitsui87.hzp.gamerule.gun;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import jp.kitsui87.hzp.HypixelZombiesProject;
import jp.kitsui87.hzp.gamerule.PermissionRule;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedSoundEffect;

/**
 * Event registered while the game is active
 * @author ypmxx
 *
 */
public class GameTracker implements Listener {
	
	@EventHandler
	public void onPlayerShootGun(PlayerInteractEvent event) {
		
		if (event.getHand() == EquipmentSlot.HAND) {
			
			Player player = event.getPlayer();
			GunBase heldGunType = getGunHeld(player, player.getInventory().getHeldItemSlot());
			if (heldGunType == null || heldGunType.isOutAmmo)
				return;
			//int gunSlotID = player.getInventory().getHeldItemSlot();
			
			
			if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				heldGunType.shoot();
			} 
			else if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
				heldGunType.reload();
			}
		}
		
	}
	
	@EventHandler
	public void onSwitchItem(PlayerItemHeldEvent event) {
		
		Player p = event.getPlayer();
		
		int newSlot = event.getNewSlot();
		int oldSlot = event.getPreviousSlot();
		
		ItemStack newItem = p.getInventory().getItem(event.getNewSlot());
		ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
		
		GunBase newGun = GunRule.getGunRule().getGunObj(newItem);
		GunBase oldGun = GunRule.getGunRule().getGunObj(oldItem);
		
		if (oldGun != null) oldGun.onGunNotHeld(oldSlot);
		if (newGun != null) newGun.onGunHeld(newSlot);
		
	}
	
	@EventHandler
	public void onItemBreak(PlayerItemBreakEvent event) {
		
		System.out.println("called");
		ItemStack broken = event.getBrokenItem();
		GunBase gun = GunRule.getGunRule().getGunObj(broken);
		if (gun != null) {
			broken.setDurability((short)1);
			gun.holder.getInventory().setItem(gun.gunSlot, broken);
		}
		
	}
	
	@EventHandler
	public void onItemTakeDamage(PlayerItemDamageEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onItemTakeDamage(PlayerInteractEntityEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onZombieMeleeAttacked(EntityDamageByEntityEvent event) {
		
		if (event.getEntityType() != EntityType.ZOMBIE) return;
		Vector aD = event.getDamager().getLocation().getDirection().multiply(0.4f);
		Vector victimVelocity = event.getEntity().getVelocity();
		event.getEntity().setVelocity(
			victimVelocity.add(new Vector(aD.getX(), 0.4f, aD.getZ())));
		
	}
	
	@EventHandler
	public void onPlayerHunger(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	public static GunBase getGunHeld(Player player, int slotID) {
		
		ItemStack bukkitItem = player.getInventory().getItem(slotID);
		if (bukkitItem == null) 
			return null;
		NBTTagCompound tag = CraftItemStack.asNMSCopy(bukkitItem).getTag();
		if (tag == null || !tag.hasKey("gunType")) 
			return null;
		return GunRule.getGunRule().getGunObj(bukkitItem);
		
	}
	
}
