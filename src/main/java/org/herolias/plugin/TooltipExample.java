package org.herolias.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import org.herolias.tooltips.api.DynamicTooltipsApi;
import org.herolias.tooltips.api.DynamicTooltipsApiProvider;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Example plugin that demonstrates how to use the DynamicTooltipsLib API.
 * <p>
 * Registers three chat commands that each showcase a different tooltip feature:
 * <ul>
 *   <li>{@code /rename <name>} — overrides the item's display name</li>
 *   <li>{@code /addTooltip <text>} — appends a line to the item's tooltip</li>
 *   <li>{@code /replaceTooltip <text>} — replaces the entire tooltip description</li>
 *   <li>{@code /addGlobalLine <text>} — appends a line to the item type globally</li>
 *   <li>{@code /replaceGlobalTooltip <text>} — replaces the tooltip of the item type globally</li>
 * </ul>
 */
public class TooltipExample extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private DynamicTooltipsApi tooltipsApi;
    private SharedItemList sharedItemList;

    public TooltipExample(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public DynamicTooltipsApi getTooltipsApi() {
        return tooltipsApi;
    }

    public SharedItemList getSharedItemList() {
        return sharedItemList;
    }

    @Override
    protected void setup() {
        // Initialize the shared item list (persisted to disk)
        Path dataDir = this.getDataDirectory();
        sharedItemList = SharedItemList.load(dataDir);

        // Obtain the DynamicTooltipsLib API
        tooltipsApi = DynamicTooltipsApiProvider.get();
        if (tooltipsApi == null) {
            LOGGER.atSevere().log("DynamicTooltipsLib API not available! Is the library installed?");
            return;
        }

        // Register our tooltip providers
        tooltipsApi.registerProvider(new RenameTooltipProvider());
        tooltipsApi.registerProvider(new CustomTooltipProvider());
        tooltipsApi.registerProvider(new MorphTooltipProvider());
        // Register new providers
        tooltipsApi.registerProvider(new RarityTooltipProvider());
        tooltipsApi.registerProvider(new AnimationTooltipProvider());
        tooltipsApi.registerProvider(new ArmorOverrideTooltipProvider());

        LOGGER.atInfo().log("TooltipExample: Registered tooltip providers");
    }

    @Override
    protected void start() {
        if (tooltipsApi == null) return;
        tooltipsApi.addGlobalLine("Tool_Pickaxe_Adamantite", "Upgrade Level 1");
        tooltipsApi.addGlobalLine("Ingredient_Poop", "Upgrade Level 2");
        tooltipsApi.addGlobalTranslationLine("Ingredient_Poop", "server.tooltipexample.global.deletethis");
      

        // Register chat commands
        this.getCommandRegistry().registerCommand(new RenameCommand(this));
        this.getCommandRegistry().registerCommand(new AddTooltipCommand(this));
        this.getCommandRegistry().registerCommand(new ReplaceTooltipCommand(this));
        this.getCommandRegistry().registerCommand(new RemoveTooltipCommand(this));
        this.getCommandRegistry().registerCommand(new MorphCommand(this));
        // Register new commands
        this.getCommandRegistry().registerCommand(new SetRarityCommand(this));
        this.getCommandRegistry().registerCommand(new ChangeAnimationCommand(this));
        this.getCommandRegistry().registerCommand(new ToggleAnimationCommand(this));
        this.getCommandRegistry().registerCommand(new SetArmorCommand(this));
        // Register global APIs commands
        this.getCommandRegistry().registerCommand(new AddGlobalLineCommand(this));
        this.getCommandRegistry().registerCommand(new ReplaceGlobalTooltipCommand(this));
        // Register UI commands
        this.getCommandRegistry().registerCommand(new TestUICommand(this));
        this.getCommandRegistry().registerCommand(new OpenListCommand(this));
        this.getCommandRegistry().registerCommand(new TestMetadataCommand(this));

        LOGGER.atInfo().log("TooltipExample: Registered commands (including /testUI, /openList)");
    }
}
