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
/*
	Map(s): 		Empress' Road : Training Forest I
	Description: 		Takes you to Tiv's Forest
*/

importPackage(Packages.server.life);

function enter(pi) {
	if(pi.isQuestStarted(20301) || pi.isQuestStarted(20302) || pi.isQuestStarted(20303) || pi.isQuestStarted(20304) || pi.isQuestStarted(20305)) {
		var map = pi.getClient().getChannelServer().getMap(108010610);
		spawnMob(3345, -452, 9001009, map);
		pi.warp(108010610, "out00");
	} else {
		pi.warp(130010020, "out00");
	}
	return true;
}

function spawnMob(x, y, id, map) {
	if(map.getMonsterById(id) != null)
		return;

	var mob = MapleLifeFactory.getMonster(id);
	map.spawnMonsterOnGroudBelow(mob, new java.awt.Point(x, y));
}
