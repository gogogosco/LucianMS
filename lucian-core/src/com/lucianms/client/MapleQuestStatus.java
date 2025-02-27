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
package com.lucianms.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.lucianms.server.quest.MapleQuest;
import tools.StringUtil;

/**
 *
 * @author Matze
 */
public class MapleQuestStatus {
    public enum Status {
        UNDEFINED(-1),
        NOT_STARTED(0),
        STARTED(1),
        COMPLETED(2);
        final int status;

        private Status(int id) {
            status = id;
        }

        public int getId() {
            return status;
        }

        public static Status getById(int id) {
            for (Status l : Status.values()) {
                if (l.getId() == id) {
                    return l;
                }
            }
            return null;
        }
    }
    private short questID;
    private Status status;
    private Map<Integer, String> progress = new LinkedHashMap<Integer, String>();
    private List<Integer> medalProgress = new LinkedList<Integer>();
    private int npc;
    private long completionTime;
    private int forfeited = 0;

    public MapleQuestStatus(MapleQuest quest, Status status) {
        this.questID = quest.getId();
        this.setStatus(status);
        this.completionTime = System.currentTimeMillis();
        if (status == Status.STARTED) 
            registerMobs();      
    }

    public MapleQuestStatus(MapleQuest quest, Status status, int npc) {
        this.questID = quest.getId();
        this.setStatus(status);
        this.setNpc(npc);
        this.completionTime = System.currentTimeMillis();
        if (status == Status.STARTED) {
            registerMobs();
        }
    }

    public MapleQuest getQuest() {
        return MapleQuest.getInstance(questID);
    }
	
	public short getQuestID() {
		return questID;
	}

    public Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public int getNpc() {
        return npc;
    }

    public final void setNpc(int npc) {
        this.npc = npc;
    }

    private void registerMobs() {
        for (int i : MapleQuest.getInstance(questID).getRelevantMobs()) {
            progress.put(i, "000");
        }
    }

    public boolean addMedalMap(int mapid) {
        if (medalProgress.contains(mapid)) return false;
        medalProgress.add(mapid);
        return true;
    }

    public int getMedalProgress() {
        return medalProgress.size();
    }

    public List<Integer> getMedalMaps() {
        return medalProgress;
    }

    public boolean progress(int id) {
        if (progress.get(id) != null) {
            int current = Integer.parseInt(progress.get(id));
            String str = StringUtil.getLeftPaddedStr(Integer.toString(current + 1), '0', 3);
            progress.put(id, str);
            return true;
        }
        return false;
    }

    public void setProgress(int id, String pr) {
        	progress.put(id, pr);
    }

    public boolean madeProgress() {
        return progress.size() > 0;
    }

    public String getProgress(int id) {
        if (progress.get(id) == null) return "";
        return progress.get(id);
    }

    public Map<Integer, String> getProgress() {
        return Collections.unmodifiableMap(progress);
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getForfeited() {
        return forfeited;
    }
    
    public String getInfo() {
        if(!progress.containsKey(0) && !getMedalMaps().isEmpty()) {
            return Integer.toString(getMedalProgress());
        }
        return getProgress(0);
    }
    
    public void setInfo(String newInfo) {
        progress.put(0, newInfo);
    }

    public void setForfeited(int forfeited) {
        if (forfeited >= this.forfeited) {
            this.forfeited = forfeited;
        } else {
            throw new IllegalArgumentException("Can't set forfeits to something lower than before.");
        }
    }

    public String getQuestData() {
        StringBuilder str = new StringBuilder();
        for (String ps : progress.values()) {
            str.append(ps);
        }
        return str.toString();
    }
}