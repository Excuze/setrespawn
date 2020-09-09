/*     */ package me.excuze.setrespawn;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.math.BigDecimal;
/*     */ import java.math.RoundingMode;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Scanner;
/*     */ import java.util.Set;
/*     */ import me.excuze.setrespawn.utils.Utils;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.OfflinePlayer;
/*     */ import org.bukkit.World;
/*     */ import org.bukkit.command.Command;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.configuration.file.YamlConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.player.PlayerRespawnEvent;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.bukkit.plugin.java.JavaPlugin;
/*     */ 
/*     */ public class Main
/*     */   extends JavaPlugin
/*     */   implements Listener
/*     */ {
/*     */   public static File datadir;
/*     */   public static File configcreate;
/*     */   public static FileConfiguration config;
/*     */   public static FileConfiguration data;
/*     */   public static File datafile;
/*     */   public static FileConfiguration configdata;
/*     */   public static File configdatafile;
/*  40 */   private Map<String, Long> cooldowns = new HashMap<>();
/*     */   
/*     */   public static double round(double value, int places) {
/*  43 */     if (places < 0) throw new IllegalArgumentException();
/*     */     
/*  45 */     BigDecimal bd = BigDecimal.valueOf(value);
/*  46 */     bd = bd.setScale(places, RoundingMode.HALF_UP);
/*  47 */     return bd.doubleValue();
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean isInteger(String s, int radix) {
/*  52 */     Scanner sc = new Scanner(s.trim());
/*  53 */     if (!sc.hasNextInt(radix)) return false;
/*     */ 
/*     */     
/*  56 */     sc.nextInt(radix);
/*  57 */     return !sc.hasNext();
/*     */   }
/*     */   
/*     */   
/*     */     
/*  62 */      
/*     */       
/*  64 */       
/*     */     
/*  66 */     
/*  67 */       
/*     */     
/*     */   
/*     */ 
/*     */   
/*  72 */   YamlConfiguration playerconfig = new YamlConfiguration();
/*     */ 
/*     */   
/*     */   public void onEnable() {
/*  76 */     Bukkit.getPluginManager().registerEvents(this, (Plugin)this);
/*  77 */     saveDefaultConfig();
/*  78 */     configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
/*  79 */     if (!configcreate.exists()) {
/*     */       try {
/*  81 */         configcreate.createNewFile();
/*  82 */       } catch (IOException e) {
/*  83 */         e.printStackTrace();
/*     */       } 
/*     */     }
/*  86 */     config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
/*     */     try {
/*  88 */       config.save(configcreate);
/*  89 */     } catch (IOException e) {
/*     */       
/*  91 */       e.printStackTrace();
/*     */     } 
/*     */     
/*  94 */     String path = "a";
/*     */     try {
/*  96 */       path = (new File(".")).getCanonicalPath().toString();
/*  97 */     } catch (IOException e1) {
/*     */       
/*  99 */       e1.printStackTrace();
/*     */     } 
/* 101 */     datadir = new File(String.valueOf(path) + "/plugins/PersonalSpawnPoint/data");
/* 102 */     if (!datadir.exists()) {
/* 103 */       datadir.mkdir();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
/* 120 */     if (cmd.getName().equalsIgnoreCase("psp")) {
/*     */       
/* 122 */       if (!config.getBoolean("psp.enabled")) {
/* 123 */         sender.sendMessage(Utils.chat("&cCommands can't be run while the plugin is disabled. Set psp.enabled to true in the config.yml to use commands."));
/* 124 */         return true;
/*     */       } 
/* 126 */       if (args.length < 1) {
/* 127 */         sender.sendMessage(Utils.chat("&bHelp Page for PSP:\n&6/psp setspawn: &2Sets your spawn to your current location.\n&6/psp spawninfo [player]: &2Allows you to view information about your PSP, or, if you have the right permissions, the information about other player's PSPs.\n&6/psp delspawn [player]: &2Allows you to delete your PSP, or, if you have the right permissions, other user's PSPs.\n&6/psp nuke [force]: &2Allows you to perform a full wipe of all PSP data. Entering &lforce &2will skip the warning.\n&6/psp convert [collective/separate]: &2Converts stored data between the two forms, all in one file and in separate files. Please note that you'll need to change &lpsp.collective &2in config before it can read converted data.\n&6/psp reload: &2Reloads the config settings for PSP.\n&bPSP by Tohbot (Discord username: Excuze#3063)"));
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 139 */         return true;
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 146 */       if (args[0].equalsIgnoreCase("setspawn")) {
/*     */ 
/*     */         
/* 149 */         if (!(sender instanceof Player)) {
/* 150 */           sender.sendMessage(Utils.chat("&cOnly players can set their spawn."));
/* 151 */           return true;
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 157 */         Player p = (Player)sender;
/*     */         
/* 159 */         if (this.cooldowns.containsKey(p.getName())) {
/*     */           
/* 161 */           if (((Long)this.cooldowns.get(p.getName())).longValue() > System.currentTimeMillis()) {
/*     */             
/* 163 */             long timeleft = (((Long)this.cooldowns.get(p.getName())).longValue() - System.currentTimeMillis()) / 1000L;
/* 164 */             p.sendMessage(Utils.chat("&cYou have " + timeleft + " seconds before you can use this command again."));
/* 165 */             return true;
/*     */           } 
/*     */           
/* 168 */           this.cooldowns.remove(p.getName());
/* 169 */           this.cooldowns.put(p.getName(), Long.valueOf(System.currentTimeMillis() + (config.getInt("psp.cooldown") * 1000)));
/*     */         }
/*     */         else {
/*     */           
/* 173 */           this.cooldowns.put(p.getName(), Long.valueOf(System.currentTimeMillis() + (config.getInt("psp.cooldown") * 1000)));
/*     */         } 
/*     */ 
/*     */         
/* 177 */         if (p.getWorld().getEnvironment() == World.Environment.NETHER || p.getWorld().getEnvironment() == World.Environment.THE_END) {
/* 178 */           p.sendMessage(Utils.chat("&cYou can only set a personal spawn point in the Overworld."));
/* 179 */           return true;
/*     */         } 
/*     */ 
/*     */         
/* 183 */         Location spawnloc = p.getLocation();
/*     */ 
/*     */         
/* 186 */         if (!config.getBoolean("psp.collective")) {
/* 187 */           datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
/* 188 */           if (!datafile.exists()) {
/*     */             try {
/* 190 */               datafile.createNewFile();
/* 191 */             } catch (IOException e) {
/*     */               
/* 193 */               e.printStackTrace();
/*     */             } 
/*     */           }
/*     */ 
/*     */           
/* 198 */           data = (FileConfiguration)YamlConfiguration.loadConfiguration(datafile);
/*     */           
/* 200 */           data.set("spawnloc", spawnloc);
/* 201 */           Date date = new Date();
/* 202 */           SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
/* 203 */           data.set("spawndate", formatter.format(date));
/*     */           
/*     */           try {
/* 206 */             data.save(datafile);
/* 207 */           } catch (IOException e) {
/*     */             
/* 209 */             e.printStackTrace();
/*     */           }
/*     */         
/*     */         }
/*     */         else {
/*     */           
/* 215 */           configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
/* 216 */           if (!configdatafile.exists()) {
/*     */             try {
/* 218 */               configdatafile.createNewFile();
/* 219 */             } catch (IOException e) {
/*     */               
/* 221 */               e.printStackTrace();
/*     */             } 
/*     */           }
/*     */           
/* 225 */           configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
/*     */           
/* 227 */           configdata.set("playerdata." + p.getUniqueId() + ".spawnloc", spawnloc);
/* 228 */           Date date = new Date();
/* 229 */           SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
/* 230 */           configdata.set("playerdata." + p.getUniqueId() + ".spawndate", formatter.format(date));
/*     */           
/*     */           try {
/* 233 */             configdata.save(configdatafile);
/* 234 */           } catch (IOException e) {
/*     */             
/* 236 */             e.printStackTrace();
/*     */           } 
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 242 */         p.sendMessage(Utils.chat("&aSet your personal spawn point to your current location."));
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 249 */       if (args[0].equalsIgnoreCase("spawninfo")) {
/*     */         OfflinePlayer p;
/* 251 */         if (args.length < 2 && !(sender instanceof Player)) {
/* 252 */           sender.sendMessage(Utils.chat("&cOnly players can view their own spawn info."));
/* 253 */           return true;
/*     */         } 
/*     */ 
/*     */         
/* 257 */         if (args.length < 2) {
/* 258 */           Player player = (Player)sender;
/*     */         } else {
/* 260 */           if (!sender.hasPermission("psp.infoothers")) {
/* 261 */             sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
/* 262 */             return true;
/*     */           } 
/* 264 */           if (args[1].length() > 16) {
/* 265 */             sender.sendMessage(Utils.chat("&cThat's not a valid username."));
/* 266 */             return true;
/*     */           } 
/* 268 */           if (Bukkit.getServer().getOfflinePlayer(args[1]) == null) {
/* 269 */             sender.sendMessage(Utils.chat("&cThat player doesn't seem to exist."));
/* 270 */             return true;
/*     */           } 
/* 272 */           p = Bukkit.getServer().getOfflinePlayer(args[1]);
/*     */         } 
/*     */         
/* 275 */         if (!config.getBoolean("psp.collective")) {
/* 276 */           datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
/* 277 */           if (!datafile.exists()) {
/* 278 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 279 */             return true;
/*     */           } 
/*     */ 
/*     */           
/* 283 */           data = (FileConfiguration)YamlConfiguration.loadConfiguration(datafile);
/*     */ 
/*     */ 
/*     */           
/* 287 */           Location grabloc = data.getLocation("spawnloc");
/*     */           
/* 289 */           if (grabloc == null) {
/* 290 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 291 */             return true;
/*     */           } 
/*     */ 
/*     */ 
/*     */           
/* 296 */           sender.sendMessage(Utils.chat("&a" + 
/*     */                 
/* 298 */                 p.getName() + "'(s) Personal Spawn Point\n" + 
/*     */                 
/* 300 */                 "Coordinates: &6" + round(grabloc.getX(), 3) + " " + round(grabloc.getY(), 3) + " " + round(grabloc.getZ(), 3) + "&a\n" + 
/* 301 */                 "Creation Date: &6" + data.getString("spawndate")));
/*     */ 
/*     */         
/*     */         }
/*     */         else {
/*     */ 
/*     */           
/* 308 */           configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
/* 309 */           if (!configdatafile.exists()) {
/* 310 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 311 */             return true;
/*     */           } 
/*     */           
/* 314 */           configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
/*     */           
/* 316 */           Location grabloc = configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc");
/*     */           
/* 318 */           if (grabloc == null) {
/* 319 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 320 */             return true;
/*     */           } 
/*     */ 
/*     */ 
/*     */           
/* 325 */           sender.sendMessage(Utils.chat("&a" + 
/*     */                 
/* 327 */                 p.getName() + "'(s) Personal Spawn Point\n" + 
/*     */                 
/* 329 */                 "Coordinates: &6" + round(grabloc.getX(), 3) + " " + round(grabloc.getY(), 3) + " " + round(grabloc.getZ(), 3) + "&a\n" + 
/* 330 */                 "Creation Date: &6" + configdata.getString("playerdata." + p.getUniqueId() + ".spawndate")));
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 343 */       if (args[0].equalsIgnoreCase("delspawn")) {
/*     */         OfflinePlayer p;
/* 345 */         if (args.length < 2 && !(sender instanceof Player)) {
/* 346 */           sender.sendMessage(Utils.chat("&cOnly players can delete their own spawn info."));
/* 347 */           return true;
/*     */         } 
/*     */ 
/*     */         
/* 351 */         if (args.length < 2) {
/* 352 */           Player player = (Player)sender;
/*     */         } else {
/* 354 */           if (!sender.hasPermission("psp.delothers")) {
/* 355 */             sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
/* 356 */             return true;
/*     */           } 
/* 358 */           if (args[1].length() > 16) {
/* 359 */             sender.sendMessage(Utils.chat("&cThat's not a valid username."));
/* 360 */             return true;
/*     */           } 
/* 362 */           if (Bukkit.getServer().getOfflinePlayer(args[1]) == null) {
/* 363 */             sender.sendMessage(Utils.chat("&cThat player doesn't seem to exist."));
/* 364 */             return true;
/*     */           } 
/* 366 */           p = Bukkit.getServer().getOfflinePlayer(args[1]);
/*     */         } 
/*     */         
/* 369 */         if (!config.getBoolean("psp.collective")) {
/* 370 */           datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
/* 371 */           if (!datafile.exists()) {
/* 372 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 373 */             return true;
/*     */           } 
/*     */ 
/*     */           
/* 377 */           datafile.delete();
/*     */           
/* 379 */           sender.sendMessage(Utils.chat("&cDeleted PSP data for user " + p.getName() + "."));
/*     */         
/*     */         }
/*     */         else {
/*     */           
/* 384 */           configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
/* 385 */           if (!configdatafile.exists()) {
/* 386 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 387 */             return true;
/*     */           } 
/*     */           
/* 390 */           configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
/*     */           
/* 392 */           if (configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc") == null) {
/* 393 */             sender.sendMessage(Utils.chat("&cThis player doesn't seem to have a PSP!"));
/* 394 */             return true;
/*     */           } 
/*     */           
/* 397 */           configdata.set("playerdata." + p.getUniqueId(), null);
/*     */           
/*     */           try {
/* 400 */             configdata.save(configdatafile);
/* 401 */           } catch (IOException e) {
/*     */             
/* 403 */             e.printStackTrace();
/*     */           } 
/*     */           
/* 406 */           sender.sendMessage(Utils.chat("&cDeleted PSP data for user " + p.getName() + "."));
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 412 */       if (args[0].equalsIgnoreCase("nuke")) {
/* 413 */         if (!sender.hasPermission("psp.nuke")) {
/* 414 */           sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
/* 415 */           return true;
/*     */         } 
/* 417 */         if (!config.getBoolean("psp.opscannuke")) {
/* 418 */           sender.sendMessage(Utils.chat("&cCommand based nuking is disabled in the config."));
/* 419 */           return true;
/*     */         } 
/* 421 */         if (args.length < 2) {
/* 422 */           sender.sendMessage(Utils.chat("&cNuking will irreversibly destroy &lall &cplayer data that PSP has &lever &ccreated. Run &l/psp nuke force &cif you'd like to continue."));
/* 423 */           return true;
/*     */         } 
/* 425 */         if (!args[1].equalsIgnoreCase("force")) {
/* 426 */           sender.sendMessage(Utils.chat("&cNuking will irreversibly destroy &lall &cplayer data that PSP has &lever &ccreated. Run &l/psp nuke force &cif you'd like to continue."));
/* 427 */           return true;
/*     */         } 
/* 429 */         sender.sendMessage(Utils.chat("&cI hope you know what you're doing xD"));
/* 430 */         Bukkit.getLogger().info(Utils.chat("&c<!> WARNING <!> &4User " + sender.getName() + " just nuked all PSP data. If you didn't allow this, that might be a problem. &c<!> WARNING <!>"));
/* 431 */         Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable() {
/*     */               public void run() {
/*     */                 try {
/* 434 */                   File nuke = new File(String.valueOf(Main.this.getDataFolder().toString()) + "/playerdata.yml");
/* 435 */                   if (nuke.exists()) {
/* 436 */                     nuke.delete();
/*     */                   }
/* 438 */                   nuke = new File(String.valueOf(Main.this.getDataFolder().toString()) + "/data");
/* 439 */                   if (nuke.exists()) {
/* 440 */                     String[] entries = nuke.list(); byte b; int i; String[] arrayOfString1;
/* 441 */                     for (i = (arrayOfString1 = entries).length, b = 0; b < i; ) { String s = arrayOfString1[b];
/* 442 */                       File currentFile = new File(nuke.getPath(), s);
/* 443 */                       currentFile.delete(); b++; }
/*     */                     
/* 445 */                     nuke.delete();
/*     */                   } 
/*     */                   
/* 448 */                   sender.sendMessage(Utils.chat("&cData fully destroyed."));
/*     */                   
/*     */                   return;
/* 451 */                 } catch (Exception e) {
/* 452 */                   sender.sendMessage(Utils.chat("&cError deleting data. Most likely, the data is already deleted. Check server logs for additional info."));
/* 453 */                   e.printStackTrace();
/*     */                   return;
/*     */                 } 
/*     */               }
/* 457 */             }40L);
/*     */       } 
/*     */ 
/*     */       
/* 461 */       if (args[0].equalsIgnoreCase("convert")) {
/* 462 */         if (!sender.hasPermission("psp.convert")) {
/* 463 */           sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
/* 464 */           return true;
/*     */         } 
/* 466 */         if (args.length < 2) {
/* 467 */           sender.sendMessage(Utils.chat("&cToo few arguments!"));
/* 468 */           return true;
/*     */         } 
/*     */         
/* 471 */         if (args[1].equalsIgnoreCase("collective")) {
/* 472 */           File convert = new File(String.valueOf(getDataFolder().toString()) + "/data");
/*     */ 
/*     */ 
/*     */           
/* 476 */           configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
/* 477 */           if (!configdatafile.exists()) {
/*     */             try {
/* 479 */               configdatafile.createNewFile();
/* 480 */             } catch (IOException e) {
/*     */               
/* 482 */               e.printStackTrace();
/*     */             } 
/*     */           }
/*     */           
/* 486 */           configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
/*     */ 
/*     */           
/* 489 */           int failed = 0;
/* 490 */           if (convert.exists()) {
/* 491 */             String[] entries = convert.list(); byte b; int i; String[] arrayOfString1;
/* 492 */             for (i = (arrayOfString1 = entries).length, b = 0; b < i; ) { String s = arrayOfString1[b];
/* 493 */               File convertconfigfile = new File(convert.getPath(), s);
/*     */               
/* 495 */               if (convertconfigfile.exists()) {
/*     */                 
/* 497 */                 YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(convertconfigfile);
/*     */                 
/* 499 */                 if (yamlConfiguration.get("spawnloc") != null && yamlConfiguration.get("spawndate") != null) {
/* 500 */                   configdata.set("playerdata." + s.substring(0, s.length() - 4) + ".spawnloc", yamlConfiguration.get("spawnloc"));
/* 501 */                   configdata.set("playerdata." + s.substring(0, s.length() - 4) + ".spawndate", yamlConfiguration.get("spawndate"));
/*     */                 } else {
/* 503 */                   failed++;
/*     */                 } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/*     */                 try {
/* 511 */                   configdata.save(configdatafile);
/* 512 */                 } catch (IOException e) {
/*     */                   
/* 514 */                   e.printStackTrace();
/*     */                 } 
/*     */               } 
/*     */               
/*     */               b++; }
/*     */             
/* 520 */             sender.sendMessage(Utils.chat("&aJob complete! A total of &c" + failed + " &aconversions failed. If this number is greater than 0, it may be the result of bad data."));
/*     */           } else {
/* 522 */             sender.sendMessage(Utils.chat("&cI couldn't find the data folder! Are you sure it exists?"));
/* 523 */             return true;
/*     */           } 
/*     */         } 
/*     */         
/* 527 */         if (args[1].equalsIgnoreCase("separate")) {
/*     */           
/* 529 */           if (!sender.hasPermission("psp.convert")) {
/* 530 */             sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
/* 531 */             return true;
/*     */           } 
/* 533 */           if (args.length < 2) {
/* 534 */             sender.sendMessage(Utils.chat("&cToo few arguments!"));
/* 535 */             return true;
/*     */           } 
/*     */           
/* 538 */           File convert = new File(String.valueOf(getDataFolder().toString()) + "/data");
/* 539 */           if (!convert.exists()) {
/* 540 */             convert.mkdir();
/*     */           }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 547 */           configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
/* 548 */           if (!configdatafile.exists()) {
/* 549 */             sender.sendMessage(Utils.chat("&cI was unable to find any stored playerdata in playerdata.yml."));
/* 550 */             return true;
/*     */           } 
/*     */           
/* 553 */           configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
/*     */           
/* 555 */           Set<String> dataset = configdata.getConfigurationSection("playerdata").getKeys(false);
/*     */           
/* 557 */           if (dataset == null) {
/* 558 */             sender.sendMessage(Utils.chat("&cI was unable to find any stored playerdata in playerdata.yml."));
/* 559 */             return true;
/*     */           } 
/* 561 */           int failed = 0;
/* 562 */           for (String s : dataset) {
/* 563 */             File convertconfigfile = new File(convert.getPath(), String.valueOf(s) + ".yml");
/* 564 */             if (!convertconfigfile.exists()) {
/*     */               try {
/* 566 */                 convertconfigfile.createNewFile();
/* 567 */               } catch (IOException e) {
/*     */                 
/* 569 */                 e.printStackTrace();
/*     */               } 
/*     */             }
/*     */             
/* 573 */             YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(convertconfigfile);
/*     */             
/* 575 */             if (configdata.getLocation("playerdata." + s + ".spawnloc") != null && configdata.getString("playerdata." + s + ".spawndate") != null) {
/* 576 */               yamlConfiguration.set("spawnloc", configdata.getLocation("playerdata." + s + ".spawnloc"));
/* 577 */               yamlConfiguration.set("spawndate", configdata.getString("playerdata." + s + ".spawndate"));
/*     */             } else {
/* 579 */               failed++;
/*     */             } 
/*     */             
/*     */             try {
/* 583 */               yamlConfiguration.save(convertconfigfile);
/* 584 */             } catch (IOException e) {
/*     */               
/* 586 */               e.printStackTrace();
/*     */             } 
/*     */           } 
/*     */ 
/*     */           
/* 591 */           sender.sendMessage(Utils.chat("&aJob complete! A total of &c" + failed + " &aconversions failed. If this number is greater than 0, it may be the result of bad data."));
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 597 */       if (args[0].equalsIgnoreCase("reload")) {
/* 598 */         if (!sender.hasPermission("psp.reload")) {
/* 599 */           sender.sendMessage(Utils.chat("&cYou don't have permission to do this!"));
/* 600 */           return true;
/*     */         } 
/*     */         
/* 603 */         configcreate = new File(String.valueOf(getDataFolder().toString()) + "/config.yml");
/* 604 */         if (!configcreate.exists()) {
/*     */           try {
/* 606 */             configcreate.createNewFile();
/* 607 */           } catch (IOException e) {
/* 608 */             e.printStackTrace();
/*     */           } 
/*     */         }
/* 611 */         config = (FileConfiguration)YamlConfiguration.loadConfiguration(configcreate);
/*     */         try {
/* 613 */           config.save(configcreate);
/* 614 */         } catch (IOException e) {
/*     */           
/* 616 */           e.printStackTrace();
/*     */         } 
/*     */         
/* 619 */         String path = "a";
/*     */         try {
/* 621 */           path = (new File(".")).getCanonicalPath().toString();
/* 622 */         } catch (IOException e1) {
/*     */           
/* 624 */           e1.printStackTrace();
/*     */         } 
/* 626 */         datadir = new File(String.valueOf(path) + "/plugins/PersonalSpawnPoint/data");
/* 627 */         if (!datadir.exists()) {
/* 628 */           datadir.mkdir();
/*     */         }
/*     */         
/* 631 */         sender.sendMessage(Utils.chat("&aReload complete! Updated config.yml settings."));
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 636 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void onRespawn(PlayerRespawnEvent e) {
/* 642 */     if (!config.getBoolean("psp.enabled")) {
/*     */       return;
/*     */     }
/*     */     
/* 646 */     Player p = e.getPlayer();
/* 647 */     if (!config.getBoolean("psp.collective")) {
/* 648 */       datafile = new File(datadir + "/" + p.getUniqueId() + ".yml");
/* 649 */       if (!datafile.exists()) {
/*     */         return;
/*     */       }
/*     */       
/* 653 */       data = (FileConfiguration)YamlConfiguration.loadConfiguration(datafile);
/*     */       
/* 655 */       if (data.getLocation("spawnloc") == null) {
/*     */         return;
/*     */       }
/*     */       
/* 659 */       if (!e.isBedSpawn() && !e.isAnchorSpawn()) {
/*     */         try {
/* 661 */           e.setRespawnLocation(data.getLocation("spawnloc"));
/* 662 */         } catch (NullPointerException nullPointerException) {}
/*     */ 
/*     */         
/* 665 */         p.sendMessage(Utils.chat("&6Sent you to your &apersonal spawn point&6."));
/*     */       } 
/*     */       return;
/*     */     } 
/* 669 */     configdatafile = new File(String.valueOf(getDataFolder().toString()) + "/playerdata.yml");
/* 670 */     if (!configdatafile.exists()) {
/*     */       return;
/*     */     }
/*     */     
/* 674 */     configdata = (FileConfiguration)YamlConfiguration.loadConfiguration(configdatafile);
/*     */     
/* 676 */     if (configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc") == null) {
/*     */       return;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 683 */     if (!e.isBedSpawn() && !e.isAnchorSpawn()) {
/*     */       try {
/* 685 */         e.setRespawnLocation(configdata.getLocation("playerdata." + p.getUniqueId() + ".spawnloc"));
/* 686 */       } catch (NullPointerException nullPointerException) {}
/*     */ 
/*     */       
/* 689 */       p.sendMessage(Utils.chat("&6Sent you to your &apersonal spawn point&6."));
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Tobia\Documents\Plugin Exports\SetRespawn.jar!\me\excuze\setrespawn\Main.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */
