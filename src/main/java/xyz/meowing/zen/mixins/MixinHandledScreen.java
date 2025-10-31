package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.meowing.zen.events.core.GuiEvent;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(HandledScreen.class)
public class MixinHandledScreen {
    @Shadow
    @Final
    protected ScreenHandler handler;

    @Inject(
            method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"),
            cancellable = true
    )
    private void zen$onSlotClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        if (EventBus.INSTANCE.post(new GuiEvent.Slot.Click(slot, slotId, button, actionType, handler, screen))) ci.cancel();
    }

    @WrapOperation(
            method = "drawSlots",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V")
    )
    private void zen$drawSlots(HandledScreen instance, DrawContext context, Slot slot, Operation<Void> original) {
        EventBus.INSTANCE.post(new GuiEvent.Slot.Render(context, slot, instance));
        original.call(instance, context, slot);
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;close()V", shift = At.Shift.BEFORE), cancellable = true)
    //#if MC >= 1.21.9
    //$$ private void closeWindowPressed(net.minecraft.client.input.KeyInput input, CallbackInfoReturnable<Boolean> cir) {
    //#else
    private void closeWindowPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    //#endif
        if (this.handler != null && EventBus.INSTANCE.post(new GuiEvent.Close((HandledScreen) (Object) this, this.handler))) {
            cir.setReturnValue(true);
        }
    }
}
