package com.foxyjr.dpdownloader.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

//TODO rework
public class DatapackInfoListWidget extends AlwaysSelectedEntryListWidget<DatapackInfoListWidget.DatapackInfoEntry> {
    private final InstallDatapackScreen screen;

    public DatapackInfoListWidget(InstallDatapackScreen screen, MinecraftClient client) {
        super(client, screen.width/2 - 180, screen.height - 110,  70,  40);
        this.screen = screen;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (screen.datapackList.getSelectedOrNull() == null) {
            context.drawTextWithShadow(this.client.textRenderer, Text.of("No Datapack Selected!"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xAA0000);
            return;
        }
        DatapackInfo info = screen.datapackList.getSelectedOrNull().getInfo();
        context.drawTextWithShadow(this.client.textRenderer, info.title, this.getX() + 10, this.getY() + 10, 0xFFFFFF);
        context.drawTextWithShadow(this.client.textRenderer, info.author, this.getX() + 10, this.getY() + 20, 0x999999);
        for(int i = 0; i < client.textRenderer.wrapLines(StringVisitable.plain(info.description), this.width - 20).size(); i++) {
            context.drawTextWithShadow(this.client.textRenderer, client.textRenderer.wrapLines(StringVisitable.plain(info.description), this.width - 10).get(i), this.getX() + 10, this.getY() + 30 + 10 * i, 0x777777);
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    public class DatapackInfoEntry extends AlwaysSelectedEntryListWidget.Entry<DatapackInfoEntry> {

        public DatapackInfoEntry(InstallDatapackScreen screen, MinecraftClient client, DatapackInfo info) {
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) { }

        @Override
        public Text getNarration() {
            return Text.of("Datapack Info");
        }
    }
}
