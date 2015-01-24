package org.starnub.commands;

import org.starnub.starnubserver.connections.player.session.PlayerSession;
import org.starnub.starnubserver.pluggable.Command;
import org.starnub.starnubserver.pluggable.PluggableFileType;
import org.starnub.starnubserver.pluggable.PluggableManager;
import org.starnub.starnubserver.pluggable.Plugin;
import org.starnub.starnubserver.pluggable.commandprocessor.*;
import org.starnub.starnubserver.pluggable.generic.LoadSuccess;
import org.starnub.starnubserver.pluggable.generic.PluggableReturn;
import org.starnub.utilities.strings.StringUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Plugins extends Command {

    private final RootNode ROOT_COMMAND_NODE;

    public Plugins() {
        EndNode all = new EndNode("all", ArgumentType.STATIC, this::allPlugins);
        EndNode variable = new EndNode("{plugin-name}", ArgumentType.VARIABLE, this::plugin);
        EndNode infoVariable = new EndNode("{plugin-name} Optional: {name, owner, progamlanguage, version, size, author, url, description, dependancies, permissions} - Can use more then one at a time.", ArgumentType.VARIABLE, this::pluginInfo);
        EndNode list = new EndNode("list", ArgumentType.STATIC, this::pluginList);
        SubNode loadEnable = new SubNode("enable", all, variable);
        SubNode load = new SubNode("load", loadEnable, all, variable);
        SubNode unload = new SubNode("unload", all, variable);
        SubNode enable = new SubNode("enable", all, variable);
        SubNode update = new SubNode("update", all, variable);
        SubNode disable = new SubNode("disable", all, variable);
        SubNode info = new SubNode("info", infoVariable);
        SubNode baseNode = new SubNode("plugins", load, unload, enable, update, disable, info, list);
        ROOT_COMMAND_NODE = new RootNode(baseNode);
    }

    @Override
    public void onRegister() {
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

    public void plugin(PlayerSession playerSession, int argsCount, String[] args){
        String arg = args[0];
        String pluginName;
        if (argsCount == 2) {
            pluginName = args[1];
        } else {
            pluginName = args[2];
        }
        switch (arg){
            case "load":{
                boolean enable = false;
                if (argsCount == 3){
                    String argTest2 = args[1];
                    if (argTest2.equalsIgnoreCase("enable")){
                        enable = true;
                    }
                }
                LoadSuccess loadSuccess = PluggableManager.getInstance().loadSpecificPlugin(pluginName, enable, false);
                sendMessage(playerSession, loadSuccess.getREASON());
                break;
            }
            case "unload":{
                Plugin plugin = getPlugin(playerSession, pluginName);
                if (plugin == null){
                    return;
                }
                boolean enabled = plugin.isEnabled();
                plugin.unregister();
                plugin.disable();
                PluggableManager.getInstance().getPLUGINS().remove(plugin.getDetails().getNAME().toLowerCase());
                if (!enabled){
                    sendMessage(playerSession, "Plugin " + plugin.getDetails().getNameVersion() + " was successfully unloaded.");
                } else {
                    sendMessage(playerSession, "Plugin " + plugin.getDetails().getNameVersion() + " was successfully disabled and unloaded.");
                }
                break;
            }
            case "update":{
                LoadSuccess loadSuccess = PluggableManager.getInstance().loadSpecificPlugin(pluginName, true, true);
                sendMessage(playerSession, loadSuccess.getREASON());
                break;
            }
            case "enable":{
                Plugin plugin = getPlugin(playerSession, pluginName);
                if (plugin == null){
                    return;
                }
                if (!plugin.isEnabled()) {
                    plugin.register();
                    plugin.enable();
                    sendMessage(playerSession, "Plugin " + plugin.getDetails().getNameVersion() + " was successfully enabled.");
                } else {
                    sendMessage(playerSession, "Plugin " + plugin.getDetails().getNameVersion() + " was already enabled.");
                }
                break;
            }
            case "disable":{
                Plugin plugin = getPlugin(playerSession, pluginName);
                if (plugin == null){
                    return;
                }
                if (plugin.isEnabled()) {
                    plugin.unregister();
                    plugin.disable();
                    sendMessage(playerSession, "Plugin " + plugin.getDetails().getNameVersion() + " was successfully disabled.");
                } else {
                    sendMessage(playerSession, "Plugin " + plugin.getDetails().getNameVersion() + " was already disabled.");
                }
                break;
            }
        }
    }

    private void pluginInfo(PlayerSession playerSession, int argsCount, String[] args){
        String pluginName = args[1];
        Plugin plugin = getPlugin(playerSession, pluginName);
        if (plugin == null){
            return;
        }
        String requestedInfo = "";
        if (argsCount == 2){
            requestedInfo = getOwnerString(requestedInfo, plugin) +
                            getNameString(requestedInfo, plugin) +
                            getAuthorString(requestedInfo, plugin) +
                            getProgramLanguageString(requestedInfo, plugin) +
                            getVersionString(requestedInfo, plugin) +
                            getSizeString(requestedInfo, plugin) +
                            getURLString(requestedInfo, plugin) +
                            getDependenciesString(requestedInfo, plugin) +
                            getDescriptionString(requestedInfo, plugin) +
                            getPermissionsString(requestedInfo, plugin);
        } else {
            for (int i = 2; i < args.length; i++) {
                String requestType = args[i];
                switch (requestType){
                    case "owner":{
                        requestedInfo = getOwnerString(requestedInfo, plugin);
                        break;
                    }
                    case "name":{
                        requestedInfo = getNameString(requestedInfo, plugin);
                        break;
                    }
                    case "language":{
                        requestedInfo = getProgramLanguageString(requestedInfo, plugin);
                        break;
                    }
                    case "version":{
                        requestedInfo = getVersionString(requestedInfo, plugin);
                        break;
                    }
                    case "size":{
                        requestedInfo = getSizeString(requestedInfo, plugin);
                        break;
                    }
                    case "author":{
                        requestedInfo = getAuthorString(requestedInfo, plugin);
                        break;
                    }
                    case "url":{
                        requestedInfo = getURLString(requestedInfo, plugin);
                        break;
                    }
                    case "description":{
                        requestedInfo = getDescriptionString(requestedInfo, plugin);
                        break;
                    }
                    case "dependancies":{
                        requestedInfo = getDependenciesString(requestedInfo, plugin);
                        break;
                    }
                    case "permissions":{
                        requestedInfo = getPermissionsString(requestedInfo, plugin);
                        break;
                    }
                }
            }
        }
        sendMessage(playerSession, "Requested \"" + pluginName + "\" Plugin Info: " + requestedInfo);
    }

    private Plugin getPlugin(PlayerSession playerSession, String pluginName){
        PluggableReturn<Plugin> pluggableReturn = PluggableManager.getInstance().getSpecificLoadedPluginOrNearMatchs(pluginName);
        if (pluggableReturn.size() == 1 && pluggableReturn.isExactMatch()){
            return pluggableReturn.get(0);
        } else {
            String nearMatches = "";
            for(Plugin plugin : pluggableReturn){
                nearMatches = nearMatches + plugin.getDetails().getNameVersion() + ", ";
            }
            sendMessage(playerSession, "Could not find a Plugin named \"" + pluginName + "\". Here are some near matches: " + StringUtilities.trimCommaForPeriod(nearMatches));
            return null;
        }
    }

    private String getOwnerString(String addTo, Plugin plugin){
        String owner = plugin.getDetails().getORGANIZATION();
        return addTo + "Organization: " + owner + ". ";
    }

    private String getNameString(String addTo, Plugin plugin){
        String name = plugin.getDetails().getNAME();
        return addTo + "Name: " + name + ". ";
    }

    private String getProgramLanguageString(String addTo, Plugin plugin){
        PluggableFileType language = plugin.getFileType();
        String languageString = null;
        if (language == PluggableFileType.JAVA){
            languageString = "Java";
        } else if (language == PluggableFileType.PYTHON) {
            languageString = "Python";
        }
        return addTo + "Program Language: " + languageString + ". ";
    }

    private String getVersionString(String addTo, Plugin plugin){
        double version = plugin.getDetails().getVERSION();
        return addTo + "Version: " + version + ". ";
    }

    private String getSizeString(String addTo, Plugin plugin){
        double size = plugin.getDetails().getSIZE_KBS();
        return addTo + "Size: " + Double.toString((double) Math.round(size * 100) / 100) + "KBs. ";
    }

    private String getAuthorString(String addTo, Plugin plugin){
        String author = plugin.getDetails().getAUTHOR();
        return addTo + "Author: " + author + ". ";
    }

    private String getURLString(String addTo, Plugin plugin){
        String url = plugin.getDetails().getURL();
        return addTo + "URL: " + url + ". ";
    }

    private String getDescriptionString(String addTo, Plugin plugin){
        String description = plugin.getDetails().getDESCRIPTION();
        return addTo + "Description: " + description + " ";
    }

    private String getDependenciesString(String addTo, Plugin plugin){
        String[] dependencies = plugin.getDetails().getDEPENDENCIES();
        if (dependencies.length == 0){
            dependencies =  new String[]{"None"};
        }
        return addTo + "Dependencies: " + Arrays.toString(dependencies) + ". ";
    }

    private String getPermissionsString(String addTo, Plugin plugin){
        String[] permissions = plugin.getAdditionalPermissions();
        if (permissions.length == 0){
            permissions =  new String[]{"None"};
        }
        return addTo + "Permissions: " + Arrays.toString(permissions) + ". ";
    }

    public void allPlugins(PlayerSession playerSession, int argsCount, String[] args){
        String arg = args[0];
        switch (arg){
            case "load":{
                boolean enable = false;
                if (argsCount == 3){
                    String argTest2 = args[2];
                    if (argTest2.equalsIgnoreCase("enable")){
                        enable = true;
                    }
                }
                loadPluginsOrUpdate(playerSession, enable, arg);
                break;
            }
            case "unload":{
                PluggableManager.getInstance().unloadAllPlugins();
                break;
            }
            case "update":{
                loadPluginsOrUpdate(playerSession, true, arg);
                break;
            }
            case "enable":{
                PluggableManager.getInstance().enableAllPlugins();
                break;
            }
            case "disable":{
                PluggableManager.getInstance().disableAllPlugins();
                break;
            }
        }
    }

    public void pluginList(PlayerSession playerSession, int argsCount, String[] args){
        ArrayList<String> enabledArray = new ArrayList<>();
        ArrayList<String> disableArray = new ArrayList<>();
        ConcurrentHashMap<String, Plugin> plugins = PluggableManager.getInstance().getPLUGINS();
        plugins.values().stream()
                .sorted(Comparator.comparing(p -> p.getDetails().getNAME()))
                .forEach(p -> {
                    String nameVersion = p.getDetails().getNameVersion();
                    if (p.isEnabled()) {
                        enabledArray.add(nameVersion);
                    } else {
                        disableArray.add(nameVersion);
                    }
                });
        String loadedPlugins = "Loaded plugins: ";
        if (enabledArray.size() > 0){
            loadedPlugins = loadedPlugins + "Enabled: " + Arrays.toString(enabledArray.toArray()) + ". ";
        }
        if (disableArray.size() > 0){
            loadedPlugins = loadedPlugins + "Disabled: " + Arrays.toString(disableArray.toArray()) + ". ";
        }
        sendMessage(playerSession, loadedPlugins);
    }

    private String concatString(String base, String addition){
        return base + addition + ", ";
    }


    public void loadPluginsOrUpdate(PlayerSession playerSession, boolean enable, String arg){
        HashSet<LoadSuccess> loaded = PluggableManager.getInstance().loadAllPlugins(enable);
        if (loaded.isEmpty()){
            playerSession.sendBroadcastMessageToClient("ServerName", "It appears all plugins are already " + arg + " from disk.");
            return;
        }
        ArrayList<String> enabledArray = new ArrayList<>();
        ArrayList<String> disableArray = new ArrayList<>();
        ArrayList<String> failedArray = new ArrayList<>();
        loaded.stream()
                .sorted(Comparator.comparing(LoadSuccess::getNameVersion))
                .forEach(ls -> {
                    String nameVersion = ls.getNameVersion();
                    if (ls.isSUCCESS()){
                        if (ls.isEnabled()) {
                            enabledArray.add(nameVersion);
                        } else {
                            disableArray.add(nameVersion);
                        }
                    } else {
                        failedArray.add(nameVersion);
                    }
                });
        String loadedPlugins = "Loaded plugins: ";
        if (enabledArray.size() > 0){
            loadedPlugins = loadedPlugins + "Enabled: " + Arrays.toString(enabledArray.toArray()) + ". ";
        }
        if (disableArray.size() > 0){
            loadedPlugins = loadedPlugins + "Disabled: " + Arrays.toString(disableArray.toArray()) + ". ";
        }
        if (failedArray.size() > 0){
            loadedPlugins = loadedPlugins + "Failed: " + Arrays.toString(failedArray.toArray()) + ". ";
        }
        sendMessage(playerSession, loadedPlugins);
    }

    public void sendMessage(PlayerSession playerSession, String message){
        playerSession.sendBroadcastMessageToClient("ServerName", message);
    }
}
