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
package com.lucianms.server.life;

import com.lucianms.client.DiseaseValueHolder;
import com.lucianms.client.MapleBuffStat;
import com.lucianms.client.MapleCharacter;
import com.lucianms.client.MapleDisease;
import com.lucianms.client.status.MonsterStatus;
import com.lucianms.server.BuffContainer;
import com.lucianms.server.maps.MapleMapObject;
import com.lucianms.server.maps.MapleMapObjectType;
import com.lucianms.server.maps.MapleMist;
import org.slf4j.LoggerFactory;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Randomizer;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Danny (Leifde)
 */
public class MobSkill {

    private int skillId, skillLevel, mpCon;
    private List<Integer> toSummon = new ArrayList<>();
    private int spawnEffect, hp, x, y;
    private long duration, cooltime;
    private float prop;
    private Point lt, rb;
    private int limit;

    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }

    public void addSummons(List<Integer> toSummon) {
        this.toSummon.addAll(toSummon);
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void apply(MapleCharacter player) {
        MapleDisease disease;
        try {
            disease = MapleDisease.valueOf(getSkillId());
        } catch (IllegalArgumentException e) {
            LoggerFactory.getLogger(MobSkill.class).warn("No disease for mob skill {}", getSkillId());
            return;
        }
        long currentTime = System.currentTimeMillis();
        int duration = (int) (getDuration() / 1000);
        player.getDiseases().put(disease, new DiseaseValueHolder(currentTime, getDuration()));

        Map<MapleBuffStat, BuffContainer> buff = Map.of(disease.getBuff(), new BuffContainer(this, null, currentTime, duration));
        player.announce(MaplePacketCreator.setTempStats(buff));
        player.getMap().sendPacketExclude(MaplePacketCreator.setRemoteTempStats(player, buff), player);
    }

    public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill) {
        MapleDisease disease = null;
        Map<MonsterStatus, Integer> stats = new ArrayMap<>();
        List<Integer> reflection = new LinkedList<>();
        switch (skillId) {
            case 100:
            case 110:
            case 150:
                stats.put(MonsterStatus.WEAPON_ATTACK_UP, x);
                break;
            case 101:
            case 111:
            case 151:
                stats.put(MonsterStatus.MAGIC_ATTACK_UP, x);
                break;
            case 102:
            case 112:
            case 152:
                stats.put(MonsterStatus.WEAPON_DEFENSE_UP, x);
                break;
            case 103:
            case 113:
            case 153:
                stats.put(MonsterStatus.MAGIC_DEFENSE_UP, x);
                break;
            case 114:
                if (lt != null && rb != null && skill) {
                    List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                    final int hps = (getX() / 1000) * (int) (950 + 1050 * Math.random());
                    for (MapleMapObject mons : objects) {
                        ((MapleMonster) mons).heal(hps, getY());
                    }
                } else {
                    monster.heal(getX(), getY());
                }
                break;
            case 120:
                disease = MapleDisease.SEAL;
                break;
            case 121:
                disease = MapleDisease.DARKNESS;
                break;
            case 122:
                disease = MapleDisease.WEAKEN;
                break;
            case 123:
                disease = MapleDisease.STUN;
                break;
            case 124:
                disease = MapleDisease.CURSE;
                break;
            case 125:
                disease = MapleDisease.POISON;
                break;
            case 126: // Slow
                disease = MapleDisease.SLOW;
                break;
            case 127:
                if (lt != null && rb != null && skill) {
                    for (MapleCharacter character : getPlayersInRange(monster, player)) {
                        character.dispel();
                    }
                } else {
                    player.dispel();
                }
                break;
            case 128: // Seduce
                disease = MapleDisease.SEDUCE;
                break;
            case 129: // Banish
                if (lt != null && rb != null && skill) {
                    for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                        chr.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
                    }
                } else {
                    player.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
                }
                break;
            case 131: // Mist
                monster.getMap().spawnMist(new MapleMist(calculateBoundingBox(monster.getPosition(), true), monster, this), x * 10, false, false, false);
                break;
            case 132:
                disease = MapleDisease.CONFUSE;
                break;
            case 133: // zombify
                break;
            case 140:
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
                }
                break;
            case 141:
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
                }
                break;
            case 143: // Weapon Reflect
                stats.put(MonsterStatus.WEAPON_REFLECT, x);
                stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
                reflection.add(x);
                break;
            case 144: // Magic Reflect
                stats.put(MonsterStatus.MAGIC_REFLECT, x);
                stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
                reflection.add(x);
                break;
            case 145: // Weapon / Magic reflect
                stats.put(MonsterStatus.WEAPON_REFLECT, x);
                stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
                stats.put(MonsterStatus.MAGIC_REFLECT, x);
                stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
                reflection.add(x);
                break;
            case 154: // accuracy up
            case 155: // avoid up
            case 156: // speed up
                break;
            case 200:
                if (monster.getMap().getSpawnedMonstersOnMap().get() < 80) {
                    for (Integer mobId : getSummons()) {
                        MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
                        toSpawn.setPosition(monster.getPosition());
                        int ypos, xpos;
                        xpos = (int) monster.getPosition().getX();
                        ypos = (int) monster.getPosition().getY();
                        switch (mobId) {
                            case 8500003: // Pap bomb high
                                toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
                                ypos = -590;
                                break;
                            case 8500004: // Pap bomb
                                xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                                if (ypos != -590) {
                                    ypos = (int) monster.getPosition().getY();
                                }
                                break;
                            case 8510100: //Pianus bomb
                                if (Math.ceil(Math.random() * 5) == 1) {
                                    ypos = 78;
                                    xpos = Randomizer.nextInt(5) + (Randomizer.nextInt(2) == 1 ? 180 : 0);
                                } else {
                                    xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                                }
                                break;
                        }
                        switch (monster.getMap().getId()) {
                            case 220080001: //Pap map
                                if (xpos < -890) {
                                    xpos = (int) (Math.ceil(Math.random() * 150) - 890);
                                } else if (xpos > 230) {
                                    xpos = (int) (230 - Math.ceil(Math.random() * 150));
                                }
                                break;
                            case 230040420: // Pianus map
                                if (xpos < -239) {
                                    xpos = (int) (Math.ceil(Math.random() * 150) - 239);
                                } else if (xpos > 371) {
                                    xpos = (int) (371 - Math.ceil(Math.random() * 150));
                                }
                                break;
                        }
                        toSpawn.setPosition(new Point(xpos, ypos));
                        if (toSpawn.getId() == 8500004) {
                            monster.getMap().spawnFakeMonster(toSpawn);
                        } else {
                            monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), toSpawn.getPosition());
                        }

                    }
                }
                break;
            default:
                System.out.println("Unhandeled Mob skill: " + skillId);
                break;
        }
        if (stats.size() > 0) {
            if (lt != null && rb != null && skill) {
                for (MapleMapObject mons : getObjectsInRange(monster, MapleMapObjectType.MONSTER)) {
                    ((MapleMonster) mons).applyMonsterBuff(stats, getX(), getSkillId(), getDuration(), this, reflection);
                }
            } else {
                monster.applyMonsterBuff(stats, getX(), getSkillId(), getDuration(), this, reflection);
            }
        }
        if (disease != null) {
            if (lt != null && rb != null && skill) {
                int i = 0;
                for (MapleCharacter character : getPlayersInRange(monster, player)) {
                    if (!character.isActiveBuffedValue(2321005)) {
                        if (disease == MapleDisease.SEDUCE) {
                            if (i < 10) {
                                character.giveDebuff(MapleDisease.SEDUCE, this);
                                i++;
                            }
                        } else {
                            character.giveDebuff(disease, this);
                        }
                    }
                }
            } else {
                player.giveDebuff(disease, this);
            }
        }
        monster.usedSkill(skillId, skillLevel, cooltime);
        monster.setMp(monster.getMp() - getMpCon());
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        List<MapleCharacter> players = new ArrayList<>();
        players.add(player);
        return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), players);
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMpCon() {
        return mpCon;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(toSummon);
    }

    public int getSpawnEffect() {
        return spawnEffect;
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public int getHP() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCoolTime() {
        return cooltime;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public Point getLt() {
        return lt;
    }

    public Point getRb() {
        return rb;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        int multiplier = facingLeft ? 1 : -1;
        Point mylt = new Point(lt.x * multiplier + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x * multiplier + posFrom.x, rb.y + posFrom.y);
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        List<MapleMapObjectType> objectTypes = new ArrayList<>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), objectTypes);
    }
}
