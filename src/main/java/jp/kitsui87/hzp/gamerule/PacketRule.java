package jp.kitsui87.hzp.gamerule;

import java.io.IOException;
import java.lang.reflect.Field;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;

import jp.kitsui87.hzp.HypixelZombiesProject;
import net.minecraft.server.v1_12_R1.PacketPlayInSteerVehicle;

public class PacketRule {
	
	private static PacketRule rule;
	
	private final PacketAdapter packetParticle;
	private final PacketAdapter packetSound;
	private final PacketAdapter vehicle;
	
	public PacketRule() {
		
		this.packetParticle = new PacketAdapter(
			    HypixelZombiesProject.getPlugin(),
			    ListenerPriority.NORMAL,
			    PacketType.Play.Server.WORLD_PARTICLES
			) {
			    @Override
			    public void onPacketSending(PacketEvent event) {
			    	boolean sweep = event.getPacket().getParticles().read(0).getName().equals("sweepAttack");
			        event.setCancelled(sweep);
			    }
		};
		
		this.packetSound = new PacketAdapter(
				HypixelZombiesProject.getPlugin(),
				ListenerPriority.NORMAL,
				PacketType.Play.Server.NAMED_SOUND_EFFECT
			) {
				@Override
				public void onPacketSending(PacketEvent event) {
					String soundName = event.getPacket().getSoundEffects().read(0).name();
					PermissionRule rule = PermissionRule.getPermissionRule();
					event.setCancelled(
							soundName.contains("ATTACK_SWEEP") || 
							soundName.contains("ATTACK_CRIT") ||
							soundName.contains("ATTACK_STRONG") ||
							soundName.contains("ATTACK_KNOCKBACK") ||
							(
								rule.hasPermission(event.getPlayer(), PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND)
								&& 
								soundName.contains("ENTITY_PLAYER_LEVELUP")
							)
							);
				}
		};

		
		this.vehicle = new PacketAdapter(
					HypixelZombiesProject.getPlugin(),
					ListenerPriority.NORMAL,
					PacketType.Play.Client.STEER_VEHICLE
				) {
					@Override
					public void onPacketReceiving(PacketEvent event) {
						
						try {
							Field d = PacketPlayInSteerVehicle.class.getDeclaredField("d");
				            d.setAccessible(true);
				            d.set((PacketPlayInSteerVehicle)event.getPacket().getHandle(), false);
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
			
		};
		
		HypixelZombiesProject.getPlugin().getProtocolManager().addPacketListener(this.packetParticle);
		HypixelZombiesProject.getPlugin().getProtocolManager().addPacketListener(this.packetSound);
		HypixelZombiesProject.getPlugin().getProtocolManager().addPacketListener(this.vehicle);
		
	}
	
	public static PacketRule getPacketRule() {
		return rule == null ? (rule = new PacketRule()) : rule;
	}
	
	public static void disablePacketRule() {
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		plugin.getProtocolManager().removePacketListeners(plugin);
	}
	
}
