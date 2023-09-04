package jp.kitsui87.hzp.gamerule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.util.Vector;

import jp.kitsui87.hzp.entity.Corpse;
import jp.kitsui87.hzp.entity.Zombie;
import jp.kitsui87.hzp.gamerule.GameStateRule.GameProfile;
import jp.kitsui87.hzp.gamerule.GameStateRule.PlayerState;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityZombie;
import net.minecraft.server.v1_12_R1.WorldServer;


public class EventListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		
		if (
			event.getAction() == Action.RIGHT_CLICK_AIR && 
			event.getHand() == EquipmentSlot.HAND &&
			event.getItem().getType() == Material.STICK
		) {
			event.getPlayer().sendMessage("Interact");
			WorldServer world = ((CraftWorld)event.getPlayer().getWorld()).getHandle();
			EntityZombie zombie = new Zombie(world);
			Location pl = event.getPlayer().getLocation();
			zombie.setLocation(pl.getX(), pl.getY(), pl.getZ(), pl.getYaw(), pl.getPitch());
			zombie.setSize(2f, 2f);
			LivingEntity bZombie = (LivingEntity)zombie.getBukkitEntity();
			bZombie.setMaximumNoDamageTicks(0);
			world.addEntity(((CraftEntity)bZombie).getHandle());
			
			AttributeInstance attribute = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		    if (attribute == null)
		      return; 
		    attribute.setBaseValue(24f);
		    p.saveData();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		GameProfile profile = GameRunningRule.getZombies().getInGamePlayers().get(event.getPlayer().getUniqueId());
		if (profile == null) {
			GameRunningRule.getZombies().addPlayerIfNew(event.getPlayer());
			return;
		}
		profile.isQuit = false;
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		
		GameRunningRule.getZombies().getInGamePlayers().get(event.getPlayer().getUniqueId()).isQuit = true;
	}
	
	@EventHandler
	public void onPlayerOpenInventory(InventoryClickEvent event) {
		
		InventoryType p = event.getClickedInventory().getType();
		System.out.println("invoke");
	}
	
	@EventHandler
	public void onPlayerDie(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player p = (Player) event.getEntity();
		if (event.getFinalDamage() >= p.getHealth()) {
			event.setCancelled(true);
			p.setHealth(20);
			new PlayerDeathRule().knockdownPlayer(p.getUniqueId());
			return;
		}
		if (GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).playerState == PlayerState.IN_GAME_DOWN) {
			event.setCancelled(true);
			return;
		}
	}
	
}
