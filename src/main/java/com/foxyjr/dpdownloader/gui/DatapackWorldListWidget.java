package com.foxyjr.dpdownloader.gui;

import com.foxyjr.dpdownloader.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.CrashReport;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageException;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DatapackWorldListWidget extends ObjectSelectionList<DatapackWorldListWidget.WorldEntry> {
	final List<WorldEntry> worlds = new ArrayList<>();
	String search = "";
	String tempPath;
	
	public DatapackWorldListWidget(InstallDatapackScreen parent, Minecraft client, String temp) {
		super(client, 120 + 12, 100, 70, 12);
		tempPath = temp;
		if (tempPath.isEmpty()) {
			this.loadLevels();
		}

	}
	
	private void loadLevels() {
		LevelStorageSource.LevelCandidates levelList;
		try {
			levelList = this.minecraft.getLevelSource().findLevelCandidates();
		} catch (LevelStorageException levelStorageException) {
			Mod.LOGGER.error("Couldn't load level list", levelStorageException);
			this.showUnableToLoadScreen(levelStorageException.getMessageComponent());
			return;
		}
		this.minecraft.getLevelSource().loadLevelSummaries(levelList).exceptionally(throwable -> {
			this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
			return List.of();
		}).whenComplete((levelSummaries, throwable) -> {
			levelSummaries.forEach(levelSummary -> this.worlds.add(new WorldEntry(this.minecraft, levelSummary.getLevelName())));
			this.showSummaries("", this.worlds);
		});

		if (FabricLoader.getInstance().isModLoaded("global-datapack")) {
			this.worlds.add(new WorldEntry(this.minecraft, "Global Datapack (mod)"));
		}
	}
	
	private void showUnableToLoadScreen(Component message) {
		this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), message));
	}
	
	public void setSearch(String search) {
		if (!this.worlds.isEmpty()) {
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
			this.addEntry(new WorldEntry(this.minecraft, worldEntry.worldName));
		}
	}
	
	private boolean shouldShow(String search, WorldEntry summary) {
		return summary.worldName.toLowerCase().contains(search.toLowerCase());
	}
	
	@Override
	public int getRowWidth() {
		return this.width;
	}

	protected int scrollBarX() {
		return this.getRight() - 5;
	}
	
	public String getSelectedName() {
		WorldEntry selected = this.getSelected();
		if (selected == null) {
			return null;
		}
		return selected.worldName;
	}
	
	public class WorldEntry extends Entry<WorldEntry> {
		private final Minecraft client;
		private final String worldName;
		
		public WorldEntry(Minecraft client, String worldName) {
			this.client = client;
			this.worldName = worldName;
		}

		@Override
		public @NonNull Component getNarration() {
			return Component.translatable("datapackdownloader.narration.world");
		}

		@Override
		public boolean mouseReleased(@NonNull MouseButtonEvent click) {
			DatapackWorldListWidget.this.setSelected(this);
			return true;
		}

		@Override
		public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.text(this.client.font, this.worldName, getX(), getY(), 0xFFFFFFFF);
		}
	}
}
