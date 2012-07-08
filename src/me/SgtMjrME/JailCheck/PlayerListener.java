package me.SgtMjrME.JailCheck;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener{
	private JailCheck plugin;
	
	PlayerListener(JailCheck jailCheck)
	{
		plugin = jailCheck;
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void PlayerEvent(PlayerCommandPreprocessEvent e)
	{
		if (!plugin.checkPermsRecord(e.getPlayer()) && !e.getPlayer().isOp())
		{
			return;
		}
		if (e.isCancelled())
			return;
		String[] test = e.getMessage().split(" ");
		if (test[0].equalsIgnoreCase("/ban") || test[0].equalsIgnoreCase("/unban") 
				|| test[0].equalsIgnoreCase("/banIp") || test[0].equalsIgnoreCase("/unbanip")
				|| test[0].equalsIgnoreCase("/tempban"))
		{
			plugin.record(e.getPlayer().getName(), e.getMessage());
		}
		else if(test[0].equalsIgnoreCase("/jail") || test[0].equalsIgnoreCase("/tjail")|| test[0].equalsIgnoreCase("/unjail"))
		{
			plugin.recordcheck(e.getPlayer().getName(), e.getMessage());
		}
		else
			return;
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onTP(PlayerTeleportEvent e)
	{
		if (!plugin.tpblock())
			return;
//		if (e.getFrom().getWorld().getName().equalsIgnoreCase(plugin.getJail()) && !plugin.checkPermsRecord(e.getPlayer())){
//			//Teleporting FROM jail
//			if (e.getTo().getWorld().getName().equalsIgnoreCase(plugin.getTo()))
//			{
//				return;
//			}
//			else if (plugin.getAllowed(e.getPlayer()))
//			{
//				plugin.removeAllowed(e.getPlayer());
//				return;
//			}
//			else
//			{
//				e.setCancelled(true);
//				e.getPlayer().sendMessage("Not allowed to teleport out of jail");
//			}
//		}
		else if (e.getTo().getWorld().getName().equalsIgnoreCase(plugin.getTo())){
			//Teleporting TO jail
			if (plugin.checkPermsRecord(e.getPlayer()))
				return;//Helpers/mods
			else if (plugin.getAllowed(e.getPlayer()))
				return;//Anyone given permission (one time use only)
			else
			{
				e.setTo(plugin.getSpawn());
				e.getPlayer().sendMessage("Not allowed to tp to jail");
			}
		}
			
	}

}
