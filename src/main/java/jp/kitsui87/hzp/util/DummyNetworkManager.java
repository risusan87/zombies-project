package jp.kitsui87.hzp.util;

import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.NetworkManager;

public class DummyNetworkManager extends NetworkManager {

	public DummyNetworkManager(EnumProtocolDirection enumprotocoldirection) {
		super(enumprotocoldirection);
	}

}
