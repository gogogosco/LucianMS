load('scripts/util_achievements.js');
const OuterSpaceFieldID = 98;
/* izarooni */

function getName() {
    return "Discover Outer Space, Planet Aura";
}

function testForPlayer(player) {
    return player.getMap().getId() == OuterSpaceFieldID;
}

function reward(player) {
    return tryGiveItem(player, [new RewardItem(ServerConstants.CURRENCY, 3)]);
}

function readableRewards(rr) {
    return rr.add(`3x #z${ServerConstants.CURRENCY}#`);
}
