package org.starnub.commands;

import io.netty.channel.ChannelHandlerContext;
import org.starnub.plugins.JoinLeaveMessages;
import org.starnub.starbounddata.types.color.Colors;
import org.starnub.starnubserver.cache.objects.PlayerSessionCache;
import org.starnub.starnubserver.cache.wrappers.PlayerCtxCacheWrapper;
import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.starnubserver.pluggable.PluggableManager;
import org.starnub.starnubserver.pluggable.commandprocessor.*;
import org.starnub.utilities.cache.objects.TimeCache;

public class JoinLeave extends Command {

    private final RootNode ROOT_COMMAND_NODE;

    public JoinLeave() {
        EndNode baseNode = new EndNode("joinleave", ArgumentType.STATIC, this::joinLeave);
        ROOT_COMMAND_NODE = new RootNode(baseNode);
    }

    @Override
    public void onRegister() {
        /* No events to register */
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
        JoinLeaveMessages joinLeaveMessages = (JoinLeaveMessages) PluggableManager.getInstance().getSpecificLoadedPlugin("JoinLeaveMessages");
        PlayerCtxCacheWrapper joinLeave = joinLeaveMessages.getUNSUBSCRIBED_JOIN_LEAVE();
        String colorUnvalidated = (String) getConfiguration().getNestedValue("color");
        String chatColor = Colors.validate(colorUnvalidated);
        TimeCache cache = joinLeave.getCache(clientCtx);
        if (cache == null) {
            joinLeave.addCache(clientCtx, new PlayerSessionCache(playerSession));
            String unsubMessage = chatColor + getConfiguration().getNestedValue("unsubscribe");
            sendMessage(playerSession, unsubMessage);
        } else {
            joinLeave.removeCache(clientCtx);
            String subMessage = chatColor + getConfiguration().getNestedValue("subscribe");
            sendMessage(playerSession, subMessage);
        }
    }

    private void sendMessage(PlayerSession playerSession, String message) {
        playerSession.sendBroadcastMessageToClient("ServerName", message);
    }
}
