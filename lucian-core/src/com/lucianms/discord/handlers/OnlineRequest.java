package com.lucianms.discord.handlers;

import com.lucianms.client.MapleCharacter;
import com.lucianms.discord.DiscordConnection;
import com.lucianms.discord.Headers;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.server.Server;
import com.lucianms.server.channel.MapleChannel;
import com.lucianms.server.world.MapleWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.Collection;
import java.util.List;

/**
 * @author izarooni
 */
public class OnlineRequest extends DiscordRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineRequest.class);

    @Override
    public void handle(MaplePacketReader reader) {
        List<MapleWorld> worlds = Server.getWorlds();

        MaplePacketLittleEndianWriter writer = new MaplePacketLittleEndianWriter();
        writer.write(Headers.Online.value);
        writer.writeLong(reader.readLong()); // Channel_ID
        writer.write(worlds.size());
        for (MapleWorld world : worlds) {
            List<MapleChannel> channels = world.getChannels();
            writer.write(channels.size());
            for (MapleChannel channel : channels) {
                Collection<MapleCharacter> players = world.getPlayers(p -> p.getClient().getChannel() == channel.getId());
                writer.writeShort(players.size());
                for (MapleCharacter player : players) {
                    writer.writeMapleAsciiString(player.getName());
                }
                players.clear();
            }
        }

        DiscordConnection.sendPacket(writer.getPacket());
    }
}
