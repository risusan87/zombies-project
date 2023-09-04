package jp.kitsui87.hzp.gamerule;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import jp.kitsui87.hzp.HypixelZombiesProject;

public class PermissionRule {
	
	private final JavaPlugin plugin;
	private static PermissionRule rule = null;
	
	private final HashMap<UUID, PermissionAttachment> perms = new HashMap<UUID, PermissionAttachment>();
	
	public static final String HZP_PERM_ALL = "hzp.perm.all";
	public static final String HZP_STATE_INGAME = "hzp.state.ingame.active";
	public static final String HZP_STATE_INGAME_SPECTATE = "hzp.state.ingame.spectate";
	
	public static final String HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND = "hzp.flag.shouldnothearxpsound";
	
	private PermissionRule() {
		this.plugin = HypixelZombiesProject.getPlugin();
	}
	
	public static void setupPermissionRule() {
		if (rule == null) rule = new PermissionRule();
	}
	
	public static void disablePermissionRule() {
		
	}
	
	public static PermissionRule getPermissionRule() {
		return rule;
	}

	public boolean hasPermission(Player player, String permission) {
		
		UUID playerID = player.getUniqueId();
		PermissionAttachment pa = !this.perms.containsKey(playerID) ? player.addAttachment(this.plugin) : this.perms.get(playerID);
		if (!pa.getPermissions().containsKey(permission)) return false;
		return pa.getPermissions().get(permission);
		
	}
	
	public void addPermissionAll(Player player) {
		PermissionAttachment pa = player.addAttachment(this.plugin);
		pa.setPermission(HZP_PERM_ALL, true);
	}
	
	public void addPermission(Player player, String perm) {
		
		UUID playerID = player.getUniqueId();
		PermissionAttachment pa = !this.perms.containsKey(playerID) ? player.addAttachment(this.plugin) : this.perms.get(playerID);
		pa.setPermission(perm, true);
		this.perms.put(playerID, pa);
		
	}
	
	public void removePermission(Player player, String perm) {
		
		UUID playerID = player.getUniqueId();
		PermissionAttachment pa = !this.perms.containsKey(playerID) ? player.addAttachment(this.plugin) : this.perms.get(playerID);
		pa.unsetPermission(perm);
		this.perms.put(playerID, pa);
		
	}
}
