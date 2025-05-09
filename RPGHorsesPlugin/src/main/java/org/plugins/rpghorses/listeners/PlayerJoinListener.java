package org.plugins.rpghorses.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.plugins.rpghorses.RPGHorsesMain;
import org.plugins.rpghorses.managers.HorseOwnerManager;
import org.plugins.rpghorses.managers.MessageQueuer;
import org.plugins.rpghorses.managers.gui.StableGUIManager;
import org.plugins.rpghorses.players.HorseOwner;
import org.plugins.rpghorses.utils.RPGMessagingUtil;
import roryslibrary.util.UpdateNotifier;

public class PlayerJoinListener implements Listener {

	private final RPGHorsesMain plugin;
	private final HorseOwnerManager horseOwnerManager;
	private final StableGUIManager stableGuiManager;
	private final RPGMessagingUtil messagingUtil;
	private final MessageQueuer messageQueuer;
	private final UpdateNotifier updateNotifier;

	public PlayerJoinListener(RPGHorsesMain plugin, HorseOwnerManager horseOwnerManager, StableGUIManager stableGuiManager, RPGMessagingUtil messagingUtil, MessageQueuer messageQueuer, UpdateNotifier updateNotifier) {
		this.plugin = plugin;
		this.horseOwnerManager = horseOwnerManager;
		this.stableGuiManager = stableGuiManager;
		this.messagingUtil = messagingUtil;
		this.messageQueuer = messageQueuer;
		this.updateNotifier = updateNotifier;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		plugin.getExecutorService().execute(() -> {
			if (p.isOnline()) {
				HorseOwner horseOwner = horseOwnerManager.loadData(p);
				stableGuiManager.setupStableGUI(horseOwner);
			}
		});
	}

}
