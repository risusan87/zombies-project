package jp.kitsui87.hzp.gamerule;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.base.Function;

import jp.kitsui87.hzp.HypixelZombiesProject;
import jp.kitsui87.hzp.entity.Corpse;
import jp.kitsui87.hzp.gamerule.GameStateRule.GameProfile;
import jp.kitsui87.hzp.gamerule.GameStateRule.PlayerState;
import jp.kitsui87.hzp.util.ActionBarConstructor;
import jp.kitsui87.hzp.util.ChatJsonBuilder;
import net.minecraft.server.v1_12_R1.EntityPlayer;

/**
 * Class {@link PlayerDeathRule} provides methods to make custom behaviors under regarding deaths of players.<br>
 * Use of this class should be pretty straight forward.<br>
 * For example, to make player Player123 knocked down state: <br>
 * <br>
 * {@code Player player = Bukkit.getPlayer("Player123");} <br>
 * {@code new ReviveTask().knockdownPlayer(player.getUniqueID());}<br>
 * <br>
 * Brand new instances are mandatory for each player to work.<br>
 * Do NOT reuse instances between different players.<br>
 * <br>
 * Methods in this class will not function if target players are under mismatch {@link PlayerState}.<br>
 * Such like calling knockdownPlayer() 
 * for a player who is already dead.
 */
public class PlayerDeathRule {

	private int reviveLookforTaskID = -1;
	private int reviveTickingTaskID = -1;
	private float revLifeRemaining = 30.0f;
	private float revRemaining = 1.5f;
	
	private ArmorStand hologram;
	private ArmorStand vehicle;
	private ArmorStand vehicle2;
	
	private Player profileOwner;
	
	public void knockdownPlayer(UUID p) {

		GameProfile profile = GameRunningRule.getZombies().getInGamePlayers().get(p);
		
		if (profile != null && profile.playerState == PlayerState.IN_GAME_ALIVE) {
			
			Corpse playerCorpse = new Corpse(Bukkit.getPlayer(p));
			System.out.println(playerCorpse.getEntity());
			this.profileOwner = Bukkit.getPlayer(p);
			profile.playerState = PlayerState.IN_GAME_DOWN;
			Location baseLoc = playerCorpse.getLocation().clone();
			baseLoc.add(0, 0, -1);
			profileOwner.teleport(baseLoc);
			profile.deathRule = this;
			
			hologram = (ArmorStand)profileOwner.getWorld().spawnEntity(baseLoc.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
			((CraftEntity)hologram).getHandle().setInvisible(true);
			hologram.setGravity(false);
			hologram.setCustomName("HELOGDHWYUWD");
			hologram.setCustomNameVisible(true);
			
			vehicle = (ArmorStand)profileOwner.getWorld().spawnEntity(baseLoc.clone().add(0, -2.5, 0), EntityType.ARMOR_STAND);
			((CraftEntity)vehicle).getHandle().setInvisible(true);
			vehicle.setGravity(false);
			
			vehicle.setPassenger(profileOwner);
			ActionBarConstructor ac = ActionBarConstructor.constractActionBarText(
					new ChatJsonBuilder().withText("You are dead!"));
			ac.addViewers(profileOwner);
			ac.setTextVisible(true);
			HypixelZombiesProject.getSchedular().scheduleSyncDelayedTask(
					HypixelZombiesProject.getPlugin(), () -> {
						ac.setTextVisible(false);
					}, 10);
			
			ScoreboardRule.getScoreboardRule().removePlayer(profileOwner, ScoreboardRule.TEAM_IN_GAME_PLAYERS);
			ScoreboardRule.getScoreboardRule().addPlayer(profileOwner, ScoreboardRule.TEAM_CORPSE);

			saveInventory(profileOwner);

			((CraftEntity)profileOwner).getHandle().setInvisible(true);

			scheduleKnockdownTask(profileOwner, playerCorpse);
		}
	}

	private void scheduleKnockdownTask(Player profileOwner, Corpse playerCorpse) {
		// start revive task and look for players nearby who is sneaking
		// this task runs asynchronously yet synchronized with server thread.
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		Runnable reviveLookforTask = () -> {

			Location corpseLoc = playerCorpse.getLocation().clone();
			corpseLoc.add(0, 0, -1);
			profileOwner.getWorld().getNearbyEntities(corpseLoc, 0.5, 0.5, 0.5).forEach(p -> {
				if (!(p instanceof Player)) 
					return;
				Player player = ((Player)p);
				// player is trying to revive profileOwner
				if (player.isSneaking()) {
					plugin.getServer().getScheduler().cancelTask(reviveLookforTaskID);
					this.revLifeRemaining = 30.f;
					this.scheduleReviveTickingTask(player, profileOwner, playerCorpse);
					return;
				}
			});
			this.revLifeRemaining -= 0.1f;
			hologram.setCustomName(String.format("Dies in %.1f sec", this.revLifeRemaining));
			// if time runs out
			if (this.revLifeRemaining <= 0f) {
				GameRunningRule.getZombies().getInGamePlayers().get(profileOwner.getUniqueId()).playerState = PlayerState.IN_GAME_DEAD;
				
				profileOwner.teleport(profileOwner.getLocation().add(0,  2.5, 0));
				profileOwner.setGameMode(GameMode.CREATIVE);
				HypixelZombiesProject.getSchedular().cancelTask(reviveLookforTaskID);
				
				hologram.remove();
				vehicle.remove();
				
				return;
			}
		};
		this.reviveLookforTaskID = HypixelZombiesProject.getSchedular().scheduleSyncRepeatingTask(
				plugin, reviveLookforTask, 0, 2);
	}

	private void scheduleReviveTickingTask(Player reviver, Player profileOwner, Corpse playerCorpse) {
		ActionBarConstructor actionBar = ActionBarConstructor.constractActionBarText(
				new ChatJsonBuilder().withText(String.format("Reviving %s %.1f", profileOwner.getName(), this.revRemaining))
			);
		actionBar.addViewers(reviver);
		actionBar.setTextVisible(true);
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		this.reviveTickingTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
				plugin, () -> {

					hologram.setCustomName(String.format("Being revived %.1f ", this.revRemaining));
					// player cancels revive
					if (!reviver.isSneaking()) {
						this.scheduleKnockdownTask(profileOwner, playerCorpse);
						plugin.getServer().getScheduler().cancelTask(this.reviveTickingTaskID);
						actionBar.setTextVisible(false);
						this.revRemaining = 1.5f;
						return;
					}
					this.revRemaining -= 0.1f;
					actionBar.editMessage(new ChatJsonBuilder().withText(String.format("Reviving %s %.1f", profileOwner.getName(), this.revRemaining)));
					// player revived
					if (this.revRemaining <= 0f) {
						
						profileOwner.teleport(profileOwner.getLocation().add(0,  2.5, 0));
						HypixelZombiesProject.getSchedular().cancelTask(reviveTickingTaskID);
						GameProfile profile = GameRunningRule.getZombies().getInGamePlayers().get(profileOwner.getUniqueId());
						profile.playerState = PlayerState.IN_GAME_ALIVE;

						ScoreboardRule.getScoreboardRule().removePlayer(profileOwner, ScoreboardRule.TEAM_CORPSE);
						ScoreboardRule.getScoreboardRule().addPlayer(profileOwner, ScoreboardRule.TEAM_IN_GAME_PLAYERS);

						restoreInventory(profileOwner);

						((CraftPlayer)profileOwner).getHandle().setInvisible(false);
						playerCorpse.kill();

						profileOwner.setGameMode(GameMode.ADVENTURE);
						
						actionBar.setTextVisible(false);
						
						
						hologram.remove();
						vehicle.remove();
						
						return;
					}
				}, 0, 2);
	}
	
	public void remount() {
		this.vehicle.setPassenger(this.profileOwner);
	}
	
	private void sneakCheck() {
		if(this.profileOwner.isSneaking()) {
			System.out.println("remount");
			this.remount();
		}
	}
	
	private void saveInventory(Player p) {
		GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).savedInventory 
				= p.getInventory().getContents();
		p.getInventory().clear();
	}
	
	private void restoreInventory(Player p) {
		ItemStack[] saved = GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).savedInventory;
		p.getInventory().setContents(saved);
		GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).savedInventory = null;
	}
}
