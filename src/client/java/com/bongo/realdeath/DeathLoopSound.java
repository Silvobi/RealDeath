package com.bongo.realdeath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public final class DeathLoopSound extends AbstractTickableSoundInstance {
	private static final SoundEvent SOUND = SoundEvent.createVariableRangeEvent(
		Identifier.fromNamespaceAndPath(RealDeathClient.MOD_ID, "death-ambient")
	);
	private final float maximumVolume;
	private int fadeOutTicksRemaining;
	private float fadeOutStep;

	public DeathLoopSound(final float maximumVolume) {
		super(SOUND, SoundSource.MASTER, SoundInstance.createUnseededRandom());
		this.maximumVolume = maximumVolume;
		this.looping = true;
		this.relative = true;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.volume = 0.0F;
	}

	public void setFade(final float fade) {
		if (this.fadeOutTicksRemaining == 0) {
			this.volume = this.maximumVolume * fade;
		}
	}

	public void beginFadeOut(final int durationTicks) {
		this.fadeOutTicksRemaining = Math.max(1, durationTicks);
		this.fadeOutStep = this.volume / this.fadeOutTicksRemaining;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		if (this.fadeOutTicksRemaining > 0) {
			this.volume = Math.max(0.0F, this.volume - this.fadeOutStep);
			this.fadeOutTicksRemaining--;
			if (this.fadeOutTicksRemaining == 0) {
				this.stop();
			}
		} else if (!(Minecraft.getInstance().screen instanceof DeathScreen)) {
			this.stop();
		}
	}
}
