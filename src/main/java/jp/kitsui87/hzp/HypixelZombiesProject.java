package jp.kitsui87.hzp;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;

import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import jp.kitsui87.hzp.command.CommandHZP;
import jp.kitsui87.hzp.entity.Corpse;
import jp.kitsui87.hzp.entity.Zombie;
import jp.kitsui87.hzp.gamerule.EventListener;
import jp.kitsui87.hzp.gamerule.GameRunningRule;
import jp.kitsui87.hzp.gamerule.GameStateRule;
import jp.kitsui87.hzp.gamerule.GameStateRule.GameState;
import jp.kitsui87.hzp.gamerule.PacketRule;
import jp.kitsui87.hzp.gamerule.PermissionRule;
import jp.kitsui87.hzp.gamerule.ScoreboardRule;
import jp.kitsui87.hzp.gamerule.VisibleBoard;
import jp.kitsui87.hzp.gamerule.gun.GameTracker;
import jp.kitsui87.hzp.gamerule.gun.GunRule;

/**
 * Spigot APIによるゾンビ再現プロジェクト。
 * 
 * 
 * ver note: Major.Minor.Build
 * @author kitsui87
 */
public class HypixelZombiesProject extends JavaPlugin {
	
	private ProtocolManager protocolManager;
	private static HypixelZombiesProject plugin = null;
	
	/**
	 * Called once when the plugin is enabled.
	 */
	@Override
	public void onEnable() {
		
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		
		Listener listener = new EventListener();
		Listener gunlistener = new GameTracker();
		this.getServer().getPluginManager().registerEvents(listener, this);
		this.getServer().getPluginManager().registerEvents(gunlistener, this);

		Thread daemonJob = new Thread(() -> {
			Server s = null;
			do {
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				s = HypixelZombiesProject.getPlugin().getServer();
				
			} while (s == null);
			s.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				GameRunningRule.getZombies();
				PermissionRule.setupPermissionRule();
				GunRule.setupGunRule();
				ScoreboardRule.getScoreboardRule();
				VisibleBoard.setupBoard();
				PacketRule.getPacketRule();
				GameStateRule.getGameStateRule();
				
				GameStateRule.getGameStateRule().switchGameState(GameState.PLAY);
			});
		});
		daemonJob.start();
		
		int id = 54;
		MinecraftKey key = new MinecraftKey("zombiebasic");
		EntityTypes.b.a(id, key, Zombie.class);
		EntityTypes.d.add(key);
		try {
			Field gField = EntityTypes.class.getDeclaredField("g");
			gField.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<String> g = (List<String>)gField.get(null);
			while(g.size() <= id) g.add(null);
			g.set(id, "zombiebasic");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		CommandHZP hzp = new CommandHZP();
		this.getCommand("hzp").setExecutor(hzp);
		this.getCommand("hzp").setTabCompleter(hzp);
		
		// configs
		File dataFolder = this.getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdirs();
	}
	
	/**
	 * Called once when the plugin is disabled.
	 */
	@Override
	public void onDisable() {
		VisibleBoard.disableVisibleBoard();
		Corpse.killCorpses();
	}
	
	public static HypixelZombiesProject getPlugin() {
		return plugin;
	}
	
	public void logInfo(String msg) {
		getPlugin().getLogger().log(Level.INFO, msg);
	}
	
	public ProtocolManager getProtocolManager() {
		return this.protocolManager;
	}
	
	public static BukkitScheduler getSchedular() {
		return plugin.getServer().getScheduler();
	}
	                                             

}
