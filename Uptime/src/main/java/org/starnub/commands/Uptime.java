package org.starnub.commands;

import org.starnub.starnubserver.StarNub;
import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.starnubserver.pluggable.commandprocessor.*;
import org.starnub.utilities.events.Priority;
import org.starnub.utilities.time.DateAndTimes;

public class Uptime extends Command {

    private final RootNode ROOT_COMMAND_NODE;
    private long starboundUptime = 0L;
    private long starnubUptime = 0L;

    public Uptime() {
        EndNode starbound = new EndNode("starbound", ArgumentType.STATIC, this::starbound);
        EndNode starnub = new EndNode("starnub", ArgumentType.STATIC, this::starnub);
        EndNode allTime = new EndNode("all", ArgumentType.STATIC, this::allTime);
        SubNode baseNode = new SubNode("uptime", starbound, starnub, allTime);
        ROOT_COMMAND_NODE = new RootNode(baseNode);
    }

    @Override
    public void onRegister() {
        newStarNubEventSubscription(Priority.LOW, "StarNub_Uptime", objectEvent -> starnubUptime = (long) objectEvent.getEVENT_DATA());
        newStarNubEventSubscription(Priority.LOW, "Starbound_Uptime",objectEvent -> starboundUptime = (long) objectEvent.getEVENT_DATA());
    }

    @Override
    public void onCommand(PlayerSession playerSession, String command, int argsCount, String[] args) {
        try {
            ROOT_COMMAND_NODE.processCommand(playerSession, command, argsCount, args);
        } catch (CommandProcessorError e) {
            playerSession.sendBroadcastMessageToClient("ServerName", "Command Processor Error! Please speak with an administrator.");
            e.printStackTrace();
        }
    }

    private void allTime(PlayerSession playerSession, int argsCount, String[] args){
        messageSend(playerSession, getUptimeString("StarNub", starnubUptime, "Starbound", starboundUptime));
    }

    private void starbound(PlayerSession playerSession, int argsCount, String[] args){
        messageSend(playerSession, getUptimeString("Starbound", starboundUptime));
    }

    private void starnub(PlayerSession playerSession, int argsCount, String[] args){
        messageSend(playerSession, getUptimeString("StarNub", starnubUptime));
    }

    private String getUptimeString(String uptimeName, long time){
        String formattedTime = DateAndTimes.getPeriodFormattedFromMilliseconds(time, false);
        return String.format("Current up-time for the %s Server is %s.", uptimeName, formattedTime);
    }

    private String getUptimeString(String uptimeName, long time, String uptimeName2, long time2){
        String formattedTime = DateAndTimes.getPeriodFormattedFromMilliseconds(time, false);
        String formattedTime2 = DateAndTimes.getPeriodFormattedFromMilliseconds(time, false);
        return String.format("Current up-time: %s Server %s & %s Server %s.", uptimeName, formattedTime, uptimeName2, formattedTime2);
    }

    private void messageSend(PlayerSession playerSession, String message){
        String serverName = (String) StarNub.getConfiguration().getNestedValue("starnub_info", "server_name");
        playerSession.sendBroadcastMessageToClient(serverName, message);
    }
}
