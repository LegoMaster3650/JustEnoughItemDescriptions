package io._3650.jeidescs.config;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io._3650.jeidescs.util.JEIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

public class DescriptionJson {
	
	private final ImmutableMultimap<List<ItemStack>, List<Component>> items;
	private final ImmutableMultimap<List<FluidStack>, List<Component>> fluids;
	
	public DescriptionJson() {
		this.items = ImmutableMultimap.of();
		this.fluids = ImmutableMultimap.of();
	}
	
	private DescriptionJson(ImmutableMultimap.Builder<List<ItemStack>, List<Component>> items, ImmutableMultimap.Builder<List<FluidStack>, List<Component>> fluids) {
		this.items = items.build();
		this.fluids = fluids.build();
	}
	
	public ImmutableMultimap<List<ItemStack>, List<Component>> getItems() {
		return this.items;
	}
	
	public ImmutableMultimap<List<FluidStack>, List<Component>> getFluids() {
		return this.fluids;
	}
	
	public static class Serializer implements JsonSerializer<DescriptionJson>, JsonDeserializer<DescriptionJson> {
		
		@Override
		public DescriptionJson deserialize(JsonElement jsonElem, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject json = jsonElem.getAsJsonObject();
			ImmutableMultimap.Builder<List<ItemStack>, List<Component>> items = ImmutableMultimap.builder();
			ImmutableMultimap.Builder<List<FluidStack>, List<Component>> fluids = ImmutableMultimap.builder();
			if (GsonHelper.isArrayNode(json, "entries")) {
				JsonArray entries = GsonHelper.getAsJsonArray(json, "entries");
				for (var entryElem : entries) {
					JsonObject entry = entryElem.getAsJsonObject();
					
					List<Component> components;
					if (entry.has("text")) {
						components = JEIDUtil.optionalJsonArray(entry.get("text"), txt -> {
							if (GsonHelper.isStringValue(txt)) {
								return Component.literal(txt.getAsString());
							} else return null;
						});
					} else if (entry.has("translate")) {
						components = JEIDUtil.optionalJsonArray(entry.get("translate"), key -> {
							if (GsonHelper.isStringValue(key)) {
								return Component.translatable(key.getAsString());
							} else return null;
						});
					} else if (entry.has("component")) {
						components = JEIDUtil.optionalJsonArray(entry.get("component"), Component.Serializer::fromJson);
					} else throw new JsonParseException("No description component found");
					
					if (entry.has("item")) {
						ImmutableList.Builder<ItemStack> itemsList = ImmutableList.builder();
						var flag = JEIDUtil.holder(false);
						JEIDUtil.forEachOptionalJsonArray(entry.get("item"), elem -> {
							if (!GsonHelper.isStringValue(elem)) throw new JsonParseException("Element is not plain text: " + elem.toString());
							String itemName = elem.getAsString();
							if (itemName.startsWith("#")) {
								String tagName = itemName.substring(1);
								if (!ResourceLocation.isValidResourceLocation(tagName)) throw new JsonParseException(tagName + " is not a valid tag name");
								TagKey<Item> tagKey = ItemTags.create(new ResourceLocation(tagName));
								if (!ForgeRegistries.ITEMS.tags().isKnownTagName(tagKey)) throw new JsonParseException("Item Tag " + tagName + " does not exist");
								ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
								int amount = GsonHelper.getAsInt(json, "amount", 1);
								for (Item item : tag) itemsList.add(new ItemStack(item, amount));
								flag.value = true;
							} else {
								if (!ResourceLocation.isValidResourceLocation(itemName)) throw new JsonParseException(itemName + " is not a valid item name");
								Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
								if (item == null) throw new JsonParseException("Item " + itemName + " does not exist");
								itemsList.add(new ItemStack(item, GsonHelper.getAsInt(entry, "amount", 1)));
								flag.value = true;
							}
						});
						if (flag.value) items.put(itemsList.build(), components);
					} else if (entry.has("fluid")) {
						ImmutableList.Builder<FluidStack> fluidsList = ImmutableList.builder();
						var flag = JEIDUtil.holder(false);
						JEIDUtil.forEachOptionalJsonArray(entry.get("fluid"), elem -> {
							if (!GsonHelper.isStringValue(elem)) throw new JsonParseException("Element is not plain text: " + elem.toString());
							String fluidName = elem.getAsString();
							if (fluidName.startsWith("#")) {
								String tagName = fluidName.substring(1);
								if (!ResourceLocation.isValidResourceLocation(tagName)) throw new JsonParseException(tagName + " is not a valid tag name");
								TagKey<Fluid> tagKey = FluidTags.create(new ResourceLocation(tagName));
								if (!ForgeRegistries.FLUIDS.tags().isKnownTagName(tagKey)) throw new JsonParseException("Fluid Tag " + tagName + " does not exist");
								ITag<Fluid> tag = ForgeRegistries.FLUIDS.tags().getTag(tagKey);
								int amount = GsonHelper.getAsInt(json, "amount", 1000);
								for (Fluid fluid : tag) fluidsList.add(new FluidStack(fluid, amount));
								flag.value = true;
							} else {
								if (!ResourceLocation.isValidResourceLocation(fluidName)) throw new JsonParseException(fluidName + " is not a valid fluid name");
								Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
								if (fluid == null) throw new JsonParseException("Fluid " + fluidName + " does not exist");
								fluidsList.add(new FluidStack(fluid, GsonHelper.getAsInt(entry, "amount", 1000)));
								flag.value = true;
							}
						});
						if (flag.value) fluids.put(fluidsList.build(), components);
					}
				}
			}
			if (GsonHelper.isObjectNode(json, "items")) {
				var itemSet = GsonHelper.getAsJsonObject(json, "items").entrySet();
				int amount = GsonHelper.getAsInt(json, "amount", 1);
				for (var entry : itemSet) {
					List<Component> components = JEIDUtil.optionalJsonArray(entry.getValue(), Component.Serializer::fromJson);
					String[] itemNames = entry.getKey().split(",");
					ImmutableList.Builder<ItemStack> itemsList = ImmutableList.builder();
					var flag = JEIDUtil.holder(false);
					for (String itemName : itemNames) {
						if (itemName.startsWith("#")) {
							String tagName = itemName.substring(1);
							if (!ResourceLocation.isValidResourceLocation(tagName)) throw new JsonParseException(tagName + " is not a valid tag name");
							TagKey<Item> tagKey = ItemTags.create(new ResourceLocation(tagName));
							if (!ForgeRegistries.ITEMS.tags().isKnownTagName(tagKey)) throw new JsonParseException("Item Tag " + tagName + " does not exist");
							ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
							for (Item item : tag) itemsList.add(new ItemStack(item, amount));
							flag.value = true;
						} else {
							if (!ResourceLocation.isValidResourceLocation(itemName)) throw new JsonParseException(itemName + " is not a valid item name");
							Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
							if (item == null) throw new JsonParseException("Item " + itemName + " does not exist");
							itemsList.add(new ItemStack(item, amount));
							flag.value = true;
						}
					}
					if (flag.value) items.put(itemsList.build(), components);
				}
			}
			if (GsonHelper.isObjectNode(json, "fluids")) {
				var fluidSet = GsonHelper.getAsJsonObject(json, "fluids").entrySet();
				int amount = GsonHelper.getAsInt(json, "amount", 1000);
				for (var entry : fluidSet) {
					List<Component> components = JEIDUtil.optionalJsonArray(entry.getValue(), Component.Serializer::fromJson);
					String[] fluidNames = entry.getKey().split(",");
					ImmutableList.Builder<FluidStack> fluidsList = ImmutableList.builder();
					var flag = JEIDUtil.holder(false);
					for (String fluidName : fluidNames) {
						if (fluidName.startsWith("#")) {
							String tagName = fluidName.substring(1);
							if (!ResourceLocation.isValidResourceLocation(tagName)) throw new JsonParseException(tagName + " is not a valid tag name");
							TagKey<Fluid> tagKey = FluidTags.create(new ResourceLocation(tagName));
							if (!ForgeRegistries.FLUIDS.tags().isKnownTagName(tagKey)) throw new JsonParseException("Fluid Tag " + tagName + " does not exist");
							ITag<Fluid> tag = ForgeRegistries.FLUIDS.tags().getTag(tagKey);
							for (Fluid item : tag) fluidsList.add(new FluidStack(item, amount));
							flag.value = true;
						} else {
							if (!ResourceLocation.isValidResourceLocation(fluidName)) throw new JsonParseException(fluidName + " is not a valid fluid name");
							Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
							if (fluid == null) throw new JsonParseException("Fluid " + fluidName + " does not exist");
							fluidsList.add(new FluidStack(fluid, amount));
							flag.value = true;
						}
					}
					if (flag.value) fluids.put(fluidsList.build(), components);
				}
			}
			return new DescriptionJson(items, fluids);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public JsonElement serialize(DescriptionJson src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			JsonArray entries = new JsonArray();
			for (var entry : src.items.entries()) {
				JsonObject entryJson = new JsonObject();
				List<ItemStack> stacks = entry.getKey();
				ItemStack stack1 = stacks.get(1);
				if (stacks.size() == 1) entryJson.addProperty("item", ForgeRegistries.ITEMS.getKey(stack1.getItem()).toString());
				else {
					JsonArray jsonStacks = new JsonArray(stacks.size());
					for (var stack : stacks) jsonStacks.add(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
					entryJson.add("item", jsonStacks);
				}
				if (stack1.getCount() > 1) entryJson.addProperty("amount", stack1.getCount());
				List<Component> components = entry.getValue();
				JsonArray comps = new JsonArray(components.size());
				for (var component : components) comps.add(Component.Serializer.toJsonTree(component));
				entryJson.add("component", comps);
				entries.add(entryJson);
			}
			for (var entry : src.fluids.entries()) {
				JsonObject entryJson = new JsonObject();
				List<FluidStack> stacks = entry.getKey();
				FluidStack stack1 = stacks.get(1);
				if (stacks.size() == 1) entryJson.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(stack1.getFluid()).toString());
				else {
					JsonArray jsonStacks = new JsonArray(stacks.size());
					for (var stack : stacks) jsonStacks.add(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString());
					entryJson.add("fluid", jsonStacks);
				}
				if (stack1.getAmount() > 1) entryJson.addProperty("amount", stack1.getAmount());
				List<Component> components = entry.getValue();
				JsonArray comps = new JsonArray(components.size());
				for (var component : components) comps.add(Component.Serializer.toJsonTree(component));
				entryJson.add("component", comps);
				entries.add(entryJson);
			}
			return json;
		}
		
	}
	
}