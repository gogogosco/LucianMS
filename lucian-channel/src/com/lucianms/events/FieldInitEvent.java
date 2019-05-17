package com.lucianms.events;

import com.lucianms.client.MapleCharacter;
import com.lucianms.client.MapleClient;
import com.lucianms.constants.GameConstants;
import com.lucianms.io.scripting.event.EventInstanceManager;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.maps.MapleMap;
import com.lucianms.server.maps.MapleMiniDungeon;
import tools.MaplePacketCreator;

import java.util.Calendar;

/**
 * @author izarooni
 */
public class FieldInitEvent extends PacketEvent {
    @Override
    public void processInput(MaplePacketReader reader) {
    }

    @Override
    public Object onPacket() {
        MapleClient client = getClient();
        MapleCharacter player = client.getPlayer();
        MapleMap map = player.getMap();
        final int mapID = map.getId();
        EventInstanceManager eim = player.getEventInstance();

        if (player.getForcedStat() != null) {
            player.setForcedStat(null);
            client.announce(MaplePacketCreator.getForcedStatReset());
        }

        player.setRates();

        if (MapleMiniDungeon.isDungeonMap(mapID)) {
            MapleMiniDungeon dungeon = MapleMiniDungeon.getDungeon(mapID);
            if (dungeon != null) {
                client.announce(MaplePacketCreator.getClock(30 * 60));
                TaskExecutor.createTask(new Runnable() {

                    @Override
                    public void run() {
                        if (MapleMiniDungeon.isDungeonMap(player.getMapId())) {
                            player.changeMap(dungeon.getBase());
                        }
                    }
                }, 30 * 60 * 1000);
            }
        }

        if (eim != null && eim.isTimerStarted()) {
            client.announce(MaplePacketCreator.getClock((int) (eim.getTimeLeft() / 1000)));
        }
        if (player.getFitness() != null && player.getFitness().isTimerStarted()) {
            client.announce(MaplePacketCreator.getClock((int) (player.getFitness().getTimeLeft() / 1000)));
        }

        if (player.getOla() != null && player.getOla().isTimerStarted()) {
            client.announce(MaplePacketCreator.getClock((int) (player.getOla().getTimeLeft() / 1000)));
        }

        if (mapID == 109060000) {
            client.announce(MaplePacketCreator.rollSnowBall(true, 0, null, null));
        }
        if (mapID == 914000200 || mapID == 914000210 || mapID == 914000220) {
            client.announce(MaplePacketCreator.aranGodlyStats());
        }

        if (map.hasClock()) {
            Calendar cal = Calendar.getInstance();
            client.announce((MaplePacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }

        if (player.getDragon() == null && GameConstants.hasSPTable(player.getJob())) {
            player.createDragon();
            map.sendPacket(MaplePacketCreator.spawnDragon(player.getDragon()));
        }
        return null;
    }
}
