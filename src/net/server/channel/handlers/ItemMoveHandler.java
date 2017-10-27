/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.server.channel.handlers;

import client.MapleCharacter;
import client.autoban.Cheater;
import client.autoban.Cheats;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import net.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 */
public class ItemMoveHandler extends PacketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemMoveHandler.class);

    private MapleInventoryType inventoryType;
    private short source;
    private short action;
    private short quantity;


    @Override
    public void process(SeekableLittleEndianAccessor slea) {
        slea.skip(4);
        inventoryType = MapleInventoryType.getByType(slea.readByte());
        source = slea.readShort();
        action = slea.readShort();
        quantity = slea.readShort();
    }

    @Override
    public Object onPacket() {
        MapleCharacter player = getClient().getPlayer();
        Cheater.CheatEntry entry = player.getCheater().getCheatEntry(Cheats.FastInventorySort);

        if (System.currentTimeMillis() - entry.latestOperationTimestamp < 300) {
            entry.spamCount++;
            getClient().announce(MaplePacketCreator.enableActions());
            return null;
        } else {
            entry.spamCount = 0;
        }

        if (source < 0 && action > 0) {
            MapleInventoryManipulator.unequip(getClient(), source, action);
        } else if (action < 0) {
            MapleInventoryManipulator.equip(getClient(), source, action);
        } else if (action == 0) {
            return MapleInventoryManipulator.drop(getClient(), inventoryType, source, quantity);
        } else {
            MapleInventoryManipulator.move(getClient(), inventoryType, source, action);
        }
        return null;
    }

    public MapleInventoryType getInventoryType() {
        return inventoryType;
    }

    public short getSource() {
        return source;
    }

    public short getAction() {
        return action;
    }

    public short getQuantity() {
        return quantity;
    }
}