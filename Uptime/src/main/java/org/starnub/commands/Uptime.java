package org.starnub.commands;

import org.starnub.starnubserver.StarNub;
import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.events.starnub.StarNubEventHandler;
import org.starnub.starnubserver.events.starnub.StarNubEventSubscription;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.utilities.events.Priority;
import org.starnub.utilities.events.types.ObjectEvent;
import org.starnub.utilities.time.DateAndTimes;

public class Uptime extends Command {

    private final StarNubEventSubscription STARBOUND_UPTIME_EVENT;
    private final StarNubEventSubscription STARNUB_UPTIME_EVENT;
    private long starboundUptime = 0L;
    private long starnubUptime = 0L;

    public Uptime() {
        this.STARBOUND_UPTIME_EVENT = new StarNubEventSubscription("Essentials", Priority.LOW, "Starbound_Uptime", new StarNubEventHandler() {
            @Override
            public void onEvent(ObjectEvent objectEvent) {
                starboundUptime = (long) objectEvent.getEVENT_DATA();
            }
        });
        this.STARNUB_UPTIME_EVENT = new StarNubEventSubscription("Essentials", Priority.LOW, "StarNub_Uptime", new StarNubEventHandler() {
            @Override
            public void onEvent(ObjectEvent objectEvent) {
                starnubUptime = (long) objectEvent.getEVENT_DATA();
            }
        });
    }

    @Override
    public void onCommand(PlayerSession playerSession, String command, int argsCount, String[] args) {
        int argsLength = args.length;
        if (argsLength == 0) {
            playerSession.sendBroadcastMessageToClient("Essentials", "You did not supply enough arguments. /uptime {starbound} or /uptime starnub}");
        } else {

            String arg = args[0];
            long uptime = 0L;
            String name = null;
            if (arg.equals("starbound")) {
                uptime = starboundUptime;
                name = "Starbound Server";
            } else if (arg.equals("starnub")) {
                uptime = starnubUptime;
                name = "StarNub Server";
            }
            String formattedTime = DateAndTimes.getPeriodFormattedFromMilliseconds(uptime, false);
            String serverName = (String) StarNub.getConfiguration().getNestedValue("starnub_info", "server_name");
            playerSession.sendBroadcastMessageToClient(serverName, "The current uptime of the " + name + " is " + formattedTime + ".");
        }
    }
}