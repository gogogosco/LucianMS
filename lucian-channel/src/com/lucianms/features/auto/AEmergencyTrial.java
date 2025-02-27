package com.lucianms.features.auto;

import com.lucianms.client.MapleCharacter;
import com.lucianms.constants.ExpTable;
import com.lucianms.constants.ServerConstants;
import com.lucianms.events.ChangeMapEvent;
import com.lucianms.lang.annotation.PacketWorker;
import com.lucianms.nio.SendOpcode;
import com.lucianms.nio.send.MaplePacketWriter;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.channel.MapleChannel;
import com.lucianms.server.life.MapleLifeFactory;
import com.lucianms.server.life.MapleMonster;
import com.lucianms.server.life.MapleNPC;
import com.lucianms.server.life.MonsterListener;
import com.lucianms.server.maps.MapleMap;
import com.lucianms.server.maps.MapleMapObject;
import com.lucianms.server.world.MapleWorld;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author izarooni
 */
public class AEmergencyTrial extends GAutoEvent {

    public static final int BossMapID = 993080000;
    public static final int TransferMapID = 993060000;
    public static final int WaitingMapID = 993070000;

    private static final int NpcID = 9010022;
    private static final int EndNpc = 9000025;
    private static final int BossID = 8840000;

    private HashMap<Integer, Integer> spawnLocations = new HashMap<>();
    private HashMap<Integer, Integer> returnLocations = new HashMap<>();

    private long bonusExperience = 0;
    private long endTimestamp;

    private static byte[] createNpc(MapleNPC life, Point location) {
        final MaplePacketWriter w = new MaplePacketWriter(24);
        w.writeShort(SendOpcode.SPAWN_NPC.getValue());
        w.writeInt(life.getObjectId());
        w.writeInt(life.getId());
        w.writeShort(location.x);
        w.writeShort(location.y);
        w.write(0);
        w.writeShort(0);
        w.writeShort(location.x - 50);
        w.writeShort(location.x + 50);
        w.write(1);
        return w.getPacket();
    }

    public AEmergencyTrial(MapleWorld world) {
        super(world, false);
        registerAnnotationPacketEvents(this);
    }

    @PacketWorker
    public void onFieldChange(ChangeMapEvent event) {
        // for when players are transferring to boss from waiting room,
        // send a timer that displays how much time is left before timeout
        event.onPost(new Runnable() {
            @Override
            public void run() {
                int left = (int) (getTimeLeft() / 1000);
                if (left > 0) {
                    event.getClient().announce(MaplePacketCreator.getClock(left));
                }
            }
        });
    }

    @Override
    public void start() {
        for (MapleChannel channel : getWorld().getChannels()) {
            channel.removeMap(BossMapID);
            Collection<MapleCharacter> players = getWorld().getPlayers(p -> p.getClient().getChannel() == channel.getId());
            for (MapleCharacter player : players) {
                MapleMap map = player.getMap();
                if (!map.getMonsterSpawnPoints().isEmpty()) {
                    Integer objectID = spawnLocations.get(map.getId());
                    MapleNPC npc;
                    if (objectID == null) {
                        npc = MapleLifeFactory.getNPC(NpcID);
                        npc.setScript("f_emergency_trial");
                        map.addMapObject(npc);
                    } else {
                        npc = (MapleNPC) map.getMapObject(objectID);
                    }
                    spawnLocations.putIfAbsent(map.getId(), npc.getObjectId());
                    player.announce(createNpc(npc, player.getPosition()));
                    player.sendMessage(5, "A mysterious portal has appeared in the map");
                }
            }
            players.clear();
        }

        endTimestamp = System.currentTimeMillis() + (1000 * 30);
        TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                endTimestamp = System.currentTimeMillis() + (1000 * 120);
                getWorld().getChannels().forEach(ch -> ch.getMap(WaitingMapID).warpEveryone(TransferMapID));
                despawnDoors();
                summonBoss();
            }
        }, 1000 * 30);
    }

    @Override
    public void stop() {
        despawnDoors();
        getWorld().getChannels().forEach(this::returnPlayers);
    }

    @Override
    public void playerRegistered(MapleCharacter player) {
        if (player.addGenericEvent(this)) {
            returnLocations.put(player.getId(), player.getMapId());
            player.changeMap(WaitingMapID);
            long left = getTimeLeft();
            if (left > 0) {
                player.announce(MaplePacketCreator.getClock((int) (left / 1000)));
            }
        }
    }

    @Override
    public void playerUnregistered(MapleCharacter player) {
        player.changeMap(returnLocations.remove(returnLocations.getOrDefault(player.getId(), ServerConstants.MAPS.Home)));
        player.getGenericEvents().removeIf(g -> g instanceof AEmergencyTrial);
    }

    public Integer popLocation(int playerID) {
        return returnLocations.remove(playerID);
    }

    public long getExpGain(int level) {
        return (long) ((ExpTable.getExpNeededForLevel(level) * 0.35) + bonusExperience);
    }

    private void returnPlayers(MapleChannel channel) {
        MapleMap map = channel.getMap(BossMapID);
        for (Integer playerID : new ArrayList<>(returnLocations.values())) {
            MapleCharacter player = map.getCharacterById(playerID);
            if (player != null) {
                playerUnregistered(player);
            }
        }
        channel.removeMap(BossMapID);
    }

    private void summonBoss() {
        for (MapleChannel channel : getWorld().getChannels()) {
            final MapleMap map = channel.getMap(BossMapID);
            MapleMonster monster = MapleLifeFactory.getMonster(BossID);
            if (monster != null) {
                TaskExecutor.createTask(new Runnable() {
                    @Override
                    public void run() {
                        monster.getListeners().clear();
                        map.killAllMonsters();
                        if (monster.isAlive()) {
                            // 100% - HP loss as percentage
                            long dealt = 100 - (((100 / monster.getMaxHp()) * monster.getHp()) / 100);
                            // give exp based on damage done to boss
                            bonusExperience = (monster.getExp() * dealt);
                        }
                        summonEndNPC(map);
                    }
                }, 1000 * 120);

                monster.getListeners().add(new MonsterListener() {
                    @Override
                    public void monsterKilled(MapleMonster monster, MapleCharacter player) {
                        map.broadcastMessage(MaplePacketCreator.showEffect("PSO2/stuff/5"));
                        map.broadcastMessage(MaplePacketCreator.playSound("PSO2/Completed"));
                        summonEndNPC(map);
                        bonusExperience += monster.getExp() * 1.35;
                    }
                });
                map.broadcastMessage(MaplePacketCreator.getClock(120));
                map.spawnMonsterOnGroudBelow(monster, new Point(528, 117));
            } else {
                stop();
            }
        }
    }

    private void despawnDoors() {
        for (Map.Entry<Integer, Integer> entry : spawnLocations.entrySet()) {
            for (MapleChannel channel : getWorld().getChannels()) {
                MapleMap map = channel.getMap(entry.getKey());
                MapleMapObject object = map.removeMapObject(entry.getValue());
                if (object != null) {
                    map.broadcastMessage(MaplePacketCreator.removeNPC(object.getObjectId()));
                }
            }
        }
    }

    private void summonEndNPC(MapleMap map) {
        MapleNPC npc = MapleLifeFactory.getNPC(EndNpc);
        npc.setScript("f_emergency_trial_end");
        map.addMapObject(npc);
        map.broadcastMessage(createNpc(npc, new Point(125, 117)));
    }

    private long getTimeLeft() {
        return endTimestamp - System.currentTimeMillis();
    }
}
