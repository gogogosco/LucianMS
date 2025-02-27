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
package com.lucianms.client.inventory;

import tools.Duplicable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Item implements Comparable<Item>, Duplicable<Item> {

    protected List<String> log;
    private int id, cashId, sn;
    private short position;
    private short quantity;
    private int petid = -1;
    private MaplePet pet;
    private String owner = "";
    private byte flag;
    private long expiration = -1;
    private String giftFrom = "";

    private boolean obtainable = true;

    public Item(int id) {
        this(id, (short) 0, (short) 1);
    }

    public Item(int id, short position, short quantity) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.log = new ArrayList<>();
        this.flag = 0;
    }

    public Item(int id, short position, short quantity, int petid) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.petid = petid;
        if (petid > -1) this.pet = MaplePet.loadFromDb(id, position, petid);
        this.flag = 0;
        this.log = new ArrayList<>();
    }

    @Override
    public Item duplicate() {
        if (getPetId() > -1) throw new UnsupportedOperationException("Cannot duplicate an item that is a pet");
        Item item = new Item(getItemId(), getPosition(), getQuantity());
        item.flag = flag;
        item.owner = owner;
        item.expiration = expiration;
        item.log.addAll(log);
        return item;
    }

    public int getItemId() {
        return id;
    }

    public int getCashId() {
        if (cashId == 0) {
            cashId = new Random().nextInt(Integer.MAX_VALUE) + 1;
        }
        return cashId;
    }

    public short getPosition() {
        return position;
    }

    public void setPosition(short position) {
        this.position = position;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public byte getType() {
        if (getPetId() > -1) {
            return 3;
        }
        return 2;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPetId() {
        return petid;
    }

    public void setPetId(int id) {
        this.petid = id;
    }

    @Override
    public int compareTo(Item other) {
        return Integer.compare(getItemId(), other.getItemId());
    }

    @Override
    public String toString() {
        return String.format("Position: %d, Item: %d, Quantity: %s", position, id, quantity);
    }

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte b) {
        this.flag = b;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expire) {
        this.expiration = expire;
    }

    public int getSN() {
        return sn;
    }

    public void setSN(int sn) {
        this.sn = sn;
    }

    public String getGiftFrom() {
        return giftFrom;
    }

    public void setGiftFrom(String giftFrom) {
        this.giftFrom = giftFrom;
    }

    public MaplePet getPet() {
        return pet;
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public void setObtainable(boolean obtainable) {
        this.obtainable = obtainable;
    }
}
