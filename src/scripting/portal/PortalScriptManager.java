package scripting.portal;

import client.MapleCharacter;
import client.MapleClient;
import client.SpamTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripting.ScriptUtil;
import server.MaplePortal;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author izarooni
 */
public class PortalScriptManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortalScriptManager.class);

    private static HashMap<String, Invocable> scripts = new HashMap<>();

    private PortalScriptManager() {
    }

    private static Invocable getPortalScript(MapleClient client, String script) throws IOException, ScriptException {
        if (scripts.containsKey(script)) {
            return scripts.get(script);
        }
        try {
            Invocable iv = ScriptUtil.eval(client, "portal/" + script + ".js", Collections.emptyList());
            return scripts.put(script, iv);
        } catch (FileNotFoundException e) {
            LOGGER.warn("No portal script found '{}'", script);
        } catch (NullPointerException e) {
            LOGGER.warn("Unable to execute script '{}' in map '{}'", script, client.getPlayer().getMapId(), e);
        }
        return null;
    }

    public static void clearPortalScripts() {
        scripts.clear();
        System.gc();
    }

    public static boolean executePortalScript(MapleClient client, MaplePortal portal) {
        MapleCharacter player = client.getPlayer();
        SpamTracker.SpamData spamTracker = player.getSpamTracker(SpamTracker.SpamOperation.PortalScripts);
        if (!spamTracker.testFor(1000)) {
            return false;
        }
        spamTracker.record();

        try {
            Invocable iv = getPortalScript(client, portal.getScriptName());
            if (iv != null) {
                return (boolean) iv.invokeFunction("enter", new PortalPlayerInteraction(client, portal));
            }
        } catch (IOException | ScriptException | NoSuchMethodException e) {
            LOGGER.info("Unable to invoke function 'enter' in script Name: {}, ID: {}, Map: {}", portal.getScriptName(), portal.getId(), player.getMapId(), e);
        }
        return false;
    }
}