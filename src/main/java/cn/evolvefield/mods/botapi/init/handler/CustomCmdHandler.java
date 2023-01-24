package cn.evolvefield.mods.botapi.init.handler;

import cn.evolvefield.mods.botapi.Static;
import cn.evolvefield.mods.botapi.api.cmd.CustomCmd;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/2 13:25
 * Version: 1.0
 */
public class CustomCmdHandler {

    public static final CustomCmdHandler INSTANCE = new CustomCmdHandler();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    private static final File dir = FabricLoader.getInstance().getConfigDir().resolve("botapi/custom_cmd/").toFile();

    private final Map<String, CustomCmd> customCmdMap = new LinkedHashMap<>();

    public List<CustomCmd> getCustomCmds() {
        return Lists.newArrayList(this.customCmdMap.values());
    }

    public CustomCmd getCmdByAlies(String alies) {
        return this.customCmdMap.get(alies);
    }

    public Map<String, CustomCmd> getCustomCmdMap() {
        return customCmdMap;
    }

    public void load() {
        var stopwatch = Stopwatch.createStarted();

        this.writeDefault();

        clear();

        if (!dir.mkdirs() && dir.isDirectory()) {
            this.loadFiles();
        }

        stopwatch.stop();

        Static.LOGGER.info("Loaded {} custom cmd(s) in {} ms", this.customCmdMap.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void writeDefault() {
        if (!dir.exists() && dir.mkdirs()) {
            var json = new JsonObject();
            json.addProperty("alies", "list");
            json.addProperty("content", "list");
            json.addProperty("role", 0);
            json.addProperty("enable", true);

            var json2 = new JsonObject();
            json.addProperty("alies", "say");
            json.addProperty("content", "say %");
            json.addProperty("role", 1);
            json.addProperty("enable", true);

            FileWriter writer = null;
            FileWriter writer2 = null;

            try {
                var file = new File(dir, "list.json");
                var file2 = new File(dir, "say.json");
                writer = new FileWriter(file);
                writer2 = new FileWriter(file2);

                GSON.toJson(json, writer);
                GSON.toJson(json2, writer2);

                writer.close();
                writer2.close();
            } catch (Exception e) {
                Static.LOGGER.error("An error occurred while generating default custom cmd", e);
            } finally {
                IOUtils.closeQuietly(writer);
                IOUtils.closeQuietly(writer2);
            }

        }
    }

    private void loadFiles() {
        var files = CustomCmdHandler.dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            CustomCmd customCmd = null;

            try {
                var parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                var name = file.getName().replace(".json", "");
                json = JsonParser.parseReader(reader).getAsJsonObject();

                customCmd = CustomCmd.loadFromJson(new ResourceLocation(Static.MODID, name), json);

                reader.close();
            } catch (Exception e) {
                Static.LOGGER.error("An error occurred while loading custom cmd", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (customCmd != null && customCmd.isEnabled()) {
                String alies = customCmd.getCmdAlies();
                if (customCmd.getCmdContent().contains("%"))
                    alies = customCmd.getCmdContent().split("%")[0].stripTrailing();
                Static.LOGGER.debug(alies);
                this.customCmdMap.put(alies, customCmd);


            }
        }
    }

    public void clear() {
        this.customCmdMap.clear();
    }
}
