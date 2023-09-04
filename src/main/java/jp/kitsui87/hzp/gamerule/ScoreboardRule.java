package jp.kitsui87.hzp.gamerule;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardRule {

	private static ScoreboardRule rule;

	protected final Scoreboard scoreboard;
	private final Scoreboard scoreboardCorpse;
	private final Team teamInGamePlayers;
	private final Team teamCorpse;

	public static final String TEAM_IN_GAME_PLAYERS = "team1";
	public static final String TEAM_CORPSE = "team2";

	private ScoreboardRule() {

		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.teamInGamePlayers = this.scoreboard.registerNewTeam(TEAM_IN_GAME_PLAYERS);
		this.teamInGamePlayers.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		this.teamInGamePlayers.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);   
		
		this.scoreboardCorpse = Bukkit.getScoreboardManager().getNewScoreboard();
		this.teamCorpse = this.scoreboardCorpse.registerNewTeam(TEAM_CORPSE);
		this.teamCorpse.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		this.teamCorpse.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER); 
		
	}

	public void addPlayer(Player p, String team) {

		Team t = this.scoreboard.getTeam(team);
		if (t == null)
			return;

		t.addEntry(p.getName());
		p.setScoreboard(this.scoreboard);

	}

	public void removePlayer(Player p, String team) {
		if(p.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) 
			return;
		if(p.getScoreboard().getTeam(team) != null)
			p.getScoreboard().getTeam(team).removeEntry(p.getName());
	}

	public boolean containsPlayer(Player p, String team) {
		return p.getScoreboard().getTeam(team) != null && p.getScoreboard().getTeam(team).hasEntry(p.getName());
	}

	public static ScoreboardRule getScoreboardRule() {
		return rule == null ? (rule = new ScoreboardRule()) : rule;
	}

	public static void disableScoreboardRule() {

		rule = null;
	}

}
