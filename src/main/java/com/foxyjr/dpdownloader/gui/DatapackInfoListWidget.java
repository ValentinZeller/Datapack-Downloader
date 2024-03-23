package com.foxyjr.dpdownloader.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

public class DatapackInfoListWidget extends EntryListWidget<DatapackInfoListWidget.DatapackInfoEntry> {
    private final InstallDatapackScreen screen;

    public DatapackInfoListWidget(InstallDatapackScreen screen, MinecraftClient client) {
        super(client, screen.width/2 - 250, screen.height - 110,  70,  40);
        this.screen = screen;
        updateDatapack();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (screen.datapackList.getSelectedOrNull() == null) {
            context.drawTextWithShadow(this.client.textRenderer, Text.of("No Datapack Selected!"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xAA0000);
            return;
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    public void updateDatapack() {
        this.clearEntries();
        if (screen.datapackList.getSelectedOrNull() != null) {
            this.addEntry(new DatapackInfoEntry(client, screen.datapackList.getSelectedOrNull().getInfo()));
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        if (screen.datapackList.getSelectedOrNull() == null) {
            builder.put(NarrationPart.TITLE, "No Datapack Selected!" );
        } else {
            builder.put(NarrationPart.TITLE, screen.datapackList.getSelectedOrNull().getInfo().title);
        }
    }

    public class DatapackInfoEntry extends EntryListWidget.Entry<DatapackInfoEntry> {
        private final MinecraftClient client;
        private final DatapackInfo info;

        public DatapackInfoEntry(MinecraftClient client, DatapackInfo info) {
            this.client = client;
            this.info = info;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(this.client.textRenderer, info.title, x + 5, y + 10, 0xFFFFFF);
            context.drawTextWithShadow(this.client.textRenderer, info.author, x + 5, y + 20, 0x999999);
            context.drawTextWithShadow(this.client.textRenderer,"⭳ " + info.downloads +" - ♡ " + info.follows, x + 5 , y, 0x777777);
            context.drawTextWithShadow(this.client.textRenderer, info.license, x+5 , y+370 , 0x777777);
            for(int i = 0; i < client.textRenderer.wrapLines(StringVisitable.plain(info.description), entryWidth - 10).size(); i++) {
                context.drawTextWithShadow(this.client.textRenderer, client.textRenderer.wrapLines(StringVisitable.plain(info.description), entryWidth - 10).get(i), x + 5, y + 30 + 10 * i, 0x777777);
            }

            for(int i = 0; i < info.display_categories.length; i++) {
                context.drawTextWithShadow(this.client.textRenderer, info.display_categories[i], x + 5, y + 360 - i * 10, 0x999999);
            }
        }

    }
}
