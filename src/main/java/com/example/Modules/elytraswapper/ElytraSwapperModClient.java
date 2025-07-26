package com.example.Modules.elytraswapper;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ElytraSwapperModClient implements ClientModInitializer {
    private boolean wasKeyPressed = false;
    public static boolean PressForJump = false;
    public static boolean ElytraPlus = false;
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean isKeyPressed = KeybindManager.swapKey.isPressed();
            
            // Обмен элитр
            if (isKeyPressed && !wasKeyPressed) {
                swapElytraSafely(client.player);
            }

            // Переключение автопрыжка
            if (KeybindManager.swapKey.wasPressed()) {
                PressForJump = !PressForJump;
                String message = PressForJump ? "§aElytraSwapper On" : "§cElytraSwapper Off";
                client.player.sendMessage(Text.literal("§6[SaharevMod]:" + message), false);
            }
            wasKeyPressed = isKeyPressed;
        });
    }
    

    public static void swapElytraSafely(PlayerEntity player) {
    MinecraftClient client = MinecraftClient.getInstance();
    
    // 1. Ищем повреждённую элитру в инвентаре (но не в оффхенде)
    int damagedSlot = -1;
    for (int i = 0; i < 36; i++) { // Только основные слоты инвентаря
        ItemStack stack = player.getInventory().getStack(i);
        ItemStack ChestItemCheck = player.getEquippedStack(EquipmentSlot.CHEST);
        if (ChestItemCheck.getDamage() == 0){
            if (stack.isOf(Items.ELYTRA) && stack.getDamage() >= 1) {
                damagedSlot = i;
                break;
            }
        }
    }

    if (damagedSlot == -1) {
        ItemStack ChestItemCheck2 = player.getEquippedStack(EquipmentSlot.CHEST);
        if (ChestItemCheck2.getDamage() > 0){
            return;
        }    
        
        player.sendMessage(Text.literal("§6[SaharevMod] §bElytraSwapper: §6No damaged elytras in the inventory!"), false);
        System.out.println("SAHAREVMOD: The ElytraSwapper is turned off. Reason: No damaged elytras in the inventory!");
        PressForJump = !PressForJump;
        return;
    }
        

    // 2. Проверяем экипированные элитры
    ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
    ItemStack offhand = player.getEquippedStack(EquipmentSlot.OFFHAND);
    boolean shouldActivate = !chest.isOf(Items.ELYTRA) 
                                || chest.isOf(Items.DIAMOND_CHESTPLATE) 
                                || chest.isOf(Items.NETHERITE_CHESTPLATE) 
                                || chest.isOf(Items.IRON_CHESTPLATE)
                                || chest.isOf(Items.LEATHER_CHESTPLATE)
                                || chest.isOf(Items.GOLDEN_CHESTPLATE)
                                || chest.isOf(Items.CHAINMAIL_CHESTPLATE);
    // Если элитра в груди - меняем её
    if (shouldActivate == true){
        swapSlotsSafely(client, player, 6, damagedSlot);
    }
    if (chest.isOf(Items.ELYTRA)) {
        swapSlotsSafely(client, player, 6, damagedSlot); // 6 - chest
        return;
    }

    // If in offhand, changing to chest
    if (offhand.isOf(Items.ELYTRA)) {
        if (chest.isEmpty()) { 
            swapSlotsSafely(client, player, 45, damagedSlot); // 45 - offhand
        } else {
            player.sendMessage(Text.literal("§6SaharevMod: §cRelease the breast slot!"), false);
        }
        return;
    }
}

    public static void swapSlotsSafely(MinecraftClient client, PlayerEntity player, int equipSlot, int targetSlot) {
        client.execute(() -> {
            // 1. SHIFT+click (In inventory)
            client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                equipSlot,
                0,
                SlotActionType.QUICK_MOVE,
                player
            );
            
            // 2. SHIFT+Click (equip)
            client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                targetSlot < 9 ? targetSlot + 36 : targetSlot, // converting in ID of slot GUI
                0,
                SlotActionType.QUICK_MOVE,
                player
            );
        });
    }
}