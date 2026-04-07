package org.bensam.arcanerelics.renderer;

import net.minecraft.world.InteractionHand;

public final class WandCastAnimationState {
    private InteractionHand activeHand;
    private boolean casting;
    private long castStartTick;
    private long castReleaseTick = -1L;
    private float releaseProgress;

    public InteractionHand getActiveHand() {
        return this.activeHand;
    }

    public boolean isCasting() {
        return this.casting;
    }

    public long getCastStartTick() {
        return this.castStartTick;
    }

    public long getCastReleaseTick() {
        return this.castReleaseTick;
    }

    public float getReleaseProgress() {
        return this.releaseProgress;
    }

    public void beginCast(boolean mainHand, long gameTick) {
        this.activeHand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.casting = true;
        this.castStartTick = gameTick;
        this.castReleaseTick = -1L;
        this.releaseProgress = 0.0f;
    }

    public void beginRelease(long gameTick) {
        this.castReleaseTick = gameTick;
        this.releaseProgress = 0.0f;
    }

    public void tickRelease(float progress) {
        this.releaseProgress = progress;
    }

    public void clear() {
        this.activeHand = null;
        this.casting = false;
        this.castStartTick = 0L;
        this.castReleaseTick = -1L;
        this.releaseProgress = 0.0f;
    }
}
