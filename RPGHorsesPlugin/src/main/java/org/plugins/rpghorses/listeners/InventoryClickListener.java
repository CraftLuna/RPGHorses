package org.plugins.rpghorses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.plugins.rpghorses.RPGHorsesMain;
import org.plugins.rpghorses.guis.*;
import org.plugins.rpghorses.guis.instances.*;
import org.plugins.rpghorses.horses.RPGHorse;
import org.plugins.rpghorses.managers.HorseOwnerManager;
import org.plugins.rpghorses.managers.MessageQueuer;
import org.plugins.rpghorses.managers.RPGHorseManager;
import org.plugins.rpghorses.managers.SQLManager;
import org.plugins.rpghorses.managers.gui.*;
import org.plugins.rpghorses.players.HorseOwner;
import org.plugins.rpghorses.tiers.Tier;
import org.plugins.rpghorses.utils.ItemUtil;
import org.plugins.rpghorses.utils.RPGMessagingUtil;
import roryslibrary.util.MessagingUtil;
import roryslibrary.util.SoundUtil;

import java.util.Map;

public class InventoryClickListener implements Listener {

	private final RPGHorsesMain     plugin;
	private final HorseOwnerManager horseOwnerManager;
	private final RPGHorseManager   rpgHorseManager;
	private final StableGUIManager  stableGUIManager;
	private final HorseGUIManager   horseGUIManager;
	private final TrailGUIManager   trailGUIManager;
	private final SQLManager        sqlManager;
	private final MessageQueuer     messageQueuer;
	private final RPGMessagingUtil  messagingUtil;

	public InventoryClickListener(RPGHorsesMain plugin, HorseOwnerManager horseOwnerManager, RPGHorseManager rpgHorseManager, StableGUIManager stableGUIManager, HorseGUIManager horseGUIManager, TrailGUIManager trailGUIManager, MessageQueuer messageQueuer, RPGMessagingUtil messagingUtil) {
		this.plugin = plugin;
		this.horseOwnerManager = horseOwnerManager;
		this.rpgHorseManager = rpgHorseManager;
		this.stableGUIManager = stableGUIManager;
		this.horseGUIManager = horseGUIManager;
		this.trailGUIManager = trailGUIManager;
		this.sqlManager = plugin.getSQLManager();
		this.messageQueuer = messageQueuer;
		this.messagingUtil = messagingUtil;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player     p          = (Player) e.getWhoClicked();
		HorseOwner horseOwner = this.horseOwnerManager.getHorseOwner(p);
		int        slot       = e.getSlot();

		if (horseOwner.getGUILocation() != GUILocation.NONE) {
			e.setCancelled(true);
		}

		if (horseOwner.isInGUI(GUILocation.STABLE_GUI)) {
			if (e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
				StableGUIPage stableGUIPage = horseOwner.getCurrentStableGUIPage();
				if (stableGUIPage == null) {
					horseOwner.setGUILocation(GUILocation.NONE);
					return;
				}

				Inventory inv = p.getOpenInventory().getTopInventory();

				if (slot == inv.getSize() - 1) {
					horseOwner.openStableGUIPage(stableGUIPage.getPageNum() + 1);
				} else if (slot == inv.getSize() - 9) {
					horseOwner.openStableGUIPage(stableGUIPage.getPageNum() - 1);
				} else {
					RPGHorse rpgHorse = stableGUIPage.getRPGHorse(slot);
					if (rpgHorse != null) {
						if (e.isLeftClick()) {
							horseOwner.toggleRPGHorse(rpgHorse);
						} else if (e.isRightClick()) {
							horseOwner.openHorseGUI(horseGUIManager.getHorseGUI(rpgHorse));
						}
					}
				}
			}
		} else if (horseOwner.isInGUI(GUILocation.HORSE_GUI)) {
			if (e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
				HorseGUI    horseGUI    = horseOwner.getHorseGUI();
				RPGHorse    rpgHorse    = horseGUI.getRpgHorse();
				int         horseNumber = horseOwner.getHorseNumber(rpgHorse);
				ItemPurpose itemPurpose = horseGUIManager.getItemPurpose(slot);
				if (itemPurpose == ItemPurpose.BACK) {
					horseOwner.openStableGUIPage(horseOwner.getCurrentStableGUIPage());
				} else if (itemPurpose == ItemPurpose.UPGRADE) {
					Tier tier = rpgHorseManager.getNextTier(rpgHorse);
					if (tier == null) {
						SoundUtil.playSound(p, plugin.getConfig(), "upgrade-options.success-sound");
						this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.max-tier-horse").replace("{HORSE-NUMBER}", "" + horseNumber), rpgHorse);
					} else if (rpgHorse.getXp() >= tier.getExpCost()) {
						if (plugin.getEconomy() == null || tier.getCost() <= plugin.getEconomy().getBalance(p)) {
							Map<ItemStack, Integer> itemsMissing = rpgHorseManager.getMissingItems(p, tier);
							if (itemsMissing.isEmpty()) {
								String tierStr = "" + (rpgHorse.getTier() + 1);
								if (this.rpgHorseManager.upgradeHorse(p, rpgHorse)) {
									this.stableGUIManager.updateRPGHorse(rpgHorse);
									p.closeInventory();
									this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.upgrade-horse-success").replace("{HORSE-NUMBER}", "" + horseNumber).replace("{TIER}", tierStr), rpgHorse);
									SoundUtil.playSound(p, plugin.getConfig(), "upgrade-options.success-sound");
									for (String cmd : this.plugin.getConfig().getStringList("command-options.on-upgrade-success")) {
										Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), MessagingUtil.format(cmd.replace("{PLAYER}", p.getName())));
									}
								} else {
									this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.upgrade-horse-failure").replace("{HORSE-NUMBER}", "" + horseNumber).replace("{TIER}", tierStr), rpgHorse);
									SoundUtil.playSound(p, plugin.getConfig(), "upgrade-options.failure-sound");
									for (String cmd : this.plugin.getConfig().getStringList("command-options.on-upgrade-fail")) {
										Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), MessagingUtil.format(cmd.replace("{PLAYER}", p.getName())));
									}
								}
							} else {
								SoundUtil.playSound(p, plugin.getConfig(), "upgrade-options.failure-sound");

								String items        = "";
								int    totalMissing = itemsMissing.size(), count = 0;
								for (ItemStack item : itemsMissing.keySet()) {
									int amount = itemsMissing.get(item);

									String name = "";
									if (item.getItemMeta().hasDisplayName()) {
										name = item.getItemMeta().getDisplayName();
									} else {
										name = "&6";
										String typeName = item.getType().name().toLowerCase().replace("_", "");
										for (String word : typeName.split("\\s")) {
											name += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
										}
										name = name.trim();
									}

									items += ChatColor.GRAY + "" + amount + "x " + name + ChatColor.GRAY;

									if (++count < totalMissing) {
										if (count == totalMissing - 1) {
											items += " and ";
										} else {
											items += ", ";
										}
									}
								}

								items = MessagingUtil.format(items);

								this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.missing-items-upgrade").replace("{HORSE-NUMBER}", "" + horseNumber), rpgHorse, "ITEMS", items);
								p.closeInventory();
							}
						} else {
							SoundUtil.playSound(p, plugin.getConfig(), "upgrade-options.failure-sound");
							this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.cant-afford-upgrade").replace("{HORSE-NUMBER}", "" + horseNumber), rpgHorse);
							p.closeInventory();
						}
					} else {
						SoundUtil.playSound(p, plugin.getConfig(), "upgrade-options.failure-sound");
						messagingUtil.sendMessageAtPath(p, "messages.not-enough-xp", rpgHorse);
						p.closeInventory();
					}
				} else if (itemPurpose == ItemPurpose.RENAME) {
					rpgHorseManager.openRenameGUI(p, horseOwner, rpgHorse);
				} else if (itemPurpose == ItemPurpose.TOGGLE_AUTOMOUNT_OFF || itemPurpose == ItemPurpose.TOGGLE_AUTOMOUNT_ON) {
					horseGUIManager.toggleAutoMount(horseGUI);
				} else if (itemPurpose == ItemPurpose.TRAILS) {
					horseOwner.setTrailsGUI(trailGUIManager.setupTrailsGUI(horseGUI));
					horseOwner.openTrailsGUIPage(horseOwner.getTrailsGUI().getPage(1));
				} else if (itemPurpose == ItemPurpose.DELETE) {
					p.closeInventory();
					this.rpgHorseManager.addRemoveConfirmation(p, rpgHorse);
					this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.confirm-remove-horse").replace("{HORSE-NUMBER}", "" + horseNumber), rpgHorse);
				} else if (slot == ItemUtil.getSlot(plugin.getConfig(), "horse-gui-options.horse-item")) {
					horseOwner.toggleRPGHorse(rpgHorse);
				}
			}
		} else if (horseOwner.isInGUI(GUILocation.TRAILS_GUI)) {
			if (e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
				TrailsGUI trailsGUI = horseOwner.getTrailsGUI();
				TrailsGUIPage page = horseOwner.getTrailsGUIPage();
				ItemPurpose itemPurpose = trailGUIManager.getItemPurpose(slot, page);
				if (itemPurpose == ItemPurpose.TRAIL) {
					if (trailsGUI.applyTrail(page, slot)) {
						RPGHorse rpgHorse = trailsGUI.getRpgHorse();
						String trailName = page.getTrailName(slot);
						this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.particle-set").replace("{HORSE-NUMBER}", "" + horseOwner.getHorseNumber(rpgHorse)).replace("{PARTICLE}", trailName.toUpperCase()), rpgHorse);
						horseOwner.openHorseGUI(horseOwner.getHorseGUI());
					}
				} else if (itemPurpose == ItemPurpose.PREVIOUS_PAGE) {
					horseOwner.openTrailsGUIPage(trailsGUI.getPage(page.getPageNum() - 1));
				} else if (itemPurpose == ItemPurpose.NEXT_PAGE) {
					horseOwner.openTrailsGUIPage(trailsGUI.getPage(page.getPageNum() + 1));
				} else if (itemPurpose == ItemPurpose.BACK) {
					horseOwner.openHorseGUI(horseOwner.getHorseGUI());
				} else if (itemPurpose == ItemPurpose.CLEAR_TRAIL)
				{
					TrailGUIItem currentTrail = page.getCurrentTrail();

					if (trailsGUI.removeTrail())
					{
						horseOwner.openHorseGUI(horseOwner.getHorseGUI());

						RPGHorse rpgHorse = trailsGUI.getRpgHorse();
						this.messagingUtil.sendMessage(p, this.plugin.getConfig().getString("messages.particle-removed").replace("{HORSE-NUMBER}", "" + horseOwner.getHorseNumber(rpgHorse)).replace("{PARTICLE}", currentTrail.getTrailName().toUpperCase()), rpgHorse);
					}
				}
			}
		} else if (horseOwner.isInGUI(GUILocation.YOUR_HORSES_GUI)) {
			e.setCancelled(true);
		} else if (horseOwner.getCurrentHorse() != null && e.getView().getTitle().equals(horseOwner.getCurrentHorse().getHorse().getName()) && e.getView().getTopInventory() == e.getClickedInventory() && e.getSlot() == 0) {
			e.setCancelled(true);
		}
	}

}
