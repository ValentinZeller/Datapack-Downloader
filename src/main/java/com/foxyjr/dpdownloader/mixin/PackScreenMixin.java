package com.foxyjr.dpdownloader.mixin;

import com.foxyjr.dpdownloader.gui.InstallDatapackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(PackScreen.class)
public class PackScreenMixin {
    ButtonWidget installDatapackButton;

    @Final @Shadow private Path file;

    @Inject(at = @At("HEAD"), method = "init")
    public void injectInit(CallbackInfo ci) {
        if (((PackScreen)(Object)this).getTitle().equals(Text.translatable("dataPack.title"))) {
            MinecraftClient client = MinecraftClient.getInstance();
            Screen currentScreen = client.currentScreen;
            int y = ((Screen)(Object)this).height - 48;

            assert currentScreen != null;
            installDatapackButton = currentScreen.addDrawableChild(
                    ButtonWidget.builder(
                            Text.translatable("datapackdownloader.download"),
                            button -> {
                                assert file != null;
                                MinecraftClient.getInstance().setScreen(new InstallDatapackScreen(currentScreen, file.toString()));
                            }
                    ).dimensions(((Screen) (Object) this).width / 2 - 70, y + 24, 140, 20)
                            .tooltip(Tooltip.of(Text.translatable("datapackdownloader.download.warning")))
                            .build());
        }
    }
}
