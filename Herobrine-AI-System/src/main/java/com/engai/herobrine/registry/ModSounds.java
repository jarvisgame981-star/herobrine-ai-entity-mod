package com.engai.herobrine.registry;

import com.engai.herobrine.ClassicHerobrineMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	public static final SoundEvent CAVE_1 = register("cave1");
	public static final SoundEvent CAVE_2 = register("cave2");
	public static final SoundEvent CAVE_3 = register("cave3");
	public static final SoundEvent CAVE_4 = register("cave4");
	public static final SoundEvent CAVE_5 = register("cave5");
	public static final SoundEvent CAVE_6 = register("cave6");
	public static final SoundEvent CAVE_7 = register("cave7");
	public static final SoundEvent CAVE_8 = register("cave8");
	public static final SoundEvent CAVE_9 = register("cave9");
	public static final SoundEvent CAVE_10 = register("cave10");

	private ModSounds() {
	}

	public static void register() {
		ClassicHerobrineMod.LOGGER.info("Registering Herobrine sounds");
	}

	private static SoundEvent register(String name) {
		Identifier id = Identifier.fromNamespaceAndPath(ClassicHerobrineMod.MOD_ID, name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}
}
