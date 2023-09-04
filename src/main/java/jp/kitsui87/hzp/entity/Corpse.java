package jp.kitsui87.hzp.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.plugin.npc.NPC;

import jp.kitsui87.hzp.HypixelZombiesProject;
import jp.kitsui87.hzp.gamerule.GameRunningRule;
import jp.kitsui87.hzp.gamerule.ScoreboardRule;
import jp.kitsui87.hzp.util.DummyNetworkManager;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffectList;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;

public class Corpse extends NPC {
	
	public final Player owner;
	public static Corpse corpse;
	private int corpseTrackingID = -1;
	
	private static List<Corpse> corpses = new ArrayList<Corpse>();
	
	@SuppressWarnings("rawtypes")
	public Corpse(Player owner) {
		super("", owner.getLocation(), HypixelZombiesProject.getPlugin());
		
		this.owner = owner;
		corpses.add(this);
		String rawJson = "";
		try {
			String str_url = "https://sessionserver.mojang.com/session/minecraft/profile/" + owner.getUniqueId().toString() + "?unsigned=false";
			URL url = new URL(str_url);
			URLConnection connection = url.openConnection();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        while ((line = br.readLine()) != null) 
	            rawJson += line;
	        br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        if (!rawJson.equals("")) {
        	Gson gson = new Gson();
            Map jsonMap = gson.fromJson(rawJson, Map.class);
			ArrayList list = (ArrayList) jsonMap.get("properties");
			LinkedTreeMap map = (LinkedTreeMap)list.get(0);
            String texture = (String) map.get("value");
    		String signature = (String) map.get("signature");
            this.setSkin(texture, signature);
        }
		
        this.setRecipientType(Recipient.ALL);
        
		this.spawn(false, true);
		Location finalLoc = this.getLocation().clone().toVector().toLocation(owner.getWorld());
		finalLoc.setYaw(200f);
		this.teleport(finalLoc, true);
		while (
				this.getLocation().getBlock().getType() == Material.AIR ||
				this.getLocation().getBlock().getType() == Material.CARPET
			) {
			this.teleport(this.getLocation().add(new Vector(0, -0.1f, 0)), true);
		}
		
		this.teleport(this.getLocation().add(new Vector(0, 0.2f, 0)), true);
		this.setSleep(true);
		
	}
	
	public Entity getEntity() {
		for (Entity e : GameRunningRule.getZombies().getWorld().getEntities()) {
			if (e.getEntityId() == this.getEntityId()) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * toggles to be seen as dead player
	 * @param flag
	 */
	public void setInvisible(boolean flag) {
		
		this.setEffect(new MobEffect(MobEffectList.fromId(14), 1, 1, false, false));
		
	}
	
	public static void showCorpse(Corpse c) {
		
		GameRunningRule.getZombies().getInGamePlayers().forEach((uuid, profile) -> {
			Player p = Bukkit.getPlayer(uuid);
			c.addRecipient(p);
			
			c.teleport(c.owner.getLocation().toVector().toLocation(c.owner.getWorld()), true);
			//c.setSleep(true);
			c.removeRecipient(p);
		});
		HypixelZombiesProject.getSchedular().cancelTask(c.corpseTrackingID);
		c.teleport(new Vector(0, 0, 0).toLocation(GameRunningRule.getZombies().getWorld()), false);
	}
	
	public static void hideCorpse(Corpse c) {
		GameRunningRule.getZombies().getInGamePlayers().forEach((uuid, profile) -> {
			Player p = Bukkit.getPlayer(uuid);
			c.addRecipient(p);
			c.teleport(c.getLocation(), true);
			//c.setSleep(true);
			c.removeRecipient(p);
		});
		c.corpseTrackingID = HypixelZombiesProject.getSchedular().scheduleSyncRepeatingTask(
				HypixelZombiesProject.getPlugin(), () -> {
					c.teleport(c.owner.getLocation().toVector().toLocation(c.owner.getWorld()), true);
				}, 0, 5);	
	}
	
	public void removeCorpse() {
		this.destroy();
	}
	
	public void update() {
		this.reloadNpc();
	}
	
	public static void killCorpses() {
		corpses.forEach(c -> {
			c.destroy();
		});
	}
	
	public void kill() {
		for (Corpse c : corpses) {
			if (c.equals(this)) {
				c.destroy();
				corpses.remove(c);
				return;
			}
		}
	}
}
