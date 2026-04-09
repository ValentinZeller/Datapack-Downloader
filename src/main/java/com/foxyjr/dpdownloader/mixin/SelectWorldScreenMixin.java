package com.foxyjr.dpdownloader.mixin;

import com.foxyjr.dpdownloader.gui.InstallDatapackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin {

    @Inject(at = @At("TAIL"), method = "init")
	public void injectInit(CallbackInfo ci) {
        Button installDatapackButton = ((SelectWorldScreen) (Object) this).addRenderableWidget(Button.builder(Component.translatable("datapackdownloader.title"), button -> Minecraft.getInstance().setScreen(new InstallDatapackScreen((SelectWorldScreen) (Object) this))).bounds(((Screen) (Object) this).width / 2 - 200, 22, 95, 20).build());
	}

}
