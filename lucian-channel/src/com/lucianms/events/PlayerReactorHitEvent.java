package com.lucianms.events;

import com.lucianms.client.MapleCharacter;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.server.maps.MapleReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author izarooni
 */
public class PlayerReactorHitEvent extends PacketEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerReactorHitEvent.class);

    private int objectId;
    private int position;
    private int skillId;

    private short stance;

    @Override
    public void exceptionCaught(MaplePacketReader reader, Throwable t) {
        MapleReactor reactor = getClient().getPlayer().getMap().getReactorById(objectId);
        if (reactor != null) {
            LOGGER.warn("An occurred occurred within the reactor '{}'", reactor.getId());
        }
        super.exceptionCaught(reader, t);
    }

    @Override
    public void processInput(MaplePacketReader reader) {
        objectId = reader.readInt();
        position = reader.readInt();
        stance = reader.readShort();
        reader.skip(4);
        skillId = reader.readInt();
    }

    @Override
    public Object onPacket() {
        MapleCharacter player = getClient().getPlayer();
        MapleReactor reactor = player.getMap().getReactorByOid(objectId);
        if (player.isDebug()) {
            player.sendMessage("Reactor Hit ID: {}, Name: {}, State {}", reactor.getId(), reactor.getName(), reactor.getState());
        }
        if (reactor != null && reactor.isAlive()) {
            reactor.hitReactor(getClient(), position, stance, skillId);
        }
        return null;
    }
}
