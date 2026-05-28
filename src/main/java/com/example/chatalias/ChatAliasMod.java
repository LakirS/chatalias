package com.example.chatalias;

import com.example.chatalias.config.AliasConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ChatAliasMod implements ClientModInitializer {
    public static final String MOD_ID = "chatalias";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        AliasConfig.load();

        // Замена в обычных сообщениях (текст без слэша).
        ClientSendMessageEvents.MODIFY_CHAT.register(message -> {
            if (!AliasConfig.get().enabled) return message;
            return applyAliases(message);
        });

        // Замена внутри команд: /msg Player !r -> /msg Player спам
        ClientSendMessageEvents.MODIFY_COMMAND.register(command -> {
            AliasConfig cfg = AliasConfig.get();
            if (!cfg.enabled || !cfg.applyToCommands) return command;
            return applyAliases(command);
        });

        LOGGER.info("ChatAlias loaded with {} aliases", AliasConfig.get().aliases.size());
    }

    /**
     * Заменяет все вхождения алиасов в строке.
     * Алиасы сортируются по длине (по убыванию), чтобы "!warn" не пересекался с "!w".
     */
    public static String applyAliases(String input) {
        Map<String, String> aliases = AliasConfig.get().aliases;
        if (aliases.isEmpty()) return input;

        // Сортируем ключи по длине, длинные сначала
        List<String> keys = aliases.keySet().stream()
                .filter(k -> k != null && !k.isEmpty())
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .toList();

        String result = input;
        for (String key : keys) {
            String value = aliases.get(key);
            if (value == null) continue;
            // Простая подстрочная замена. Никакого regex, чтобы спецсимволы
            // вроде "!" в ключах не требовали экранирования.
            result = result.replace(key, value);
        }
        return result;
    }
}
