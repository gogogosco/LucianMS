package com.lucianms.features.controllers;

import client.MapleCharacter;
import net.server.channel.handlers.UseItemHandler;
import com.lucianms.features.GenericEvent;
import com.lucianms.features.PlayerBattle;
import server.life.FakePlayer;
import tools.MaplePacketCreator;
import com.lucianms.lang.annotation.PacketWorker;

import java.util.Optional;

/**
 * @author izarooni
 */
public class CloneController extends GenericEvent {

    public CloneController() {
        registerAnnotationPacketEvents(this);
    }

    @Override
    public void registerPlayer(MapleCharacter player) {
    }

    @Override
    public void unregisterPlayer(MapleCharacter player) {
    }

    @PacketWorker
    public void onItemUse(UseItemHandler event) {
        MapleCharacter player = event.getClient().getPlayer();
        FakePlayer fPlayer = player.getFakePlayer();
        if (fPlayer == null) {
            return;
        }
        if (event.getItemId() == 2002002) { // toggle follow
            fPlayer.setFollowing(!fPlayer.isFollowing());
            player.dropMessage("Your clone is " + (fPlayer.isFollowing() ? "now" : "no longer") + " following you");
            event.setCanceled(true);
        } else if (event.getItemId() == 2002001) { // toggle pvp
            Optional<GenericEvent> pvp = fPlayer.getGenericEvents().stream().filter(g -> (g instanceof PlayerBattle)).findFirst();
            if (pvp.isPresent()) {
                fPlayer.removeGenericEvent(pvp.get());
                player.dropMessage("Clone is no longer PvPing");
            } else {
                PlayerBattle battle = new PlayerBattle(fPlayer);
                fPlayer.addGenericEvent(battle);
                player.dropMessage("Clone is now PvPing");
            }
        } else if (event.getItemId() == 2002003) { // unregister
            player.removeGenericEvent(this);
            player.dropMessage("Unregistered!");
            event.setCanceled(true);
        }
        if (event.isCanceled()) {
            player.announce(MaplePacketCreator.enableActions());
        }
    }
}
