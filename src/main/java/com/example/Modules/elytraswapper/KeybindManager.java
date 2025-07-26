package com.example.Modules.elytraswapper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import com.mojang.brigadier.arguments.StringArgumentType;

@Environment(EnvType.CLIENT)
public class KeybindManager implements ClientModInitializer {
    public static KeyBinding swapKey;
    private static String currentKeybind = "V";
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("elytra_swapper_bind.txt");

    @Override
    public void onInitializeClient() {
        loadKeybind();
        registerKeyBinding();
        registerCommand();
    }
    
    public void registerKeyBinding() {
        swapKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.elytraswapper.swap",
                InputUtil.Type.KEYSYM,
                getKeyCode(currentKeybind),
                "category.elytraswapper"
            )
        );
    }

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("bindElytraSwapper")
                .then(ClientCommandManager.argument("key", StringArgumentType.greedyString())
                .executes(context -> {
                    String newKey = context.getArgument("key", String.class).toUpperCase().trim();
                    if (setNewKeyBind(newKey)) {
                        context.getSource().sendFeedback(Text.literal("§6Rebinded on: " + newKey));
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("§cInvalid Key! Use A-Z, 0-9 or F1-F12"));
                        return 0;
                    }
                }))
            );
        });
    }

    private static boolean setNewKeyBind(String key) {
        if (!isValidKey(key)) return false;
        
        try {
            int keyCode = getKeyCode(key);
            if (keyCode == -1) return false;
            
            swapKey.setBoundKey(InputUtil.fromKeyCode(keyCode, 0));
            currentKeybind = key;
            KeyBinding.updateKeysByCode();
            saveKeybind();
            return true;
        } catch (Exception e) {
            System.err.println("Error setting keybind: " + e.getMessage());
            return false;
        }
    }

    private static int getKeyCode(String key) {
        if (key.startsWith("F") && key.length() > 1) {
            try {
                int fNum = Integer.parseInt(key.substring(1));
                if (fNum >= 1 && fNum <= 12) {
                    return GLFW.GLFW_KEY_F1 + (fNum - 1);
                }
            } catch (NumberFormatException ignored) {}
        }
        
        if (key.length() == 1) {
            char c = key.charAt(0);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                return InputUtil.fromTranslationKey("key.keyboard." + Character.toLowerCase(c)).getCode();
            }
        }
        
        return -1;
    }

    private static void saveKeybind() {
        try {
            Files.writeString(CONFIG_FILE, currentKeybind);
        } catch (Exception e) {
            System.err.println("Failed to save keybind: " + e.getMessage());
        }
    }

    private static void loadKeybind() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                String savedKey = Files.readString(CONFIG_FILE).trim();
                if (isValidKey(savedKey)) {
                    currentKeybind = savedKey;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load keybind: " + e.getMessage());
        }
    }

    private static boolean isValidKey(String key) {
        return key != null && key.matches("^[A-Z0-9]$|^F[1-9]$|^F1[0-2]$");
    }

    public static String getCurrentBind() {
        return currentKeybind;
    }
}