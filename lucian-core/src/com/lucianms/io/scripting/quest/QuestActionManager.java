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
package com.lucianms.io.scripting.quest;

import com.lucianms.client.MapleClient;
import com.lucianms.io.scripting.npc.NPCConversationManager;
import com.lucianms.server.quest.MapleQuest;

/**
 *
 * @author RMZero213
 */
public class QuestActionManager extends NPCConversationManager {
    private boolean start; // this is if the script in question is start or end
    private int quest;

    public QuestActionManager(MapleClient c, int quest, int npc, boolean start) {
        super(c, 0, npc, null);
        this.quest = quest;
        this.start = start;
    }

    public int getQuest() {
        return quest;
    }

    public boolean isStart() {
        return start;
    }

    @Override
    public void dispose() {
        QuestScriptManager.dispose(this, getClient());
    }

    public boolean forceStartQuest() {
        return forceStartQuest(quest);
    }

    public boolean forceStartQuest(int id) {
        return MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc());
    }

    public boolean forceCompleteQuest() {
        return forceCompleteQuest(quest);
    }
    
    // For compatability with some older scripts...
    public void startQuest() {
        forceStartQuest();
    }
    
    // For compatability with some older scripts...
    public void completeQuest() {
        forceCompleteQuest();
    }

    public boolean forceCompleteQuest(int id) {
        return MapleQuest.getInstance(id).forceComplete(getPlayer(), getNpc());
    }
}
