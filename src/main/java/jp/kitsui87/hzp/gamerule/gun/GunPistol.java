package jp.kitsui87.hzp.gamerule.gun;

import net.minecraft.server.v1_12_R1.Items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;


public class GunPistol extends GunBase {
	
	public GunPistol(Player holder, int slot) {
		super(holder, GunType.PISTOL.getGunName(), 2, Items.WOODEN_HOE, slot);
		
		this.ultLv = 0;
		
		this.currentClip = (int)this.getAttribute(MAX_CLIP_AMMO);
		this.currentAmmo = (int)this.getAttribute(MAX_AMMO);
	}

	@Override
	protected void onShoot() {
		new BurstSchedular(this);
	}

	@Override
	protected boolean eachShot() {
		
		World world = holder.getWorld();
		world.playSound(holder.getLocation(), Sound.ENTITY_IRONGOLEM_HURT, 0.5f, 2.0f);
		
		Location currentLoc = holder.getEyeLocation();
		Vector lookVec = holder.getLocation().getDirection();
		
		super.spawnParticle(Particle.CRIT, holder);
		
		EntityFinder finder = EntityFinder.inATrajectoryOf(currentLoc, lookVec, 100.f);
		finder.setEntityFilter(entity -> {
			return entity instanceof LivingEntity && !(entity instanceof Player) && !entity.isDead();
		});
		finder.setBlockFilter(block -> {
			return block.getType() != Material.AIR;
		});
		
		LivingEntity victim = (LivingEntity)finder.find();
		if (victim != null) {
			double d = this.getAttribute(BASE_DAMAGE);
			int gold = (int)this.getAttribute(GOLD_PER_HIT);
			if (this.checkHeadShot(currentLoc, victim.getEyeLocation())) {
				d *= 1.5D;
				gold = (int)Math.floor((float) gold * 1.5f);
			}
			victim.damage(d);
			victim.setVelocity(victim.getVelocity().add(getKnockbackVec()));
		}


		this.currentAmmo--;
		this.currentClip--;

		return this.currentClip == 0;
	}

	@Override
	protected Map<String, Float[]> setGunAttribute() {
		return super.setGunAttribute();
	}

	@Override
	protected Vector getKnockbackVec() {
		return this.holder.getEyeLocation().getDirection().multiply(getAttribute(KNOCKBACK_POWER));
	}

}
