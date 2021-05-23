package pronze.hypixelify.lib.lang;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import pronze.hypixelify.api.lang.ILanguageService;
import pronze.hypixelify.api.lang.Message;
import pronze.hypixelify.config.SBAConfig;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@Getter
public class LanguageService implements ILanguageService {
    private static final List<String> validLocale = List.of(
            "af", "ar", "ca", "cs", "da", "de", "el", "en", "es", "fi", "fr", "he", "hu",
            "it", "ja", "ko", "nl", "no", "pl", "pt", "pt-BR", "ro", "ru", "sr", "sv", "tr",
            "uk", "vi", "zh", "zh-CN"
    );

    public static LanguageService getInstance() {
        return ServiceManager.get(LanguageService.class);
    }

    public LanguageService(JavaPlugin plugin) {
        locale = SBAConfig.getInstance().node("locale").getString("en");
        if (!validLocale.contains(locale.toLowerCase())) {
            throw new UnsupportedOperationException("Invalid locale provided!");
        }

        try {
            var pathStr = plugin.getDataFolder().getAbsolutePath();
            pathStr = pathStr + "/languages/language_" + locale + ".yml";

            var loader = YamlConfigurationLoader
                    .builder()
                    .path(Paths.get(pathStr))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();
            configurationNode = loader.load();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("There was an error loading language file!");
            ex.printStackTrace();
        }

        try {
            var pathStr = plugin.getDataFolder().getAbsolutePath();
            pathStr = pathStr + "/languages/language_fallback.yml";

            var loader = YamlConfigurationLoader
                    .builder()
                    .path(Paths.get(pathStr))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();
            fallbackNode = loader.load();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("There was an error loading fallback language!");
            ex.printStackTrace();
        }
    }

    private String locale;
    private ConfigurationNode configurationNode;
    private ConfigurationNode fallbackNode;

    @Override
    public Message get(String... arguments) {
        return get(false, arguments);
    }

    public Message get(boolean fallback, String... arguments) {
        ConfigurationNode argumentNode = fallback ? fallbackNode.node((Object[]) arguments) :
                configurationNode.node((Object[]) arguments);

        try {
            if (argumentNode == null || argumentNode.empty()) {
                throw new UnsupportedOperationException("Could not find key for: " + Arrays.toString(arguments));
            }
            if (argumentNode.isList()) {
                return Message.of(argumentNode.getList(String.class));
            } else {
                return Message.of(List.of(argumentNode.getString()));
            }
        } catch (SerializationException | UnsupportedOperationException e) {
            if (!fallback)
                return get(true, arguments);
            e.printStackTrace();
        }
        return Message.of(List.of("TRANSLATION FOR: " + Arrays.toString(arguments) + " NOT FOUND!"));
    }
}
