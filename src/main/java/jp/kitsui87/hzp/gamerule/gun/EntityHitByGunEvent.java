package jp.kitsui87.hzp.gamerule.gun;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

/**
 * An event fired when a traveling bullet detects an entity to be hit.
 * 
 * @deprecated - exactly same situation with info can be handled in GunBase.
 */
public class EntityHitByGunEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	public final Entity victim;
	public final Entity suspect;
	
	public EntityHitByGunEvent(Entity suspect, Entity victim, double damage, Vector knockbackVec) {
		this.victim = victim;
		this.suspect = suspect;
	}
	
	public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

}
