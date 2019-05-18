package com.lucianms.commands;

import com.lucianms.BanManager;
import com.lucianms.Whitelist;
import com.lucianms.command.Command;
import com.lucianms.command.CommandArgs;
import com.lucianms.command.executors.ConsoleCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author izarooni
 */
public class LoginConsoleCommands extends ConsoleCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginConsoleCommands.class);

    @Override
    public void execute(Command command, CommandArgs args) {
        if (command.equals("help")) {
            System.out.println("unban - Attempts an un-ban for a specified account");
            System.out.println("whitelist - Modify the whitelist cache");
            System.out.println("stop - Terminates the JVM");
        } else if (command.equals("whitelist")) {
            if (args.length() > 0) {
                switch (args.get(0)) {
                    default:
                        LOGGER.info("unknown argument '{}'", args.get(0));
                        break;
                    case "add": {
                        Integer accountID = args.parseNumber(1, int.class);
                        if (accountID == null) {
                            LOGGER.info("'{}' is not a number", args.get(1));
                            return;
                        }
                        Whitelist.getAccounts().add(accountID);
                        Whitelist.saveCache();
                        LOGGER.info("success");
                        break;
                    }
                    case "remove": {
                        Integer accountID = args.parseNumber(1, int.class);
                        if (accountID == null) {
                            LOGGER.info("'{}' is not a number", args.get(1));
                            return;
                        }
                        Whitelist.getAccounts().remove(accountID);
                        Whitelist.saveCache();
                        LOGGER.info("success");
                        break;
                    }
                    case "list":
                        break;
                }
                System.out.println(Whitelist.getAccounts());
            } else {
                LOGGER.info("Usage: whitelist <add | remove> <account_id | account_name>");
                LOGGER.info("whitelist <list>");
            }
        } else if (command.equals("unban")) {
            if (args.length() != 1) {
                LOGGER.info("Usage: unban <username>");
                return;
            }
            String username = args.get(0);
            if (BanManager.pardonUser(username)) {
                LOGGER.info("Successfully unbanned '{}' and all relating data (machineID, MAC, IP)", username);
            } else {
                LOGGER.info("Failed to find any user account via username '{}", username);
            }
        } else if (command.equals("stop")) {
            setReading(false);
            System.exit(0);
        } else {
            LOGGER.info("Unknown command '{}'", command.getName());
        }
    }
}
