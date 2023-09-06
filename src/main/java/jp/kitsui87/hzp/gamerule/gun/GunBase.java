package jp.kitsui87.hzp.gamerule.gun;

import jp.kitsui87.hzp.HypixelZombiesProject;
import jp.kitsui87.hzp.gamerule.PermissionRule;
import jp.kitsui87.hzp.util.ActionBarConstructor;
import jp.kitsui87.hzp.util.ChatJsonBuilder;
import jp.kitsui87.hzp.util.Script;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Base class for guns.
 * 
 * Every guns in game is nothing more than just an item, the only difference is that items marked
 * as "gun" have unique identifier for event listeners to process who, has what, guns at the time.
 * 
 * On creation of a gun, an itemstack contains the corresponded item for a specific gun(ex. Pistol is a wooden hoe) with unique identifier 
 * is generated and bound to an instance of this class. When a gun event occurs, it searches for the instance bound to the identifier
 * to run an appropriate method for the event(Gun shoot, Gun reload, etc.) to keep track on attributes for each guns.
 * 
 * @author kitsui87
 */
public abstract class GunBase {

	public final String gunName;
	public final String gunID; 
	protected final Item rawItem;
	protected final Map<String, Float[]> gunAttribute;
	protected final Player holder;
	protected final int gunSlot;

	/** Time in second that this gun takes to reload */
	public static final String RELOAD_TIME = "reloadTime";

	/** Time in second of cooldown to trigger a shoot */
	public static final String SHOOT_INTERVAL = "interval";

	/** Maximum amount of ammo that this gun carries */
	public static final String MAX_AMMO = "maxAmmo";

	/** Maximum amount of clip ammo that this gun carries */
	public static final String MAX_CLIP_AMMO = "maxClipAmmo";

	/** Time in second of cool down between each shoot when burst mode */
	public static final String BURST_INTERVAL = "burstInterval";

	/** Number of burst for this gun. Set of 1 means no burst */
	public static final String BURST_COUNT = "burstCount";

	public static final String BASE_DAMAGE = "baseDamage";

	public static final String GOLD_PER_HIT = "goldPerHit";
	
	public static final String KNOCKBACK_POWER = "knockbackPower";

	protected int currentAmmo;
	protected int currentClip;
	public final int maxUlt;
	protected int ultLv;

	protected boolean isReloading = false;
	protected boolean onCooldown = false;
	protected boolean isOutAmmo = false;
	
	private long lastShot = -1l;
	private int reloadTaskID = -1;
	private int intervalTaskID = -1;
	private float xpBarProgress = 1.0f;
	private ActionBarConstructor actionBarText = null;
	private int toleratedTime;

	/**
	 * Inherited classes MUST set their gun status 
	 * for each level of ultimate in their constructors.
	 * 
	 * Override {@link GunBase#setGunAttribute()} to set these values.
	 * 
	 * @param gunName - The global identifier used to differentiate from other guns.
	 * @param maxUlt - Maximum Level of ultimate that this gun could be.
	 * @param rawItem - Vanilla item that represents as this particular gun.
	 */
	protected GunBase(Player owner, String gunName, int maxUlt, Item rawItem, int slot) {
		
		this.gunSlot = slot;
		this.toleratedTime = this.getToleratedTime();
		this.gunID = UUID.randomUUID().toString();
		this.holder = owner;
		
		this.gunName = gunName;
		this.maxUlt = maxUlt;
		this.rawItem = rawItem;

		this.gunAttribute = this.setGunAttribute();

	}

	/**
	 * returns true if this shoot was last in clip
	 */
	protected abstract boolean eachShot();

	protected abstract void onShoot();
	
	protected abstract Vector getKnockbackVec();
	
	/**
	 * This is the most important method to override in child classes.
	 * The returned map of this method will contain everything needed to characterize each guns.
	 * 
	 * The default map will return an attribute of Pistol, to prevent null pointers in default.
	 * 
	 * @return Map object contains an attribute of Pistol.
	 */
	protected Map<String, Float[]> setGunAttribute() {
		Map<String, Float[]> att = new HashMap<String, Float[]>(8);
		att.put(RELOAD_TIME, new Float[] {1.5f, 1.0f});
		att.put(SHOOT_INTERVAL, new Float[] {0.5f, 0.4f});
		att.put(MAX_AMMO, new Float[] {300.f, 450.f});
		att.put(MAX_CLIP_AMMO, new Float[] {10.f, 14.f});
		att.put(BURST_COUNT, new Float[] {1f, 2f});
		att.put(BURST_INTERVAL, new Float[] {0.f, 0.1f});
		att.put(BASE_DAMAGE, new Float[] {3.f, 3.f});
		att.put(GOLD_PER_HIT, new Float[] {10.f, 10.f});
		att.put(KNOCKBACK_POWER, new Float[] {0.2f, 0.3f});
		return att;
	};

	public final void shoot() {
		
		if (this.isReloading) 
			return;
		if (this.currentAmmo == 0)
			return;
		
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();

		if (this.onCooldown) {
			long remaining = (long)(this.getAttribute(SHOOT_INTERVAL) * 1000f) - (System.currentTimeMillis() - this.lastShot);
			if (remaining < this.toleratedTime && remaining > -this.toleratedTime) {
				// System.out.println("remaining " + remaining + " millisec tolerated");
				schedular.cancelTask(this.intervalTaskID);
			} else
				return;
		}
		
		this.onCooldown = true;
		this.lastShot = System.currentTimeMillis();
		
		final GunBase gun = this;
		final int goalCount = (int)Math.ceil(gun.getAttribute(SHOOT_INTERVAL) * 20.f);
		Runnable r = new Runnable() {
			int count = 0;
			@Override
			public void run() {
				try {
					if (count >= goalCount) {
						schedular.cancelTask(gun.intervalTaskID);
						gun.onCooldown = false;
						return;
					}
					count++;
					gun.xpBarProgress = (float)count / (float)goalCount;
					if (gun.holder.isDead()) 
						return;
					ItemStack itemGun = gun.holder.getInventory().getItemInMainHand();
					NBTTagCompound tag = CraftItemStack.asNMSCopy(itemGun).getTag();
					if (tag != null && tag.hasKey("gunType") && tag.getString("gunID").equals(gun.gunID)) {
						gun.holder.setExp(gun.xpBarProgress);
					}
				} catch (Exception e) {
					e.printStackTrace();
					schedular.cancelTask(gun.reloadTaskID);
				}

			}

		};
		this.intervalTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);
		
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("javascript");
		try {
			Script script = new Script();
			scriptEngine.put("__javaImpScriptObj_", script);
			scriptEngine.eval("function greet(script) { script.hi(); }");
			scriptEngine.eval("greet(__javaImpScriptObj_);");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
		this.onShoot();

	}

	public final void reload() {
		
		if (this.isReloading) 
			return;

		if (
			this.currentClip == (int)this.getAttribute(MAX_CLIP_AMMO) ||
			this.currentClip == this.currentAmmo
		) {
			return;
		}

		this.isReloading = true;
		
		ChatJsonBuilder cjb = new ChatJsonBuilder();
		cjb.withText("RELOADING");
		cjb.withColor(ChatColor.RED);
		this.actionBarText = ActionBarConstructor.constractActionBarText(cjb);
		this.actionBarText.addViewers(holder);
		this.actionBarText.setTextVisible(true);
		
		final GunBase gun = this;
		final int goalCount = (int)Math.ceil(gun.getAttribute(RELOAD_TIME) * 20.f);
		final short maxDurability = holder.getInventory().getItemInMainHand().getType().getMaxDurability();
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		Runnable r = new Runnable() {

			int count = 0;

			@Override
			public void run() {
				try {
					if (count >= goalCount) {
						schedular.cancelTask(gun.reloadTaskID);
						
						int clip = (int)gun.getAttribute(MAX_CLIP_AMMO) <= gun.currentAmmo ? 
									(int)gun.getAttribute(MAX_CLIP_AMMO) : gun.currentAmmo;
						gun.currentClip = clip;
						
						updateGun();
						
						gun.actionBarText.setTextVisible(false);
						gun.actionBarText = null;
						
						gun.isReloading = false;
						return;
					}
					count++;
					float progress = (float)count / (float)goalCount;
					holder.getInventory().getItem(gun.gunSlot).setDurability((short)(maxDurability * (1f - progress)));
				} catch (Exception e) {
					// リロードバグ
					e.printStackTrace();
					schedular.cancelTask(gun.reloadTaskID);
				}

			}

		};
		holder.playSound(holder.getLocation(), Sound.ENTITY_HORSE_GALLOP, 0.5f, 0.5f);
		this.reloadTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);
	}

	public float getAttribute(String value) {
		return this.gunAttribute.get(value)[this.ultLv];
	}
	
	protected static GunBase createGun(Player owner, int slot, GunType gunType) {
		
		switch (gunType) {
			case PISTOL:
				return new GunPistol(owner, slot);
			default:
				return null;
		}
		
	}
	
	/**
	 * +/- tolerated time in millisec.
	 * @return
	 */
	protected int getToleratedTime() {
		return 25;
	}

	private ItemStack getGun() {
		net.minecraft.server.v1_12_R1.ItemStack nmsGun = new net.minecraft.server.v1_12_R1.ItemStack(rawItem);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("gunType", this.gunName);
		nbt.setString("gunID", this.gunID);
		nmsGun.setTag(nbt);
		return CraftItemStack.asBukkitCopy(nmsGun);
	}

	public void setUltLv(int lv) {
		this.ultLv = lv;
		this.currentAmmo = (int)this.getAttribute(MAX_AMMO);
		this.currentClip = (int)this.getAttribute(MAX_CLIP_AMMO);
		PermissionRule.getPermissionRule().removePermission(holder, PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND);
		holder.playSound(holder.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
		PermissionRule.getPermissionRule().addPermission(holder, PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND);
		this.updateGun();
	}
	
	public int getUltLv() {
		return this.ultLv;
	}
	
	public void onGunHeld(int slot) {
		
		if (this.actionBarText != null) 
			this.actionBarText.setTextVisible(true);
		
		int xp = holder.getLevel();
		int delta = this.currentAmmo - xp;
		holder.giveExpLevels(delta);
		holder.setExp(this.xpBarProgress);
		
	}
	
	public void onGunNotHeld(int slot) {
		
		if (this.actionBarText != null) 
			this.actionBarText.setTextVisible(false);
		
		int xp = holder.getExpToLevel();
		holder.giveExpLevels(-xp);
		holder.setExp(1f);
		
		
	}
	
	public void setCurrentAmmo(int amount) {
		
		if (amount < 0) amount = 0;
		else if (amount > (int)this.getAttribute(MAX_AMMO)) amount = (int)this.getAttribute(MAX_AMMO);
		
		
		
	}
	
	public int getCurrentAmmo() {
		return this.currentAmmo;
	}
	
	public void setCurrentClipAmmo(int ammount) {
		
	}
	
	public int getCurrentClipAmmo() {
		return this.currentClip;
	}
	
	// UPDATE AND STUFF
	
	/**
	 * Updates everything of this gun
	 * @return
	 */
	public final void updateGun() {
		
		ItemStack gun = this.getGun();
		
		PermissionRule.getPermissionRule().addPermission(holder, PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND);
		
		if (this.ultLv > 0) {
			gun.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			ItemMeta meta = gun.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			gun.setItemMeta(meta);
		} else {
			gun.removeEnchantment(Enchantment.ARROW_INFINITE);
		}
		
		updateAmmoDisplay();
		gun.setAmount(this.currentClip);
		
		holder.getInventory().setItem(this.gunSlot, gun);
		
	}
	
	private void updateAmmoDisplay() {
		if (holder.getInventory().getHeldItemSlot() == this.gunSlot) {
			int xp = holder.getLevel();
			int delta = this.currentAmmo - xp;
			holder.giveExpLevels(delta);
		}
	}
	
	private void updateClipDisplay() {
		PlayerInventory inventory = holder.getInventory();
		if (inventory.getHeldItemSlot() == this.gunSlot) {
			ItemStack stack = inventory.getItem(this.gunSlot);
			stack.setAmount(currentClip);
			inventory.setItem(this.gunSlot, stack);
		}
	}
	
	public final void refill() {
		
		BukkitScheduler schedular = HypixelZombiesProject.getPlugin().getServer().getScheduler();
		if (this.isReloading) {
			schedular.cancelTask(this.reloadTaskID);
			this.isReloading = false;
		}
		
		if (this.onCooldown) {
			schedular.cancelTask(this.intervalTaskID);
			this.onCooldown = false;
		}
		
		if (this.actionBarText != null) {
			this.actionBarText.setTextVisible(false);
			this.actionBarText = null;
		}
		
		this.currentAmmo = (int) this.getAttribute(MAX_AMMO);
		this.currentClip = (int) this.getAttribute(MAX_CLIP_AMMO);
		
		this.updateGun();
	}

	protected void spawnParticle(Particle p, Player holder) {

		Location currentLoc = holder.getEyeLocation();
		Vector lookVec = holder.getLocation().getDirection();

		for (float f = 1f; f < 6f; f++) {
			Location l = currentLoc.clone().add(lookVec.clone().multiply(f));
			
			holder.getWorld().spawnParticle(p, l, 0);
		}
	}

	/* TODO
	 * ヘッショ計算
	 */
	/**
	 * Called when a player's shot hit an enemy.
	 * 
	 * @param playerLoc - Current eye location of the player.
	 * @param enemyLoc - Current eye location of the enemy.
	 * @return isHeadShot
	 */
	protected boolean checkHeadShot(Location playerLoc, Location enemyLoc) {
		boolean headShot = false;
		
		double distance = playerLoc.distance(enemyLoc);
		
		Location l = playerLoc.clone();
		l.add(playerLoc.getDirection().multiply(distance));
		
		headShot = l.toVector().isInSphere(enemyLoc.toVector(), 0.35D);
		
		float pitch = headShot ? 1.5f : 2f;
		holder.playSound(holder.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, pitch);
		
		return headShot;
	}

	protected static class EntityFinder {

		private final float range;
		private final Location origin;
		private final Vector direction;

		private Function<Entity, Boolean> entityFiler = null;
		private Function<Block, Boolean> blockFiler = null;

		private EntityFinder(Location origin, Vector direction, float range) {
			this.origin = origin;
			this.direction = direction;
			this.range = range;
		}


		public static EntityFinder inATrajectoryOf(Location origin, Vector direction, float range) {
			return new EntityFinder(origin, direction, range);
		}


		public EntityFinder setEntityFilter(Function<Entity, Boolean> filter) {
			this.entityFiler = filter;
			return this;
		}

		public EntityFinder setBlockFilter(Function<Block, Boolean> filter) {
			this.blockFiler = filter;
			return this;
		}

		public Entity find() {

			World world = origin.getWorld();
			Location currentLoc = this.origin;
			Vector lookVec = this.direction;
			boolean hit = false;

			for (float f = 0f; !hit && f < range; f += 0.1f) {
				Location evalLoc = currentLoc.clone().add(lookVec.clone().multiply(f));
				Collection<Entity> victim = world.getNearbyEntities(evalLoc, .25f, .25f, .25f);
				for (Entity e : victim.toArray(new Entity[victim.size()])) {
					if (this.entityFiler.apply(e)) {
						hit = true;
						return e;
					}
				}
				Block currentBlock = world.getBlockAt(evalLoc);
				if (this.blockFiler.apply(currentBlock)) {
					hit = true;
					return null;
				}
			}
			return null;
		}

	}

	protected static class BurstSchedular {

		private final int taskID;

		public BurstSchedular(GunBase controller) {

			HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
			BukkitScheduler schedular = plugin.getServer().getScheduler();
			final BurstSchedular bs = this;
			final int burstCount = (int)controller.getAttribute(BURST_COUNT);
			final float burstInterval = controller.getAttribute(BURST_INTERVAL);
			Runnable r = new Runnable() {

				int count = 0;
				int burst = 0;

				@Override
				public void run() {

					if (controller.isReloading) {
						schedular.cancelTask(bs.taskID);
						return;
					}
					int i = (int)(burstInterval * 20f);
					if (count % (i == 0 ? 1 : i) == 0) {

						if (controller.eachShot()) {
							
							schedular.cancelTask(bs.taskID);
							
							if (controller.currentAmmo == 0) {
								controller.actionBarText = 
										ActionBarConstructor.constractActionBarText(
											new ChatJsonBuilder().withText("OUT OF AMMO").withColor(ChatColor.RED)
										);
								controller.actionBarText.addViewers(controller.holder);
								controller.actionBarText.setTextVisible(true);
								PlayerInventory inventory = controller.holder.getInventory();
								ItemStack item = inventory.getItem(controller.gunSlot);
								short dul = (short)((float)item.getType().getMaxDurability() * 0.99f);
								item.setDurability(dul);
								inventory.setItem(controller.gunSlot, item);
								controller.updateAmmoDisplay();
								controller.isOutAmmo = true;
								return;
							}
							
							int xp = controller.holder.getLevel();
							int delta = controller.currentAmmo - xp;
							controller.holder.giveExpLevels(delta);
							
							controller.reload();
							
							return;
						}
						burst++;
						controller.updateGun();
					}

					if (burst >= burstCount) {
						schedular.cancelTask(bs.taskID);
						return;
					}
					count++;
				}
			};
			this.taskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);

		}
	}
}
