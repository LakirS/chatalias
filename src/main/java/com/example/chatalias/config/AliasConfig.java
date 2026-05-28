package com.example.chatalias.config;

import com.example.chatalias.ChatAliasMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class AliasConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("chatalias.json");

    private static AliasConfig INSTANCE = new AliasConfig();

    public boolean enabled = true;
    public boolean applyToCommands = true;
    public Map<String, String> aliases = new LinkedHashMap<>();

    public AliasConfig() {
        // Дефолтный набор — основан на правилах сервера, которые ты прислал
        aliases.put("!r",     "спам");
        aliases.put("!f",     "флуд");
        aliases.put("!fs",    "флуд символами");
        aliases.put("!caps",  "капс");
        aliases.put("!beg",   "попрошайничество");
        aliases.put("!mat",   "мат");
        aliases.put("!tox",   "неадекватное поведение");
        aliases.put("!ins",   "оскорбление игрока");
        aliases.put("!insf",  "оскорбление родных");
        aliases.put("!insa",  "оскорбление администрации");
        aliases.put("!troll", "троллинг");
        aliases.put("!conf",  "организация конфликта");
        aliases.put("!prop",  "пропаганда запрещённого контента");
        aliases.put("!ref",   "ссылка на сторонний ресурс");
        aliases.put("!fake",  "выдача себя за персонал");
        aliases.put("!bug",   "багюз");
    }

    public static AliasConfig get() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                AliasConfig loaded = GSON.fromJson(json, AliasConfig.class);
                if (loaded != null) {
                    if (loaded.aliases == null) loaded.aliases = new LinkedHashMap<>();
                    INSTANCE = loaded;
                }
            } else {
                save();
            }
        } catch (Exception e) {
            ChatAliasMod.LOGGER.error("Failed to load chatalias config", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            ChatAliasMod.LOGGER.error("Failed to save chatalias config", e);
        }
    }

    public static void replaceInstance(AliasConfig cfg) {
        INSTANCE = cfg;
        save();
    }
}
