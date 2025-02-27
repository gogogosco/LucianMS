load("scripts/npc/generic_shop.js");

dialog.first = "You currently have #b{PointsQuantity} {PointsName}#k"
    + "\r\nAre you interested in the Android gear that I sell?\r\n#b";

var pointsType = Java.type("com.lucianms.constants.ServerConstants").CURRENCY;

try {
    var file = new java.io.File("resources/data-android.json");
    var content = Packages.org.apache.commons.io.FileUtils.readFileToString(file);
    var json = JSON.parse(content);

    for (var j in json) {
        items[j] = JSON.parse("[" + json[j] + "]");
    }
} catch (e) {
    broken = e.message.replace(/\\/g, "/");
}
