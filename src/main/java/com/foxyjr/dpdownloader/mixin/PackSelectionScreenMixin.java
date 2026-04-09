package com.foxyjr.dpdownloader.mixin;

import com.foxyjr.dpdownloader.gui.InstallDatapackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin {
    @Unique
    Button installDatapackButton;

    @Final @Shadow private Path packDir;

    @Inject(at = @At("HEAD"), method = "init")
    public void injectInit(CallbackInfo ci) {
        if (((PackSelectionScreen)(Object)this).getTitle().equals(Component.translatable("dataPack.title"))) //noinspection UnreachableCode
        {
            Minecraft client = Minecraft.getInstance();
            Screen currentScreen = client.screen;
            int y = ((Screen)(Object)this).height - 48;

            assert currentScreen != null;
            installDatapackButton = currentScreen.addRenderableWidget(
                    Button.builder(
                            Component.translatable("datapackdownloader.download"),
                            button -> {
                                assert packDir != null;
                                Minecraft.getInstance().setScreen(new InstallDatapackScreen(currentScreen, packDir.toString()));
                            }
                    ).bounds(180, y + 22, 140, 20)
                            .tooltip(Tooltip.create(Component.translatable("datapackdownloader.download.warning")))
                            .build());
        }
    }
}
