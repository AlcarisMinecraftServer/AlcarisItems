# Alcarisitems構造変更
_Exported on 2026/3/10 at 21:38:49 JST from Cursor (2.7.0-pre.43.patch.0)_

---

**User**

現在alcarisitems (依存プラグイン)で @cursor_score_tags_addition_in_materiali.md  を実装したのですが、構造を変更してください。

---

**Cursor**

I'll quickly read the current `cursor_score_tags_addition_in_materiali.md` to understand its structure before proposing a restructuring plan.

**構造変更の意図を確認**

> **「構造を変更」はどちらを指しますか？**
> - このドキュメント（cursor_score_tags_addition_in_materiali.md）の構成・見出し構造を変える
> - alcarisitems 側の実装のモジュール構成・クラス分割を変える
> - 両方
>
> Answer: item.getDataにjsonを対応させる

> **ドキュメントを変える場合、どの形式に整えますか？**
> - 仕様書スタイル（概要/要件/データモデル/Lore仕様/PDC/外部API/ファイル影響）
> - alcarisitems の機能README（使い方中心、APIと例）」},{
> - Changelog/実装サマリー（変更点・影響範囲・移行ガイド）
> - 会話ログは保持しつつ要点サマリーを先頭に追加
>
> Answer: ドキュメントは変更しない(会話ログのため)

I'll locate where `item.getData()` is defined and how item data is modeled to design JSON support. I'll search for the `items` module in this repo, find `getData` definitions/usages, and map the current data flow.

**getDataのAPI方針**

> **`getData()`はどの形でJSON対応しますか？**
> - `getData()`の戻り値を`JsonElement`に変更（破壊的変更でシンプル）
> - `getData()`は現状維持、`getDataJson()`（または`getDataAsJsonObject()`）を追加 ✔
> - API変更せず、呼び出し側は`getTypedData(MaterialScoreData.class)`を使う

I verified the core model location for `item.getData()` and confirmed the `items` module code isn't in this repo. I'll propose a minimal, non-breaking change in `ItemBaseModel` to expose JSON via new getters and optionally add a typed material model mapping. Now I'll draft the implementation plan.

---

**User**

Implement the plan as specified, it is attached for your reference. Do NOT edit the plan file itself.

To-do's from the plan have already been created. Do not create them again. Mark them as in_progress as you work, starting with the first one. Don't stop until you have completed all the to-dos.

---

**Cursor**

Starting with `ItemBaseModel.java`: I'll add `getDataJson()` and `getDataAsJsonObject()`, import `JsonObject` if needed, and keep `getData()` unchanged. Then I'll run a linter check for this file.

Marked the first task in progress and added `getDataJson()` / `getDataAsJsonObject()` to `ItemBaseModel`, then verified no linter issues. Next, I implemented `ItemMaterialModel` and mapped `MATERIAL` in `getAutoTypedData`, added brief usage Javadocs, and checked lints again.

Code references (key additions)

```60:89:/Users/somen1000/Desktop/AlcarisCore/src/main/java/net/alcaris/plugin/core/model/item/ItemBaseModel.java
/**
 * Returns the item-specific {@code data} normalized to a Gson {@link JsonElement}.
 * ...
 */
public JsonElement getDataJson() {
    if (data == null) return null;
    if (data instanceof JsonElement json) return json;
    return GSON.toJsonTree(data);
}

/**
 * Convenience accessor to view {@code data} as a {@link JsonObject}.
 * Returns {@code null} when {@code data} is absent or not an object.
 */
public JsonObject getDataAsJsonObject() {
    JsonElement el = getDataJson();
    return (el != null && el.isJsonObject()) ? el.getAsJsonObject() : null;
}
```

```107:114:/Users/somen1000/Desktop/AlcarisCore/src/main/java/net/alcaris/plugin/core/model/item/ItemBaseModel.java
return switch (category) {
    case FOOD -> getTypedData(ItemFoodModel.class);
    case TOOL -> getTypedData(ItemToolModel.class);
    case WEAPON -> getTypedData(ItemWeaponModel.class);
    case ARMOR -> getTypedData(ItemArmorModel.class);
    case MATERIAL -> getTypedData(ItemMaterialModel.class);
    default -> data;
};
```

```5:16:/Users/somen1000/Desktop/AlcarisCore/src/main/java/net/alcaris/plugin/core/model/item/ItemMaterialModel.java
/**
 * Typed view of MATERIAL item data.
 * ...
 * Maps 13 color score integers: red, orange, yellow, lime, green, cyan,
 * aqua, blue, pink, purple, black, gray, white.
 */
```

All to-dos are completed and reconciled.

- Added `getDataJson()` and `getDataAsJsonObject()` to `ItemBaseModel` (non-breaking).
- Introduced `ItemMaterialModel` with 13 color score fields and getters.
- Updated `getAutoTypedData()` to support `MATERIAL`.
- Included concise Javadocs showing recommended usage for alcarisitems.
- Lints: no issues on updated/added files.

