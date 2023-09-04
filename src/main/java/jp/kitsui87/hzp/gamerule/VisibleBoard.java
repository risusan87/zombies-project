package jp.kitsui87.hzp.gamerule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import jp.kitsui87.hzp.HypixelZombiesProject;

public class VisibleBoard {

	private static VisibleBoard board;
	
	private String mapName = "Quintistic";
	private int playerReady = 1;
	private int maxPlayers = 4;
	private final Map<BoardType, BoardSidebar> boardMap;
	private final Map<UUID, Integer> playerPos;
	private BoardType currentBoard;
	
	private int ingameTimer = 0;
	private int ingameTimerTaskID = -1;
	private int ingameZombiesLeft = 0;
	private int ingameRound = 0;
	
	public static enum BoardType {
		INVISIBLE,
		WAITING,
		INGAME,
		ENDED;
	}
	
	private VisibleBoard() {

		BoardSidebar waitBoard = new BoardSidebar("" + ChatColor.YELLOW + ChatColor.BOLD + "ZOMBIES");
		BoardSidebar ingameBoard = new BoardSidebar("" + ChatColor.YELLOW + ChatColor.BOLD + "ZOMBIES");
		BoardSidebar waitEnded = new BoardSidebar("" + ChatColor.YELLOW + ChatColor.BOLD + "ZOMBIES");
		
		waitBoard.setLine(8, "ivg");
		waitBoard.setLine(6, "" + ChatColor.WHITE + "Map: " + ChatColor.GREEN + mapName);
		waitBoard.setLine(5, "" + ChatColor.WHITE + "Players: " + ChatColor.GREEN + playerReady + "/" + maxPlayers);
		waitBoard.setLine(3, "" + ChatColor.WHITE + "Waiting...");
		waitBoard.setLine(1, "" + ChatColor.YELLOW + "quintistic.net");
		
		ingameBoard.setLine(15, " ");
		ingameBoard.setLine(13, "" + ChatColor.RED + ChatColor.BOLD + "Round " + ingameRound);
		ingameBoard.setLine(12, "" + ChatColor.WHITE + "Zombies Left: " + ChatColor.GREEN + ingameZombiesLeft);
		ingameBoard.setLine(10, "" + ChatColor.WHITE + "Shirokuma_Risu: " + ChatColor.GOLD + "0");
		ingameBoard.setLine(9, "" + ChatColor.WHITE + "EMPTY");
		ingameBoard.setLine(8, "" + ChatColor.GRAY + "EMPTY ");
		ingameBoard.setLine(7, "" + ChatColor.GRAY + "EMPTY  ");
		ingameBoard.setLine(5, "" + ChatColor.WHITE + "Zombie Kills: " + ChatColor.GREEN + "114,514");
		ingameBoard.setLine(4, "" + ChatColor.WHITE + "Time: " + ChatColor.GREEN + toTime(ingameTimer));
		ingameBoard.setLine(3, "" + ChatColor.WHITE + "Map: " + ChatColor.GREEN + mapName);
		
		this.boardMap = new HashMap<BoardType, BoardSidebar>(4);
		this.boardMap.put(BoardType.WAITING, waitBoard);
		this.boardMap.put(BoardType.INGAME, ingameBoard);
		this.boardMap.put(BoardType.ENDED, waitEnded);
		this.boardMap.put(BoardType.INVISIBLE, null);
		this.playerPos = null;
		
	}
	
	public void ingameBoardTimerStart() {
		this.ingameTimerTaskID = HypixelZombiesProject.getSchedular().scheduleSyncRepeatingTask(
				HypixelZombiesProject.getPlugin(), 
				() -> {
					VisibleBoard vb = VisibleBoard.getBoard();
					if (vb.currentBoard != BoardType.INGAME) {
						vb.ingameBoardTimerStop();
						vb.ingameBoardTimerReset();
						return;
					}
					vb.ingameTimer++;
					vb.boardMap.get(BoardType.INGAME)
					.setLine(4, "" + ChatColor.WHITE + "Time: " + ChatColor.GREEN + toTime(ingameTimer));
					vb.setVisibleBoard(BoardType.INGAME);
				}, 0, 20);
	}
	
	public void ingameBoardTimerStop() {
		HypixelZombiesProject.getSchedular().cancelTask(this.ingameTimerTaskID);
	}
	
	public void ingameBoardTimerReset() {
		this.ingameTimer = 0;
	}
	
	private static String toTime(int second) {
		int hour = (int) Math.floor((float) second / 3600.f);
		int min = (int) Math.floor(((float) second % 3600.f) / 60.f);
		int sec = (int) Math.floor((float) second % 60.f);
		return String.format("%s%s", 
				hour != 0 ? (Integer.toString(hour) + ":") : "",
				String.format("%02d:%02d", min, sec));
	}
	
	public void setVisibleBoard(BoardType type) {
		this.currentBoard = type;
		GameRunningRule.getZombies().getInGamePlayers().forEach((uuid, profile) -> {
			
			if (Bukkit.getPlayer(uuid) == null) 
				return;
			
			Scoreboard sb; 
			if (type != BoardType.INVISIBLE) 
				sb = VisibleBoard.getBoard().boardMap.get(type).getScoreboard();
			else
				sb = Bukkit.getScoreboardManager().getNewScoreboard();
			Bukkit.getPlayer(uuid).setScoreboard(sb);
		});
		
	}
	
	public static VisibleBoard setupBoard() {
		if (board != null)
			return board;
		return (board = new VisibleBoard());
	}
	
	public static VisibleBoard getBoard() {
		return board;
	}
	
	public static void disableVisibleBoard() {
		
		GameRunningRule.getZombies().getInGamePlayers().forEach((uuid, profile) -> {
			Bukkit.getPlayer(uuid).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		});
		board = null;
		
	}
	
	private static class BoardSidebar {
		
		private Objective obj;
		private String title;

		private final List<String> lines;
		
		public BoardSidebar(String title) {
			this.title = title;
			this.lines = new ArrayList<String>();	
		}
		
		public void setLine(int lineNum, String line) {
			int lineSize = this.lines.size();
			for (int i = lineSize; i < lineNum; i++) {
				String s = "";
				for (int j = 0; j < i; j++) s+=" ";
				this.lines.add(s);
			}
			
			this.lines.set(lineNum - 1, line);
		}
		
		public Scoreboard getScoreboard() {
			
			if (this.obj != null) {
				obj.unregister();
			}
			this.obj = ScoreboardRule.getScoreboardRule().scoreboard.registerNewObjective("obj", "dummy");
			this.obj.setDisplayName(title);
			this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			//this.obj.set
			for (int i = this.lines.size(); i > 0; i--) {
				Score s = this.obj.getScore(this.lines.get(i - 1));
				
				s.setScore(i);
			}
			
			return ScoreboardRule.getScoreboardRule().scoreboard;
		}
		
	}
}
