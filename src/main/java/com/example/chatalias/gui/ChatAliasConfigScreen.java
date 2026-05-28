package com.example.chatalias.gui;

import com.example.chatalias.config.AliasConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatAliasConfigScreen {

    public static Screen create(Screen parent) {
        AliasConfig cfg = AliasConfig.get();

        // Преобразуем Map в список строк "ключ = значение" для редактирования
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> e : cfg.aliases.entrySet()) {
            entries.add(e.getKey() + " = " + e.getValue());
        }

        Option<Boolean> enabledOption = Option.<Boolean>createBuilder()
                .name(Text.literal("Включить замену алиасов"))
                .description(OptionDescription.of(Text.literal(
                        "Если выключено — ваши сообщения отправляются без изменений.")))
                .binding(true, () -> cfg.enabled, v -> cfg.enabled = v)
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Boolean> applyToCommandsOption = Option.<Boolean>createBuilder()
                .name(Text.literal("Применять замену и в командах"))
                .description(OptionDescription.of(Text.literal(
                        "Если включено — алиасы заменяются и в сообщениях, начинающихся со " +
                        "слэша (/mute Player !r → /mute Player спам). Если выключено — " +
                        "только в обычном чате.")))
                .binding(true, () -> cfg.applyToCommands, v -> cfg.applyToCommands = v)
                .controller(TickBoxControllerBuilder::create)
                .build();

        ListOption<String> aliasList = ListOption.<String>createBuilder()
                .name(Text.literal("Алиасы"))
                .description(OptionDescription.of(Text.literal(
                        "Каждая строка в формате:  ключ = значение\n" +
                        "Пример:  !r = спам\n" +
                        "При вводе в чат \"Вы замучены за !r\" отправится " +
                        "\"Вы замучены за спам\".\n\n" +
                        "Длинные ключи имеют приоритет над короткими, поэтому " +
                        "!warn и !w могут сосуществовать.")))
                .binding(
                        new ArrayList<>(),
                        () -> entries,
                        newList -> {
                            entries.clear();
                            entries.addAll(newList);
                        })
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("ChatAlias"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Основные"))
                        .option(enabledOption)
                        .option(applyToCommandsOption)
                        .group(aliasList)
                        .build())
                .save(() -> applyChanges(entries))
                .build()
                .generateScreen(parent);
    }

    /**
     * Парсим строки вида "ключ = значение" обратно в Map и сохраняем.
     * Пустые строки и строки без '=' просто игнорируются (не ломают конфиг).
     */
    private static void applyChanges(List<String> entries) {
        AliasConfig cfg = AliasConfig.get();
        Map<String, String> parsed = new LinkedHashMap<>();
        for (String raw : entries) {
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty()) continue;
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (key.isEmpty()) continue;
            parsed.put(key, value);
        }
        cfg.aliases = parsed;
        AliasConfig.save();
    }
}
