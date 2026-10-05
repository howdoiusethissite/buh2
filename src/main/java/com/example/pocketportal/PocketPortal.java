package com.example.pocketportal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class PocketPortal implements ModInitializer {
	public static final String MOD_ID = "pocketportal";

	public static final ResourceKey<Item> POCKET_NETHER_PORTAL_KEY =
		ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "pocket_nether_portal"));

	public static final Item POCKET_NETHER_PORTAL = Registry.register(
		BuiltInRegistries.ITEM,
		POCKET_NETHER_PORTAL_KEY,
		new PocketPortalItem(new Item.Properties().setId(POCKET_NETHER_PORTAL_KEY).stacksTo(1).rarity(Rarity.UNCOMMON))
	);

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(output -> output.accept(POCKET_NETHER_PORTAL));
	}
}
