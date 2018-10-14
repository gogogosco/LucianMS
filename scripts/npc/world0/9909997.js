/* izarooni
Gate of Darkness
Map: 96
 */
load('scripts/npc/generic_jobadvance.js');
const FirstAdvancementGreet = "Your presence is weak. It was meant to be, your future of darkness. "
    + "Become the nightmare as fear grows in darkness and rule all of LucianMS.";
jobs = {
    First: {
        Rogue: { ID: 400, req: (p) => p.getLuk() >= 25,
            failMessage: "Make sure you have at least #b25#k points in your #bLUK#k stat.",
            equips: [[1472000, 1], [1332007, 1], [2070000, 1000]] }
    },
    Second: {
        Dancer        : { ID: 410 },
        "Dual Blader" : { ID: 420 }
    },
    Third: {
        Dancer        : { ID: 411 },
        "Dual Blader" : { ID: 421 }
    },
    Fourth: {
        Dancer        : { ID: 412 },
        "Dual Blader" : { ID: 422 }
    }
};
