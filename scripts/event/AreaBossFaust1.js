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
/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Faust1 Spawner
-- Edited by --------------------------------------------------------------------------------------
	ThreeStep (based on xQuasar's King Clang spawner)

**/
const nFieldID = 100040105;

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 0);
}

function cancelSchedule() {
    if (setupTask != null)
        setupTask.cancel();
}

function start() {
    if (em.getChannel().isMapLoaded(nFieldID)) {
        var theForestOfEvil1 = em.getChannel().getMap(nFieldID);
        var faust1 = Packages.server.life.MapleLifeFactory.getMonster(5220002);

        if(theForestOfEvil1.getMonsterById(5220002) != null) {
            em.schedule("start", 3 * 60 *60 * 1000);
            return;
        }

        theForestOfEvil1.spawnMonsterOnGroundBelow(faust1, new Packages.java.awt.Point(456, 278));
        theForestOfEvil1.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "Faust appeared amidst the blue fog."));
    }
	em.schedule("start", 3 * 60 *60 * 1000);
}
