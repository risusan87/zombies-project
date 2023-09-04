package jp.kitsui87.hzp.gamerule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import jp.kitsui87.hzp.HypixelZombiesProject;
import jp.kitsui87.hzp.gamerule.GameStateRule.GameProfile;
import jp.kitsui87.hzp.gamerule.GameStateRule.GameState;

public class GameRunningRule {
	
	private static GameRunningRule rule = null;
	
	private final Map<UUID, GameProfile> inServerPlayers;
	private World mainWorld;
	
	private GameRunningRule() {
		
		this.inServerPlayers = new HashMap<UUID, GameProfile>();
		HypixelZombiesProject.getPlugin().getServer().getOnlinePlayers().forEach(p -> {
			this.inServerPlayers.put(p.getUniqueId(), new GameProfile(p));
			if (mainWorld == null) mainWorld = p.getWorld();
		});
		
	}
	
	public Map<UUID, GameProfile> getInGamePlayers() {
		return new HashMap<UUID, GameProfile>(inServerPlayers);
	}
	
	public void setMainWorld(World world) {
		this.mainWorld = world;
	}
	
	public World getWorld() {
		return this.mainWorld;
	}
	
	public static GameRunningRule getZombies() {
		return rule == null ? (rule = new GameRunningRule()) : rule;
	}
	
	public void addPlayerIfNew(Player player) {
		if (!this.inServerPlayers.containsKey(player.getUniqueId()))
			this.inServerPlayers.put(player.getUniqueId(), new GameProfile(player));
	}
	
	public void removePlayer(Player player) {
		if (this.inServerPlayers.containsKey(player.getUniqueId()))
			this.inServerPlayers.remove(player.getUniqueId());
	}
}
