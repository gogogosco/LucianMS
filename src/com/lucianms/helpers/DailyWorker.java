package com.lucianms.helpers;

import net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author izarooni
 */
public class DailyWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyWorker.class);


    @Override
    public void run() {
        try (Connection con = Database.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.execute("update entry_limit set entries = 0");
            }
            LOGGER.info("Entry limits reset!");
        } catch (SQLException e) {
            LOGGER.error("Error while resetting entry limits", e);
        }

        LOGGER.info("Testing for account age achievements");
        // testFor character data (character age)
        Server.getInstance().getWorlds().forEach(w -> w.getPlayerStorage().getAllCharacters().forEach(p -> com.lucianms.io.scripting.Achievements.testFor(p, -1)));
    }
}
