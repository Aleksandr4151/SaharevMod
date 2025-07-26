package com.example.Modules.elytraswapper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;

public class ElytraSwapperAutoJump implements ClientModInitializer {
    private static long lastJumpTime = 0;
    private static final long JUMP_COOLDOWN = 5000; // 5 seconds
    private static boolean autoJumpEnabled = true;
    
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Get a player
            PlayerEntity player = client.player;
            
            if (player == null) {
                return;
            }

            // Checking the condition for the jump
            if (ElytraSwapperModClient.PressForJump) {
                if (autoJumpEnabled && System.currentTimeMillis() - lastJumpTime >= JUMP_COOLDOWN) {
                    performJump(player);
                    
                    // The Challenge of Secure elytra exchange
                    try {
                        ElytraSwapperModClient.swapElytraSafely(player);
                    } catch (Exception e) {
                        System.err.println("Error to swap elytra!: " + e.getMessage());
                    }
                }
            }
        });
    }
    private void performJump(PlayerEntity player) {
        if (player != null) {
            player.jump();
            lastJumpTime = System.currentTimeMillis();
        }
    }
}