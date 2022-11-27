package io._3650.jeid.jei;

import java.nio.file.NotDirectoryException;

import io._3650.jeid.JEID;
import io._3650.jeid.config.Config;
import io._3650.jeid.config.DescriptionConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIDPlugin implements IModPlugin {
	
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(JEID.MOD_ID, "descriptions");
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		try {
			Config.reload().getConfigs().stream().map(DescriptionConfig::getData).forEach(config -> {
				for (var entry : config.getItems().entries()) {
					registration.addIngredientInfo(entry.getKey(), VanillaTypes.ITEM_STACK, entry.getValue().toArray(Component[]::new));
				}
				for (var entry : config.getFluids().entries()) {
					registration.addIngredientInfo(entry.getKey(), ForgeTypes.FLUID_STACK, entry.getValue().toArray(Component[]::new));
				}
			});
		} catch (NotDirectoryException e) {
			JEID.LOGGER.error("File is not a directory: ", e);
		}
	}
	
}