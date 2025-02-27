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

package com.lucianms.server.expeditions;

import com.lucianms.client.MapleCharacter;
import com.lucianms.scheduler.Task;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.maps.MapleMap;
import tools.MaplePacketCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SharpAceX(Alan)
 */
@Deprecated
public class MapleExpedition {

    private static final int[] EXPEDITION_BOSSES = {
            8800000, // Zakum's first body
            8800001, // Zakum's second body
            8800002, // Zakum's third body
            8800003, // Zakum's Arm 1
            8800004, // Zakum's Arm 2
            8800005, // Zakum's Arm 3
            8800006, // Zakum's Arm 4
            8800007, // Zakum's Arm 5
            8800008, // Zakum's Arm 6
            8800009, // Zakum's Arm 7
            8800010, // Zakum's Arm 8
            8810000, // Horntail's Left Head
            8810001, // Horntail's Right Head
            8810002, // Horntail's Head A
            8810003, // Horntail's Head B
            8810004, // Horntail's Head C
            8810005, // Horntail's Left Hand
            8810006, // Horntail's Right Hand
            8810007, // Horntail's Wings
            8810008, // Horntail's Legs
            8810009, // Horntail's Tails
            9420546, // Scarlion Boss
            9420547, // Scarlion Boss
            9420548, // Angry Scarlion Boss
            9420549, // Furious Scarlion Boss
            9420541, // Targa
            9420542, // Targa
            9420543, // Angry Targa
            9420544, // Furious Targa
    };

    private MapleCharacter leader;
    private MapleExpeditionType type;
    private boolean registering;
    private MapleMap startMap;
    private Task task;
    private List<MapleCharacter> members = new ArrayList<>();
    private List<MapleCharacter> banned = new ArrayList<>();
    private long startTime;

    public MapleExpedition(MapleCharacter player, MapleExpeditionType met) {
        leader = player;
        members.add(leader);
        startMap = player.getMap();
        type = met;
        beginRegistration();
    }

    private void beginRegistration() {
        registering = true;
        startMap.broadcastMessage(MaplePacketCreator.getClock(type.getRegistrationTime() * 60));
        startMap.broadcastMessage(MaplePacketCreator.serverNotice(6, leader.getName() + " has been declared the expedition captain. Please register for the expedition."));
        scheduleRegistrationEnd();
    }

    private void scheduleRegistrationEnd() {
        final MapleExpedition exped = this;
        task = TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                if (registering) {
                    leader.getClient().getChannelServer().getExpeditions().remove(exped);
                    startMap.broadcastMessage(MaplePacketCreator.serverNotice(6, "Time limit has been reached. Expedition has been disbanded."));
                }
                dispose();
            }
        }, type.getRegistrationTime() * 60 * 1000);
    }

    public void dispose() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void start() {
        registering = false;
        startMap.broadcastMessage(MaplePacketCreator.removeClock());
        broadcastExped(MaplePacketCreator.serverNotice(6, "The expedition has started! The expedition leader is waiting inside!"));
        startTime = System.currentTimeMillis();
    }

    public String addMember(MapleCharacter player) {
        if (!registering) {
            return "Sorry, this expedition is already underway. Registration is closed!";
        }
        if (banned.contains(player)) {
            return "Sorry, you've been banned from this expedition by #b" + leader.getName() + "#k.";
        }
        if (members.size() >= type.getMaxSize()) { //Would be a miracle if anybody ever saw this
            return "Sorry, this expedition is full!";
        }
        if (members.add(player)) {
            broadcastExped(MaplePacketCreator.serverNotice(6, player.getName() + " has joined the expedition!"));
            return "You have registered for the expedition successfully!";
        }
        return "Sorry, something went really wrong. Report this on the forum with a screenshot!";
    }

    private void broadcastExped(byte[] data) {
        for (MapleCharacter member : members) {
            member.getClient().announce(data);
        }
    }

    public boolean removeMember(MapleCharacter chr) {
        return members.remove(chr);
    }

    public MapleExpeditionType getType() {
        return type;
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public MapleCharacter getLeader() {
        return leader;
    }

    public boolean contains(MapleCharacter player) {
        for (MapleCharacter member : members) {
            if (member.getId() == player.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLeader(MapleCharacter player) {
        return leader.equals(player);
    }

    public boolean isRegistering() {
        return registering;
    }

    public boolean isInProgress() {
        return !registering;
    }

    public void ban(MapleCharacter player) {
        if (!banned.contains(player)) {
            banned.add(player);
            members.remove(player);
        }
    }

    public long getStartTime() {
        return startTime;
    }
}
