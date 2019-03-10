package com.lucianms.nio;

import com.lucianms.events.*;

public enum RecvOpcode {

    // @formatter:off
    LOGIN_PASSWORD          (0x0001, ReceivePacketState.LoginServer),
    GUEST_LOGIN             (0x0002, ReceivePacketState.LoginServer),
    SERVERLIST_REREQUEST    (0x0004, ReceivePacketState.LoginServer),
    CHARLIST_REQUEST        (0x0005, ReceivePacketState.LoginServer),
    SERVERSTATUS_REQUEST    (0x0006, ReceivePacketState.LoginServer),
    ACCEPT_TOS              (0x0007, ReceivePacketState.LoginServer),
    SET_GENDER              (0x0008, ReceivePacketState.LoginServer),
    AFTER_LOGIN             (0x0009, ReceivePacketState.LoginServer),
    REGISTER_PIN            (0x000A, ReceivePacketState.LoginServer),
    SERVERLIST_REQUEST      (0x000B, ReceivePacketState.LoginServer),
    PLAYER_DC               (0x000C, ReceivePacketState.LoginServer),
    VIEW_ALL_CHAR           (0x000D, ReceivePacketState.LoginServer),
    PICK_ALL_CHAR           (0x000E, ReceivePacketState.LoginServer),
    CHAR_SELECT             (0x0013, ReceivePacketState.LoginServer),
    PLAYER_LOGGEDIN         (0x0014, ReceivePacketState.ChannelServer),
    CHECK_CHAR_NAME         (0x0015, ReceivePacketState.LoginServer),
    CREATE_CHAR             (0x0016, ReceivePacketState.LoginServer),
    DELETE_CHAR             (0x0017, ReceivePacketState.LoginServer),
    PONG                    (0x0018, ReceivePacketState.Both, PongEvent.class),
    CLIENT_START_ERROR      (0x0019, ReceivePacketState.LoginServer, ClientCrashReportEvent.class),
    CLIENT_ERROR            (0x001A, ReceivePacketState.LoginServer),
    STRANGE_DATA            (0x001B, ReceivePacketState.Both),
    RELOG                   (0x001C, ReceivePacketState.LoginServer),
    REGISTER_PIC            (0x001D, ReceivePacketState.LoginServer),
    CHAR_SELECT_WITH_PIC    (0x001E, ReceivePacketState.LoginServer),
    VIEW_ALL_PIC_REGISTER   (0x001F, ReceivePacketState.LoginServer),
    VIEW_ALL_WITH_PIC       (0x0020, ReceivePacketState.LoginServer),
    UNKNOWN                 (0x0023, ReceivePacketState.Both, IgnoredPacketEvent.class),
    CHANGE_MAP              (0x0026, ReceivePacketState.ChannelServer),
    CHANGE_CHANNEL          (0x0027, ReceivePacketState.ChannelServer),
    ENTER_CASHSHOP          (0x0028, ReceivePacketState.ChannelServer),
    MOVE_PLAYER             (0x0029, ReceivePacketState.ChannelServer),
    CANCEL_CHAIR            (0x002A, ReceivePacketState.ChannelServer),
    USE_CHAIR               (0x002B, ReceivePacketState.ChannelServer),
    CLOSE_RANGE_ATTACK      (0x002C, ReceivePacketState.ChannelServer),
    RANGED_ATTACK           (0x002D, ReceivePacketState.ChannelServer),
    MAGIC_ATTACK            (0x002E, ReceivePacketState.ChannelServer),
    TOUCH_MONSTER_ATTACK    (0x002F, ReceivePacketState.ChannelServer),
    TAKE_DAMAGE             (0x0030, ReceivePacketState.ChannelServer),
    GENERAL_CHAT            (0x0031, ReceivePacketState.ChannelServer),
    CLOSE_CHALKBOARD        (0x0032, ReceivePacketState.ChannelServer),
    FACE_EXPRESSION         (0x0033, ReceivePacketState.ChannelServer),
    USE_ITEMEFFECT          (0x0034, ReceivePacketState.ChannelServer),
    USE_DEATHITEM           (0x0035, ReceivePacketState.ChannelServer),
    MONSTER_BOOK_COVER      (0x0039, ReceivePacketState.ChannelServer),
    NPC_TALK                (0x003A, ReceivePacketState.ChannelServer),
    REMOTE_STORE            (0x003B, ReceivePacketState.ChannelServer),
    NPC_TALK_MORE           (0x003C, ReceivePacketState.ChannelServer),
    NPC_SHOP                (0x003D, ReceivePacketState.ChannelServer),
    STORAGE                 (0x003E, ReceivePacketState.ChannelServer),
    HIRED_MERCHANT_REQUEST  (0x003F, ReceivePacketState.ChannelServer),
    FREDRICK_ACTION         (0x0040, ReceivePacketState.ChannelServer),
    DUEY_ACTION             (0x0041, ReceivePacketState.ChannelServer),
    ADMIN_SHOP              (0x0044, ReceivePacketState.ChannelServer),
    ITEM_SORT               (0x0045, ReceivePacketState.ChannelServer),
    ITEM_SORT2              (0x0046, ReceivePacketState.ChannelServer),
    ITEM_MOVE               (0x0047, ReceivePacketState.ChannelServer),
    USE_ITEM                (0x0048, ReceivePacketState.ChannelServer),
    CANCEL_ITEM_EFFECT      (0x0049, ReceivePacketState.ChannelServer),
    USE_SUMMON_BAG          (0x004B, ReceivePacketState.ChannelServer),
    PET_FOOD                (0x004C, ReceivePacketState.ChannelServer),
    USE_MOUNT_FOOD          (0x004D, ReceivePacketState.ChannelServer),
    SCRIPTED_ITEM           (0x004E, ReceivePacketState.ChannelServer),
    USE_CASH_ITEM           (0x004F, ReceivePacketState.ChannelServer),
    USE_CATCH_ITEM          (0x0051, ReceivePacketState.ChannelServer),
    USE_SKILL_BOOK          (0x0052, ReceivePacketState.ChannelServer),
    USE_TELEPORT_ROCK       (0x0054, ReceivePacketState.ChannelServer),
    USE_RETURN_SCROLL       (0x0055, ReceivePacketState.ChannelServer),
    USE_UPGRADE_SCROLL      (0x0056, ReceivePacketState.ChannelServer),
    DISTRIBUTE_AP           (0x0057, ReceivePacketState.ChannelServer),
    AUTO_DISTRIBUTE_AP      (0x0058, ReceivePacketState.ChannelServer),
    HEAL_OVER_TIME          (0x0059, ReceivePacketState.ChannelServer),
    DISTRIBUTE_SP           (0x005A, ReceivePacketState.ChannelServer),
    SPECIAL_MOVE            (0x005B, ReceivePacketState.ChannelServer),
    CANCEL_BUFF             (0x005C, ReceivePacketState.ChannelServer),
    SKILL_EFFECT            (0x005D, ReceivePacketState.ChannelServer),
    MESO_DROP               (0x005E, ReceivePacketState.ChannelServer),
    GIVE_FAME               (0x005F, ReceivePacketState.ChannelServer),
    CHAR_INFO_REQUEST       (0x0061, ReceivePacketState.ChannelServer),
    SPAWN_PET               (0x0062, ReceivePacketState.ChannelServer),
    CANCEL_DEBUFF           (0x0063, ReceivePacketState.ChannelServer, IgnoredPacketEvent.class),
    CHANGE_MAP_SPECIAL      (0x0064, ReceivePacketState.ChannelServer),
    USE_INNER_PORTAL        (0x0065, ReceivePacketState.ChannelServer),
    TROCK_ADD_MAP           (0x0066, ReceivePacketState.ChannelServer),
    REPORT                  (0x006A, ReceivePacketState.ChannelServer),
    QUEST_ACTION            (0x006B, ReceivePacketState.ChannelServer),
    UNKNOWN2                (0x006C, ReceivePacketState.Both, IgnoredPacketEvent.class),
    SKILL_MACRO             (0x006E, ReceivePacketState.ChannelServer),
    USE_ITEM_REWARD         (0x0070, ReceivePacketState.ChannelServer),
    MAKER_SKILL             (0x0071, ReceivePacketState.ChannelServer),
    USE_REMOTE              (0x0074, ReceivePacketState.ChannelServer),
    ADMIN_CHAT              (0x0076, ReceivePacketState.ChannelServer),
    PARTYCHAT               (0x0077, ReceivePacketState.ChannelServer),
    WHISPER                 (0x0078, ReceivePacketState.ChannelServer),
    SPOUSE_CHAT             (0x0079, ReceivePacketState.ChannelServer),
    MESSENGER               (0x007A, ReceivePacketState.ChannelServer),
    PLAYER_INTERACTION      (0x007B, ReceivePacketState.ChannelServer),
    PARTY_OPERATION         (0x007C, ReceivePacketState.ChannelServer),
    DENY_PARTY_REQUEST      (0x007D, ReceivePacketState.ChannelServer),
    GUILD_OPERATION         (0x007E, ReceivePacketState.ChannelServer),
    DENY_GUILD_REQUEST      (0x007F, ReceivePacketState.ChannelServer),
    ADMIN_COMMAND           (0x0080, ReceivePacketState.ChannelServer),
    ADMIN_LOG               (0x0081, ReceivePacketState.ChannelServer, IgnoredPacketEvent.class),
    BUDDYLIST_MODIFY        (0x0082, ReceivePacketState.ChannelServer),
    NOTE_ACTION             (0x0083, ReceivePacketState.ChannelServer),
    USE_DOOR                (0x0085, ReceivePacketState.ChannelServer),
    CHANGE_KEYMAP           (0x0087, ReceivePacketState.ChannelServer),
    RPS_ACTION              (0x0088, ReceivePacketState.ChannelServer),
    RING_ACTION             (0x0089, ReceivePacketState.ChannelServer),
    UNKNOWN3                (0x008F, ReceivePacketState.Both, IgnoredPacketEvent.class),
    WEDDING_ACTION          (0x008A, ReceivePacketState.ChannelServer),
    OPEN_FAMILY             (0x0092, ReceivePacketState.ChannelServer),
    ADD_FAMILY              (0x0093, ReceivePacketState.ChannelServer),
    ACCEPT_FAMILY           (0x0096, ReceivePacketState.ChannelServer),
    USE_FAMILY              (0x0097, ReceivePacketState.ChannelServer),
    ALLIANCE_OPERATION      (0x0098, ReceivePacketState.ChannelServer),
    BBS_OPERATION           (0x009B, ReceivePacketState.ChannelServer),
    ENTER_MTS               (0x009C, ReceivePacketState.ChannelServer),
    USE_SOLOMON_ITEM        (0x009D, ReceivePacketState.ChannelServer),
    USE_GACHA_EXP           (0x009E, ReceivePacketState.ChannelServer),
    CLICK_GUIDE             (0x00A2, ReceivePacketState.ChannelServer),
    ARAN_COMBO_COUNTER      (0x00A3, ReceivePacketState.ChannelServer),
    MOVE_PET                (0x00A7, ReceivePacketState.ChannelServer),
    PET_CHAT                (0x00A8, ReceivePacketState.ChannelServer),
    PET_COMMAND             (0x00A9, ReceivePacketState.ChannelServer),
    PET_LOOT                (0x00AA, ReceivePacketState.ChannelServer),
    PET_AUTO_POT            (0x00AB, ReceivePacketState.ChannelServer),
    PET_EXCLUDE_ITEMS       (0x00AC, ReceivePacketState.ChannelServer),
    MOVE_SUMMON             (0x00AF, ReceivePacketState.ChannelServer),
    SUMMON_ATTACK           (0x00B0, ReceivePacketState.ChannelServer),
    DAMAGE_SUMMON           (0x00B1, ReceivePacketState.ChannelServer),
    BEHOLDER                (0x00B2, ReceivePacketState.ChannelServer),
    MOVE_DRAGON             (0x00B5, ReceivePacketState.ChannelServer),
    QUICK_SLOT_UPDATE       (0x00B7, ReceivePacketState.ChannelServer, IgnoredPacketEvent.class),
    MOVE_LIFE               (0x00BC, ReceivePacketState.ChannelServer),
    AUTO_AGGRO              (0x00BD, ReceivePacketState.ChannelServer),
    MOB_DAMAGE_MOB_FRIENDLY (0x00C0, ReceivePacketState.ChannelServer),
    MONSTER_BOMB            (0x00C1, ReceivePacketState.ChannelServer),
    MOB_DAMAGE_MOB          (0x00C2, ReceivePacketState.ChannelServer),
    NPC_ACTION              (0x00C5, ReceivePacketState.ChannelServer),
    ITEM_PICKUP             (0x00CA, ReceivePacketState.ChannelServer),
    DAMAGE_REACTOR          (0x00CD, ReceivePacketState.ChannelServer),
    TOUCHING_REACTOR        (0x00CE, ReceivePacketState.ChannelServer),
    TEMP_SKILL              (0x00CF, ReceivePacketState.ChannelServer, IgnoredPacketEvent.class),
    MAPLETV                 (0xFFFE, ReceivePacketState.ChannelServer),
    SNOWBALL                (0x00D3, ReceivePacketState.ChannelServer),
    LEFT_KNOCKBACK          (0x00D4, ReceivePacketState.ChannelServer),
    COCONUT                 (0x00D5, ReceivePacketState.ChannelServer),
    MATCH_TABLE             (0x00D6, ReceivePacketState.ChannelServer),
    MONSTER_CARNIVAL        (0x00DA, ReceivePacketState.ChannelServer),
    PARTY_SEARCH_REGISTER   (0x00DC, ReceivePacketState.ChannelServer),
    PARTY_SEARCH_START      (0x00DE, ReceivePacketState.ChannelServer),
    FIELD_SET               (0x00DF, ReceivePacketState.Both, FieldSetEvent.class),
    CHECK_CASH              (0x00E4, ReceivePacketState.ChannelServer),
    CASHSHOP_OPERATION      (0x00E5, ReceivePacketState.ChannelServer),
    COUPON_CODE             (0x00E6, ReceivePacketState.ChannelServer),
    OPEN_ITEMUI             (0x00EB, ReceivePacketState.ChannelServer),
    CLOSE_ITEMUI            (0x00EC, ReceivePacketState.ChannelServer),
    USE_ITEMUI              (0x00ED, ReceivePacketState.ChannelServer),
    MTS_OPERATION           (0x00FD, ReceivePacketState.ChannelServer),
    USE_MAPLELIFE           (0x00FE, ReceivePacketState.ChannelServer),
    USE_HAMMER              (0x0104, ReceivePacketState.ChannelServer);
    public final int value;
    public final ReceivePacketState packetState;
    public Class<? extends PacketEvent> clazz = null;
    // @formatter:on

    RecvOpcode(int value, ReceivePacketState packetState) {
        this(value, packetState, null);
    }

    RecvOpcode(int value, ReceivePacketState packetState, Class<? extends PacketEvent> clazz) {
        this.value = value;
        this.packetState = packetState;
        this.clazz = clazz;
    }

    public int getValue() {
        return value;
    }
}
