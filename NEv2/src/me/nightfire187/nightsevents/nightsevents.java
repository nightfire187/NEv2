package me.nightfire187.nightsevents;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.minecraft.util.org.apache.commons.lang3.Validate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.fusesource.jansi.Ansi;

import com.google.common.collect.Maps;

public class nightsevents extends JavaPlugin{
	/**
	 * All supported color values for chat
	 */
	public enum ChatColor {
	    /**
	     * Represents black
	     */
	    BLACK('0', 0x00),
	    /**
	     * Represents dark blue
	     */
	    DARK_BLUE('1', 0x1),
	    /**
	     * Represents dark green
	     */
	    DARK_GREEN('2', 0x2),
	    /**
	     * Represents dark blue (aqua)
	     */
	    DARK_AQUA('3', 0x3),
	    /**
	     * Represents dark red
	     */
	    DARK_RED('4', 0x4),
	    /**
	     * Represents dark purple
	     */
	    DARK_PURPLE('5', 0x5),
	    /**
	     * Represents gold
	     */
	    GOLD('6', 0x6),
	    /**
	     * Represents gray
	     */
	    GRAY('7', 0x7),
	    /**
	     * Represents dark gray
	     */
	    DARK_GRAY('8', 0x8),
	    /**
	     * Represents blue
	     */
	    BLUE('9', 0x9),
	    /**
	     * Represents green
	     */
	    GREEN('a', 0xA),
	    /**
	     * Represents aqua
	     */
	    AQUA('b', 0xB),
	    /**
	     * Represents red
	     */
	    RED('c', 0xC),
	    /**
	     * Represents light purple
	     */
	    LIGHT_PURPLE('d', 0xD),
	    /**
	     * Represents yellow
	     */
	    YELLOW('e', 0xE),
	    /**
	     * Represents white
	     */
	    WHITE('f', 0xF),
	    /**
	     * Represents magical characters that change around randomly
	     */
	    MAGIC('k', 0x10, true),
	    /**
	     * Makes the text bold.
	     */
	    BOLD('l', 0x11, true),
	    /**
	     * Makes a line appear through the text.
	     */
	    STRIKETHROUGH('m', 0x12, true),
	    /**
	     * Makes the text appear underlined.
	     */
	    UNDERLINE('n', 0x13, true),
	    /**
	     * Makes the text italic.
	     */
	    ITALIC('o', 0x14, true),
	    /**
	     * Resets all previous chat colors or formats.
	     */
	    RESET('r', 0x15);

	    /**
	     * The special character which prefixes all chat colour codes. Use this if you need to dynamically
	     * convert colour codes from your custom format.
	     */
	    public static final char COLOR_CHAR = '\u00A7';
	    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

	    private final int intCode;
	    private final char code;
	    private final boolean isFormat;
	    private final String toString;
	    private final static Map<Integer, ChatColor> BY_ID = Maps.newHashMap();
	    private final static Map<Character, ChatColor> BY_CHAR = Maps.newHashMap();

	    private ChatColor(char code, int intCode) {
	        this(code, intCode, false);
	    }

	    private ChatColor(char code, int intCode, boolean isFormat) {
	        this.code = code;
	        this.intCode = intCode;
	        this.isFormat = isFormat;
	        this.toString = new String(new char[] {COLOR_CHAR, code});
	    }

	    /**
	     * Gets the char value associated with this color
	     *
	     * @return A char value of this color code
	     */
	    public char getChar() {
	        return code;
	    }

	    @Override
	    public String toString() {
	        return toString;
	    }

	    /**
	     * Checks if this code is a format code as opposed to a color code.
	     */
	    public boolean isFormat() {
	        return isFormat;
	    }

	    /**
	     * Checks if this code is a color code as opposed to a format code.
	     */
	    public boolean isColor() {
	        return !isFormat && this != RESET;
	    }

	    /**
	     * Gets the color represented by the specified color code
	     *
	     * @param code Code to check
	     * @return Associative {@link org.bukkit.ChatColor} with the given code, or null if it doesn't exist
	     */
	    public static ChatColor getByChar(char code) {
	        return BY_CHAR.get(code);
	    }

	    /**
	     * Gets the color represented by the specified color code
	     *
	     * @param code Code to check
	     * @return Associative {@link org.bukkit.ChatColor} with the given code, or null if it doesn't exist
	     */
	    public static ChatColor getByChar(String code) {
	        Validate.notNull(code, "Code cannot be null");
	        Validate.isTrue(code.length() > 0, "Code must have at least one char");

	        return BY_CHAR.get(code.charAt(0));
	    }

	    /**
	     * Strips the given message of all color codes
	     *
	     * @param input String to strip of color
	     * @return A copy of the input string, without any coloring
	     */
	    public static String stripColor(final String input) {
	        if (input == null) {
	            return null;
	        }

	        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
	    }

	    /**
	     * Translates a string using an alternate color code character into a string that uses the internal
	     * ChatColor.COLOR_CODE color code character. The alternate color code character will only be replaced
	     * if it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
	     *
	     * @param altColorChar The alternate color code character to replace. Ex: &
	     * @param textToTranslate Text containing the alternate color code character.
	     * @return Text containing the ChatColor.COLOR_CODE color code character.
	     */
	    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
	        char[] b = textToTranslate.toCharArray();
	        for (int i = 0; i < b.length - 1; i++) {
	            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
	                b[i] = ChatColor.COLOR_CHAR;
	                b[i+1] = Character.toLowerCase(b[i+1]);
	            }
	        }
	        return new String(b);
	    }

	    /**
	     * Gets the ChatColors used at the end of the given input string.
	     *
	     * @param input Input string to retrieve the colors from.
	     * @return Any remaining ChatColors to pass onto the next line.
	     */
	    public static String getLastColors(String input) {
	        String result = "";
	        int length = input.length();

	        // Search backwards from the end as it is faster
	        for (int index = length - 1; index > -1; index--) {
	            char section = input.charAt(index);
	            if (section == COLOR_CHAR && index < length - 1) {
	                char c = input.charAt(index + 1);
	                ChatColor color = getByChar(c);

	                if (color != null) {
	                    result = color.toString() + result;

	                    // Once we find a color or reset we can stop searching
	                    if (color.isColor() || color.equals(RESET)) {
	                        break;
	                    }
	                }
	            }
	        }

	        return result;
	    }

	    static {
	        for (ChatColor color : values()) {
	            BY_ID.put(color.intCode, color);
	            BY_CHAR.put(color.code, color);
	        }
	    }
	}
    public String mess = "";
	public String mess1 = "";
	public String messJ = mess;

	private static Location eventLoc, eventSpec = null;
 
    public final Logger logger = Logger.getLogger("Minecraft");
     
    public static nightsevents plugin;
    FileConfiguration config;
    File cFile; 
    
    public void onDisable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        this.logger.info(pdfFile.getName() + "Has Been Disabled!");
    }
        
    public void onEnable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        this.logger.info(pdfFile.getName() + " Version" + pdfFile.getVersion() + " Has Been Enabled!");
        cFile = new File(getDataFolder(), "config.yml");
        File file = new File(getDataFolder() + File.separator + "config.yml");
		String updatecheck = "updatecheck";
        if(!file.exists()){
            this.getLogger().info(Ansi.ansi().fg(Ansi.Color.RED) + "No config.yml found." + Ansi.ansi().fg(Ansi.Color.GREEN) + " Generating a new config.yml..." + Ansi.ansi().fg(Ansi.Color.DEFAULT));
            // Prefix Config Gen
            this.getConfig().addDefault("prefix", "[NE]");
            this.getConfig().addDefault("prefixcolor", "DARK_RED");
            // Palyer sent message (mess) Config Gen
            this.getConfig().addDefault("emessagecolor", "a");
            // Player received messages
            this.getConfig().addDefault("jmessage", "You have joined a server event!");
            this.getConfig().addDefault("jmessagecolor", "5");
            this.getConfig().addDefault("jsmessage", "You have joined a spectator event!");
            this.getConfig().addDefault("jsmessagecolor", "5");
            //OpenEvent Config Gen
            this.getConfig().addDefault("oemessage", "Opening event-");
            this.getConfig().addDefault("oemessagecolor", "6");
            this.getConfig().addDefault("oemessage2", " Do /joinevent to attend.");
            this.getConfig().addDefault("ocmessage2color", "b");
            //Spectator Config Gen
            this.getConfig().addDefault("smessage", "Opening spectator event-");
            this.getConfig().addDefault("smessagecolor", "6" );
            this.getConfig().addDefault("smessage2", " Do /joinspec to attend.");
            this.getConfig().addDefault("smessage2color", "b");
            //EventStatus Config Gen
            this.getConfig().addDefault("esmessage", "Open event-");
            this.getConfig().addDefault("esmessagecolor", "6" );
            this.getConfig().addDefault("esmessage2", " Do /joinevent to attend.");
            this.getConfig().addDefault("esmessage2color", "b");
            //Resend Config Gen
            this.getConfig().addDefault("rsmessage", "Open event-");
            this.getConfig().addDefault("rsmessagecolor", "6" );
            this.getConfig().addDefault("rsmessage2", " Do /joinevent to attend.");
            this.getConfig().addDefault("rsmessage2color", "b");
            //Resend Spec Config Gen
            this.getConfig().addDefault("rsmessage3", "Open spectator event-");
            this.getConfig().addDefault("rsmessage3color", "6" );
            this.getConfig().addDefault("rsmessage4", " Do /joinspec to attend.");
            this.getConfig().addDefault("rsmessage4color", "b");
            //CloseEvent Config Gen
            this.getConfig().addDefault("cmessage", "Event has been closed!");
            this.getConfig().addDefault("cmessagecolor", "4");
            this.getConfig().addDefault("csmessage", "Event has been closed!");
            this.getConfig().addDefault("csmessagecolor", "4");
            //Close event rec message
            this.getConfig().addDefault("cmessage2", "You Have closed the active event!");
            this.getConfig().addDefault("cmessage2color", "5");
            this.getConfig().addDefault("csmessage2", "You Have closed the active spectator event!");
            this.getConfig().addDefault("csmessage2color", "5");
            this.getConfig().options().copyDefaults(true);
            this.saveDefaultConfig();
            this.logger.info(Ansi.ansi().fg(Ansi.Color.GREEN) + "Config file has been generated successfully!" + Ansi.ansi().fg(Ansi.Color.DEFAULT));
        } else {
            this.logger.info(Ansi.ansi().fg(Ansi.Color.GREEN) + "NightsEvents config file has been loaded successfully!" + Ansi.ansi().fg(Ansi.Color.DEFAULT));
        }
        if (getConfig().getBoolean(updatecheck, true)){
        UpdateChecker checker = new UpdateChecker(this, "http://dev.bukkit.org/bukkit-plugins/nightsevents/files.rss");
            if (checker.updateNeeded()){
                this.logger.info(Ansi.ansi().fg(Ansi.Color.RED) + "A new version of NightsEvents is available: " + Ansi.ansi().fg(Ansi.Color.GREEN) +  checker.getVersion() + Ansi.ansi().fg(Ansi.Color.DEFAULT));
                this.logger.info(Ansi.ansi().fg(Ansi.Color.GREEN) + "Get it from: " + checker.getLink() + Ansi.ansi().fg(Ansi.Color.DEFAULT));
                this.logger.info(Ansi.ansi().fg(Ansi.Color.GREEN) + "Direct Link: " + checker.getJarLink() + Ansi.ansi().fg(Ansi.Color.DEFAULT));
            }
        } else {
        	this.logger.info(Ansi.ansi().fg(Ansi.Color.RED) + "NightsEvents updatecheck is disabled!" + Ansi.ansi().fg(Ansi.Color.DEFAULT));
        }
        try {
            Metrics metrics = new Metrics(this); metrics.start();
        } catch (IOException e) { // Failed to submit the stats :-(
            System.out.println("Error Submitting stats!");
        }
    }
 
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        
        //---------------------DEFINING ALL STRINGS -----------------------------//
    	
        String mess2 = "";
        
        String prefix = this.getConfig().getString("prefix");
        String prefixcolor = this.getConfig().getString("prefixcolor");
        
        String oemessage = this.getConfig().getString("oemessage");
        String oemessagecolor = this.getConfig().getString("oemessagecolor");
        String oemessage2 = this.getConfig().getString("oemessage2");
        String ocmessage2color = this.getConfig().getString("ocmessage2color");
        
        String esmessage = this.getConfig().getString("esmessage");
        String esmessagecolor = this.getConfig().getString("esmessagecolor");
        String esmessage2 = this.getConfig().getString("esmessage2");
        String esmessage2color = this.getConfig().getString("esmessage2color");
        
        String rsmessage = this.getConfig().getString("rsmessage");
        String rsmessagecolor = this.getConfig().getString("rsmessagecolor");
        String rsmessage2 = this.getConfig().getString("rsmessage2");
        String rsmessage2color = this.getConfig().getString("rsmessage2color");
        
        String rsmessage3 = this.getConfig().getString("rsmessage3");
        String rsmessage3color = this.getConfig().getString("rsmessage3color");
        String rsmessage4 = this.getConfig().getString("rsmessage4");
        String rsmessage4color = this.getConfig().getString("rsmessage4color");
        
        String jmessage = this.getConfig().getString("jmessage");
        String jmessagecolor = this.getConfig().getString("jmessagecolor");
        String jsmessage = this.getConfig().getString("jsmessage");
        String jsmessagecolor = this.getConfig().getString("jsmessagecolor");
        
        String smessage = this.getConfig().getString("smessage");
        String smessagecolor = this.getConfig().getString("smessagecolor");
        String smessage2 = this.getConfig().getString("smessage2");
        String smessage2color = this.getConfig().getString("smessage2color");
        
        String cmessage = this.getConfig().getString("cmessage");
        String cmessagecolor = this.getConfig().getString("cmessagecolor");
        String csmessage = this.getConfig().getString("csmessage");
        String csmessagecolor = this.getConfig().getString("csmessagecolor");
        
        String cmessage2 = this.getConfig().getString("cmessage2");
        String cmessage2color = this.getConfig().getString("cmessage2color");
        String csmessage2 = this.getConfig().getString("csmessage2");
        String csmessage2color = this.getConfig().getString("csmessage2color");
        
        String emessagecolor = this.getConfig().getString("emessagecolor");
        
        //---------------------OPEN EVENT FUNCTION -----------------------------//
      
		if(cmd.getName().equalsIgnoreCase("openevent")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventLoc == null) {
                    	if (args.length != 0){
                            for(int i = 0; i<args.length;i++) { 
                                mess = mess + args[i] +" ";
                            }
                            eventLoc = ((Player) sender).getLocation();
                            Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + " " + ChatColor.getByChar(oemessagecolor) + oemessage + "  " + ChatColor.getByChar(emessagecolor) + mess);
                            Bukkit.getServer().broadcastMessage(ChatColor.getByChar(ocmessage2color) + "      " + oemessage2);
                    	} else {
                    		sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to create server events without names.");
                    	}
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There is already an active event.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot manage server events.");
            }
            
        //---------------------SPECTATOR OPEN EVENT FUNCTION -----------------------------//
            
        }else if(cmd.getName().equalsIgnoreCase("openspec")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventSpec == null) {
                    	if (args.length != 0){
                            for(int i = 0; i<args.length;i++) { 
                                mess1 = mess1 + args[i] +" ";
                            }
                            eventSpec = ((Player) sender).getLocation();
                            Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + " " + ChatColor.getByChar(smessagecolor) + smessage + "  " + ChatColor.getByChar(emessagecolor) + mess1);
                            Bukkit.getServer().broadcastMessage(ChatColor.getByChar(smessage2color) + "      " + smessage2);
                    	} else {
                    		sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to create spectator events without names.");
                    	}
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There is already an active spectater event.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot manage spectator events.");
            }
            
        //---------------------RESEND EVENT FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("resendevent")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventLoc != null) {
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + " " + ChatColor.getByChar(rsmessagecolor) + rsmessage + "  " + ChatColor.getByChar(emessagecolor) + mess);
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(rsmessage2color) + "      " + rsmessage2);
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There are no open events.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot manage events.");
            }
            
            //---------------------RESEND SPEC EVENT FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("resendspec")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventLoc != null) {
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + " " + ChatColor.getByChar(rsmessage3color) + rsmessage3 + "  " + ChatColor.getByChar(emessagecolor) + mess1);
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(rsmessage4color) + "      " + rsmessage4);
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There are no open events.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot manage events.");
            }
                
            //---------------------EVENT STATUS FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("eventstatus")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventLoc != null) {
                        for(int i = 0; i<args.length;i++) {
                            mess2 = mess2 + args[i] +" ";
                        }
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + " " + ChatColor.getByChar(esmessagecolor) + esmessage + "  " + ChatColor.getByChar(emessagecolor) + mess2);
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(esmessage2color) + "      " + esmessage2);
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There are no open events.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot manage events.");
            }
            
        //---------------------CONFIG RELOAD COMMAND----------------------------//ChatColor.valueOf()
            
        } else if(cmd.getName().equalsIgnoreCase("nereload")) {
            if ((sender.hasPermission("nightsevents.manage"))) {
              	config = YamlConfiguration.loadConfiguration(cFile);
              	this.reloadConfig();
            	sender.sendMessage(ChatColor.GREEN + "NightsEvents has been reloaded!");
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
            }
            
        //---------------------JOIN EVENT FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("joinevent")) { 
            if (sender instanceof Player) { 
                if (sender.hasPermission("nightsevents.join")) { 
                    if (eventLoc != null) {
                        if (args.length != 0){ 
                        	for(int i = 0; i<args.length;i++){
                        	    messJ = messJ + args +" ";
                        	    }
                        	if (messJ == mess);{ 
                                ((Player) sender).teleport(eventLoc, TeleportCause.PLUGIN); 
                                sender.sendMessage(ChatColor.getByChar(jmessagecolor) + jmessage); 
                	    	}
                	    } else {
                	    	sender.sendMessage(ChatColor.DARK_RED + "Do /joinevent [eventname]");   
                	    }
                	} else {
                		sender.sendMessage(ChatColor.DARK_RED + "There is not an active event to join.");
                	}
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to join server events,  if this is a mistake please contact a moderator.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console,  can't join events.");
            }
            
	   //---------------------JOIN SPECTATOR EVENT FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("joinspec")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.join")) {
                	if (eventSpec != null) {
                	    if (args.length != 0) { 
                	    	if (args.clone().equals(mess1)) {
                	            ((Player) sender).teleport(eventSpec, TeleportCause.PLUGIN);
       			    	        sender.sendMessage(ChatColor.getByChar(jsmessagecolor) + jsmessage);
                	    	}
                	    } else {
                	    	sender.sendMessage(ChatColor.DARK_RED + "Please include the spectator event name");   
                	        }
                	} else {
                		sender.sendMessage(ChatColor.DARK_RED + "There is not an active event to join.");
                	}
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to join spectator events,  if this is a mistake please contact a moderator.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console,  can't join events.");
            }
            
       //---------------------CLOSE EVENT FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("closeevent")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventLoc != null) {
                        eventLoc = null;
                        sender.sendMessage(ChatColor.getByChar(cmessage2color) + cmessage2);
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + "  " + ChatColor.getByChar(cmessagecolor) + cmessage);
                        mess = "";
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There is no active event.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot manage events.");
            }
            
        //---------------------CLOSE SPECTATOR EVENT FUNCTION -----------------------------//
            
        } else if(cmd.getName().equalsIgnoreCase("closespec")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("nightsevents.manage")) {
                    if (eventSpec != null) {
                        eventSpec = null;
                        sender.sendMessage(ChatColor.getByChar(csmessage2color) + csmessage2);
                        mess1 = "";
                        Bukkit.getServer().broadcastMessage(ChatColor.getByChar(prefixcolor) + prefix + "  " + ChatColor.getByChar(csmessagecolor) + csmessage);
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "There is no active spectater event.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to manage events.");
                }
            } else {
                sender.sendMessage(Ansi.ansi().fg(Ansi.Color.RED) + "Console cannot close events.");
            }
        }
        return true;
    }
}
