package jp.kitsui87.hzp.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.plugin.npc.NPC;

import jp.kitsui87.hzp.HypixelZombiesProject;
import jp.kitsui87.hzp.entity.Corpse;
import jp.kitsui87.hzp.gamerule.PermissionRule;
import jp.kitsui87.hzp.gamerule.ScoreboardRule;
import jp.kitsui87.hzp.gamerule.gun.GunBase;
import jp.kitsui87.hzp.gamerule.gun.GunPistol;
import jp.kitsui87.hzp.gamerule.gun.GunRule;
import jp.kitsui87.hzp.gamerule.gun.GunType;
import jp.kitsui87.hzp.gamerule.GameRunningRule;
import jp.kitsui87.hzp.gamerule.VisibleBoard;
import jp.kitsui87.hzp.gamerule.VisibleBoard.BoardType;
import jp.kitsui87.hzp.util.ActionBarConstructor;
import jp.kitsui87.hzp.util.ChatJsonBuilder;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;

public class CommandHZP implements CommandExecutor, TabCompleter {
	
 	private static List<CommandBase> commands = new ArrayList<>();
	static {
		commands.add(new CommandJoin());
		commands.add(new CommandSaveLocation());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			HypixelZombiesProject.getPlugin().logInfo("Command hzp is only compatible from player use.");
			HypixelZombiesProject.getPlugin().logInfo("Note that only OP and permitted players can use hzp command.");
			return false;
		}

		Player pSender = (Player) sender;
		
		if (!pSender.isOp() && !PermissionRule.getPermissionRule().hasPermission(pSender, PermissionRule.HZP_PERM_ALL)) {
			ChatJsonBuilder jb = new ChatJsonBuilder("You do not have permission to use this command.").withColor(ChatColor.RED);
			jb.sendJson(pSender);
			return false;
		}
		
		if (args.length == 0) {
			ChatJsonBuilder jb = new ChatJsonBuilder().withText("Use \"/hzp help\" to list commands.").withColor(ChatColor.RED);
			System.out.println(jb);
			jb.sendJson(pSender);
			return true;
		}
		
		for (CommandBase c : commands.toArray(new CommandBase[commands.size()])) {
			if (args[0].equals(c.getCommand())) {
				return c.onCommand(pSender, args);
			}
		}
		
		if (args[0].equals("pistol")) {
			GunRule.getGunRule().registerGun(pSender, pSender.getInventory().getHeldItemSlot(), GunType.PISTOL);
			return true;
		}
		
		if (args[0].equals("show")) {
			Corpse c = GameRunningRule.getZombies().getInGamePlayers().get(pSender.getUniqueId()).playerCorpse;
	        Corpse.showCorpse(c);
			return true;
		}
		
		if (args[0].equals("hide")) {
			Corpse c = GameRunningRule.getZombies().getInGamePlayers().get(pSender.getUniqueId()).playerCorpse;
	        c.setInvisible(true);
			return true;
		}
		
		
		if (args[0].equals("ult")) {
			GunBase gun = GunRule.getGunRule().getGunObj(pSender.getInventory().getItemInMainHand());
			if (gun != null && gun.getUltLv() + 1 <= gun.maxUlt - 1)
				gun.setUltLv(gun.getUltLv() + 1);
			return true;
		}
		if (args[0].equals("deUlt")) {
			GunBase gun = GunRule.getGunRule().getGunObj(pSender.getInventory().getItemInMainHand());
			if (gun != null && gun.getUltLv() - 1 >= 0)
				gun.setUltLv(gun.getUltLv() - 1);
			return true;
		}
		if (args[0].equals("maxAmmo")) {
			GunBase gun = GunRule.getGunRule().getGunObj(pSender.getInventory().getItemInMainHand());
			if (gun != null)
				gun.refill();
			return true;
		}
		if (args[0].equals("startTimer")) {
			VisibleBoard.getBoard().ingameBoardTimerStart();
			return true;
		}
		if (args[0].equals("stopTimer")) {
			VisibleBoard.getBoard().ingameBoardTimerStop();
		}
		if (args[0].equals("resetTimer")) {
			VisibleBoard.getBoard().ingameBoardTimerReset();
		}
		
		ChatJsonBuilder jb = new ChatJsonBuilder("Unknown command. Use \"/hzp help\" to list commands.");
		jb.withColor(ChatColor.RED);
		jb.sendJson(pSender);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		if (!command.getName().equals("hzp")) return null;
		
		List<String> l = new ArrayList<String>();
		
		if (args.length == 1) {
			l.add("perm");
			l.add("give");
			l.add("help");
		}
		
		if (args.length == 2 && args[0].equals("perm")) {
			return null;
		}
		
		return l;
	}

}
