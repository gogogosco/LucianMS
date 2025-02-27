/* izarooni

-- Ratz drops
insert into drop_data values (default9300006, 4001022, 1, 1, 0, 999999);
insert into drop_data values (default9300005, 4001022, 1, 1, 0, 999999);
*/
var exitMap;
var instanceId;
var finishMap;
var bonusMap;
var bonusTime = 60;//1 Minute
var pqTime = 3600;//60 Minutes

function init() {
    instanceId = 1;
	exitMap = em.getChannel().getMap(922010000);
    em.setProperty("LPQOpen", "true");
}

function monsterValue(eim, player, monster) {
    return 1;
}


function setup() {
    var instanceName = "LudiPQ" + instanceId;
    var eim = em.newInstance(instanceName);
	var eventTime = 60 * (1000 * 60); // 60 mins.
    instanceId++;

	em.schedule("timeOut", eim, eventTime); // invokes "timeOut" in how ever many seconds.
	eim.startEventTimer(eventTime); // Sends a clock packet and tags a timer to the players.
    //eim.schedule("respawn", 5000);

    var stage1Portal = eim.getMapInstance(922010100).getPortal("next00");
    stage1Portal.setScriptName("lpq1");
    var stage2Portal = eim.getMapInstance(922010200).getPortal("next00");
    stage2Portal.setScriptName("lpq2");
    var stage3Portal = eim.getMapInstance(922010300).getPortal("next00");
    stage3Portal.setScriptName("lpq3");
    var stage4Portal = eim.getMapInstance(922010400).getPortal("next00");
    stage4Portal.setScriptName("lpq4");
    var stage5Portal = eim.getMapInstance(922010500).getPortal("next00");
    stage5Portal.setScriptName("lpq5");
    var stage6Portal = eim.getMapInstance(922010600).getPortal("next00");
    stage6Portal.setScriptName("lpq6");
    var stage7Portal = eim.getMapInstance(922010700).getPortal("next00");
    stage7Portal.setScriptName("lpq7");
    var stage8Portal = eim.getMapInstance(922010800).getPortal("next00");
    stage8Portal.setScriptName("lpq8");
    return eim;
}

function playerEntry(eim, player) {
    var map0 = eim.getMapInstance(922010100);
    player.changeMap(map0, map0.getPortal(0));
}

function playerDead(eim, player) {
    if (player.isAlive()) { //don't trigger on death, trigger on manual revive
        if (eim.isLeader(player)) {
            var party = eim.getPlayers();
            for (var i = 0; i < party.size(); i++)
                playerExit(eim, party.get(i));
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            if (party.get(i).equals(player))
                removePlayer(eim, player);
            else
                playerExit(eim, party.get(i));
        eim.dispose();
    }
    else
        removePlayer(eim, player);
}

function leftParty(eim, player) {
    playerExit(eim, player);
}

function disbandParty(eim) {
    eim.getPlayers().forEach(function(chr) {
        playerExit(eim, chr);
    });
    eim.dispose();
}

function OpenLPQ() {
    em.setProperty("LPQOpen", "true");
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}


function playerFinish(eim, player) {
	eim.unregisterPlayer(player);
    var map = eim.getMapInstance(922011100);
    player.changeMap(map, map.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerFinish(eim, party.get(i));
    eim.dispose();
}

function allMonstersDead(eim) {
}

function dispose() {
    em.schedule("OpenLPQ", 10000); // 10 seconds ?
}

function cancelSchedule() {
}

function timeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
        eim.dispose();
    }
}

function finishBonus(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++)
		if(party.get(i).getMap().getId() == 922011000)
			playerFinish(eim, party.get(i));
}

function startBonus(eim) {
	var bonusMap = eim.getMapInstance(922011000);
	var party = eim.getPlayers();

	em.schedule("finishBonus", eim, 60000); // invokes "timeOut" in how ever many seconds.
	eim.startEventTimer(60000);

	for (var i = 0; i < party.size(); i++) {
		if(party.get(i).getMap().getId() == 922010900) {
			party.get(i).changeMap(bonusMap, bonusMap.getPortal(0));
		}
	}

}
