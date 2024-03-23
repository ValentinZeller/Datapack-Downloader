package com.foxyjr.dpdownloader.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DatapackListWidget extends AlwaysSelectedEntryListWidget<DatapackListWidget.DatapackEntry> {
	private final InstallDatapackScreen screen;
	private boolean resultsFound = false;
	public int moreIndex = 1;
	
	public DatapackListWidget(InstallDatapackScreen screen, MinecraftClient client) {
		super(client, screen.width/2 - 10, screen.height - 110,  70,  50);
		this.screen = screen;
		if (screen.worldList.getSelectedOrNull() != null) {
			this.updateDatapacks(this.screen.fetchProjects(0), true);
		}
	}
	
	@Override
	public int getRowWidth() {
		return this.width;
	}
	
	@Override
	protected int getScrollbarPositionX() {
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

		resultInfo.hits().forEach(info -> this.addEntry(new DatapackEntry(this.screen, this.client, info)));
		this.resultsFound = resultInfo.hits().size() != 0 || !isInit;
	}
	
	public List<DatapackInfo> getDatapacks() {
		List<DatapackInfo> result = new ArrayList<>();
		for (int i = 0; i < this.getEntryCount(); i++) {
			result.add(this.getEntry(i).info);
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
		if (this.screen.worldList.getSelectedOrNull() == null) {
			context.drawTextWithShadow(this.client.textRenderer, Text.of("No World Selected!"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xAA0000);
			return;
		}
		if (!this.resultsFound) {
			context.drawTextWithShadow(this.client.textRenderer, Text.of("No Results Found!"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xAA0000);
			return;
		}
		super.renderWidget(context, mouseX, mouseY, delta);
	}

	public void setDatapacks(List<DatapackInfo> datapackInfo) {
		this.updateDatapacks(new ResultInfo(datapackInfo), true);
	}
	
	public class DatapackEntry extends AlwaysSelectedEntryListWidget.Entry<DatapackEntry> {
		private final InstallDatapackScreen screen;
		private final MinecraftClient client;
		private final DatapackInfo info;
		private final ButtonWidget installButton;
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
			this.installed = new File(this.screen.getDatapackPath(this.info.slug)).exists();
		}

		public DatapackInfo getInfo() {
			return info;
		}
		
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.x = x;
			this.y = y;
			this.width = entryWidth;
			context.drawTextWithShadow(this.client.textRenderer, this.info.title, x+5, y+5, 0xFFFFFF);
			context.drawTextWithShadow(this.client.textRenderer, this.info.author, x+5, y + 17, 0x999999);
			context.drawTextWithShadow(this.client.textRenderer, client.textRenderer.trimToWidth(this.info.description, width - 15), x+5, y + 30, 0x777777);
			context.drawTextWithShadow(this.client.textRenderer,"⭳ " + info.downloads +" - ♡ " + info.follows, entryWidth - 5 , y + 5, 0x777777);
			installButton.setX(entryWidth + 100);
			installButton.setY(y);
			installButton.render(context, mouseX, mouseY, tickDelta);
			if (this.installed) {
				this.installButton.setMessage(Text.of("Uninstall"));
			} else {
				this.installButton.setMessage(Text.of("Install"));
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button != 0) {
				return false;
			}
			if (mouseX > this.x + this.width - 55 && mouseX < this.x + this.width + 5 && mouseY > this.y - 5 && mouseY < this.y + 20) {
				if (this.installed) {
					try {
						Files.deleteIfExists(new File(this.screen.getDatapackPath(this.info.slug)).toPath());
					} catch (IOException e) {
						e.printStackTrace();
						return true;
					}
				} else {
					this.screen.installDatapack(this.info.slug);
				}
				this.installed = !this.installed;
			}
			DatapackListWidget.this.setSelected(this);
			return true;
		}
		
		@Override
		public Text getNarration() {
			return Text.of("Datapack List");
		}
	}
}
