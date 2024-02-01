package io.github.deathbeam.plugins.fixedhidechat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("FixedHideChat")
public interface FixedHideChatConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "resizeViewport",
			name = "Resize viewport",
			description = "Resize viewport when opening/closing chat"
	)
	default boolean resizeViewport()
	{
		return false;
	}
}
