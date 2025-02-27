package com.lucianms.features.carnival;

import com.lucianms.scheduler.Task;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.channel.MapleChannel;
import com.lucianms.server.maps.MapleMap;
import com.lucianms.server.world.MapleParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;
import tools.Randomizer;

import java.util.Optional;

/**
 * @author izarooni
 */
public class MCarnivalLobby {

    public enum State {
        Available, // Lobby is empty
        Waiting,  // Lobby is waiting for opponents
        Starting, // Lobby is about to begin
        InProgress // Game started
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MCarnivalLobby.class);
    private static final int M_Office = 980000000;

    private final MapleChannel channel;
    private final int maxPartySize;
    private final int mapId;

    private MCarnivalGame carnivalGame = null;
    private State state = State.Available;

    private MapleParty party1 = null;
    private MapleParty party2 = null;

    private Task waitingTask;

    public MCarnivalLobby(MapleChannel channel, int maxPartySize, int mapId) {
        this.channel = channel;
        this.maxPartySize = maxPartySize;
        this.mapId = mapId;
    }

    private void broadcastPacket(byte[] packet) {
        if (party1 != null) {
            party1.sendPacket(packet);
        }
        if (party2 != null) {
            party2.sendPacket(packet);
        }
    }

    public MapleChannel getChannel() {
        return channel;
    }

    public int getMaxPartySize() {
        return maxPartySize;
    }

    public int getMapId() {
        return mapId;
    }

    public int getBattlefieldMapId() {
        return getMapId() + 1;
    }

    public int getVictoryMapId() {
        return getMapId() + 3;
    }

    public int getDefeatedMapId() {
        return getMapId() + 4;
    }

    public Task getWaitingTask() {
        return waitingTask;
    }

    public void setWaitingTask(Task waitingTask) {
        this.waitingTask = waitingTask;
    }

    public MCarnivalGame getGame() {
        return carnivalGame;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        switch (this.state) {
            case InProgress: {
                if (waitingTask != null) {
                    waitingTask.cancel();
                }
                MCarnivalGame carnivalGame = createGame();
                party1.forEachPlayer(carnivalGame::registerPlayer);
                party2.forEachPlayer(carnivalGame::registerPlayer);

                MapleMap map = channel.getMap(getBattlefieldMapId());
                MonsterCarnival carnival = map.getMonsterCarnival();
                final long timeFinish = carnival.getTimeFinish() * 1000;

                waitingTask = TaskExecutor.createTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MCarnivalTeam teamBlue = carnivalGame.getTeamBlue();
                            MCarnivalTeam teamRed = carnivalGame.getTeamRed();
                            boolean blueWinner = teamBlue.getTotalCarnivalPoints() > teamRed.getTotalCarnivalPoints();
                            carnivalGame.broadcastPacket(blueWinner ? teamBlue : teamRed, MaplePacketCreator.mapSound(carnival.getSoundWin()));
                            carnivalGame.broadcastPacket(blueWinner ? teamBlue : teamRed, MaplePacketCreator.mapEffect(carnival.getEffectWin()));
                            carnivalGame.broadcastPacket(blueWinner ? teamRed : teamBlue, MaplePacketCreator.mapSound(carnival.getSoundLose()));
                            carnivalGame.broadcastPacket(blueWinner ? teamRed : teamBlue, MaplePacketCreator.mapEffect(carnival.getEffectLose()));
                        } catch (Exception e) {
                            LOGGER.info("Failed to show game results", e);
                        }
                        carnivalGame.broadcastPacket(null, MaplePacketCreator.getClock(carnival.getTimeFinish()));
                        TaskExecutor.createTask(new Runnable() {
                            @Override
                            public void run() {
                                setState(State.Available);
                                party1.forEachPlayer(carnivalGame::unregisterPlayer);
                                party2.forEachPlayer(carnivalGame::unregisterPlayer);
                            }
                        }, timeFinish);
                    }
                }, carnival.getTimeDefault() * 1000);
                break;
            }
            case Starting: {
                waitingTask = TaskExecutor.cancelTask(waitingTask);
                broadcastPacket(MaplePacketCreator.getClock(10));
                // party 2 clock via npc
                waitingTask = TaskExecutor.createTask(() -> setState(State.InProgress), 10000);
                break;
            }
            case Waiting: {
                waitingTask = TaskExecutor.cancelTask(waitingTask);
                broadcastPacket(MaplePacketCreator.getClock(300));
                waitingTask = TaskExecutor.createTask(() -> setState(State.Available), 60000 * 5); // 5 minutes
                break;
            }
            case Available: {
                MapleMap room = channel.removeMap(getMapId());// resets
                if (room != null) {
                    room.forEachPlayer(p -> p.changeMap(M_Office));
                }
                waitingTask = TaskExecutor.cancelTask(waitingTask);
                Optional.ofNullable(carnivalGame).ifPresent(MCarnivalGame::dispose);
                party1 = party2 = null;
                break;
            }
        }
    }

    /**
     * @param party the party to add to the lobby
     * @return trye if both party slots in the lobby are filled, false otherwise
     */
    public boolean joiningParty(MapleParty party) {
        if (party1 == null) {
            party1 = party;
        } else {
            party2 = party;
        }
        party.getPlayers().forEach(p -> p.changeMap(getMapId()));
        return party1 != null && party2 != null;
    }

    /**
     * Used in scripts to remove a party from the waiting room
     *
     * @param party the party to remove
     * @return if both parties are non-existing
     */
    public boolean removeParty(MapleParty party) {
        if (party1 != null && party1.getID() == party.getID()) {
            // if the lobby creator leaves, both parties must leave
            party1 = null;
            if (party2 != null) {
                party.getPlayers().forEach(p -> p.changeMap(M_Office));
            }
            party2 = null;
        } else if (party2 != null && party2.getID() == party.getID()) {
            party2 = null;
            // warp the party that leaves
            party.getPlayers().forEach(p -> p.changeMap(M_Office));
        }
        return party1 == null && party2 == null;
    }

    /**
     * First entering party must have at least 1 party member but may not exceed maximum party size
     * <p>
     * Second entering party must have member count equal to the first entered party
     * </p>
     *
     * @param party the party attempt to enter the lobby
     * @return true if the party may enter, false otherwise
     */
    public boolean canEnter(MapleParty party) {
        if (party1 == null && party2 == null) {
            return party.size() <= maxPartySize;
        }
        MapleParty nn = party1 == null ? party2 : party1;
        return nn.size() == party.size();
    }

    private MCarnivalGame createGame() {
        carnivalGame = new MCarnivalGame(this);
        boolean red = Randomizer.nextBoolean();
        carnivalGame.setTeamRed(new MCarnivalTeam(0, red ? party1 : party2));
        carnivalGame.setTeamBlue(new MCarnivalTeam(1, red ? party2 : party1));
        return carnivalGame;
    }
}
