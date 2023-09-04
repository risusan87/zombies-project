package jp.kitsui87.hzp.gamerule.gun;

public enum GunType {

	PRACTICE("practice"),
	PISTOL  ("pistol"),
	SHOTGUN ("shotgun"),
	RIFLE   ("rifle"),
	SNIPER  ("sniper"),
	ROCKET  ("rocket"),
	SOAKER  ("soaker"),
	ELDER   ("elder"),
	DIGGER  ("digger"),
	ZAPPER  ("zapper"),
	FLAME   ("flame"),
	RAINBOW ("rainbow"),
	BARREL  ("barrel")
	; 

	private String gunType;

	private GunType(String gunType) {
		this.gunType = gunType;
	}

	public String getGunName() {
		return this.gunType;
	}
}
