package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Unique;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.meowing.zen.events.core.GuiEvent;
import xyz.meowing.zen.features.general.ContainerChat;

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Unique
    private EditBox zen$chatField;

    @Inject(
            method = "init",
            at = @At("RETURN")
    )
    private void zen$onInit(
            CallbackInfo ci
    ) {
        zen$chatField = ContainerChat.INSTANCE.createInputField((AbstractContainerScreen<?>) (Object) this);
    }

    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private void zen$onRender(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        if (ContainerChat.INSTANCE.shouldDrawInput() && zen$chatField != null) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
            context.fill(2, screen.height - 14, screen.width - 2, screen.height - 2, Integer.MIN_VALUE);
            zen$chatField.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"),
            cancellable = true
    )
    private void zen$onSlotClick(
            Slot slot,
            int slotId,
            int button,
            ClickType actionType,
            CallbackInfo ci
    ) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (EventBus.INSTANCE.post(new GuiEvent.Slot.Click(slot, slotId, button, actionType, this.menu, screen))) ci.cancel();
    }

    @WrapOperation(
            method = "renderSlots",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V")
    )
    private void zen$drawSlots(
            AbstractContainerScreen instance,
            GuiGraphics context,
            Slot slot,
            Operation<Void> original
    ) {
        EventBus.INSTANCE.post(new GuiEvent.Slot.Render(context, slot, instance));
        original.call(instance, context, slot);
    }

    @Inject(
            method = "keyPressed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;onClose()V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    //#if MC >= 1.21.9
    //$$ private void closeWindowPressed(net.minecraft.client.input.KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
    //#else
    private void closeWindowPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    //#endif
        if (EventBus.INSTANCE.post(new GuiEvent.Close((AbstractContainerScreen) (Object) this, this.menu))) {
            cir.setReturnValue(true);
        }
    }
}
