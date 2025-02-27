 /*
 
    Author: Lucasdieswagger @ discord
 
 */
 
 importPackage(Packages.tools);
var LifeFactory = Java.type("com.lucianms.server.life.MapleLifeFactory");
 var sections = {};
 var method = null;
 var status = 0;
 var text = "";

 var moveTo = 90000007;
 
function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === -1) {
        cm.dispose();
        return;
    } else if (mode === 0) {
        status--;
        if (status === 0) {
            cm.dispose();
            return;
        }
    } else {
        status++;
    }
    if (status === 1) {
        method = null;
        if(cm.getPlayer().getKillType() == 9899998) {
            if(cm.getPlayer().getCurrent() >= cm.getPlayer().getGoal()) {
                // complete quest
                cm.getPlayer().gainExp(350, 0, true, true, false);
                cm.getPlayer().changeMap(moveTo);
                cm.dispose();
                cm.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect("quest/party/clear4"));
                cm.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound("customJQ/quest"));
            } else {
                var amountLeft = cm.getPlayer().getGoal() - cm.getPlayer().getCurrent();
                text = "You still need to kill " + amountLeft + " heartless to continue.";
            }
        } else {
            text = "It is time to erase the darkness within you! You need to kill 15 #rHeartless#k. you can kill them by standing nearby them and clicking on your attack key (default: #bctrl#k), kill 15 monsters and talk to me again.";
            cm.getPlayer().setKillType(99899998);
            cm.getPlayer().setGoal(15);
            cm.getPlayer().setCurrent(0);
        }
        cm.sendOk(text);
        cm.dispose();
        
    } else {
        if (method == null) {
            method = sections[get(selection)];
        }
        method(mode, type, selection);
    }
}


function get(index) {
    var i = 0;
    for (var s in sections) {
        if (i === index)
            return s;
        i++;
    }
    return null;
}

