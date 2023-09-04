package jp.kitsui87.hzp.entity.ai;

import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import net.minecraft.server.v1_12_R1.RandomPositionGenerator;
import net.minecraft.server.v1_12_R1.Vec3D;

public class AIWalkTowardsPlayer extends PathfinderGoal {

	private final EntityCreature a;

	private EntityLiving b;

	private double c;

	private double d;

	private double e;

	private final double f;


	public AIWalkTowardsPlayer(EntityCreature paramEntityCreature, double paramDouble) {
		this.a = paramEntityCreature;
		this.f = paramDouble;
		a(1);
	}

	public boolean a() {
		this.b = this.a.getGoalTarget();
		if (this.b == null)
			return false; 
		Vec3D vec3D = this.b.d();
		if (vec3D == null)
			return false; 
		this.c = vec3D.x;
		this.d = vec3D.y;
		this.e = vec3D.z;
		return true;
	}

	public boolean b() {
		return (!this.a.getNavigation().o() && this.b.isAlive());
	}

	public void d() {
		this.b = null;
	}

	public void c() {
		this.a.getNavigation().a(this.c, this.d, this.e, this.f);
	}

}
