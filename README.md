# DynamicTooltipsLib — Example Plugin

> A complete example plugin demonstrating how to use the **DynamicTooltipsLib** API to add dynamic, per-item tooltips in Hytale.

This plugin registers **four chat commands** that each showcase a different tooltip capability:

| Command | API Feature | Description |
|:---|:---|:---|
| `/rename <name>` | Name override | Changes the item's display name |
| `/addTooltip <text>` | Additive lines | Appends a new line to the tooltip |
| `/replaceTooltip <text>` | Description override | Replaces the entire tooltip description |
| `/removeTooltip <type>` | Metadata removal | Removes tooltip data (`rename`, `lines`, `desc`, or `all`) |
| `/morph` | Visual Overrides | Toggles the item's appearance to an Adamantite Longsword |

---

## Setup

### 1. Gradle Dependency

Add DynamicTooltipsLib as `compileOnly` in your `build.gradle`:

```gradle
dependencies {
    compileOnly files('lib/DynamicTooltipsLib-1.0.0.jar')
}
```

### 2. Manifest

Declare the library as a dependency in `manifest.json`:

```json
{
    "Name": "TooltipExample",
    "Main": "org.herolias.plugin.TooltipExample",
    "Dependencies": {
        "org.herolias:DynamicTooltipsLib": "1.5.0"
    }
}
```

---

## How It Works

The plugin uses **three `TooltipProvider` implementations** to handle all features. Each provider reads custom data from item metadata and returns the appropriate `TooltipData`.

### Plugin Setup

```java
public class TooltipExample extends JavaPlugin {

    private DynamicTooltipsApi tooltipsApi;

    @Override
    protected void setup() {
        tooltipsApi = DynamicTooltipsApiProvider.get();
        if (tooltipsApi == null) {
            LOGGER.atSevere().log("DynamicTooltipsLib API not available!");
            return;
        }

        // Register our tooltip providers
        tooltipsApi.registerProvider(new RenameTooltipProvider());
        tooltipsApi.registerProvider(new CustomTooltipProvider());
        tooltipsApi.registerProvider(new MorphTooltipProvider());
    }

    @Override
    protected void start() {
        // ... Register commands ...
        this.getCommandRegistry().registerCommand(new MorphCommand(this));
    }
}
```

---

### Feature 1: Name Override (`/rename`)

The `/rename` command writes a `rename` key into the held item's metadata, then calls `refreshPlayer()` to update the tooltip immediately.

**Command handler:**
```java
// ... (omitted for brevity)
```

**Provider — `RenameTooltipProvider`:**
```java
public class RenameTooltipProvider implements TooltipProvider {

    @Override
    public String getProviderId() {
        return "tooltip-example:rename";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT;
    }

    @Override
    public TooltipData getTooltipData(String itemId, String metadata) {
        String customName = TooltipExample.getMetadataKey(metadata, "rename");
        if (customName == null) return null;

        return TooltipData.builder()
                .nameOverride(customName)           // Replaces the display name
                .hashInput("rename:" + customName)   // Unique cache key
                .build();
    }
}
```

> **Key concept:** `nameOverride()` is **destructive** — the highest-priority provider's name wins. Use it when you want to completely replace what the item is called.

---

### Feature 2: Additive Lines (`/addTooltip`)

The `/addTooltip` command appends lines to the item's tooltip. Multiple lines are stored as pipe-separated values in the `tooltip_lines` metadata key.

**Command handler:**
```java
// ... (omitted for brevity)
```

**Provider — `CustomTooltipProvider` (additive mode):**
```java
// Inside getTooltipData():
String[] lines = tooltipLines.split("\\|");
for (String line : lines) {
    builder.addLine("<color is=\"#55FF55\">\u2022 " + line + "</color>");
}
builder.hashInput("lines:" + tooltipLines);
```

> **Key concept:** `addLine()` is **additive** — lines from all providers are composed in priority order. Multiple mods can safely add their own lines without conflict.

---

### Feature 3: Description Override (`/replaceTooltip`)

The `/replaceTooltip` command replaces the entire tooltip description, including any additive lines from other providers.

**Command handler:**
```java
// ... (omitted for brevity)
```

**Provider — `CustomTooltipProvider` (override mode):**
```java
// Inside getTooltipData():
builder.descriptionOverride("<color is=\"#AAAAFF\">" + tooltipDesc + "</color>");
builder.hashInput("desc:" + tooltipDesc);
```

> **Key concept:** `descriptionOverride()` is **destructive** — the highest-priority provider wins, and *all* additive lines from every provider are discarded.

---

### Feature 4: Visual Overrides (`/morph`)

The `/morph` command toggles a special `morph_visuals` metadata key. When present, the `MorphTooltipProvider` instructs the client to render the item as an Adamantite Longsword, even if it's logically a wooden stick!

**Provider — `MorphTooltipProvider`:**
```java
public class MorphTooltipProvider implements TooltipProvider {
    @Override
    public TooltipData getTooltipData(String itemId, String metadata) {
        if (!metadata.contains("morph_visuals")) return null;

        return TooltipData.builder()
            .hashInput("morph:adamantite") // REQUIRED: Changing visuals MUST change the hash
            .addLine("<color is='#FF55FF'>Morphed: Adamantite Longsword</color>")
            .visualOverrides(ItemVisualOverrides.builder()
                .model("Items/Weapons/Longsword/Adamantite.blockymodel")
                .texture("Items/Weapons/Longsword/Adamantite_Texture.png")
                .icon("Icons/ItemsGenerated/Weapon_Longsword_Adamantite.png") 
                //you can add more visual overrides
                .build())
            .build();
    }
}
```

> **Key concept:** `visualOverrides()` allows specific item instances to look completely different from their base type, without affecting server-side logic (damage, etc.).

---

### Feature 5: Remove Tooltip Data (`/removeTooltip`)

The `/removeTooltip` command selectively removes tooltip metadata from the held item.

**Subcommands:**

| Subcommand | Effect |
|:---|:---|
| `/removeTooltip rename` | Removes the custom name |
| `/removeTooltip lines` | Removes all additive tooltip lines |
| `/removeTooltip desc` | Removes the description override |
| `/removeTooltip all` | Removes all tooltip data |

---

## Metadata Format

This example uses a simple `key=value;` format for item metadata:

```
rename=My Sword;tooltip_lines=Line 1|Line 2;
```

The plugin includes helper methods (`getMetadataKey`, `setMetadataKey`, `removeMetadataKey`) that handle parsing and updating this format. In a real mod, you would likely use JSON or a more robust serialization.

---

## API Quick Reference

| Method | Type | Description |
|:---|:---|:---|
| `TooltipData.builder().addLine(text)` | Additive | Appends a line after the original description |
| `TooltipData.builder().nameOverride(name)` | Destructive | Replaces the item's display name |
| `TooltipData.builder().descriptionOverride(text)` | Destructive | Replaces the entire description |
| `TooltipData.builder().hashInput(key)` | Required | Stable string for cache identity |
| `api.refreshPlayer(uuid)` | Utility | Invalidates cache & sends immediate update |
| `api.refreshAllPlayers()` | Utility | Refreshes all online players |

---

## License

MIT License. Free to use as a reference for your own Hytale mods.
