package com.bongo.realdeath.mixin.client;

import com.bongo.realdeath.DeathLoopSound;
import com.bongo.realdeath.DeathScreenAudioState;
import com.bongo.realdeath.HardcoreDeathRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen implements DeathScreenAudioState {
	@Unique
	private static final int REALDEATH_AUDIO_CUT_DELAY_TICKS = 3;
	@Unique
	private static final int REALDEATH_BLACK_TICKS = 60;
	@Unique
	private static final int REALDEATH_FADE_TICKS = 40;
	@Unique
	private static final int REALDEATH_REJOIN_FADE_TICKS = 10;
	@Unique
	private static final int REALDEATH_OBSERVE_AUDIO_FADE_TICKS = 240;
	@Unique
	private static final float REALDEATH_NORMAL_AUDIO_VOLUME = 0.7F;
	@Unique
	private static final float REALDEATH_REJOIN_AUDIO_VOLUME = 0.45F;
	@Unique
	private static final Component REALDEATH_REJOIN_MESSAGE = Component.literal(
		"You do not exist in this world anymore. Do you really want to watch life go on without you?"
	);

	@Shadow
	@Final
	private @Nullable Component causeOfDeath;
	@Shadow
	@Final
	private boolean hardcore;
	@Shadow
	@Final
	private LocalPlayer player;

	@Unique
	private final List<Button> realdeath$buttons = new ArrayList<>();
	@Unique
	private int realdeath$elapsedTicks;
	@Unique
	private boolean realdeath$started;
	@Unique
	private boolean realdeath$rejoiningHardcore;
	@Unique
	private DeathLoopSound realdeath$loopSound;
	@Unique
	private boolean realdeath$loopStarted;

	protected DeathScreenMixin(final Component title) {
		super(title);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void realdeath$onCreated(
		final @Nullable Component causeOfDeath,
		final boolean hardcore,
		final LocalPlayer player,
		final CallbackInfo ci
	) {
		this.realdeath$setCursorHidden(true);
		this.realdeath$rejoiningHardcore = hardcore
			&& HardcoreDeathRegistry.wasAlreadyDeadAndMark(this.minecraft);
		this.realdeath$loopSound = new DeathLoopSound(
			this.realdeath$rejoiningHardcore ? REALDEATH_REJOIN_AUDIO_VOLUME : REALDEATH_NORMAL_AUDIO_VOLUME
		);
	}

	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void realdeath$buildButtons(final CallbackInfo ci) {
		if (!this.realdeath$started) {
			this.realdeath$elapsedTicks = 0;
			this.realdeath$started = true;
		}
		this.realdeath$buttons.clear();

		if (this.realdeath$rejoiningHardcore) {
			this.realdeath$addButton(
				Button.builder(Component.literal("Observe"), button -> {
					this.realdeath$loopSound.beginFadeOut(REALDEATH_OBSERVE_AUDIO_FADE_TICKS);
					this.player.respawn();
					button.active = false;
				})
					.bounds(this.width / 2 - 100, this.height / 2 + 10, 200, 20)
					.build()
			);
			this.realdeath$addButton(
				Button.builder(Component.literal("Move On"), button -> this.realdeath$exitToTitle())
					.bounds(this.width / 2 - 100, this.height / 2 + 34, 200, 20)
					.build()
			);
		} else if (this.hardcore) {
			this.realdeath$addButton(
				Button.builder(Component.literal("Give Up"), button -> this.realdeath$exitToTitle())
					.bounds(this.width / 2 - 100, this.height / 2 - 10, 200, 20)
					.build()
			);
		} else {
			this.realdeath$addButton(
				Button.builder(Component.translatable("deathScreen.respawn"), button -> {
					this.player.respawn();
					button.active = false;
				})
					.bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20)
					.build()
			);
			this.realdeath$addButton(
				Button.builder(Component.translatable("deathScreen.titleScreen"), button -> this.realdeath$confirmExit())
					.bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20)
					.build()
			);
		}

		ci.cancel();
	}

	@Unique
	private void realdeath$addButton(final Button button) {
		float opacity = this.realdeath$opacity();
		button.active = opacity >= 1.0F;
		button.setAlpha(opacity);
		this.realdeath$buttons.add(this.addRenderableWidget(button));
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void realdeath$tick(final CallbackInfo ci) {
		this.realdeath$elapsedTicks++;
		float opacity = this.realdeath$opacity();
		this.realdeath$setCursorHidden(opacity < 1.0F);
		if (!this.realdeath$loopStarted && this.realdeath$isAudioCutoffActive()) {
			this.minecraft.getSoundManager().stop();
			if (opacity > 0.0F) {
				this.minecraft.getSoundManager().play(this.realdeath$loopSound);
				this.realdeath$loopStarted = true;
			}
		}
		this.realdeath$loopSound.setFade(opacity);
		for (Button button : this.realdeath$buttons) {
			button.setAlpha(opacity);
			button.active = opacity >= 1.0F;
		}
		ci.cancel();
	}

	@Override
	public boolean realdeath$isAudioCutoffActive() {
		return this.realdeath$elapsedTicks >= REALDEATH_AUDIO_CUT_DELAY_TICKS;
	}

	@Unique
	private void realdeath$setCursorHidden(final boolean hidden) {
		GLFW.glfwSetInputMode(
			this.minecraft.getWindow().handle(),
			GLFW.GLFW_CURSOR,
			hidden ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL
		);
	}

	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void realdeath$blackBackground(
		final GuiGraphicsExtractor graphics,
		final int mouseX,
		final int mouseY,
		final float partialTick,
		final CallbackInfo ci
	) {
		graphics.fill(0, 0, this.width, this.height, 0xFF000000);
		ci.cancel();
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void realdeath$renderGui(
		final GuiGraphicsExtractor graphics,
		final int mouseX,
		final int mouseY,
		final float partialTick,
		final CallbackInfo ci
	) {
		float opacity = this.realdeath$opacity();
		int interactiveMouseX = opacity >= 1.0F ? mouseX : Integer.MIN_VALUE;
		int interactiveMouseY = opacity >= 1.0F ? mouseY : Integer.MIN_VALUE;
		super.extractRenderState(graphics, interactiveMouseX, interactiveMouseY, partialTick);

		if (this.realdeath$rejoiningHardcore) {
			this.realdeath$renderRejoinMessage(graphics);
		} else if (!this.hardcore) {
			ActiveTextCollector output = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
			ActiveTextCollector.Parameters normal = output.defaultParameters().withOpacity(opacity);
			int middle = this.width / 2;

			output.accept(TextAlignment.CENTER, middle / 2, 30, normal.withScale(2.0F), this.title);
			if (this.causeOfDeath != null) {
				output.accept(TextAlignment.CENTER, middle, 85, normal, this.causeOfDeath);
			}

			Component score = Component.translatable(
				"deathScreen.score.value",
				Component.literal(Integer.toString(this.player.getScore())).withStyle(ChatFormatting.YELLOW)
			);
			output.accept(TextAlignment.CENTER, middle, 100, normal, score);
		}

		ci.cancel();
	}

	@Unique
	private void realdeath$renderRejoinMessage(final GuiGraphicsExtractor graphics) {
		float opacity = this.realdeath$opacity();
		ActiveTextCollector output = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
		ActiveTextCollector.Parameters parameters = output.defaultParameters().withOpacity(opacity);
		List<FormattedCharSequence> lines = this.font.split(REALDEATH_REJOIN_MESSAGE, Math.min(420, this.width - 40));
		int top = this.height / 2 - 42 - (lines.size() * 10) / 2;
		for (int index = 0; index < lines.size(); index++) {
			output.accept(TextAlignment.CENTER, this.width / 2, top + index * 10, parameters, lines.get(index));
		}
	}

	@Unique
	private float realdeath$opacity() {
		int blackTicks = this.realdeath$rejoiningHardcore ? 0 : REALDEATH_BLACK_TICKS;
		int fadeTicks = this.realdeath$rejoiningHardcore ? REALDEATH_REJOIN_FADE_TICKS : REALDEATH_FADE_TICKS;
		float linear = Math.clamp(
			(this.realdeath$elapsedTicks - blackTicks) / (float)fadeTicks,
			0.0F,
			1.0F
		);
		return linear * linear * (3.0F - 2.0F * linear);
	}

	@Unique
	private void realdeath$confirmExit() {
		ConfirmScreen confirm = new RealDeathConfirmScreen(
			result -> {
				if (result) {
					this.realdeath$exitToTitle();
				} else {
					this.minecraft.setScreen((Screen)(Object)this);
				}
			},
			Component.translatable("deathScreen.quit.confirm"),
			CommonComponents.EMPTY,
			Component.translatable("deathScreen.titleScreen"),
			Component.translatable("gui.cancel")
		);
		this.minecraft.setScreen(confirm);
	}

	@Unique
	private void realdeath$exitToTitle() {
		if (this.minecraft.level != null) {
			this.minecraft.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
		}
		this.minecraft.disconnectWithSavingScreen();
		this.minecraft.setScreen(new TitleScreen());
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		this.realdeath$setCursorHidden(false);
	}

	@Unique
	private static final class RealDeathConfirmScreen extends ConfirmScreen {
		private RealDeathConfirmScreen(
			final it.unimi.dsi.fastutil.booleans.BooleanConsumer callback,
			final Component title,
			final Component message,
			final Component yesButton,
			final Component noButton
		) {
			super(callback, title, message, yesButton, noButton);
		}

		@Override
		public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
			graphics.fill(0, 0, this.width, this.height, 0xFF000000);
		}
	}
}
