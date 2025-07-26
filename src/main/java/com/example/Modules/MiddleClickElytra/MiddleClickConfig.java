package com.example.Modules.MiddleClickElytra;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.fabricmc.api.EnvType;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Environment(EnvType.CLIENT)
public class MiddleClickConfig implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("middleclickmod");
    public static boolean modEnabled = true;
    @Override
    public void onInitializeClient() {
        ModConfig.loadConfig();
        registerCommand();
    }

    private void registerCommand() {
        // Загружаем сохранённое состояние
        ModConfig.loadConfig();
        modEnabled = ModConfig.isEnabled();
        
        // Регистрируем команду
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("middleclick")
                .then(CommandManager.literal("on").executes(context -> {
                    modEnabled = true;
                    ModConfig.saveConfig(true);
                    context.getSource().sendFeedback(() -> Text.literal("MiddleClick mod enabled"), false);
                    return 1;
                }))
                .then(CommandManager.literal("off").executes(context -> {
                    modEnabled = false;
                    ModConfig.saveConfig(false);
                    context.getSource().sendFeedback(() -> Text.literal("MiddleClick mod disabled"), false);
                    return 1;
                })));  // Добавлена лишняя закрывающая скобка
        });
        
        LOGGER.info("MiddleClick Mod initialized");
    }

    public class ModConfig {
        private static final File CONFIG_FILE = new File("config/middleclickmod.json");
        private static boolean enabled = true;
        
        public static boolean isEnabled() {
            return enabled;
        }
        
        public static void loadConfig() {
            if (!CONFIG_FILE.exists()) {
                saveConfig(true);
                return;
            }
            
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = gson.fromJson(reader, ConfigData.class);
                if (data != null) {
                    enabled = data.enabled;
                }
            } catch (IOException e) {
                MiddleClickConfig.LOGGER.error("Failed to load config", e);
            }
        }
        
        public static void saveConfig(boolean isEnabled) {
            enabled = isEnabled;
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ConfigData data = new ConfigData();
            data.enabled = isEnabled;
            
            try {
                if (!CONFIG_FILE.getParentFile().exists()) {
                    CONFIG_FILE.getParentFile().mkdirs();
                }
                
                try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                    gson.toJson(data, writer);
                }
            } catch (IOException e) {
                MiddleClickConfig.LOGGER.error("Failed to save config", e);
            }
        }
        
        private static class ConfigData {
            boolean enabled = true;
        }
    }
}