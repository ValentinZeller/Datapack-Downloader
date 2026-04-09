package com.foxyjr.dpdownloader.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatapackListWidget extends ObjectSelectionList<DatapackListWidget.DatapackEntry> {
	private final InstallDatapackScreen screen;
	private boolean resultsFound = false;
	public int moreIndex = 1;
	
	public DatapackListWidget(InstallDatapackScreen screen, Minecraft client) {
		super(client, screen.width/2 - 10, screen.height - 110,  70,  50);
		this.screen = screen;
		if (screen.worldList.getSelected() != null || !screen.worldList.tempPath.isEmpty()) {
			this.updateDatapacks(this.screen.fetchProjects(0), true);
		}
	}
	
	@Override
	public int getRowWidth() {
		return this.width;
	}

	protected int scrollBarX() {
		return this.getRight() - 5;
	}
	
	public void updateDatapacks(ResultInfo resultInfo, boolean isInit) {
		if (isInit) {
			this.clearEntries();
			screen.totalResult = resultInfo.hits().size();
		} else {
			this.moreIndex++;
			screen.totalResult += resultInfo.hits().size();
		}

		this.screen.readJson();
		resultInfo.hits().forEach(info -> this.addEntry(new DatapackEntry(this.screen, this.minecraft, info)));
		this.resultsFound = !resultInfo.hits().isEmpty() || !isInit;
	}
	
	public List<DatapackInfo> getDatapacks() {
		List<DatapackInfo> result = new ArrayList<>();
		for (int i = 0; i < this.getItemCount(); i++) {
			result.add(Objects.requireNonNull(this.getEntryAtPosition(i, 0)).info);
		}
		return result;
	}

	@Nullable
	@Override
	public DatapackEntry getSelected() {
		return super.getSelected();
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (this.screen.worldList.getSelected() == null && this.screen.worldList.tempPath.isEmpty()) {
			context.text(this.minecraft.font, Component.translatable("datapackdownloader.error.datapack.world"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xFFAA0000);
			return;
		}
		if (!this.resultsFound) {
			context.text(this.minecraft.font, Component.translatable("datapackdownloader.error.datapack.result"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xFFAA0000);
			return;
		}
		super.extractWidgetRenderState(context, mouseX, mouseY, delta);
	}

	public void setDatapacks(List<DatapackInfo> datapackInfo) {
		this.updateDatapacks(new ResultInfo(datapackInfo), true);
	}
	
	public class DatapackEntry extends Entry<DatapackEntry> {
		private final InstallDatapackScreen screen;
		private final Minecraft client;
		private final DatapackInfo info;
		private final Button installButton;
		private final Button updateButton;
		private int x;
		private int y;
		private int width;
		private boolean installed;
		
		public DatapackEntry(InstallDatapackScreen screen, Minecraft client, DatapackInfo info) {
			this.screen = screen;
			this.client = client;
			this.info = info;
			this.installButton = Button.builder(Component.nullToEmpty("Install"), button -> {
			}).bounds(0, 0, 50, 15).build();

			this.updateButton = Button.builder(Component.translatable("datapackdownloader.button.datapack.update"), button -> {
			}).bounds(0, 0, 50 ,15).build();

			this.installed = new File(this.screen.getDatapackPath(this.info.slug)).exists();
		}

		public DatapackInfo getInfo() {
			return info;
		}
		
		@Override
		public boolean mouseReleased(MouseButtonEvent click) {
			if (click.button() != 0) {
				return false;
			}
			var mouseX = click.x();
			var mouseY = click.y();
			if (mouseX > this.x + this.width - 55 && mouseX < this.x + this.width + 5 && mouseY > this.y - 5 && mouseY < this.y + 20) {
				if (this.installed) {
					if (!this.screen.uninstallDatapack(this.info.slug)) {
						return true;
					}
				} else {
					this.screen.installDatapack(this.info.slug, this.info.latest_version);
				}
				this.installed = !this.installed;
			}

			if (mouseX > this.x + this.width - 115 && mouseX < this.x + this.width - 55 && mouseY > this.y - 5 && mouseY < this.y +20) {
				this.screen.updateDatapack(info.slug,info.latest_version);
			}

			DatapackListWidget.this.setSelected(this);
			return true;
		}
		
		@Override
		public @NonNull Component getNarration() {
			return Component.translatable("datapackdownloader.narration.datapack");
		}

		@Override
		public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			x = getContentX();
			y = getContentY();
			width = getContentWidth();
			context.text(this.client.font, this.info.title, x+5, y+5, 0xFFFFFFFF);
			context.text(this.client.font, this.info.author, x+5, y + 17, 0xFF999999);
			context.text(this.client.font, client.font.plainSubstrByWidth(this.info.description, width - 15), x+5, y + 30, 0xFF777777);
			installButton.setPosition(width + 100, y);
			installButton.extractRenderState(context, mouseX, mouseY, deltaTicks);
			if (this.installed) {
				this.installButton.setMessage(Component.translatable("datapackdownloader.button.datapack.uninstall"));
				if (this.screen.isOutdated(info.slug,info.latest_version)) {
					this.updateButton.setPosition(width + 50, y);
					this.updateButton.extractRenderState(context, mouseX, mouseY, deltaTicks);
				}
			} else {
				this.installButton.setMessage(Component.translatable("datapackdownloader.button.datapack.install"));
			}
		}
	}
}
