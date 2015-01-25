package org.starnub.commands;


import org.starnub.starnubdata.generic.CanUse;
import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.starnubserver.pluggable.PluggableFileType;
import org.starnub.starnubserver.pluggable.PluggableManager;
import org.starnub.starnubserver.pluggable.commandprocessor.*;
import org.starnub.starnubserver.pluggable.generic.LoadSuccess;
import org.starnub.starnubserver.pluggable.generic.PluggableReturn;
import org.starnub.utilities.strings.StringUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class Commands extends Command {

    private RootNode ROOT_COMMAND_NODE;

    @Override
    public void onEnable() {
        EndNode all = new EndNode("all", ArgumentType.STATIC, this::allCommands);
        EndNode variable = new EndNode("{command-name}", ArgumentType.VARIABLE, this::command);
        EndNode list = new EndNode("list", ArgumentType.STATIC, this::commandList);
        EndNode mineVariable = new EndNode("{command-name}", ArgumentType.VARIABLE, this::mineArgs);
        EndNode mineAll = new EndNode("all", ArgumentType.VARIABLE, this::mine);
        SubNode mine = new SubNode("mine", mineAll, mineVariable);
        EndNode lookupVariable = new EndNode("{player-identifier} Optional: {command-name}", ArgumentType.VARIABLE, this::lookup);
        SubNode lookup = new SubNode("lookup", lookupVariable);
        EndNode infoVariable = new EndNode("{command-name} Optional: {name, owner, progamlanguage, version, size, author, url, mainargs, canuse, description, dependancies, permissions} - Can use more then one at a time.", ArgumentType.VARIABLE, this::commandInfo);
        SubNode info = new SubNode("info", infoVariable);
        SubNode load = new SubNode("load", all, variable);
        SubNode unload = new SubNode("unload", all, variable);
        SubNode update = new SubNode("update", all, variable);
        SubNode baseNode = new SubNode("commands", load, unload, update, info, list, mine, lookup);
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

    public void command(PlayerSession playerSession, int argsCount, String[] args){
        String arg = args[0];
        String commandName = args[1];
        switch (arg){
            case "load":{
                LoadSuccess loadSuccess = PluggableManager.getInstance().loadSpecificCommand(commandName, false);
                sendMessage(playerSession, loadSuccess.getREASON());
                break;
            }
            case "unload":{
                Command command = getCommand(playerSession, commandName);
                if (command == null){
                    return;
                }
                command.unregister();
                PluggableManager.getInstance().getCOMMANDS().remove(command.getDetails().getNAME().toLowerCase());
                sendMessage(playerSession, "Command " + command.getDetails().getNameVersion() + " was successfully unloaded.");
                break;
            }
            case "update":{
                LoadSuccess loadSuccess = PluggableManager.getInstance().loadSpecificCommand(commandName, true);
                sendMessage(playerSession, loadSuccess.getREASON());
                break;
            }
        }
    }

    private void commandInfo(PlayerSession playerSession, int argsCount, String[] args){
        String commandName = args[1];
        Command command = getCommand(playerSession, commandName);
        if (command == null){
            return;
        }
        String requestedInfo = "";
        if (argsCount == 2){
            requestedInfo = getOwnerString(requestedInfo, command) +
                            getNameString(requestedInfo, command) +
                            getAuthorString(requestedInfo, command) +
                            getProgramLanguageString(requestedInfo, command) +
                            getVersionString(requestedInfo, command) +
                            getSizeString(requestedInfo, command) +
                            getURLString(requestedInfo, command) +
                            getMainArgs(requestedInfo, command) +
                            getCanUse(requestedInfo, command) +
                            getDependenciesString(requestedInfo, command) +
                            getDescriptionString(requestedInfo, command) +
                            getPermissionsString(requestedInfo, command);
        } else {
            for (int i = 2; i < args.length; i++) {
                String requestType = args[i];
                switch (requestType){
                    case "owner":{
                        requestedInfo = getOwnerString(requestedInfo, command);
                        break;
                    }
                    case "name":{
                        requestedInfo = getNameString(requestedInfo, command);
                        break;
                    }
                    case "language":{
                        requestedInfo = getProgramLanguageString(requestedInfo, command);
                        break;
                    }
                    case "version":{
                        requestedInfo = getVersionString(requestedInfo, command);
                        break;
                    }
                    case "size":{
                        requestedInfo = getSizeString(requestedInfo, command);
                        break;
                    }
                    case "author":{
                        requestedInfo = getAuthorString(requestedInfo, command);
                        break;
                    }
                    case "url":{
                        requestedInfo = getURLString(requestedInfo, command);
                        break;
                    }
                    case "mainargs":{
                        requestedInfo = getMainArgs(requestedInfo, command);
                        break;
                    }
                    case "canuse":{
                        requestedInfo = getCanUse(requestedInfo, command);
                        break;
                    }
                    case "description":{
                        requestedInfo = getDescriptionString(requestedInfo, command);
                        break;
                    }
                    case "dependancies":{
                        requestedInfo = getDependenciesString(requestedInfo, command);
                        break;
                    }
                    case "permissions":{
                        requestedInfo = getPermissionsString(requestedInfo, command);
                        break;
                    }
                }
            }
        }
        sendMessage(playerSession, "Requested \"" + commandName + "\" Command Info: " + requestedInfo);
    }

    private Command getCommand(PlayerSession playerSession, String commandName){
        PluggableReturn<Command> pluggableReturn = PluggableManager.getInstance().getSpecificLoadedCommandOrNearMatchs(commandName);
        if (pluggableReturn.size() == 1 && pluggableReturn.isExactMatch()){
            return pluggableReturn.get(0);
        } else {
            String nearMatches = "";
            for(Command command : pluggableReturn){
                nearMatches = nearMatches + command.getDetails().getNAME().toLowerCase() + ", ";
            }
            sendMessage(playerSession, "Could not find a Command named \"" + commandName + "\". Here are some near matches: " + StringUtilities.trimCommaForPeriod(nearMatches));
            return null;
        }
    }

    private String getOwnerString(String addTo, Command command){
        String owner = command.getDetails().getORGANIZATION();
        return addTo + "Organization: " + owner + ". ";
    }

    private String getNameString(String addTo, Command command){
        String name = command.getDetails().getNAME();
        return addTo + "Name: " + name + ". ";
    }

    private String getProgramLanguageString(String addTo, Command command){
        PluggableFileType language = command.getFileType();
        String languageString = null;
        if (language == PluggableFileType.JAVA){
            languageString = "Java";
        } else if (language == PluggableFileType.PYTHON) {
            languageString = "Python";
        }
        return addTo + "Program Language: " + languageString + ". ";
    }

    private String getVersionString(String addTo, Command command){
        double version = command.getDetails().getVERSION();
        return addTo + "Version: " + version + ". ";
    }

    private String getSizeString(String addTo, Command command){
        double size = command.getDetails().getSIZE_KBS();
        return addTo + "Size: " + Double.toString((double) Math.round(size)) + "KBs. ";
    }

    private String getAuthorString(String addTo, Command command){
        String author = command.getDetails().getAUTHOR();
        return addTo + "Author: " + author + ". ";
    }

    private String getURLString(String addTo, Command command){
        String url = command.getDetails().getURL();
        return addTo + "URL: " + url + ". ";
    }

    private String getMainArgs(String addTo, Command command){
        String[] mainArgs = command.getMainArgs();
        if (mainArgs.length == 0){
            mainArgs =  new String[]{"None"};
        }
        return addTo + "Main Arguments: " + Arrays.toString(mainArgs) + ". ";
    }

    private String getCanUse(String addTo, Command command){
        CanUse canUse = command.getCanUse();
        String canUseString = null;
        if (canUse == CanUse.PLAYER){
            canUseString = "Player";
        } else if (canUse == CanUse.BOTH) {
            canUseString = "Player and Remote Player";
        } else if (canUse == CanUse.REMOTE_PLAYER){
            canUseString = "Remote Player";
        }
        return addTo + "Can Use: " + canUseString + ". ";
    }

    private String getDescriptionString(String addTo, Command command){
        String description = command.getDetails().getDESCRIPTION();
        return addTo + "Description: " + description + " ";
    }

    private String getDependenciesString(String addTo, Command command){
        String[] dependencies = command.getDetails().getDEPENDENCIES();
        if (dependencies.length == 0){
            dependencies =  new String[]{"None"};
        }
        return addTo + "Dependencies: " + Arrays.toString(dependencies) + ". ";
    }

    private String getPermissionsString(String addTo, Command command){
        String[] permissions = command.getPermissions();
        if (permissions.length == 0){
            permissions =  new String[]{"None"};
        }
        return addTo + "Permissions: " + Arrays.toString(permissions) + ". ";
    }

    public void allCommands(PlayerSession playerSession, int argsCount, String[] args){
        String arg = args[0];
        switch (arg){
            case "load":{
                loadCommandsOrUpdate(playerSession, arg);
                break;
            }
            case "unload":{
                PluggableManager.getInstance().unloadAllCommands();
                break;
            }
            case "update":{
                loadCommandsOrUpdate(playerSession, arg);
                break;
            }
        }
    }

    public void commandList(PlayerSession playerSession, int argsCount, String[] args){
        ArrayList<String> loadedArray = new ArrayList<>();
        ConcurrentHashMap<String, Command> commands = PluggableManager.getInstance().getCOMMANDS();
        commands.values().stream()
                .sorted(Comparator.comparing(p -> p.getDetails().getNAME()))
                .forEach(p -> {
                    String nameVersion = p.getDetails().getNameVersion();
                    loadedArray.add(nameVersion);
                });
        String loadedCommands = "Loaded commands: ";
        if (loadedArray.size() > 0){
            loadedCommands = loadedCommands + Arrays.toString(loadedArray.toArray()) + ". ";
        }
        sendMessage(playerSession, loadedCommands);
    }

    private String concatString(String base, String addition){
        return base + addition + ", ";
    }

    public void loadCommandsOrUpdate(PlayerSession playerSession, String arg){
        HashSet<LoadSuccess> loaded = PluggableManager.getInstance().loadAllCommands();
        if (loaded.isEmpty()){
            playerSession.sendBroadcastMessageToClient("ServerName", "It appears all commands are already " + arg + " from disk.");
            return;
        }
        ArrayList<String> loadedArray = new ArrayList<>();
        ArrayList<String> failedArray = new ArrayList<>();
        loaded.stream()
                .sorted(Comparator.comparing(LoadSuccess::getNameVersion))
                .forEach(ls -> {
                    String nameVersion = ls.getNameVersion();
                    if (ls.isSUCCESS()){
                        loadedArray.add(nameVersion);
                    } else {
                        failedArray.add(nameVersion);
                    }
                });
        String loadedCommands = "Loaded Commands: ";
        if (loadedArray.size() > 0){
            loadedCommands = loadedCommands + Arrays.toString(loadedArray.toArray()) + ". ";
        }
        if (failedArray.size() > 0){
            loadedCommands = loadedCommands + "Failed: " + Arrays.toString(failedArray.toArray()) + ". ";
        }
        sendMessage(playerSession, loadedCommands);
    }

    public void mine(PlayerSession playerSession, int argsCount, String[] args){
        String availableCommands = getAvailableCommands(playerSession);
        if (availableCommands == null){
            sendMessage(playerSession, "It appears you have no permissions for any of the loaded commands, weird seeing you used this command.");
        } else {
            sendMessage(playerSession, availableCommands);
        }
    }

    public void mineArgs(PlayerSession playerSession, int argsCount, String[] args) {
        Command command = getCommand(playerSession, args[1]);
        if (command == null){
            return;
        }
        String availableCommands = getMainArgsList(playerSession, command);
        if (availableCommands == null){
            sendMessage(playerSession, "It appears you have no permissions for any of this command at all.");
        } else {
            sendMessage(playerSession, availableCommands);
        }
    }

    public void lookup(PlayerSession playerSession, int argsCount, String[] args){
        if (argsCount == 2) {
            String playerSessionLookupString = args[1];
            PlayerSession playerSessionLookup = PlayerSession.getSession(playerSessionLookupString);
            if (playerSessionLookup == null) {
                sendMessage(playerSession, "We could not find a online player with the identifier \"" + playerSessionLookupString + "\".");
            } else {
                String availableCommands = getAvailableCommands(playerSessionLookup);
                String cleanNickName = playerSessionLookup.getCleanNickName();
                if (availableCommands == null) {
                    sendMessage(playerSession, "It appears the player \"" + cleanNickName + "\" does not have any permissions for commands.");
                } else {
                    sendMessage(playerSession, cleanNickName + "'s: " + availableCommands);
                }
            }
        } else {
            String playerSessionLookupString = args[1];
            Command command = getCommand(playerSession, args[2]);
            if (command == null){
                return;
            }
            PlayerSession playerSessionLookup = PlayerSession.getSession(playerSessionLookupString);
            if (playerSessionLookup == null){
                sendMessage(playerSession, "We could not find a online player with the identifier \"" + playerSessionLookupString + "\".");
            } else {
                String availableCommands = getMainArgsList(playerSessionLookup, command);
                String cleanNickName = playerSessionLookup.getCleanNickName();
                if (availableCommands == null) {
                    sendMessage(playerSession, "It appears the player \"" + cleanNickName + "\" does not have any permissions for commands.");
                } else {
                    sendMessage(playerSession, cleanNickName + "'s: " + availableCommands);
                }
            }
        }
    }

    private String getAvailableCommands(PlayerSession playerSession){
        ConcurrentHashMap<String, Command> commands = PluggableManager.getInstance().getCOMMANDS();
        String finalReply = "Available Commands: ";
        Object[] commandsArray = commands.values().stream()
                                    .sorted(Comparator.comparing(c -> c.getDetails().getNAME()))
                                    .filter(command -> {
                                        String organizationString = command.getDetails().getORGANIZATION().toLowerCase();
                                        String commandString = command.getDetails().getNAME().toLowerCase();
                                        return playerSession.hasPermission(organizationString, commandString, true);
                                    })
                                    .map(c -> c.getDetails().getNAME())
                                    .toArray();
        if (commandsArray.length > 0) {
            return finalReply + Arrays.toString(commandsArray) + ".";
        } else {
            return null;
        }
    }

    private String getMainArgsList(PlayerSession playerSession, Command command){
        String finalReply = "Available Arguments: ";
        String orgString = command.getDetails().getORGANIZATION().toLowerCase();
        String nameString = command.getDetails().getNAME().toLowerCase();
        boolean hasPermission = playerSession.hasPermission(orgString, nameString, true);
        if (!hasPermission){
            return "You do not have permission to use the command: " + nameString +".";
        }
        Object[] commandsArray = Stream.of(command.getMainArgs())
                .sorted(String::compareTo)
                .filter(arg -> playerSession.hasPermission(orgString, nameString, arg, true))
                .toArray();
        finalReply = finalReply + "/" + nameString;
        if (commandsArray.length == 0) {
            return  finalReply + " [NO ARGUMENTS]";
        } else {
            return finalReply + " " + Arrays.toString(commandsArray);
        }
    }

    public void sendMessage(PlayerSession playerSession, String message){
        playerSession.sendBroadcastMessageToClient("ServerName", message);
    }
}
