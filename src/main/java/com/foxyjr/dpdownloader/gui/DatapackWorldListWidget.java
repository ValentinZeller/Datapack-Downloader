package com.foxyjr.dpdownloader.gui;

import com.foxyjr.dpdownloader.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;

import java.util.ArrayList;
import java.util.List;

public class DatapackWorldListWidget extends AlwaysSelectedEntryListWidget<DatapackWorldListWidget.WorldEntry> {
	final List<DatapackWorldListWidget.WorldEntry> worlds = new ArrayList<>();
	String search = "";
	
	public DatapackWorldListWidget(InstallDatapackScreen parent, MinecraftClient client) {
		super(client, 120 + 12, 100, 70, 12);
		this.loadLevels();
	}
	
	private void loadLevels() {
		LevelStorage.LevelList levelList;
		try {
			levelList = this.client.getLevelStorage().getLevelList();
		} catch (LevelStorageException levelStorageException) {
			Mod.LOGGER.error("Couldn't load level list", levelStorageException);
			this.showUnableToLoadScreen(levelStorageException.getMessageText());
			return;
		}
		this.client.getLevelStorage().loadSummaries(levelList).exceptionally(throwable -> {
			this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Couldn't load level list"));
			return List.of();
		}).whenComplete((levelSummaries, throwable) -> {
			levelSummaries.forEach(levelSummary -> this.worlds.add(new WorldEntry(this.client, levelSummary.getDisplayName())));
			this.showSummaries("", this.worlds);
		});
	}
	
	private void showUnableToLoadScreen(Text message) {
		this.client.setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), message));
	}
	
	public void setSearch(String search) {
		if (this.worlds.size() != 0) {
			this.showSummaries(search, this.worlds);
		}
		this.search = search;
	}
	
	public void showSummaries(String search, List<WorldEntry> worldEntries) {
		this.clearEntries();
		search = search.toLowerCase();
		for (WorldEntry worldEntry : worldEntries) {
			if (!this.shouldShow(search, worldEntry))
				continue;
			this.addEntry(new WorldEntry(this.client, worldEntry.worldName));
		}
	}
	
	private boolean shouldShow(String search, WorldEntry summary) {
		return summary.worldName.toLowerCase().contains(search.toLowerCase());
	}
	
	@Override
	public int getRowWidth() {
		return this.width;
	}
	
	@Override
	protected int getScrollbarPositionX() {
		return this.getRight() - 5;
	}
	
	public String getSelected() {
		WorldEntry selected = this.getSelectedOrNull();
		if (selected == null) {
			return null;
		}
		return selected.worldName;
	}
	
	public class WorldEntry extends AlwaysSelectedEntryListWidget.Entry<WorldEntry> {
		private final MinecraftClient client;
		private final String worldName;
		
		public WorldEntry(MinecraftClient client, String worldName) {
			this.client = client;
			this.worldName = worldName;
		}
		
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			context.drawTextWithShadow(this.client.textRenderer, this.worldName, x, y, 0xFFFFFF);
		}
		
		@Override
		public Text getNarration() {
			return Text.of("Datapack World List");
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			DatapackWorldListWidget.this.setSelected(this);
			return true;
		}
	}
}
