package com.revolvingmadness.dpdownloader.mixin;

import com.revolvingmadness.dpdownloader.gui.InstallDatapackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin {
	private ButtonWidget installDatapackButton;
	
	@Inject(at = @At("TAIL"), method = "init")
	public void injectInit(CallbackInfo ci) {
		this.installDatapackButton = ((SelectWorldScreen) (Object) this).addDrawableChild(ButtonWidget.builder(Text.of("Install Datapacks"), button -> MinecraftClient.getInstance().setScreen(new InstallDatapackScreen((SelectWorldScreen) (Object) this))).dimensions(((Screen) (Object) this).width / 2 - 200, 22, 95, 20).build());
	}
	
	@Inject(at = @At("TAIL"), method = "render")
	public void injectRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		this.installDatapackButton.render(context, mouseX, mouseY, delta);
	}
}
