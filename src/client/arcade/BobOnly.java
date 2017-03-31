package client.arcade;

import client.MapleCharacter;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;

public class BobOnly extends Arcade {

	int highscore = 0;
	int prevScore = getHighscore(arcadeId, player);
	
	boolean killedWrong = false;
	
	public BobOnly(MapleCharacter player) {
		super(player);
		this.mapId = 910100000;
		this.arcadeId = 1;
		this.rewardPerKill = 0.40;
		this.itemReward = 4310149;
	}

	@Override
	public boolean fail() {
		if(killedWrong) {
			
			player.changeMap(970000000, 0);
			player.announce(MaplePacketCreator.serverNotice(1, "Game Over!"));
			if(saveData(highscore)) {
				player.dropMessage(5, "[Game Over] Your new highscore for Bob Only is " + highscore);
			} else {
				player.dropMessage(5, "[Game Over] Your highscore for Bob Only remains at " + Arcade.getHighscore(arcadeId, player));
			}
			MapleInventoryManipulator.addById(player.getClient(), itemReward, (short) (rewardPerKill  * highscore));
			respawnManager = null;
			player.setArcade(null);
		}
		return true;
	}

	@Override
	public void add() {
		++highscore;
		player.announce(MaplePacketCreator.sendHint("#e[Bob only]#n\r\nYou have killed " + ((prevScore < highscore) ?  "#g" : "#r") + highscore + "#k bob(s)!", 300, 40));
		
	}

	@Override
	public void onKill(int monster) {
		int killOnly = 9400551;
		if(monster != killOnly) {
			killedWrong = true;
			fail();
		} else {
			add();
		}
		
	}

	@Override
	public void onHit(int monster) {}

	@Override
	public boolean onBreak(int reactor) {
		return false;
	}

}
