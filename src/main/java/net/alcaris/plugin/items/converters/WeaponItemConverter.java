package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.enums.Colors;
import net.alcaris.plugin.items.models.data.WeaponDataModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WeaponItemConverter {

    private final AlcarisItems plugin;

    public WeaponItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
    }



    private Material getToolMaterial(String itemId) {
        String toolId = itemId.substring(itemId.lastIndexOf("_"));

        switch (toolId) {
            case "_bow" -> {
                return Material.BOW;
            }
        }

        return Material.WOODEN_SWORD;
    }

    @SuppressWarnings("all")
    public ItemStack createNewItem(ItemModel item, WeaponDataModel weapon, int amount) {


        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

        int damagePerformacnce = ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
        int attackRangePerformance = ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
        int attackSpeedPerformance = ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
        int walkSpeedPerformance = ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
        int xpBonusPerformance = ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
        int lootBonusPerformance = ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;

        if (itemMeta != null) {
            itemMeta.displayName(
                    plugin.miniMessage.deserialize(item.getName())
                            .color(TextColor.fromHexString(Colors.fromRarity(item.getRarity()).getHexCode()))
                            .decoration(TextDecoration.ITALIC, false)
            );
            itemMeta.setMaxStackSize(item.getMaxStack());

            if (item.getCustomModelData() != 0) {
                itemMeta.setCustomModelData(item.getCustomModelData());
            }

            List<Component> lore = new ArrayList<>();
            for (String text: item.getLore()) {
                lore.add(
                        Component.text(text)
                                .color(TextColor.fromHexString("#D8D8D8"))
                                .decoration(TextDecoration.ITALIC, false)
                );
            }

            lore.add(
                    Component.text("                          ")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.STRIKETHROUGH, true)
            );

            lore.add(
                    Component.text("必要レベル : ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(
                                    Component.text(weapon.getRequirement())
                                            .color(NamedTextColor.GREEN)
                                            .decoration(TextDecoration.ITALIC,false)
                            )
            );

            lore.add(
                    Component.text("研磨回数 : ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(
                                    Component.text(weapon.getPolishingCount())
                                            .color(NamedTextColor.GREEN)
                                            .decoration(TextDecoration.ITALIC,false)
                            )
            );

            double finalDamage = Math.floor(weapon.getDamage() * (0.5 * (1 + damagePerformacnce / 100.0)) * 10) / 10.0;
            double finalAttackRange = Math.floor(weapon.getAttackRange() * (0.5 * (1 + attackRangePerformance / 100.0)) * 100) / 100.0;
            double finalAttackSpeed = Math.floor(weapon.getAttackSpeed() * (0.5 * (1 + attackSpeedPerformance / 100.0)) * 100) / 100.0;

            int finalWalkSpeed = (int)(weapon.getWalkSpeed() * (0.5 * (1 + walkSpeedPerformance / 100.0)));
            int finalXpBonus = (int)(weapon.getXpBonus() * (0.5 * (1 + xpBonusPerformance / 100.0)));
            int finalLootBonus = (int)(weapon.getLootBonus() * (0.5 * (1 + lootBonusPerformance / 100.0)));

            if (weapon.getDamage() != 0) lore.add(buildStatLine("攻撃力", String.valueOf(finalDamage), String.valueOf(damagePerformacnce)));
            if (weapon.getAttackRange() != 0) lore.add(buildStatLine("攻撃距離", String.valueOf(finalAttackRange), String.valueOf(attackRangePerformance)));
            if (weapon.getAttackSpeed() != 0) lore.add(buildStatLine("攻撃速度", String.valueOf(finalAttackSpeed), String.valueOf(attackSpeedPerformance)));
            if (weapon.getWalkSpeed() != 0) lore.add(buildStatLine("移動速度", String.valueOf(finalWalkSpeed), String.valueOf(walkSpeedPerformance)));
            if (weapon.getXpBonus() != 0) lore.add(buildStatLine("経験値ボーナス", String.valueOf(finalXpBonus), String.valueOf(xpBonusPerformance)));
            if (weapon.getLootBonus() != 0) lore.add(buildStatLine("ドロップ率ボーナス", String.valueOf(finalLootBonus), String.valueOf(lootBonusPerformance)));


            itemMeta.lore(lore);


            NamespacedKey keyId = new NamespacedKey(plugin, "item_id");
            NamespacedKey keyVersion = new NamespacedKey(plugin, "item_version");
            NamespacedKey type_key = new NamespacedKey(plugin, "type");
            NamespacedKey requirement_key = new NamespacedKey(plugin, "requirement");
            NamespacedKey polishing_key = new NamespacedKey(plugin, "polishing_count");
            NamespacedKey damage_key = new NamespacedKey(plugin, "damage");
            NamespacedKey damage_perf_key = new NamespacedKey(plugin, "damage_perf");
            NamespacedKey walkspeed_key = new NamespacedKey(plugin, "walk_speed");
            NamespacedKey walkspeed_perf_key = new NamespacedKey(plugin, "walk_speed_perf");
            NamespacedKey attack_range_key = new NamespacedKey(plugin, "attack_range");
            NamespacedKey attack_range_perf_key = new NamespacedKey(plugin, "attack_range_perf");
            NamespacedKey attack_speed_key = new NamespacedKey(plugin, "attack_speed");
            NamespacedKey attack_speed_perf_key = new NamespacedKey(plugin, "attack_speed_perf");
            NamespacedKey xp_bonus_key = new NamespacedKey(plugin, "xp_bonus");
            NamespacedKey xp_bonus_perf_key = new NamespacedKey(plugin, "xp_bonus_perf");
            NamespacedKey loot_bonus_key = new NamespacedKey(plugin, "loot_bonus");
            NamespacedKey loot_bonus_perf_key = new NamespacedKey(plugin, "loot_bonus_perf");


            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(keyId, PersistentDataType.STRING, item.getId());
            container.set(keyVersion, PersistentDataType.INTEGER, item.getVersion());

            container.set(type_key, PersistentDataType.STRING, weapon.getType());
            container.set(requirement_key, PersistentDataType.INTEGER, weapon.getRequirement());
            container.set(polishing_key, PersistentDataType.INTEGER, weapon.getPolishingCount());
            container.set(damage_key, PersistentDataType.DOUBLE, finalDamage);
            container.set(damage_perf_key, PersistentDataType.INTEGER, damagePerformacnce);
            container.set(walkspeed_key, PersistentDataType.INTEGER, finalWalkSpeed);
            container.set(walkspeed_perf_key, PersistentDataType.INTEGER, walkSpeedPerformance);
            container.set(attack_range_key, PersistentDataType.DOUBLE, finalAttackRange);
            container.set(attack_range_perf_key, PersistentDataType.INTEGER, attackRangePerformance);
            container.set(attack_speed_key, PersistentDataType.DOUBLE, finalAttackSpeed);
            container.set(attack_speed_perf_key, PersistentDataType.INTEGER, attackSpeedPerformance);
            container.set(xp_bonus_key, PersistentDataType.INTEGER, finalXpBonus);
            container.set(xp_bonus_perf_key, PersistentDataType.INTEGER, xpBonusPerformance);
            container.set(loot_bonus_key, PersistentDataType.INTEGER, finalLootBonus);
            container.set(loot_bonus_perf_key, PersistentDataType.INTEGER, lootBonusPerformance);


            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public ItemStack updateItem(ItemModel item, WeaponDataModel weapon, int amount, ItemStack oldItem) {

        NamespacedKey keyId = new NamespacedKey(plugin, "item_id");
        NamespacedKey keyVersion = new NamespacedKey(plugin, "item_version");
        NamespacedKey type_key = new NamespacedKey(plugin, "type");
        NamespacedKey requirement_key = new NamespacedKey(plugin, "requirement");
        NamespacedKey polishing_key = new NamespacedKey(plugin, "polishing_count");
        NamespacedKey damage_key = new NamespacedKey(plugin, "damage");
        NamespacedKey damage_perf_key = new NamespacedKey(plugin, "damage_perf");
        NamespacedKey walkspeed_key = new NamespacedKey(plugin, "walk_speed");
        NamespacedKey walkspeed_perf_key = new NamespacedKey(plugin, "walk_speed_perf");
        NamespacedKey attack_range_key = new NamespacedKey(plugin, "attack_range");
        NamespacedKey attack_range_perf_key = new NamespacedKey(plugin, "attack_range_perf");
        NamespacedKey attack_speed_key = new NamespacedKey(plugin, "attack_speed");
        NamespacedKey attack_speed_perf_key = new NamespacedKey(plugin, "attack_speed_perf");
        NamespacedKey xp_bonus_key = new NamespacedKey(plugin, "xp_bonus");
        NamespacedKey xp_bonus_perf_key = new NamespacedKey(plugin, "xp_bonus_perf");
        NamespacedKey loot_bonus_key = new NamespacedKey(plugin, "loot_bonus");
        NamespacedKey loot_bonus_perf_key = new NamespacedKey(plugin, "loot_bonus_perf");

        ItemMeta meta = oldItem.getItemMeta();

        PersistentDataContainer oldItemContainer = meta.getPersistentDataContainer();

        int polishingCount = oldItemContainer.getOrDefault(polishing_key, PersistentDataType.INTEGER, 0);
        int damagePerformance = oldItemContainer.getOrDefault(damage_perf_key, PersistentDataType.INTEGER, 0);
        int walkSpeedPerformance = oldItemContainer.getOrDefault(walkspeed_perf_key, PersistentDataType.INTEGER, 0);
        int attackRangePerformance = oldItemContainer.getOrDefault(attack_range_perf_key, PersistentDataType.INTEGER, 0);
        int attackSpeedPerformance = oldItemContainer.getOrDefault(attack_speed_perf_key, PersistentDataType.INTEGER, 0);
        int xpBonusPerformance = oldItemContainer.getOrDefault(xp_bonus_perf_key, PersistentDataType.INTEGER, 0);
        int lootBonusPerformance = oldItemContainer.getOrDefault(loot_bonus_perf_key, PersistentDataType.INTEGER, 0);

        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.displayName(
                    plugin.miniMessage.deserialize(item.getName())
                            .color(TextColor.fromHexString(Colors.fromRarity(item.getRarity()).getHexCode()))
                            .decoration(TextDecoration.ITALIC, false)
            );
            itemMeta.setMaxStackSize(item.getMaxStack());

            if (item.getCustomModelData() != 0) {
                itemMeta.setCustomModelData(item.getCustomModelData());
            }

            List<Component> lore = new ArrayList<>();
            for (String text: item.getLore()) {
                lore.add(
                        Component.text(text)
                                .color(TextColor.fromHexString("#D8D8D8"))
                                .decoration(TextDecoration.ITALIC, false)
                );
            }

            lore.add(
                    Component.text("                          ")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.STRIKETHROUGH, true)
            );

            lore.add(
                    Component.text("必要レベル : ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(
                                    Component.text(weapon.getRequirement())
                                            .color(NamedTextColor.GREEN)
                                            .decoration(TextDecoration.ITALIC,false)
                            )
            );

            lore.add(
                    Component.text("研磨回数 : ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(
                                    Component.text(polishingCount)
                                            .color(NamedTextColor.GREEN)
                                            .decoration(TextDecoration.ITALIC,false)
                            )
            );

            double finalDamage = Math.floor(weapon.getDamage() * (0.5 * (1 + damagePerformance / 100.0)) * 10) / 10.0;
            double finalAttackRange = Math.floor(weapon.getAttackRange() * (0.5 * (1 + attackRangePerformance / 100.0)) * 100) / 100.0;
            double finalAttackSpeed = Math.floor(weapon.getAttackSpeed() * (0.5 * (1 + attackSpeedPerformance / 100.0)) * 100) / 100.0;

            int finalWalkSpeed = (int)(weapon.getWalkSpeed() * (0.5 * (1 + walkSpeedPerformance / 100.0)));
            int finalXpBonus = (int)(weapon.getXpBonus() * (0.5 * (1 + xpBonusPerformance / 100.0)));
            int finalLootBonus = (int)(weapon.getLootBonus() * (0.5 * (1 + lootBonusPerformance / 100.0)));

            if (weapon.getDamage() != 0) lore.add(buildStatLine("攻撃力", String.valueOf(finalDamage), String.valueOf(damagePerformance)));
            if (weapon.getAttackRange() != 0) lore.add(buildStatLine("攻撃距離", String.valueOf(finalAttackRange), String.valueOf(attackRangePerformance)));
            if (weapon.getAttackSpeed() != 0) lore.add(buildStatLine("攻撃速度", String.valueOf(finalAttackSpeed), String.valueOf(attackSpeedPerformance)));
            if (weapon.getWalkSpeed() != 0) lore.add(buildStatLine("移動速度", String.valueOf(finalWalkSpeed), String.valueOf(walkSpeedPerformance)));
            if (weapon.getXpBonus() != 0) lore.add(buildStatLine("経験値ボーナス", String.valueOf(finalXpBonus), String.valueOf(xpBonusPerformance)));
            if (weapon.getLootBonus() != 0) lore.add(buildStatLine("ドロップ率ボーナス", String.valueOf(finalLootBonus), String.valueOf(lootBonusPerformance)));


            itemMeta.lore(lore);;


            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(keyId, PersistentDataType.STRING, item.getId());
            container.set(keyVersion, PersistentDataType.INTEGER, item.getVersion());

            container.set(type_key, PersistentDataType.STRING, weapon.getType());
            container.set(requirement_key, PersistentDataType.INTEGER, weapon.getRequirement());
            container.set(polishing_key, PersistentDataType.INTEGER, weapon.getPolishingCount());
            container.set(damage_key, PersistentDataType.DOUBLE, finalDamage);
            container.set(damage_perf_key, PersistentDataType.INTEGER, damagePerformance);
            container.set(walkspeed_key, PersistentDataType.INTEGER, finalWalkSpeed);
            container.set(walkspeed_perf_key, PersistentDataType.INTEGER, walkSpeedPerformance);
            container.set(attack_range_key, PersistentDataType.DOUBLE, finalAttackRange);
            container.set(attack_range_perf_key, PersistentDataType.INTEGER, attackRangePerformance);
            container.set(attack_speed_key, PersistentDataType.DOUBLE, finalAttackSpeed);
            container.set(attack_speed_perf_key, PersistentDataType.INTEGER, attackSpeedPerformance);
            container.set(xp_bonus_key, PersistentDataType.INTEGER, finalXpBonus);
            container.set(xp_bonus_perf_key, PersistentDataType.INTEGER, xpBonusPerformance);
            container.set(loot_bonus_key, PersistentDataType.INTEGER, finalLootBonus);
            container.set(loot_bonus_perf_key, PersistentDataType.INTEGER, lootBonusPerformance);


            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    private Component buildStatLine(String label, String value, String percent) {
        return Component.text(label + " : ")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(value)
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC,false))
                .append(Component.text(" (")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false))
                .append(Component.text(percent)
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC,false))
                .append(Component.text("%")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false))
                .append(Component.text(")")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
    }
}