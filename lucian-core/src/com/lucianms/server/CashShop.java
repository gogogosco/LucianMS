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
package com.lucianms.server;

import com.lucianms.client.inventory.*;
import com.lucianms.constants.ItemConstants;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author Flav
 */
public class CashShop {

    public static class CashItem {

        private String imgdir;
        private int sn, itemId, price;
        private long period;
        private short count;
        private boolean onSale;

        private CashItem(String imgdir, int sn, int itemId, int price, long period, short count, boolean onSale) {
            this.imgdir = imgdir;
            this.sn = sn;
            this.itemId = itemId;
            this.price = price;
            this.period = (period == 0 ? 90 : period);
            this.count = count;
            this.onSale = onSale;
        }

        public String getImgdir() {
            return imgdir;
        }

        public int getSN() {
            return sn;
        }

        public int getItemId() {
            return itemId;
        }

        public int getPrice() {
            return price;
        }

        public short getCount() {
            return count;
        }

        public boolean isOnSale() {
            return onSale;
        }

        public Item toItem() {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Item item;

            int petid = -1;

            if (ItemConstants.isPet(itemId)) {
                petid = MaplePet.createPet(itemId);
            }

            if (ItemConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                item = ii.getEquipById(itemId);
                if (item == null) {
                    return new Equip(itemId);
                }
            } else {
                item = new Item(itemId, (byte) 0, count, petid);
            }

            if (ItemConstants.EXPIRING_ITEMS) {
                if (itemId == 5211048 || itemId == 5360042) { // 4 Hour 2X coupons, the period is 1, but we don't want them to last a day.
                    item.setExpiration(System.currentTimeMillis() + (1000 * 60 * 60 * 4));
                } else {
                    item.setExpiration(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * period));
                }
            }
            item.setSN(sn);
            return item;
        }
    }

    public static class SpecialCashItem {

        private int sn, modifier;
        private byte info; //?

        public SpecialCashItem(int sn, int modifier, byte info) {
            this.sn = sn;
            this.modifier = modifier;
            this.info = info;
        }

        public int getSN() {
            return sn;
        }

        public int getModifier() {
            return modifier;
        }

        public byte getInfo() {
            return info;
        }
    }

    public static class CashItemFactory {

        private static final Map<Integer, CashItem> items = new HashMap<>();
        private static final Map<Integer, List<Integer>> packages = new HashMap<>();
        private static final List<SpecialCashItem> specialcashitems = new ArrayList<>();

        public static void loadCommodities() {
            items.clear();
            packages.clear();
            specialcashitems.clear();

            MapleDataProvider etc = MapleDataProviderFactory.getWZ("Etc.wz");

            for (MapleData item : etc.getData("Commodity.img").getChildren()) {
                String imgdir = item.getName();
                int sn = MapleDataTool.getIntConvert("SN", item);
                int itemId = MapleDataTool.getIntConvert("ItemId", item);
                int price = MapleDataTool.getIntConvert("Price", item, 0);
                long period = MapleDataTool.getIntConvert("Period", item, 1);
                short count = (short) MapleDataTool.getIntConvert("Count", item, 1);
                boolean onSale = MapleDataTool.getIntConvert("OnSale", item, 0) == 1;
                items.put(sn, new CashItem(imgdir, sn, itemId, price, period, count, onSale));
            }
            for (MapleData cashPackage : etc.getData("CashPackage.img").getChildren()) {
                List<Integer> cPackage = new ArrayList<>();

                for (MapleData item : cashPackage.getChildByPath("SN").getChildren()) {
                    cPackage.add(Integer.parseInt(item.getData().toString()));
                }

                packages.put(Integer.parseInt(cashPackage.getName()), cPackage);
            }
            try (Connection con = Server.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM specialcashitems")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        specialcashitems.add(new SpecialCashItem(rs.getInt("sn"), rs.getInt("modifier"), rs.getByte("info")));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public static CashItem getItem(int sn) {
            return items.get(sn);
        }

        public static List<Item> getPackage(int itemId) {
            List<Item> cashPackage = new ArrayList<>();

            for (int sn : packages.get(itemId)) {
                cashPackage.add(getItem(sn).toItem());
            }

            return cashPackage;
        }

        public static boolean isPackage(int itemId) {
            return packages.containsKey(itemId);
        }

        public static List<SpecialCashItem> getSpecialCashItems() {
            return specialcashitems;
        }
    }

    private int accountId, characterId, nxCredit, maplePoint, nxPrepaid;
    private boolean opened;
    private ItemFactory factory;
    private List<Item> inventory = new ArrayList<>();
    private List<Integer> wishList = new ArrayList<>();
    private int notes = 0;

    public CashShop(int accountId, int characterId, int jobType) throws SQLException {
        this.accountId = accountId;
        this.characterId = characterId;

        if (jobType == 0 || jobType == 4) {
            factory = ItemFactory.CASH_EXPLORER;
        } else if (jobType == 1) {
            factory = ItemFactory.CASH_CYGNUS;
        } else if (jobType == 2) {
            factory = ItemFactory.CASH_ARAN;
        } else {
            throw new NullPointerException("Unknown job type" + jobType);
        }

        try (Connection con = Server.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT `nxCredit`, `maplePoint`, `nxPrepaid` FROM `accounts` WHERE `id` = ?")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        this.nxCredit = rs.getInt("nxCredit");
                        this.maplePoint = rs.getInt("maplePoint");
                        this.nxPrepaid = rs.getInt("nxPrepaid");
                    }
                }
            }

            List<Pair<Item, MapleInventoryType>> pairs = factory.loadItems(con, accountId, false);
            for (Pair<Item, MapleInventoryType> item : pairs) {
                inventory.add(item.getLeft());
            }
            pairs.clear();

            try (PreparedStatement ps = con.prepareStatement("SELECT `sn` FROM `wishlists` WHERE `charid` = ?")) {
                ps.setInt(1, characterId);
                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        wishList.add(rs.getInt("sn"));
                    }
                }
            }
        }
    }

    public int getCash(int type) {
        switch (type) {
            case 1:
                return nxCredit;
            case 2:
                return maplePoint;
            case 4:
                return nxPrepaid;
        }

        return 0;
    }

    public void gainCash(int type, int cash) {
        switch (type) {
            case 1:
                nxCredit += cash;
                break;
            case 2:
                maplePoint += cash;
                break;
            case 4:
                nxPrepaid += cash;
                break;
        }
    }

    public boolean isOpened() {
        return opened;
    }

    public void open(boolean b) {
        opened = b;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public Item findByCashId(int cashId) {
        boolean isRing = false;
        Equip equip = null;
        for (Item item : inventory) {
            if (item.getType() == 1) {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            }
            if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId) {
                return item;
            }
        }

        return null;
    }

    public void addToInventory(Item item) {
        inventory.add(item);
    }

    public void removeFromInventory(Item item) {
        inventory.remove(item);
    }

    public List<Integer> getWishList() {
        return wishList;
    }

    public void clearWishList() {
        wishList.clear();
    }

    public void addToWishList(int sn) {
        wishList.add(sn);
    }

    public void gift(int recipient, String from, String message, int sn) {
        gift(recipient, from, message, sn, -1);
    }

    public void gift(int recipient, String from, String message, int sn, int ringID) {
        try (Connection con = Server.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, recipient);
            ps.setString(2, from);
            ps.setString(3, message);
            ps.setInt(4, sn);
            ps.setInt(5, ringID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Pair<Item, String>> loadGifts() {
        List<Pair<Item, String>> gifts = new ArrayList<>();

        try (Connection con = Server.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `to` = ?")) {
                ps.setInt(1, characterId);
                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        notes++;
                        CashItem cItem = CashItemFactory.getItem(rs.getInt("sn"));
                        Item item = cItem.toItem();
                        Equip equip = null;
                        item.setGiftFrom(rs.getString("from"));
                        if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                            equip = (Equip) item;
                            equip.setRingId(rs.getInt("ringid"));
                            gifts.add(new Pair<Item, String>(equip, rs.getString("message")));
                        } else {
                            gifts.add(new Pair<>(item, rs.getString("message")));
                        }

                        if (CashItemFactory.isPackage(cItem.getItemId())) { //Packages never contains a ring
                            for (Item packageItem : CashItemFactory.getPackage(cItem.getItemId())) {
                                packageItem.setGiftFrom(rs.getString("from"));
                                addToInventory(packageItem);
                            }
                        } else {
                            addToInventory(equip == null ? item : equip);
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `gifts` WHERE `to` = ?")) {
                ps.setInt(1, characterId);
                ps.executeUpdate();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return gifts;
    }

    public int getAvailableNotes() {
        return notes;
    }

    public void decreaseNotes() {
        notes--;
    }

    public void save(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `nxCredit` = ?, `maplePoint` = ?, `nxPrepaid` = ? WHERE `id` = ?")) {
            ps.setInt(1, nxCredit);
            ps.setInt(2, maplePoint);
            ps.setInt(3, nxPrepaid);
            ps.setInt(4, accountId);
            ps.executeUpdate();
        }
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

        for (Item item : inventory) {
            itemsWithType.add(new Pair<>(item, ItemConstants.getInventoryType(item.getItemId())));
        }

        factory.saveItems(itemsWithType, accountId, con);
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM `wishlists` WHERE `charid` = ?")) {
            ps.setInt(1, characterId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO `wishlists` VALUES (DEFAULT, ?, ?)")) {
            ps.setInt(1, characterId);
            for (int sn : wishList) {
                ps.setInt(2, sn);
                ps.executeUpdate();
            }
        }
    }
}
