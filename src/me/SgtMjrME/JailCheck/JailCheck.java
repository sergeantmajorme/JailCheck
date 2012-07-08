package me.SgtMjrME.JailCheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class JailCheck extends JavaPlugin{

	private static Logger log;
	public PluginManager pm;
	private PlayerListener playerListener;
	public String directory;
	private YamlConfiguration config;
	private String jail;
	private String to;
	private Location spawn;
	private ArrayList<Player> allowed = new ArrayList<Player>();
	private boolean tpblock;
	private File configFile;
	
	@Override
	public void onEnable()
	{
		log = getServer().getLogger();
		pm = getServer().getPluginManager();
		playerListener = new PlayerListener(this);
		pm.registerEvents(playerListener, this);
		try{
			File duck = new File("plugins/JCData");
			if (!duck.exists())
			{
				duck.mkdir();
			}
			directory = duck + "/";
		}
		catch(Exception e)
		{
			log.info("Could not create folder");
			pm.disablePlugin(this);
		}
		loadConfig();
		jail = config.getString("from");
		to = config.getString("to");
		if (to != null)
			spawn = new Location(getServer().getWorld(to), 
				config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
		else
			spawn = null;
		tpblock = false;
		log.info("[JailCheck] Loaded");
	}
	
	private void loadConfig() {
		configFile = new File(getDataFolder(), "config.yml");
	    config = YamlConfiguration.loadConfiguration(configFile);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = (Player) sender;
		if (commandLabel.equalsIgnoreCase("jailcheck") && args.length == 2 && (checkPermsRecord(player) || player.isOp()))
		{
			if (args[0].equalsIgnoreCase("clear"))
			{
				File f = new File(directory + args[1] + ".txt");
				if (f.exists())
					if (f.delete())
						player.sendMessage(args[1] + " data deleted");
			}
		}
		else if (commandLabel.equalsIgnoreCase("jailcheck") && args.length == 1 && (checkPermsRecord(player) || player.isOp()))
		{
			Player victim = getServer().getPlayer(args[0]);
			String victimName = args[0];
			if (victim != null)
				victimName = victim.getName();
			File f = new File(directory + victimName + ".txt");
			if (!f.exists())
			{
				player.sendMessage("No jail record found, or player is offline (if offline, use exact name)");
				return true;
			}
			player.sendMessage("");
			player.sendMessage("");
			player.sendMessage(victimName + " has been:");
			try{
				FileReader filein = new FileReader(f);
				BufferedReader in = new BufferedReader(filein);
				String[] data = in.readLine().split("  :  ");
				String output;
				String output2;
				player.sendMessage("");
				for (int x = 0; x < data.length; x++)
				{
					String[] seperate = data[x].split("/~/");
					String[] playerData = seperate[0].split(" ");
					String[] commandData = seperate[1].split(" ");
					long time1 = Long.parseLong(playerData[1]);
					long time2 = System.currentTimeMillis();
					double[] d = new double[2];
					d[1] = time2-time1;
					convertUp(d);
					d[1] = d[1] * 100;
					d[1] = (double)((int)d[1]);
					d[1] = d[1] / 100;
					output = "" + d[1] + " ";
					if ((int) d[0] == 0)
						output = output.concat("second(s) ago");
					else if ((int) d[0] == 1)
						output = output.concat("minute(s) ago");
					else if ((int) d[0] == 2)
						output = output.concat("hour(s) ago");
					output2 = "";
					if (commandData[0].equalsIgnoreCase("tempban"))
						output2 = output2.concat(" for " + commandData[2]);
					else if (commandData.length == 3)
						output2 = output2.concat(" with no time");
					else if (commandData.length >= 4)
						output2 = output2.concat(" for " + commandData[3]);
					String output3 = "";
					if (commandData.length > 4)
					{
						String reason = "For the reason: ";
						int x1 = 4;
						if (commandData[0].equalsIgnoreCase("tempban"))
							x1 = 3;
						while (x1 < commandData.length)
						{
							reason = reason.concat(commandData[x1] + " ");
							x1++;
						}
						output3 = reason;
					}
					player.sendMessage(commandData[0] + "'d by " + playerData[0] + " " + output + output2);
					if (!output3.equals(""))
						player.sendMessage(output3);
				}
				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return true;
		}
		else if (commandLabel.equalsIgnoreCase("checkjailtime") && player.hasPermission("JC.check"))
		{
			File f = new File(directory + player.getName() + ".txt");
			if (!f.exists())
			{
				player.sendMessage("No jail record found");
				return true;
			}
			
			try {
				FileReader filein = new FileReader(f);
				BufferedReader in = new BufferedReader(filein);
				String[] data = in.readLine().split("  :  ");
				data = data[data.length-1].split("/~/");
				long time = Long.parseLong(data[0].split(" ")[1]);
				long time2 = System.currentTimeMillis();
				long timeComplete = (time2 - time);
				double[] type = new double[2];//0=s, 1=m, 2=h
				type[1] = timeComplete;
				String timedisp;
				
				convertUp(type);
				type[1] = type[1] * 100;
				type[1] = (double)((int)type[1]);
				type[1] = type[1] / 100;
				timedisp = "" + type[1] + " ";
				if ((int)type[0] == 0)
					timedisp = timedisp.concat("second(s) ago");
				else if ((int)type[0] == 1)
					timedisp = timedisp.concat("minute(s) ago");
				else if ((int)type[0] == 2)
					timedisp = timedisp.concat("hour(s) ago");
				player.sendMessage("You were " + data[1].split(" ")[0] + "'d by " 
						+ data[0].split(" ")[0] + " " + timedisp);
				String[] command = data[1].split(" ");
				if (command.length > 3)
				{
					timedisp = printTimeLeft(command, timeComplete);
					if (timedisp.equals("null"))
					{
						player.sendMessage("You have served your time, type \"/spawn\" to leave");
						in.close();
						return true;
					}
					player.sendMessage(timedisp);
					if (command.length > 4)
					{
						String reason = "For the reason: ";
						for (int x = 4; x < command.length; x++)
						{
							reason = reason.concat(command[x] + " ");
						}
						player.sendMessage(reason);
					}
						
				}
				in.close();
				filein.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		else if (commandLabel.equalsIgnoreCase("tpjail") && checkPermsRecord(player))
		{
			if (args.length == 0)
			{
				if (checktpJail()) return true;
				if (!tpblock)
				{
					tpblock = true;
					player.sendMessage("Blocking teleports to jail now activated");
				}
				else
				{
					tpblock = false;
					player.sendMessage("Blocking teleports to jail now deactivated");
				}
			}
					
			if (args.length == 2 && player.isOp())
			{
				if (args[0].equalsIgnoreCase("set"))
				{
					if (args[1].equalsIgnoreCase("jail"))
					{
						config.set("from", player.getWorld().getName());
						try {
							config.save(configFile);
						} catch (IOException e) {
							player.sendMessage("Problem saving config");
							e.printStackTrace();
						}
						player.sendMessage("Jail world set");
					}
					else if (args[1].equalsIgnoreCase("spawn"))
					{
						spawn = player.getLocation();
						config.set("to", player.getWorld().getName());
						config.set("x", spawn.getX());
						config.set("y", spawn.getY());
						config.set("z", spawn.getZ());
						try {
							config.save(configFile);
						} catch (IOException e) {
							player.sendMessage("Problem saving config");
							e.printStackTrace();
						}
						player.sendMessage("Spawn location (JailCheck) set");
					}
				}
			}
			if (args.length != 1)
				return false;
			Player victim = getServer().getPlayer(args[0]);
			if (victim == null)
				return false;
			if (allowed.contains(victim)){
				allowed.remove(victim);
				player.sendMessage(victim.getName() + " not allowed to tp to jail");
			}
			else {
				allowed.add(victim);
				player.sendMessage(victim.getName() + " allowed to tp to jail");
			}
		}
		return true;
	}

	private boolean checktpJail() {
		if (jail == null || to == null || spawn == null)
			return true;
		return false;
	}

	private String printTimeLeft(String[] command, long timeComplete) {
		char marker = command[3].charAt(command[3].length() - 1);
		StringBuffer s = new StringBuffer();
		for (int x = 0;x < command[3].length()-1;x++)
		{
			if (Character.isDigit(command[3].charAt(x)))
				s.append(command[3].charAt(x));
			else
				break;
		}
		long newtime = Long.parseLong(s.toString());
		if (marker == 'h')
		{
			newtime = newtime * 3600 * 1000;
		}
		else if (marker == 'm')
		{
			newtime = newtime * 60 * 1000;
		}
		else
		{
			newtime = newtime * 1000;
		}
		double[] timeElapsed = new double[2];
		timeElapsed[1] = (newtime - timeComplete);
		convertUp(timeElapsed);
		if (timeElapsed[1] < 0)
		{
			return "null";
		}
		timeElapsed[1] = timeElapsed[1] * 100;
		timeElapsed[1] = (double)((int) timeElapsed[1]);
		timeElapsed[1] = timeElapsed[1] / 100;
		String timedisp = "" + timeElapsed[1] + " ";
		if ((int)timeElapsed[0] == 2)
			timedisp = timedisp.concat("hour(s) to go (roughly, may be slightly longer)");
		else if ((int)timeElapsed[0] == 1)
			timedisp = timedisp.concat("minute(s) to go (roughly, may be slightly longer)");
		else
			timedisp = timedisp.concat("second(s) to go (roughly, may be slightly longer)");
		return timedisp;
	}

	private void convertUp(double[] type) {
		type[1] = type[1]/1000;
		if (type[1] > 60)
		{
			type[1] = type[1]/60;
			if (type[1] > 60)
			{
				type[1] = type[1]/60;
				type[0] = 2;
				return;
			}
			else
			{
				type[0] = 1;
				return;
			}
		}
		type[0] = 0;
		return;
	}

	public void sendLog(String string) {
		log.info(string);
	}

	public void record(String name, String message) {
		try{
			String[] broken = message.split(" ");
			if (broken.length < 2)
				return;
			File f = new File(directory + broken[1] + ".txt");
			if (!f.exists())
			{
				f.createNewFile();
			}
			RandomAccessFile r = new RandomAccessFile(f, "rws");
			r.seek(r.length());	
			long time = System.currentTimeMillis();
			r.writeBytes(name + " " + time + "/~" + message + "  :  ");
			r.close();
		}
		catch (Exception e)
		{
			log.info("Error saving record, " + message + " " + name);
			e.printStackTrace();
		}
	}

	public void recordcheck(String name, String message) {
		String s = message.split(" ")[1];
		if (getServer().getPlayer(s) != null)
			record(name, message);
	}

	public String getJail() {
		return jail;
	}
	
	public String getTo() {
		return to;
	}
	public Location getSpawn() {
		return spawn;
	}
	
	public boolean getAllowed(Player player) {
		return allowed.contains(player);
	}
	
	public boolean checkPermsRecord(Player player) {
		boolean returnable = player.hasPermission("essentials.ban") || player.hasPermission("essentials.jails")
				|| player.hasPermission("essentials.kick");
		return returnable;
	}

	public void removeAllowed(Player player) {
		allowed.remove(player);
	}

	public boolean tpblock() {
		return tpblock;
	}
}
