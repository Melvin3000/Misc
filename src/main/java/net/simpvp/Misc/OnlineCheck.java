package net.simpvp.Misc;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OnlineCheck implements CommandExecutor {
	private static HashSet<UUID> waiting_for = new HashSet<UUID>();
	private static long last_request = System.currentTimeMillis();

	public boolean onCommand(CommandSender sender,
			Command cmd,
			String label,
			String[] args) {

		if (cmd.getName().equals("checkonline")) {
			online_check(sender);
		} else if (cmd.getName().equals("on")) {
			im_on(sender);
		}

		return true;
	}

	private void online_check(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if ((player != null) && (!player.isOp())) {
			error_log("You do not have permission to use this command.", sender);
			return;
		}

		if (System.currentTimeMillis() < (5 * 60 * 1000) + last_request) {
			error_log("An online check has been requested too recently. Please wait a bit longer before requesting another.", sender);
			return;
		}

		last_request = System.currentTimeMillis();

		if (!waiting_for.isEmpty()) {
			error_log("For some reason the list of players we're waiting for is not already empty.", sender);
			return;
		}

		String msg = ChatColor.AQUA + "[Announcement] Please verify that you're online by typing " + ChatColor.GOLD + "/on";
		Misc.instance.getLogger().info(msg);
		for (Player p : Misc.instance.getServer().getOnlinePlayers()) {
			p.sendMessage(msg);
			if ((player != null) && (player.getUniqueId() == p.getUniqueId())) {
				continue;
			}
			waiting_for.add(p.getUniqueId());
		}

		/* 30 seconds in the future */
		final long activate_at = System.currentTimeMillis() + 30 * 1000;

		/* What we do now is a create an asynchronous task that
		 * waits 30 seconds and then creates a synchronous task
		 * that calls sync_effect_restart */
		new BukkitRunnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() < activate_at) {
					return;
				}

				this.cancel();
				new BukkitRunnable() {
					@Override
					public void run() {
						kick_afk();
					}
				}.runTaskLater(Misc.instance, 0);
			}
		}.runTaskTimerAsynchronously(Misc.instance, 20L, 20L);
	}

	/* Must be called from the server's main thread! */
	private void kick_afk() {
		Misc.instance.getLogger().info("Kicking AFK people");

		for (UUID uuid : waiting_for) {
			Player player = Misc.instance.getServer().getPlayer(uuid);
			if (player == null) {
				continue;
			}

			player.kickPlayer("Kicked for not responding with /on");
		}
		waiting_for.clear();
	}

	private void im_on(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			error_log("Only players can use this command.", sender);
			return;
		}

		if (waiting_for.remove(player.getUniqueId())) {
			player.sendMessage(ChatColor.GREEN + "Thank you!");
		} else {
			player.sendMessage(ChatColor.GREEN + "You're good.");
		}
	}

	private void error_log(String msg, CommandSender sender) {
		if (sender instanceof Player) {
			((Player) sender).sendMessage(ChatColor.RED + msg);
		} else {
			sender.sendMessage(msg);
		}
		if (!(sender instanceof ConsoleCommandSender)) {
			Misc.instance.getLogger().info("OnlineCheck error: " + msg);
		}
       }
}
