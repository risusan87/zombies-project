package jp.kitsui87.hzp.gamerule;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeRule {
	
	public void applyGamemode() {
		GameRunningRule.getZombies().getInGamePlayers().forEach((uuid, profile) -> {
			
			Player p = Bukkit.getPlayer(uuid);
			
			switch (profile.playerState) {
				case SPECTATOR:
					p.setGameMode(GameMode.SPECTATOR);
					break;
				case EDITOR:
					p.setGameMode(GameMode.CREATIVE);
					break;
				default:
					p.setGameMode(GameMode.ADVENTURE);
			}
		});
	}

}
