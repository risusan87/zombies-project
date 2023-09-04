package jp.kitsui87.hzp.entity.ai;

import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.PathfinderGoal;

public abstract class AIBase extends PathfinderGoal {
	
	protected final EntityInsentient holder;
	
	protected AIBase(EntityInsentient aiHolder) {
		this.holder = aiHolder;
	}

	@Override
	public boolean a() {
		return isAIApplicable();
	}

	@Override
	public boolean b() {
		return shouldContinue();
	}

	@Override
	public void c() {
		onStartProcess();
	}
	
	@Override
	public void d() {
		onFinalize();
	}

	@Override
	public void e() {
		onUpdate();
	}

	protected abstract boolean isAIApplicable();
	
	protected boolean shouldContinue() {
		return a();
	}
	
	protected void onStartProcess() {}
	
	protected void onFinalize() {}
	
	protected void onUpdate() {}
}
