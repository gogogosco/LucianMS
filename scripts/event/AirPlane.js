//Time Setting is in millisecond
var closeTime = 24 * 1000; //The time to close the gate
var beginTime = 30 * 1000; //The time to begin the ride
var rideTime = 6 * 1000; //The time that require move to destination
var KC_bfd;
var Plane_to_CBD;
var CBD_docked;
var CBD_bfd;
var Plane_to_KC;
var KC_docked;

function init() {
    KC_bfd = em.getChannel().getMap(540010100);
    CBD_bfd = em.getChannel().getMap(540010001);
    Plane_to_CBD = em.getChannel().getMap(540010101);
    Plane_to_KC = em.getChannel().getMap(540010002);
    CBD_docked = em.getChannel().getMap(540010000);
    KC_docked = em.getChannel().getMap(103000000);
    scheduleNew();
}

function scheduleNew() {
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("entry", "true");
	KC_bfd.warpEveryone(Plane_to_CBD.getId());
	CBD_bfd.warpEveryone(Plane_to_KC.getId());
    em.schedule("arrived", rideTime);
    scheduleNew();
}

function arrived() {
	Plane_to_CBD.warpEveryone(CBD_docked.getId());
	Plane_to_KC.warpEveryone(KC_docked.getId());
}



function cancelSchedule() {
}
