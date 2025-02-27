const MapleExpeditionType = Java.type('com.lucianms.server.expeditions.MapleExpeditionType');
const MaplePacketCreator = Java.type('tools.MaplePacketCreator');
/*Adobis
 *
 *@author SharpAceX (Alan)
 */

var status = 0;
var expedition;
var player;
var em;
var zakum = MapleExpeditionType.ZAKUM;
var list = "What would you like to do?#b\r\n\r\n#L1#View current Expedition members#l\r\n#L2#Start the fight!#l\r\n#L3#Stop the expedition.#l";

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    player = cm.getPlayer();
    expedition = cm.getExpedition(zakum);
    em = cm.getEventManager("ZakumBattle");

    if (mode < 1) {
        cm.dispose();
        return;
    }
    if (status == 0) {
        //cm.removeExpedition(expedition);
        if (player.getClient().getChannel() != 1) { //Only channel 1
            cm.sendOk("Sorry, Zakum may only be challenged on #bChannel 1#k.");
            cm.dispose();
        } else if (player.getLevel() < zakum.getMinLevel() && player.getLevel() > zakum.getMaxLevel()) { //Don't fit requirement
            cm.sendOk("You do not meet the criteria to take on Zakum!");
            cm.dispose();
        } else if (expedition == null) { //Start an expedition
            cm.sendSimple("Would you like to assemble a team to take on the mighty #rZakum#k?\r\n#b#L1#Lets get this going!#l\r\n\#L2#No, I think I'll wait a bit...#l");
            status = 1;
        } else if (expedition.isLeader(player)) { //If you're the leader, manage the exped
            cm.sendSimple(list);
            status = 2;
        } else if (expedition.isRegistering()) { //If the expedition is registering
            if (expedition.contains(player)) { //If you're in it but it hasn't started, be patient
                cm.sendOk("You have already registered for the expedition. Please wait for " + expedition.getLeader().getName() + " to begin the expedition.");
                cm.dispose();
            } else { //If you aren't in it, you're going to get added
                cm.sendOk(expedition.addMember(cm.getPlayer()));
                cm.dispose();
            }
        } else if (expedition.isInProgress()) { //Only if the expedition is in progress
            if (expedition.contains(player)) { //If you're registered, warp you in
                em.getInstance("ZakumBattle_" + player.getClient().getChannel()).registerPlayer(player);
                cm.dispose();
            } else { //If you're not in by now, tough luck
                cm.sendOk("Another expedition has taken the initiative to fight Zakum, lets pray for those brave souls.");
                cm.dispose();
            }
        }
    } else if (status == 1) {
        if (selection == 1) {
            cm.createExpedition(zakum);
            cm.sendOk("The #rZakum Expedition#k has been created.\r\n\r\nTalk to me again to view the current team, or start the fight!");
            cm.dispose();
            return;
        } else if (selection == 2) {
            cm.sendOk("Sure, not everyone's up to challenging the might of Zakum.");
            cm.dispose();
            return;
        }
    } else if (status == 2) {
        if (selection == 1) {
            if (expedition == null) {
                cm.sendOk("The expedition could not be loaded.");
                cm.dispose();
                return;
            }
            var size = expedition.getMembers().size();
            if (size == 1) {
                cm.sendOk("You are the only member of the expedition.");
                cm.dispose();
                return;
            }
            var text = "The following members make up your expedition (Click on them to expel them):\r\n";
            text += "\r\n\t\t1." + expedition.getLeader().getName();
            for (var i = 1; i < size; i++) {
                text += "\r\n#b#L" + (i + 1) + "#" + (i + 1) + ". " + expedition.getMembers().get(i).getName() + "#l\n";
            }
            cm.sendSimple(text);
            status = 6;
        } else if (selection == 2) {
            cm.sendOk("The expedition will begin and you will now be escorted to the #bEntrance to Zakum Altar#k.");
            status = 4;
        } else if (selection == 3) {
			player.getMap().broadcastMessage(MaplePacketCreator.removeClock());
			player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, expedition.getLeader().getName() + " has ended the expedition."));
			cm.endExpedition(expedition);
            cm.sendOk("The expedition has now ended. Sometimes the best strategy is to run away.");
			cm.dispose();
            return;
        }
    } else if (status == 4) {
        var min = 1; //zakum.getMinSize();
        var size = expedition.getMembers().size();
        if (size < min) {
            cm.sendOk("You need at least " + min + " players registered in your expedition.");
            cm.dispose();
            return;
        }
        if (em == null) {
            cm.sendOk("The event could not be found, please report this on the forum.");
            cm.dispose();
            return;
        }
        cm.sendOk("Good luck! Zakum is a worthy foe!");
		em.setProperty("leader", player.getName());
        em.setProperty("channel", player.getClient().getChannel());
        em.startInstance(expedition);
        cm.dispose();
        return;
    } else if (status == 6) {
        if (selection > 0) {
           var banned = expedition.getMembers().get(selection);
            expedition.ban(banned);
            cm.sendOk("You have banned " + banned.getName() + " from the expedition.");
            cm.dispose();
        } else {
            cm.sendSimple(list);
            status = 2;
        }
    }
}