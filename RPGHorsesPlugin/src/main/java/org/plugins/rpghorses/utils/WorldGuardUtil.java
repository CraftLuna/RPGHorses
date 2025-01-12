package org.plugins.rpghorses.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugins.rpghorses.AbstractWorldGuard;

import java.util.Collections;
import java.util.List;

/*
 * @author Rory Skipper (Roree) on 2024-08-25
 */
public class WorldGuardUtil {

	@Getter
    private static final AbstractWorldGuard worldGuard = new WGEmpty();

    public static String getRPGHorsesPVPFlagName() {
		return "rpghorses-pvp";
	}

	public static String getRPGHorsesFlagName() {
		return "rpghorses";
	}

	public static boolean isEnabled() {
		return getWorldGuard() != null;
	}

	public static List<String> getRegions(Location l) {
		if (!isEnabled()) return Collections.emptyList();
		return getWorldGuard().getRegions(l);
	}

	public static boolean areRPGHorsesAllowed(Player player, Location location) {
		if (!isEnabled()) return true;
		return getWorldGuard().isFlagAllowed(player, location, getRPGHorsesFlagName(), true);
	}

	public static boolean isHorsePVPAllowed(Player player, Location location) {
		if (!isEnabled()) return true;
		return getWorldGuard().isFlagAllowed(player, location, AbstractWorldGuard.getRPGHorsesPVPFlagName(), true);
	}

	public static boolean isFlagAllowed(Player player, Location location, String flagName, boolean orNone) {
		if (!isEnabled()) return true;
		return getWorldGuard().isFlagAllowed(player, location, flagName, orNone);
	}

	public static boolean isFlagDenied(Player player, Location location, String flagName, boolean orNone) {
		if (!isEnabled()) return false;
		return !getWorldGuard().isFlagAllowed(player, location, flagName, orNone);
	}

	public static void createFlags() {
		if (!isEnabled()) return;
		getWorldGuard().createFlags();
	}

	public static class WGEmpty extends AbstractWorldGuard {

		@Override
		public List<String> getRegions(Location l) {
			return Collections.emptyList();
		}

		@Override
		public boolean isFlagAllowed(Player player, Location location, String flagName, boolean orNone) {
			return true;
		}

		@Override
		public boolean isFlagDenied(Player player, Location location, String flagName, boolean orNone) {
			return false;
		}

		@Override
		public void createFlags() {

		}

	}

}
