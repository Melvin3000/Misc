package org.c4k3.Misc;

import org.bukkit.plugin.java.JavaPlugin;

public class Misc extends JavaPlugin {
	
	public static JavaPlugin instance;
		
	@Override
	public void onEnable(){
		instance = this;
		new ConsoleDeathLog(this);
		new LogCmd(this);
		getServer().getPluginManager().registerEvents(new DisableCmd(), this);
		getServer().getPluginManager().registerEvents(new FollowLogoutListener(), this);
		DisableCmd.loadDisabledCmds();
		getCommand("coords").setExecutor(new CoordsForAll());
		getCommand("f").setExecutor(new Follow());
		getCommand("b").setExecutor(new Follow());
		getCommand("a").setExecutor(new AdminChat());
		getCommand("tp").setExecutor(new AdminTeleport());
		getCommand("tps").setExecutor(new AdminTeleport());
		getCommand("tpc").setExecutor(new AdminTeleport());
		getCommand("tpw").setExecutor(new AdminTeleport());
		getCommand("dw").setExecutor(new AdminShortcuts());
		getCommand("dp").setExecutor(new AdminShortcuts());
		getCommand("x").setExecutor(new AdminShortcuts());
		getCommand("shutdown").setExecutor(new Shutdown());
		getCommand("forcefield").setExecutor(new Forcefield());
		getCommand("setspawn").setExecutor(new Setspawn());
	}
	
	@Override
	public void onDisable(){
		//onDisable
	}
	

	
}
