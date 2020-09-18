package me.excuze.setrespawn;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import me.excuze.setrespawn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  public static File datadir;
  
  public static File configcreate;
  
  public static FileConfiguration config;
  
  public static FileConfiguration data;
  
  public static File datafile;
  
  public static FileConfiguration configdata;
  
  public static File configdatafile;
  
  private Map<String, Long> cooldowns = new HashMap<>();
  
  public static double round(double value, int places) {
    if (places < 0)
      throw new IllegalArgumentException(); 
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }
  
  public static boolean isInteger(String s, int radix) {
    Scanner sc = new Scanner(s.trim());
    if (!sc.hasNextInt(radix))
      return false; 
    sc.nextInt(radix);
    return !sc.hasNext();
  }
  
  YamlConfiguration playerconfig = new YamlConfiguration();
  
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, (Plugin)this);
    saveDefaultConfig();
    configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
    if (!configcreate.exists())
      try {
        configcreate.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }  
    config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
    try {
      config.save(configcreate);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    String path = "a";
    try {
      path = (new File(".")).getCanonicalPath().toString();
    } catch (IOException e1) {
      e1.printStackTrace();
    } 
    datadir = new File(String.valueOf(path) + "/plugins/PersonalSpawnPoint/data");
    if (!datadir.exists())
      datadir.mkdir(); 
  }
  
  public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
    OfflinePlayer p;
    if (cmd.getName().equalsIgnoreCase("psp")) {
      if (!config.getBoolean("psp.enabled")) {
        sender.sendMessage(Utils.chat("&cCommands can't be run while the plugin is disabled. Set psp.enabled to true in the config.yml to use commands."));
        return true;
      } 
      if (args.length > 0) {
        if (args[0].equalsIgnoreCase("help")) {
          if (sender.hasPermission("psp.*")) {
            sender.sendMessage(Utils.chat("&bHelp Page for PSP:\n&5/psp [player]: &aShow personal spawn point (if set).\n&5/psp set: &aSet personal spawn to your current location.\nThere is a " + 
                  
                  config.getInt("psp.cooldown") + " second cooldown for this action!\n" + 
                  "&5/psp unset [player]: &aDelete personal spawn (reset to world).\n" + 
                  "&5/psp cooldown [seconds]: &aWithout argument, display the current\nglobal cooldown timer. Use argument to update it immediately.\n" + 
                  "&5/psp nuke [force]: &aDelete &lALL &aplayer spawn points.\n\n" + 
                  "&c&l<!> &4The following are strictly for server administration: &c&l<!>\n\n" + 
                  "&5/psp collective [switch]: &aWith no argument, displays whether\nPSP is using a flat file for the database. Use the argument\nto switch to (and from) discreet per-player files on the fly.\n" + 
                  "&5/psp reload: &aReloads the config settings for PSP.\n" + 
                  "&bPSP by Tohbot (Discord username: Excuze#3063)"));
            return true;
          } 
          sender.sendMessage(Utils.chat("&bHelp Page for PSP:\n&5/psp [player]: &2Show personal spawn point (if set).\n&5/psp set: &2Set personal spawn to your current location.\nThere is a " + 
                
                config.getInt("psp.cooldown") + " second cooldown for this action!\n" + 
                "&5/psp unset [player]: &2Delete personal spawn (reset to world).\n" + 
                "&bPSP by Tohbot (Discord username: Excuze#3063)"));
          return true;
        } 
        if (args[0].equalsIgnoreCase("set")) {
          if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.chat("&cOnly players can set their spawn."));
            return true;
          } 
          Player player = (Player)sender;
          if (this.cooldowns.containsKey(player.getName())) {
            if (((Long)this.cooldowns.get(player.getName())).longValue() > System.currentTimeMillis()) {
              long timeleft = (((Long)this.cooldowns.get(player.getName())).longValue() - System.currentTimeMillis()) / 1000L;
              player.sendMessage(Utils.chat("&cYou have " + timeleft + " seconds before you can use this command again."));
              return true;
            } 
            this.cooldowns.remove(player.getName());
            this.cooldowns.put(player.getName(), Long.valueOf(System.currentTimeMillis() + (config.getInt("psp.cooldown") * 1000)));
          } else {
            this.cooldowns.put(player.getName(), Long.valueOf(System.currentTimeMillis() + (config.getInt("psp.cooldown") * 1000)));
          } 
          if (player.getWorld().getEnvironment() == World.Environment.NETHER || player.getWorld().getEnvironment() == World.Environment.THE_END) {
            player.sendMessage(Utils.chat("&cYou can only set a personal spawn point in the Overworld."));
            return true;
          } 
          Location spawnloc = player.getLocation();
          if (!config.getBoolean("psp.collective")) {
            datafile = new File(datadir + "/" + player.getUniqueId() + ".yml");
            if (!datafile.exists())
              try {
                datafile.createNewFile();
              } catch (IOException e) {
                e.printStackTrace();
              }  
            data = (FileConfiguration)YamlConfiguration.loadConfiguration(datafile);
            data.set("spawnloc", spawnloc);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            data.set("spawndate", formatter.format(date));
            try {
              data.save(datafile);
            } catch (IOException e) {
              e.printStackTrace();
            } 
          } else {
            configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
            if (!configdatafile.exists())
              try {
                configdatafile.createNewFile();
              } catch (IOException e) {
                e.printStackTrace();
              }  
            configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
            configdata.set("playerdata." + player.getUniqueId() + ".spawnloc", spawnloc);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            configdata.set("playerdata." + player.getUniqueId() + ".spawndate", formatter.format(date));
            try {
              configdata.save(configdatafile);
            } catch (IOException e) {
              e.printStackTrace();
            } 
          } 
          player.sendMessage(Utils.chat("&aSet your personal spawn point to your current location."));
          return true;
        } 
        if (args[0].equalsIgnoreCase("unset")) {
          if (args.length < 2 && !(sender instanceof Player)) {
            sender.sendMessage(Utils.chat("&cOnly players can delete their own spawn info."));
            return true;
          } 
          if (args.length < 2) {
            Player player = (Player)sender;
          } else {
            if (!sender.hasPermission("psp.delothers")) {
              sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
              return true;
            } 
            if (args[1].length() > 16) {
              sender.sendMessage(Utils.chat("&cThat's not a valid username."));
              return true;
            } 
            if (Bukkit.getServer().getOfflinePlayer(args[1]) == null) {
              sender.sendMessage(Utils.chat("&cThat player doesn't seem to exist."));
              return true;
            } 
            p = Bukkit.getServer().getOfflinePlayer(args[1]);
          } 
          if (!config.getBoolean("psp.collective")) {
            datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
            if (!datafile.exists()) {
              sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
              return true;
            } 
            datafile.delete();
            sender.sendMessage(Utils.chat("&cDeleted PSP data for user " + p.getName() + "."));
          } else {
            configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
            if (!configdatafile.exists()) {
              sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
              return true;
            } 
            configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
            if (configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc") == null) {
              sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
              return true;
            } 
            configdata.set("playerdata." + p.getUniqueId(), null);
            try {
              configdata.save(configdatafile);
            } catch (IOException e) {
              e.printStackTrace();
            } 
            sender.sendMessage(Utils.chat("&cDeleted PSP data for user " + p.getName() + "."));
          } 
          return true;
        } 
        if (args[0].equalsIgnoreCase("nuke")) {
          if (!sender.hasPermission("psp.nuke")) {
            sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
            return true;
          } 
          if (!config.getBoolean("psp.opscannuke")) {
            sender.sendMessage(Utils.chat("&cCommand based nuking is disabled in the config."));
            return true;
          } 
          if (args.length < 2) {
            sender.sendMessage(Utils.chat("&cNuking will irreversibly destroy &lall &cplayer data that PSP has &lever &ccreated. Run &l/psp nuke force &cif you'd like to continue."));
            return true;
          } 
          if (!args[1].equalsIgnoreCase("force")) {
            sender.sendMessage(Utils.chat("&cNuking will irreversibly destroy &lall &cplayer data that PSP has &lever &ccreated. Run &l/psp nuke force &cif you'd like to continue."));
            return true;
          } 
          sender.sendMessage(Utils.chat("&cI hope you know what you're doing xD"));
          Bukkit.getLogger().info(Utils.chat("&c<!> WARNING <!> &4User " + sender.getName() + " just nuked all PSP data. If you didn't allow this, that might be a problem. &c<!> WARNING <!>"));
          Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable() {
                public void run() {
                  try {
                    File nuke = new File(String.valueOf(Main.this.getDataFolder().toString()) + "/playerdata.yml");
                    if (nuke.exists())
                      nuke.delete(); 
                    nuke = new File(String.valueOf(Main.this.getDataFolder().toString()) + "/data");
                    if (nuke.exists()) {
                      String[] entries = nuke.list();
                      byte b;
                      int i;
                      String[] arrayOfString1;
                      for (i = (arrayOfString1 = entries).length, b = 0; b < i; ) {
                        String s = arrayOfString1[b];
                        File currentFile = new File(nuke.getPath(), s);
                        currentFile.delete();
                        b++;
                      } 
                      nuke.delete();
                    } 
                    sender.sendMessage(Utils.chat("&cData fully destroyed."));
                    return;
                  } catch (Exception e) {
                    sender.sendMessage(Utils.chat("&cError deleting data. Most likely, the data is already deleted. Check server logs for additional info."));
                    e.printStackTrace();
                    return;
                  } 
                }
              }40L);
          return true;
        } 
        if (args[0].equalsIgnoreCase("collective")) {
          if (!sender.hasPermission("psp.convert")) {
            sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
            return true;
          } 
          if (args.length < 2) {
            if (config.getBoolean("psp.collective")) {
              sender.sendMessage(Utils.chat("&aData is currently being stored and read in a flat file."));
            } else {
              sender.sendMessage(Utils.chat("&aData is currently being stored and read in separate per-person files."));
            } 
            return true;
          } 
          if (!args[1].equalsIgnoreCase("switch")) {
            if (config.getBoolean("psp.collective")) {
              sender.sendMessage(Utils.chat("&aData is currently being stored and read in a flat file."));
            } else {
              sender.sendMessage(Utils.chat("&aData is currently being stored and read in separate per-person files."));
            } 
            return true;
          } 
          if (!config.getBoolean("psp.collective")) {
            File convert = new File(String.valueOf(getDataFolder().toString()) + "/data");
            configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
            if (!configdatafile.exists())
              try {
                configdatafile.createNewFile();
              } catch (IOException e) {
                e.printStackTrace();
              }  
            configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
            int failed = 0;
            if (convert.exists()) {
              String[] entries = convert.list();
              byte b;
              int i;
              String[] arrayOfString1;
              for (i = (arrayOfString1 = entries).length, b = 0; b < i; ) {
                String s = arrayOfString1[b];
                File convertconfigfile = new File(convert.getPath(), s);
                if (convertconfigfile.exists()) {
                  YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(convertconfigfile);
                  if (yamlConfiguration.get("spawnloc") != null && yamlConfiguration.get("spawndate") != null) {
                    configdata.set("playerdata." + s.substring(0, s.length() - 4) + ".spawnloc", yamlConfiguration.get("spawnloc"));
                    configdata.set("playerdata." + s.substring(0, s.length() - 4) + ".spawndate", yamlConfiguration.get("spawndate"));
                  } else {
                    failed++;
                  } 
                  try {
                    configdata.save(configdatafile);
                  } catch (IOException e) {
                    e.printStackTrace();
                  } 
                } 
                b++;
              } 
              config.set("psp.collective", Boolean.valueOf(true));
              try {
                config.save(configcreate);
              } catch (IOException e) {
                e.printStackTrace();
              } 
              configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
              if (!configcreate.exists())
                try {
                  configcreate.createNewFile();
                } catch (IOException e) {
                  e.printStackTrace();
                }  
              config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
              try {
                config.save(configcreate);
              } catch (IOException e) {
                e.printStackTrace();
              } 
              String path = "a";
              try {
                path = (new File(".")).getCanonicalPath().toString();
              } catch (IOException e1) {
                e1.printStackTrace();
              } 
              datadir = new File(String.valueOf(path) + "/plugins/PersonalSpawnPoint/data");
              if (!datadir.exists())
                datadir.mkdir(); 
              sender.sendMessage(Utils.chat("&aJob complete! A total of &c" + failed + " &aconversions failed. If this number is greater than 0, it may be the result of bad data."));
            } else {
              sender.sendMessage(Utils.chat("&cI couldn't find the data folder! Are you sure it exists?"));
              return true;
            } 
          } else {
            if (!sender.hasPermission("psp.convert")) {
              sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
              return true;
            } 
            if (args.length < 2) {
              sender.sendMessage(Utils.chat("&cToo few arguments!"));
              return true;
            } 
            File convert = new File(String.valueOf(getDataFolder().toString()) + "/data");
            if (!convert.exists())
              convert.mkdir(); 
            configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
            if (!configdatafile.exists()) {
              sender.sendMessage(Utils.chat("&cI was unable to find any stored playerdata in playerdata.yml."));
              return true;
            } 
            configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
            Set<String> dataset = configdata.getConfigurationSection("playerdata").getKeys(false);
            if (dataset == null) {
              sender.sendMessage(Utils.chat("&cI was unable to find any stored playerdata in playerdata.yml."));
              return true;
            } 
            int failed = 0;
            for (String s : dataset) {
              File convertconfigfile = new File(convert.getPath(), String.valueOf(s) + ".yml");
              if (!convertconfigfile.exists())
                try {
                  convertconfigfile.createNewFile();
                } catch (IOException e) {
                  e.printStackTrace();
                }  
              YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(convertconfigfile);
              if (configdata.getLocation("playerdata." + s + ".spawnloc") != null && configdata.getString("playerdata." + s + ".spawndate") != null) {
                yamlConfiguration.set("spawnloc", configdata.getLocation("playerdata." + s + ".spawnloc"));
                yamlConfiguration.set("spawndate", configdata.getString("playerdata." + s + ".spawndate"));
              } else {
                failed++;
              } 
              try {
                yamlConfiguration.save(convertconfigfile);
              } catch (IOException e) {
                e.printStackTrace();
              } 
            } 
            config.set("psp.collective", Boolean.valueOf(false));
            try {
              config.save(configcreate);
            } catch (IOException e) {
              e.printStackTrace();
            } 
            configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
            if (!configcreate.exists())
              try {
                configcreate.createNewFile();
              } catch (IOException e) {
                e.printStackTrace();
              }  
            config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
            try {
              config.save(configcreate);
            } catch (IOException e) {
              e.printStackTrace();
            } 
            String path = "a";
            try {
              path = (new File(".")).getCanonicalPath().toString();
            } catch (IOException e1) {
              e1.printStackTrace();
            } 
            datadir = new File(String.valueOf(path) + "/plugins/PersonalSpawnPoint/data");
            if (!datadir.exists())
              datadir.mkdir(); 
            sender.sendMessage(Utils.chat("&aJob complete! A total of &c" + failed + " &aconversions failed. If this number is greater than 0, it may be the result of bad data."));
          } 
          return true;
        } 
        if (args[0].equalsIgnoreCase("reload")) {
          if (!sender.hasPermission("psp.reload")) {
            sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
            return true;
          } 
          configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
          if (!configcreate.exists())
            try {
              configcreate.createNewFile();
            } catch (IOException e) {
              e.printStackTrace();
            }  
          config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
          try {
            config.save(configcreate);
          } catch (IOException e) {
            e.printStackTrace();
          } 
          String path = "a";
          try {
            path = (new File(".")).getCanonicalPath().toString();
          } catch (IOException e1) {
            e1.printStackTrace();
          } 
          datadir = new File(String.valueOf(path) + "/plugins/PersonalSpawnPoint/data");
          if (!datadir.exists())
            datadir.mkdir(); 
          sender.sendMessage(Utils.chat("&aReload complete! Updated config.yml settings."));
          return true;
        } 
        if (args[0].equalsIgnoreCase("cooldown")) {
          if (!sender.hasPermission("psp.cooldown")) {
            sender.sendMessage("&cYou don't have permission to do this!");
            return true;
          } 
          if (args.length < 2) {
            sender.sendMessage(Utils.chat("&aThe current /psp set cooldown is " + config.getInt("psp.cooldown") + " seconds."));
            return true;
          } 
          if (!isInteger(args[1], 10)) {
            sender.sendMessage(Utils.chat("&cUh oh! That's not a valid integer."));
            return true;
          } 
          config.set("psp.cooldown", Integer.valueOf(Integer.parseInt(args[1])));
          try {
            config.save(configcreate);
          } catch (IOException e) {
            e.printStackTrace();
          } 
          configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
          if (!configcreate.exists())
            try {
              configcreate.createNewFile();
            } catch (IOException e) {
              e.printStackTrace();
            }  
          config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
          try {
            config.save(configcreate);
          } catch (IOException e) {
            e.printStackTrace();
          } 
          sender.sendMessage(Utils.chat("&6Set the /psp set cooldown to " + args[1] + " seconds."));
          return true;
        } 
      } 
    } 
    if (args.length < 1 && !(sender instanceof Player)) {
      sender.sendMessage(Utils.chat("&cOnly players can view their own spawn info."));
      return true;
    } 
    if (args.length < 1) {
      Player player = (Player)sender;
    } else {
      if (!sender.hasPermission("psp.infoothers")) {
        sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
        return true;
      } 
      if (args[0].length() > 16) {
        sender.sendMessage(Utils.chat("&cThat's not a valid username."));
        return true;
      } 
      if (Bukkit.getServer().getOfflinePlayer(args[0]) == null) {
        sender.sendMessage(Utils.chat("&cThat player doesn't seem to exist."));
        return true;
      } 
      p = Bukkit.getServer().getOfflinePlayer(args[0]);
    } 
    if (!config.getBoolean("psp.collective")) {
      datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
      if (!datafile.exists()) {
        sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
        return true;
      } 
      data = (FileConfiguration)YamlConfiguration.loadConfiguration(datafile);
      Location grabloc = data.getLocation("spawnloc");
      if (grabloc == null) {
        sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
        return true;
      } 
      sender.sendMessage(Utils.chat("&a" + 
            
            p.getName() + "'(s) Personal Spawn Point\n" + 
            
            "Coordinates: &6" + round(grabloc.getX(), 3) + " " + round(grabloc.getY(), 3) + " " + round(grabloc.getZ(), 3) + "&a\n" + 
            "Creation Date: &6" + data.getString("spawndate")));
    } else {
      configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
      if (!configdatafile.exists()) {
        sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
        return true;
      } 
      configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
      Location grabloc = configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc");
      if (grabloc == null) {
        sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
        return true;
      } 
      sender.sendMessage(Utils.chat("&a" + 
            
            p.getName() + "'(s) Personal Spawn Point\n" + 
            
            "Coordinates: &6" + round(grabloc.getX(), 3) + " " + round(grabloc.getY(), 3) + " " + round(grabloc.getZ(), 3) + "&a\n" + 
            "Creation Date: &6" + configdata.getString("playerdata." + p.getUniqueId() + ".spawndate")));
    } 
    return true;
  }
  
  @EventHandler
  public void onRespawn(PlayerRespawnEvent e) {
    if (!config.getBoolean("psp.enabled"))
      return; 
    Player p = e.getPlayer();
    if (!config.getBoolean("psp.collective")) {
      datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
      if (!datafile.exists())
        return; 
      data = (FileConfiguration)YamlConfiguration.loadConfiguration(datafile);
      if (data.getLocation("spawnloc") == null)
        return; 
      if (!e.isBedSpawn() && !e.isAnchorSpawn()) {
        try {
          e.setRespawnLocation(data.getLocation("spawnloc"));
        } catch (NullPointerException nullPointerException) {}
        p.sendMessage(Utils.chat("&6Sent you to your &apersonal spawn point&6."));
      } 
      return;
    } 
    configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
    if (!configdatafile.exists())
      return; 
    configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
    if (configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc") == null)
      return; 
    if (!e.isBedSpawn() && !e.isAnchorSpawn()) {
      try {
        e.setRespawnLocation(configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc"));
      } catch (NullPointerException nullPointerException) {}
      p.sendMessage(Utils.chat("&6Sent you to your &apersonal spawn point&6."));
    } 
  }
}

