package org.starnub.commands;

import io.netty.channel.ChannelHandlerContext;
import org.starnub.plugins.JoinLeaveBroadcast;
import org.starnub.starnubserver.cache.objects.PlayerSessionCache;
import org.starnub.starnubserver.cache.wrappers.PlayerCtxCacheWrapper;
import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.starnubserver.pluggable.PluggableManager;
import org.starnub.starnubserver.pluggable.commandprocessor.ArgumentType;
import org.starnub.starnubserver.pluggable.commandprocessor.CommandProcessorError;
import org.starnub.starnubserver.pluggable.commandprocessor.EndNode;
import org.starnub.starnubserver.pluggable.commandprocessor.RootNode;
import org.starnub.utilities.cache.objects.TimeCache;

public class JoinLeave extends Command {

    private RootNode ROOT_COMMAND_NODE;

    @Override
    public void onEnable() {
        EndNode baseNode = new EndNode("joinleave", ArgumentType.STATIC, this::joinLeave);
        ROOT_COMMAND_NODE = new RootNode(baseNode);
    }

    @Override
    public void onDisable() {

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

    public void joinLeave(PlayerSession playerSession,  int argsCount, String[] args) {
        ChannelHandlerContext clientCtx = playerSession.getCONNECTION().getCLIENT_CTX();
        JoinLeaveBroadcast joinLeaveMessages = (JoinLeaveBroadcast) PluggableManager.getInstance().getSpecificLoadedPlugin("JoinLeaveBroadcast");
        System.out.println(joinLeaveMessages);
        PlayerCtxCacheWrapper joinLeave = joinLeaveMessages.getUNSUBSCRIBED_JOIN_LEAVE();
        String colorUnvalidated = (String) getConfiguration().getNestedValue("color");
        String validatedColor = validateColor(colorUnvalidated);
        TimeCache cache = joinLeave.getCache(clientCtx);
        if (cache == null) {
            joinLeave.addCache(clientCtx, new PlayerSessionCache(playerSession));
            String unsubMessage = validatedColor + getConfiguration().getNestedValue("unsubscribe");
            sendMessage(playerSession, unsubMessage);
        } else {
            joinLeave.removeCache(clientCtx);
            String subMessage = validatedColor + getConfiguration().getNestedValue("subscribe");
            sendMessage(playerSession, subMessage);
        }
    }

    private void sendMessage(PlayerSession playerSession, String message) {
        playerSession.sendBroadcastMessageToClient("ServerName", message);
    }
}
