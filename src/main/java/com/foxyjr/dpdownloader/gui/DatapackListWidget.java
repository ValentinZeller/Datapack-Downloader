package com.foxyjr.dpdownloader.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatapackListWidget extends AlwaysSelectedEntryListWidget<DatapackListWidget.DatapackEntry> {
	private final InstallDatapackScreen screen;
	private boolean resultsFound = false;
	public int moreIndex = 1;
	
	public DatapackListWidget(InstallDatapackScreen screen, MinecraftClient client) {
		super(client, screen.width/2 - 10, screen.height - 110,  70,  50);
		this.screen = screen;
		if (screen.worldList.getSelectedOrNull() != null || !screen.worldList.tempPath.isEmpty()) {
			this.updateDatapacks(this.screen.fetchProjects(0), true);
		}
	}
	
	@Override
	public int getRowWidth() {
		return this.width;
	}

	protected int getScrollbarX() {
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
		resultInfo.hits().forEach(info -> this.addEntry(new DatapackEntry(this.screen, this.client, info)));
		this.resultsFound = !resultInfo.hits().isEmpty() || !isInit;
	}
	
	public List<DatapackInfo> getDatapacks() {
		List<DatapackInfo> result = new ArrayList<>();
		for (int i = 0; i < this.getEntryCount(); i++) {
			result.add(this.getEntryAtPosition(i,0).info);
		}
		return result;
	}

	@Nullable
	@Override
	public DatapackEntry getSelectedOrNull() {
		return super.getSelectedOrNull();
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (this.screen.worldList.getSelectedOrNull() == null && this.screen.worldList.tempPath.isEmpty()) {
			context.drawTextWithShadow(this.client.textRenderer, Text.translatable("datapackdownloader.error.datapack.world"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xFFAA0000);
			return;
		}
		if (!this.resultsFound) {
			context.drawTextWithShadow(this.client.textRenderer, Text.translatable("datapackdownloader.error.datapack.result"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xFFAA0000);
			return;
		}
		super.renderWidget(context, mouseX, mouseY, delta);
	}

	public void setDatapacks(List<DatapackInfo> datapackInfo) {
		this.updateDatapacks(new ResultInfo(datapackInfo), true);
	}
	
	public class DatapackEntry extends Entry<DatapackEntry> {
		private final InstallDatapackScreen screen;
		private final MinecraftClient client;
		private final DatapackInfo info;
		private final ButtonWidget installButton;
		private final ButtonWidget updateButton;
		private int x;
		private int y;
		private int width;
		private boolean installed;
		
		public DatapackEntry(InstallDatapackScreen screen, MinecraftClient client, DatapackInfo info) {
			this.screen = screen;
			this.client = client;
			this.info = info;
			this.installButton = ButtonWidget.builder(Text.of(""), button -> {
			}).dimensions(0, 0, 50, 15).build();

			this.updateButton = ButtonWidget.builder(Text.translatable("datapackdownloader.button.datapack.update"), button -> {

			}).dimensions(0, 0, 50 ,15).build();

			this.installed = new File(this.screen.getDatapackPath(this.info.slug)).exists();
		}

		public DatapackInfo getInfo() {
			return info;
		}
		
		@Override
		public boolean mouseReleased(Click click) {
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
		public Text getNarration() {
			return Text.translatable("datapackdownloader.narration.datapack");
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			x = getContentX();
			y = getContentY();
			width = getContentWidth();
			context.drawTextWithShadow(this.client.textRenderer, this.info.title, x+5, y+5, 0xFFFFFFFF);
			context.drawTextWithShadow(this.client.textRenderer, this.info.author, x+5, y + 17, 0xFF999999);
			context.drawTextWithShadow(this.client.textRenderer, client.textRenderer.trimToWidth(this.info.description, width - 15), x+5, y + 30, 0xFF777777);
			installButton.setPosition(width + 100, y);
			installButton.render(context, mouseX, mouseY, deltaTicks);
			if (this.installed) {
				this.installButton.setMessage(Text.translatable("datapackdownloader.button.datapack.uninstall"));
				if (this.screen.isOutdated(info.slug,info.latest_version)) {
					this.updateButton.setPosition(width + 50, y);
					this.updateButton.render(context, mouseX, mouseY, deltaTicks);
				}
			} else {
				this.installButton.setMessage(Text.translatable("datapackdownloader.button.datapack.install"));
			}
		}
	}
}
