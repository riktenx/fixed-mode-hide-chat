package io.github.deathbeam.plugins.fixedhidechat;

import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import net.runelite.api.widgets.*;

public class FixedHideChatConstants
{
	// 'Rub' option (S219.0) Duel rings, Slayer Rings, Burning Amulets, etc.
	private static final Map.Entry<Integer, Integer>  CHATBOX_MESSAGES_DIALOG_OPTION = new AbstractMap.SimpleEntry<>(
		InterfaceID.DIALOG_OPTION,
		0
	);

	// A lot of other stuff, we recurse through this, it should catch any messages under (S162.566 ID: 10617398)
	// Wrong PIN popup, Gauntlet Exit Prompt (S229.0 ID: 15007745), NPC Dialog (N231.0 ID: 5138816), Make-X (N270.0 ID: 17694720)... etc.
	private static final Map.Entry<Integer, Integer>  CHATBOX_MESSAGES_SPECIAL = new AbstractMap.SimpleEntry<>(
		InterfaceID.CHATBOX,
		566
	);

	// Bank Search Container
	private static final Map.Entry<Integer, Integer>  CHATBOX_MESSAGES_CONTAINER = new AbstractMap.SimpleEntry<>(
		InterfaceID.CHATBOX,
		42
	);

	private static final Map.Entry<Integer, Integer>  FIXED_VIEWPORT_BANK_POPUP_CONTAINER = new AbstractMap.SimpleEntry<>(
		ComponentID.BANK_CONTAINER,
		0
	);

	private static final Map.Entry<Integer, Integer>  FIXED_VIEWPORT_SEED_VAULT_INVENTORY_ITEM_CONTAINER = new AbstractMap.SimpleEntry<>(
		InterfaceID.SEED_VAULT,
		1
	);

	static final Map.Entry<Integer, Integer>  FIXED_MAIN = new AbstractMap.SimpleEntry<>(
		InterfaceID.FIXED_VIEWPORT,
		9
	);

	static final int DEFAULT_VIEW_HEIGHT = 334;
	static final int EXPANDED_VIEW_HEIGHT = 476;
	static final int BANK_X = 12;
	static final int BANK_Y = 2;
	// This is the VIEW_HEIGHT minus the BANK_Y minus 1 since there is a gap of 1 pixel at the bottom without the plugin.
	static final int DEFAULT_VIEW_WIDGET_HEIGHT = DEFAULT_VIEW_HEIGHT - BANK_Y - 1;
	static final int EXPANDED_VIEW_WIDGET_HEIGHT = EXPANDED_VIEW_HEIGHT - BANK_Y - 1;

	static final Set<Map.Entry<Integer, Integer>> AUTO_EXPAND_WIDGETS = ImmutableSet
		.<Map.Entry<Integer, Integer>>builder()
		.add(CHATBOX_MESSAGES_DIALOG_OPTION)
		.add(CHATBOX_MESSAGES_CONTAINER)
		.add(CHATBOX_MESSAGES_SPECIAL)
		.build();

	static final Set<Map.Entry<Integer, Integer>> TO_CONTRACT_WIDGETS = ImmutableSet
		.<Map.Entry<Integer, Integer>>builder()
		.add(FIXED_VIEWPORT_BANK_POPUP_CONTAINER)
		.add(FIXED_VIEWPORT_SEED_VAULT_INVENTORY_ITEM_CONTAINER)
		.build();
}
