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
package com.lucianms.events;

import com.lucianms.client.*;
import com.lucianms.client.meta.Occupation;
import com.lucianms.constants.skills.Bishop;
import com.lucianms.constants.skills.Evan;
import com.lucianms.constants.skills.FPArchMage;
import com.lucianms.constants.skills.ILArchMage;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.BuffContainer;
import com.lucianms.server.MapleStatEffect;
import com.lucianms.server.life.FakePlayer;
import tools.MaplePacketCreator;

public class PlayerDealDamageMagicEvent extends AbstractDealDamageEvent {

    private AttackInfo attackInfo;

    public PlayerDealDamageMagicEvent() {
        onPost(new Runnable() {
            @Override
            public void run() {
                MapleCharacter player = getClient().getPlayer();
                Occupation occupation = player.getOccupation();
                if (occupation != null && occupation.getType() == Occupation.Type.Undead
                        && attackInfo != null && !attackInfo.allDamage.isEmpty()) {
                    int gain = (int) (player.getMaxHp() * Math.random() * 0.07 + 0.03);
                    player.addHP(gain);
                    player.updateSingleStat(MapleStat.HP, player.getHp());
                }
            }
        });
    }

    @Override
    public void processInput(MaplePacketReader reader) {
        attackInfo = parseDamage(reader, false, true);
        if (attackInfo == null) {
            setCanceled(true);
        }
    }

    @Override
    public Object onPacket() {
        MapleCharacter player = getClient().getPlayer();
        BuffContainer morph = player.getEffects().get(MapleBuffStat.MORPH);
        if (morph != null && morph.getEffect().isMorphWithoutAttack()) {
            return null;
        }

        byte[] packet = MaplePacketCreator.magicAttack(player, attackInfo.skill, attackInfo.skillLevel, attackInfo.stance, attackInfo.numAttackedAndDamage, attackInfo.allDamage, -1, attackInfo.speed, attackInfo.direction, attackInfo.display);
        if (attackInfo.skill == Evan.FIRE_BREATH || attackInfo.skill == Evan.ICE_BREATH || attackInfo.skill == FPArchMage.BIG_BANG || attackInfo.skill == ILArchMage.BIG_BANG || attackInfo.skill == Bishop.BIG_BANG) {
            packet = MaplePacketCreator.magicAttack(player, attackInfo.skill, attackInfo.skillLevel, attackInfo.stance, attackInfo.numAttackedAndDamage, attackInfo.allDamage, attackInfo.charge, attackInfo.speed, attackInfo.direction, attackInfo.display);
        }

        FakePlayer fakePlayer = player.getFakePlayer();
        if (fakePlayer != null && fakePlayer.isFollowing()) {
            TaskExecutor.createTask(() -> fakePlayer.getMap().broadcastMessage(fakePlayer, MaplePacketCreator.magicAttack(fakePlayer, attackInfo.skill, attackInfo.skillLevel, attackInfo.stance, attackInfo.numAttackedAndDamage, attackInfo.allDamage, attackInfo.charge, attackInfo.speed, attackInfo.direction, attackInfo.display), false, true), 100);
        }

        player.getMap().broadcastMessage(player, packet, false, true);
        MapleStatEffect effect = attackInfo.getAttackEffect(player, null);
        if (effect == null) {
            getLogger().warn("Skill {} has no data for level {}", attackInfo.skill, attackInfo.skillLevel);
            return null;
        }
        addCooldown(attackInfo);

        applyAttack(player, attackInfo, effect.getAttackCount());
        if (fakePlayer != null) {
            applyAttack(fakePlayer, attackInfo, effect.getAttackCount());
        }
        Skill eaterSkill = SkillFactory.getSkill((player.getJob().getId() - (player.getJob().getId() % 10)) * 10000);// MP Eater, works with right job
        if (eaterSkill != null) {
            int eaterLevel = player.getSkillLevel(eaterSkill);
            if (eaterLevel > 0) {
                for (Integer singleDamage : attackInfo.allDamage.keySet()) {
                    eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage), 0);
                }
            }
        }
        return null;
    }

    public AttackInfo getAttackInfo() {
        return attackInfo;
    }
}
