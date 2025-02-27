package com.lucianms.discord.handlers;

import com.lucianms.discord.Headers;

/**
 * A shitty attempt on having two JVM's communicate locally
 *
 * @author izarooni
 */
public class DiscordRequestManager {

    private static DiscordRequest[] requests = new DiscordRequest[Headers.values().length];

    static {
        requests[Headers.Shutdown.value] = new ShutdownRequest();
        requests[Headers.SetFace.value] = new FaceChangeRequest();
        requests[Headers.SetHair.value] = new HairChangeRequest();
        requests[Headers.Online.value] = new OnlineRequest();
        requests[Headers.Bind.value] = new BindRequest();
        requests[Headers.Search.value] = new SearchRequest();
        requests[Headers.Disconnect.value] = new DisconnectRequest();
        requests[Headers.ReloadCS.value] = new ReloadCSRequest();
    }

    private DiscordRequestManager() {
    }

    public static DiscordRequest getRequest(int i) {
        return requests[i];
    }
}
