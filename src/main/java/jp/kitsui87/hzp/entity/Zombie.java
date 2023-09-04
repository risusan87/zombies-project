package jp.kitsui87.hzp.entity;

import java.lang.reflect.Field;
import java.util.Set;

import jp.kitsui87.hzp.entity.ai.AILookAtFarPlayer;
import jp.kitsui87.hzp.entity.ai.AIWalkTowardsPlayer;
import net.minecraft.server.v1_12_R1.EntityZombie;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_12_R1.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_12_R1.World;

public class Zombie extends EntityZombie {

	public Zombie(World world) {
		super(world);
		
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		getAttributeMap().b(GenericAttributes.g);
		
		getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3D);
		getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(5D);
		getAttributeInstance(GenericAttributes.g).setValue(500.0D);
		getAttributeInstance(GenericAttributes.c).setValue(1.0D);
	}
	
	@Override
	protected void r() {
		super.r();
		try {
			Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
			bField.setAccessible(true);
			((Set<?>) bField.get(this.goalSelector)).clear();
			((Set<?>) bField.get(this.targetSelector)).clear();
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		this.goalSelector.a(0, new AILookAtFarPlayer(this));
	    //this.targetSelector.a(1, new AIWalkTowardsPlayer(this, 1.0D));
	    this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0D, true));
	    //this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
	    //this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
	    //this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0f));
	    //this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
	    
		//this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
	    //this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[] { EntityPigZombie.class }));
	    //this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false));
	    //this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntitySkeleton.class, false));
	    //this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, true));
		
	}
}
