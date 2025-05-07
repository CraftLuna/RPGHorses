package org.plugins.rpghorses.guis.instances;

import org.bukkit.inventory.Inventory;

public class YourHorsesGUIPage {

	private int pageNum;
	private Inventory gui;

	public YourHorsesGUIPage(int pageNum, Inventory gui) {
		this.pageNum = pageNum;
		this.gui = gui;
	}

	public int getPageNum() {
		return pageNum;
	}

	public Inventory getGUI() {
		return gui;
	}

	public void setGUI(Inventory gui) {
		this.gui = gui;
	}
}
