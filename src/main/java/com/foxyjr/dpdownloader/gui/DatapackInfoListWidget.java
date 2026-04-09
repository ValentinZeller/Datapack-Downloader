package com.foxyjr.dpdownloader.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;

public class DatapackInfoListWidget extends AbstractSelectionList<DatapackInfoListWidget.DatapackInfoEntry> {
    private final InstallDatapackScreen screen;

    public DatapackInfoListWidget(InstallDatapackScreen screen, Minecraft client) {
        super(client, screen.width/2 - 170, screen.height - 110,  70,  40);
        this.screen = screen;
        updateDatapack();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (screen.datapackList.getSelected() == null) {
            context.text(this.minecraft.font, Component.translatable("datapackdownloader.error.datapackinfo.datapack"), this.getX() + this.width / 2 - 40, this.getY() + 20, 0xAA0000);
            return;
        }

        super.extractWidgetRenderState(context, mouseX, mouseY, delta);
    }

    public void updateDatapack() {
        this.clearEntries();
        if (screen.datapackList.getSelected() != null) {
            this.addEntry(new DatapackInfoEntry(minecraft, screen.datapackList.getSelected().getInfo()));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        if (screen.datapackList.getSelected() == null) {
            builder.add(NarratedElementType.TITLE, Component.translatable("datapackdownloader.error.datapackinfo.datapack") );
        } else {
            builder.add(NarratedElementType.TITLE, screen.datapackList.getSelected().getInfo().title);
        }
    }

    public class DatapackInfoEntry extends Entry<DatapackInfoEntry> {
        private final Minecraft client;
        private final DatapackInfo info;

        public DatapackInfoEntry(Minecraft client, DatapackInfo info) {
            this.client = client;
            this.info = info;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            var x = screen.width / 2 + 160; //Fix offset
            var y = getY();
            context.text(this.client.font, info.title, x + 5, y + 10, 0xFFFFFFFF);
            context.text(this.client.font, info.author, x + 5, y + 20, 0xFF999999);
            context.text(this.client.font,"⭳ " + info.downloads +" - ♡ " + info.follows, x + 5 , y, 0xFF777777);
            context.text(this.client.font, info.license, x+5 , y+370 , 0xFF777777);
            for(int i = 0; i < client.font.split(FormattedText.of(info.description), getWidth() - 10).size(); i++) {
                context.text(this.client.font, client.font.split(FormattedText.of(info.description), getWidth() - 10).get(i), x + 5, y + 30 + 10 * i, 0xFF777777);
            }

            for(int i = 0; i < info.display_categories.length; i++) {
                context.text(this.client.font, info.display_categories[i], x + 5, y + 360 - i * 10, 0xFF999999);
            }
        }
    }
}
