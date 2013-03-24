package org.maxgamer.maxbans.commands;

import java.io.File;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.database.DatabaseCore;
import org.maxgamer.maxbans.database.MySQLCore;
import org.maxgamer.maxbans.database.SQLiteCore;
import org.maxgamer.maxbans.database.Database.ConnectionException;
import org.maxgamer.maxbans.util.Formatter;

public class MBExportCommand extends CmdSkeleton{
    public MBExportCommand(){
        super("mbexport", "maxbans.export");
        namePos = -1;
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0){
			sender.sendMessage(Formatter.primary + "MaxBans Exporter:");
			sender.sendMessage(Formatter.secondary + "/mbexport vanilla " + Formatter.primary + " - Exports bans to vanilla bans.");
		}
		else{
			if(args[0].equalsIgnoreCase("vanilla")){
				//Import vanilla.
				for(Entry<String, Ban> entry : plugin.getBanManager().getBans().entrySet()){
					if(entry.getValue() instanceof TempBan){
						//So we skip it in good faith.
						continue;
					}
					
					OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
					if(!p.isBanned()) p.setBanned(true);
				}
				
				for(Entry<String, IPBan> entry : plugin.getBanManager().getIPBans().entrySet()){
					if(entry.getValue() instanceof TempIPBan){
						//So we skip it in good faith.
						continue;
					}
					
					Bukkit.banIP(entry.getKey());
				}
				
				sender.sendMessage(Formatter.secondary + "Success.");
			}
			else if(args[0].equalsIgnoreCase("mysql")){
				if(plugin.getDB().getCore() instanceof MySQLCore){
					sender.sendMessage(ChatColor.RED + "Database is already MySQL");
					return true;
				}
				
				ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("database");
				String host = cfg.getString("host");
				String port = cfg.getString("port");
				String user = cfg.getString("user");
				String pass = cfg.getString("pass");
				String name = cfg.getString("name");
				
				try{
					DatabaseCore dbCore = new MySQLCore(host, user, pass, name, port);
					Database mysql = new Database(dbCore);
					
					plugin.getDB().copyTo(mysql);
					
					sender.sendMessage(ChatColor.GREEN + "Success - Exported to MySQL " + user + "@" + host + "." + name);
					return true;
				}
				catch(ConnectionException e){
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "Failed to connect to MySQL " + user + "@" + host + "." + name + ChatColor.DARK_RED + " Reason: " + e.getMessage());
				}
				catch(SQLException e){
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "Failed to export to MySQL " + user + "@" + host + "." + name + ChatColor.DARK_RED + " Reason: " + e.getMessage());
				}
			}
			else if(args[0].equalsIgnoreCase("sqlite") || args[0].equalsIgnoreCase("sql") || args[0].equalsIgnoreCase("flatfile")){
				if(plugin.getDB().getCore() instanceof SQLiteCore){
					sender.sendMessage(ChatColor.RED + "Database is already SQLite");
					return true;
				}
				
				File file = new File(plugin.getDataFolder(), "bans.db");
				try{
					DatabaseCore dbCore = new SQLiteCore(file);
					Database sqlite = new Database(dbCore);
					plugin.getDB().copyTo(sqlite);
					
					sender.sendMessage(ChatColor.GREEN + "Success - Exported to SQLite " + file.getPath());
					return true;
				}
				catch(ConnectionException e){
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "Failed to connect to SQLite "+file.getPath() + " Reason: " + e.getMessage());
				}
				catch(SQLException e){
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "Failed to export to SQLite "+file.getPath() + " Reason: " + e.getMessage());
				}
			}
			else{
				sender.sendMessage(Formatter.secondary + "Failed.  No known exporter: " + args[0]);
			}
		}
		
		return true;
	}
}
