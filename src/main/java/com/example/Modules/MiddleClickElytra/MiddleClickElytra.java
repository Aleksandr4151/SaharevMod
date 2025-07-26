package com.example.Modules.MiddleClickElytra;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import net.minecraft.screen.slot.SlotActionType;


public class MiddleClickElytra implements ClientModInitializer {
    private static KeyBinding swapMiddleKey;

    @Override
    public void onInitializeClient() {
        swapMiddleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.middlemousebind.action", // Translation key (for lang)
            InputUtil.Type.MOUSE, 
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 
            "category.middlemousebind.main" 
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MiddleClickConfig.modEnabled == true){
                if (client.player == null) return;

                //Checking bind
                if (swapMiddleKey.wasPressed()){
                    swapElytraSafely(client.player);
                }
            }
        });
    }

    public static void swapElytraSafely(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        // 1. Find Elytra
        int damagedSlot = -1;
        for (int i = 0; i < 36; i++) { // Только основные слоты инвентаря
            ItemStack stack = player.getInventory().getStack(i);
            ItemStack ChestItemCheck = player.getEquippedStack(EquipmentSlot.CHEST);
            if (!ChestItemCheck.isOf(Items.ELYTRA)){
                if (stack.isOf(Items.ELYTRA) && stack.getDamage() <= 100) {
                    damagedSlot = i;
                    break;
                }
            }
            if (ChestItemCheck.getDamage() > 1){
                if (stack.isOf(Items.ELYTRA) && stack.getDamage() <= 100) {
                    damagedSlot = i;
                    break;
                }
            }
        }
        // Authentification
        if (damagedSlot == -1) {
            ItemStack CheckElytras = player.getEquippedStack(EquipmentSlot.CHEST);
            if (CheckElytras.isOf(Items.ELYTRA)){
                player.sendMessage(Text.literal("§6[SaharevMod] §1MiddleClickElytra: §cYou already have elytra in your chest!"), false);
            }
            else{
                player.sendMessage(Text.literal("§6[SaharevMod] §1MiddleClickElytra: §cThere are no elytras in good condition!"), false);
            }
            return;
        }

        // 2. Checking slot
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        boolean shouldActivate = !chest.isOf(Items.ELYTRA) 
                                    || chest.isOf(Items.DIAMOND_CHESTPLATE) 
                                    || chest.isOf(Items.NETHERITE_CHESTPLATE) 
                                    || chest.isOf(Items.IRON_CHESTPLATE)
                                    || chest.isOf(Items.LEATHER_CHESTPLATE)
                                    || chest.isOf(Items.GOLDEN_CHESTPLATE)
                                    || chest.isOf(Items.CHAINMAIL_CHESTPLATE)
                                    || chest.isOf(Items.ELYTRA);
        // Если элитра в груди - меняем её
        if (shouldActivate == true){
            swapSlotsSafely(client, player, 6, damagedSlot);
        }
    }
    // 3.Equip Elytra
    public static void swapSlotsSafely(MinecraftClient client, PlayerEntity player, int equipSlot, int targetSlot) {
        client.execute(() -> {
            // 1. SHIFT+Click 
            client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                equipSlot,
                0,
                SlotActionType.QUICK_MOVE,
                player
            );
            // 2. SHIFT+Click 
            client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                targetSlot < 9 ? targetSlot + 36 : targetSlot, // converting in ID of clot GUI
                0,
                SlotActionType.QUICK_MOVE,
                player
            );
         });
    }

    public static void SwapArmorSafely(){
        //not now
    }
}
// Made by Saharev