package com.foxyjr.dpdownloader.mixin;

import com.foxyjr.dpdownloader.gui.InstallDatapackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.security.auth.callback.Callback;
import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Shadow @Nullable protected abstract Path getDataPackTempDir();
    ButtonWidget installDatapackButton;

    @Inject(at = @At("TAIL"), method = "init")
    protected void injectInit(CallbackInfo ci) {
        Path tempPath = getDataPackTempDir();
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;
        int y = ((Screen)(Object)this).height - 80;

        assert currentScreen != null;
        installDatapackButton = currentScreen.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("datapackdownloader.download"),
                        button -> {
                            assert tempPath != null;
                            MinecraftClient.getInstance().setScreen(new InstallDatapackScreen(currentScreen, tempPath.toString()));
                        }
                ).dimensions(((Screen) (Object) this).width / 2 - 105, y, 210, 20)
                        .tooltip(Tooltip.of(Text.translatable("datapackdownloader.download.warning")))
                        .build());
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void injectRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (installDatapackButton != null) {
            installDatapackButton.setPosition(((Screen) (Object) this).width / 2 - 105,((Screen)(Object)this).height - 80);
            installDatapackButton.render(context, mouseX, mouseY, delta);
        }
    }
}
