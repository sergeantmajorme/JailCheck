package me.SgtMjrME.JailCheck;

import java.util.TimerTask;

public class DelayTask extends TimerTask{

	private String player;
	private JailCheck plugin;
	
	DelayTask(String player, JailCheck plugin)
	{
		this.player = player;
		this.plugin = plugin;
	}
	
	public void run()
	{
		plugin.removeDelay(player);
	}
}
