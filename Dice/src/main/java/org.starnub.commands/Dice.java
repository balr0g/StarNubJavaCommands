package org.starnub.commands;

import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.utilities.numbers.RandomNumber;
import org.starnub.utilities.strings.StringUtilities;

public class Dice extends Command {

    @Override
    public void onCommand(PlayerSession playerSession, String command, int argsCount, String[] args) {
        if (argsCount == 0){
            sendMessage(playerSession, "So you want to roll some dice, eh? Use /roll followed by this formula: \"#d#\" The first number is how many dice you roll, and the second number is how many sides the dice have. For example, to roll 2 six-sided dice, use /roll 2d6.");
        } else {
            String arg = args[0];
            String[] dice = arg.split("d");
            if (dice.length == 2){
                String diceOrDices;
                int diceCount = 0;
                int diceSize = 0;
                try {
                    String diceCountString = dice[0];
                    diceCount = Integer.parseInt(diceCountString);
                    String diceSizeString = dice[1];
                    diceSize = Integer.parseInt(diceSizeString);
                } catch (NumberFormatException e){
                    sendMessage(playerSession, "You did not supply a proper dice. The formal is \"#d#\" The first number is how many dice you roll, and the second number is how many sides the dice have. For example, to roll 2 six-sided dice, use /roll 2d6.");
                }
                if (diceCount == 0 || diceSize == 0){
                    return;
                }
                if (diceCount > 20){
                    sendMessage(playerSession, "You can not roll more than 20 dice at a time.");
                    return;
                }
                if (diceSize > 500){
                    sendMessage(playerSession, "Wha? When did dice have over 500 sides.");
                    return;
                }
                String diceRollString = "";
                int total = 0;
                for (int i = 0; i < diceCount; i++) {
                    int randInt = RandomNumber.randInt(0, diceSize);
                    total = total + randInt;
                    diceRollString = diceRollString + randInt + ", ";
                }
                if (diceCount == 1){
                    diceOrDices = "die";
                } else {
                    diceOrDices = "dice";
                }
                String trimFromString = StringUtilities.trimFromString(diceRollString, ",", "");
                trimFromString = trimFromString + " (" + total + ")";
                String playerName = playerSession.getGameName();
                PlayerSession.sendChatBroadcastToClientsAll("StarNub", playerName + " rolled " + diceCount + ", " + diceSize + " sided " + diceOrDices + ": " + trimFromString);
            } else {
                sendMessage(playerSession, "You did not supply a proper dice. The formal is \"#d#\" The first number is how many dice you roll, and the second number is how many sides the dice have. For example, to roll 2 six-sided dice, use /roll 2d6.");
            }
        }
    }

    private void sendMessage(PlayerSession playerSession, String string){
        playerSession.sendBroadcastMessageToClient("ServerName", string);
    }

    @Override
    public void onRegister() {
        /* No events to register */
    }
}