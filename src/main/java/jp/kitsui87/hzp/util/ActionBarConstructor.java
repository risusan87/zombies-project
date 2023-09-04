package jp.kitsui87.hzp.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import jp.kitsui87.hzp.HypixelZombiesProject;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;

public class ActionBarConstructor {
	
	private int counterTaskID = -1;
	private boolean visible = false;
	private ChatJsonBuilder json;
	private final List<Player> viewers;
	
	private ActionBarConstructor(ChatJsonBuilder message) {
		
		this.viewers = new ArrayList<Player>();
		this.json = message;
		
	}
	
	public void editMessage (ChatJsonBuilder msg) {
		this.json = msg;
	}
	
	public void setTextVisible(boolean visible) {
		
		if (this.visible == visible) {
			return;
		}
		this.visible = visible;
		
		if (!visible) {
			if (this.counterTaskID != -1) {
				HypixelZombiesProject.getSchedular().cancelTask(this.counterTaskID);
				this.counterTaskID = -1;
			}
			IChatBaseComponent emptyChat = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"\"}"));
			PacketPlayOutChat empty = new PacketPlayOutChat(emptyChat, ChatMessageType.a((byte) 2));
			this.viewers.forEach( player -> {
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(empty);
			});
		} else {
			HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
			BukkitScheduler schedular = plugin.getServer().getScheduler();
			this.counterTaskID = schedular.scheduleSyncRepeatingTask(plugin, 
					() -> {
						IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a(json.toString());
						PacketPlayOutChat text = new PacketPlayOutChat(cbc, ChatMessageType.a((byte) 2));
						ActionBarConstructor.this.viewers.forEach( player -> {
							((CraftPlayer)player).getHandle().playerConnection.sendPacket(text);
						});
					}, 0, 1);
		}
		
		
	}
	
	public void addViewers(Player... viewers) {
		for (Player p : viewers) {
			if (!this.viewers.contains(p))
				this.viewers.add(p);
		}
	}
	
	public void removeViewers(Player... viewers) {
		for (Player p : viewers) {
			if (this.viewers.contains(p))
				this.viewers.remove(p);
		}
	}
	
	public static ActionBarConstructor constractActionBarText(ChatJsonBuilder json) {
		return new ActionBarConstructor(json);
	}
	
	public static void sendTitle(Player player, ChatJsonBuilder msgTitle, ChatJsonBuilder msgSubTitle, float durationSec) {
		
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a(msgTitle.toString());
		IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a(msgSubTitle.toString());
		PacketPlayOutTitle p = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle p2 = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p2);
		ActionBarConstructor.sendTime(player, (int)Math.floor(durationSec * 20f));
		
	}

	private static void sendTime(Player player, int ticks) {
		PacketPlayOutTitle p = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, 20, ticks, 20);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
	}

	public static void sendActionBar(Player player, ChatJsonBuilder message, float durationSec) {
		
		IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"" + message + "\"}"));
		IChatBaseComponent emptyChat = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"\"}"));
		
		PacketPlayOutChat text = new PacketPlayOutChat(cbc, ChatMessageType.a((byte) 2));
		PacketPlayOutChat empty = new PacketPlayOutChat(emptyChat, ChatMessageType.a((byte) 2));
		
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		final ActionBarConstructor cc = new ActionBarConstructor(message);
		final int goalTick = (int) Math.floor(durationSec * 20f);
		Runnable r = new Runnable() {
			
			int counter = 0;
			
			@Override
			public void run() {
				
				if (counter >= goalTick && cc.counterTaskID != -1) {
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(empty);
					schedular.cancelTask(cc.counterTaskID);
					return;
				}
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(text);
				counter++;
				
			}
			
		};
		cc.counterTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);
		
	}
}
