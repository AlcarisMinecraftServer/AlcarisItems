package net.alcaris.plugin.items.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.kyori.adventure.key.Key;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public final class ItemDataComponents {
    private static final Gson GSON = AlcarisItems.getGson();

    private ItemDataComponents() {
    }

    /**
     * Backward compatible: apply legacy integer custom model data.
     * <p>
     * New Natsume schema uses the structured {@code custom_model_data} component.
     * This method only handles the old integer getter.
     */
    public static void applyLegacyCustomModelData(ItemMeta meta, ItemBaseModel item) {
        Object raw = readValue(item, "getCustomModelData", "custom_model_data", "customModelData");
        if (!(raw instanceof Number n)) {
            return;
        }

        int legacy = n.intValue();
        if (legacy != 0) {
            meta.setCustomModelData(legacy);
        }
    }

    /**
     * Apply new item data components (item_model / tooltip_style / custom_model_data) if present.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void apply(ItemStack stack, ItemBaseModel item) {
        if (stack == null || item == null) {
            return;
        }

        String itemModel = readString(item, "getItemModel", "item_model", "itemModel");
        if (itemModel != null) {
            try {
                stack.setData(DataComponentTypes.ITEM_MODEL, Key.key(itemModel));
            } catch (IllegalArgumentException e) {
                log(Level.WARNING, "Invalid item_model key: " + itemModel, e);
            }
        }

        String tooltipStyle = readString(item, "getTooltipStyle", "tooltip_style", "tooltipStyle");
        if (tooltipStyle != null) {
            try {
                stack.setData(DataComponentTypes.TOOLTIP_STYLE, Key.key(tooltipStyle));
            } catch (IllegalArgumentException e) {
                log(Level.WARNING, "Invalid tooltip_style key: " + tooltipStyle, e);
            }
        }

        Object cmdRaw = readValue(item, "getCustomModelData", "custom_model_data", "customModelData");
        CustomModelData cmd = parseCustomModelData(cmdRaw);
        if (cmd != null) {
            stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, cmd);
        }
    }

    @Nullable
    private static CustomModelData parseCustomModelData(Object raw) {
        if (raw == null) {
            return null;
        }

        if (raw instanceof Optional<?> opt) {
            raw = opt.orElse(null);
            if (raw == null) {
                return null;
            }
        }

        // Legacy schema: integer custom model data.
        if (raw instanceof Number) {
            return null;
        }

        if (raw instanceof CustomModelData cmd) {
            return cmd;
        }

        if (raw instanceof JsonElement jsonElement) {
            raw = GSON.fromJson(jsonElement, Object.class);
        }

        Map<?, ?> map = null;
        if (raw instanceof Map<?, ?> m) {
            map = m;
        } else {
            try {
                Object asObj = GSON.fromJson(GSON.toJsonTree(raw), Object.class);
                if (asObj instanceof Map<?, ?> m) {
                    map = m;
                }
            } catch (Exception e) {
                log(Level.WARNING, "Failed to deserialize custom_model_data", e);
                return null;
            }
        }

        if (map == null) {
            return null;
        }

        Object typeObj = map.get("type");
        Object valueObj = map.get("value");
        if (!(typeObj instanceof String typeStr)) {
            return null;
        }

        String type = typeStr.toLowerCase(Locale.ROOT);
        CustomModelData.Builder builder = CustomModelData.customModelData();

        switch (type) {
            case "floats" -> {
                for (Object o : asList(valueObj)) {
                    if (o instanceof Number n) {
                        builder.addFloat(n.floatValue());
                    }
                }
                return builder.build();
            }
            case "flags" -> {
                for (Object o : asList(valueObj)) {
                    if (o instanceof Boolean b) {
                        builder.addFlag(b);
                    }
                }
                return builder.build();
            }
            case "strings" -> {
                for (Object o : asList(valueObj)) {
                    if (o instanceof String s) {
                        builder.addString(s);
                    } else if (o != null) {
                        builder.addString(o.toString());
                    }
                }
                return builder.build();
            }
            case "colors" -> {
                for (Object o : asList(valueObj)) {
                    Color c = parseColor(o);
                    if (c != null) {
                        builder.addColor(c);
                    }
                }
                return builder.build();
            }
            default -> {
                return null;
            }
        }
    }

    private static List<?> asList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }
        if (value.getClass().isArray()) {
            // best-effort; Gson should normally give List.
            return List.of((Object[]) value);
        }
        return List.of(value);
    }

    @Nullable
    private static Color parseColor(Object value) {
        if (value instanceof Number n) {
            int argb = n.intValue();
            // Treat values with alpha bits (or negative ints) as ARGB; otherwise RGB.
            if ((argb & 0xFF000000) != 0) {
                return Color.fromARGB(argb);
            }
            return Color.fromRGB(argb);
        }
        if (value instanceof String s) {
            String hex = s.trim();
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            try {
                int rgb = Integer.parseInt(hex, 16);
                if (hex.length() > 6) {
                    return Color.fromARGB(rgb);
                }
                return Color.fromRGB(rgb);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    private static String readString(Object target, String methodName, String fieldSnake, String fieldCamel) {
        Object v = readValue(target, methodName, fieldSnake, fieldCamel);
        if (v == null) {
            return null;
        }
        if (v instanceof Optional<?> opt) {
            v = opt.orElse(null);
            if (v == null) {
                return null;
            }
        }
        if (v instanceof Key k) {
            v = k.asString();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    @Nullable
    private static Object readValue(Object target, String methodName, String fieldSnake, String fieldCamel) {
        Object v = tryInvoke(target, methodName);
        if (v != null) {
            return v;
        }
        v = tryField(target, fieldSnake);
        if (v != null) {
            return v;
        }
        return tryField(target, fieldCamel);
    }

    @Nullable
    private static Object tryInvoke(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            return m.invoke(target);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Exception e) {
            log(Level.WARNING, "Failed to invoke " + methodName + " on " + target.getClass().getName(), e);
            return null;
        }
    }

    @Nullable
    private static Object tryField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (NoSuchFieldException ignored) {
            return null;
        } catch (Exception e) {
            log(Level.WARNING, "Failed to read field " + fieldName + " on " + target.getClass().getName(), e);
            return null;
        }
    }

    private static void log(Level level, String message, Throwable t) {
        AlcarisItems plugin = AlcarisItems.getInstance();
        if (plugin == null) {
            return;
        }
        if (t == null) {
            plugin.getLogger().log(level, message);
        } else {
            plugin.getLogger().log(level, message, t);
        }
    }
}
