package com.bongo.realdeath.mixin.client;

import com.bongo.realdeath.DeathLoopSound;
import com.bongo.realdeath.DeathScreenAudioState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
	@Inject(method = "play", at = @At("HEAD"), cancellable = true)
	private void realdeath$blockSoundsDuringDeath(
		final SoundInstance sound,
		final CallbackInfoReturnable<SoundEngine.PlayResult> cir
	) {
		if (Minecraft.getInstance().gui.screen() instanceof DeathScreenAudioState state
			&& state.realdeath$isAudioCutoffActive()
			&& !(sound instanceof DeathLoopSound)) {
			cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
		}
	}
}
