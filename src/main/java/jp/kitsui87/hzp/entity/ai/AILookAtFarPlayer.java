package jp.kitsui87.hzp.entity.ai;

import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IEntitySelector;
import net.minecraft.server.v1_12_R1.World;

import com.google.common.base.Predicates;

public final class AILookAtFarPlayer extends AIBase {

	private EntityPlayer target;
	private final float range;

	public AILookAtFarPlayer(EntityInsentient aiHolder) {
		super(aiHolder);
		this.range = 256.f;
		a(0);
	}
	
	@Override
	public boolean isAIApplicable() {
		if (this.holder.getGoalTarget() == null) {
			World world = this.holder.world;
			double x = holder.locX, y = holder.locY, z = holder.locZ;
			this.target = (EntityPlayer) world.a(x, y, z, this.range, Predicates.and(IEntitySelector.e, IEntitySelector.b(this.holder)));
			this.holder.setGoalTarget(this.target);
		} else {
			this.target = (EntityPlayer) holder.getGoalTarget();
		}
		
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		if (!this.target.isAlive())
			return false; 
		if (this.holder.h(this.target) > (range * range))
			return false;
		else 
			return true;
	}
	
	@Override
	public void onFinalize() {
		this.target = null;
	}
	
	@Override
	public void onUpdate() {
		double x = target.locX, y = target.locY + target.getHeadHeight(), z = target.locZ;
		this.holder.getControllerLook().a(x, y, z, 10, 40);
	}
}
