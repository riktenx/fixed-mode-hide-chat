package io.github.deathbeam.plugins.fixedhidechat;

import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.AUTO_EXPAND_WIDGETS;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.DEFAULT_VIEW_HEIGHT;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.EXPANDED_VIEW_HEIGHT;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.BANK_X;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.BANK_Y;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.DEFAULT_VIEW_WIDGET_HEIGHT;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.EXPANDED_VIEW_WIDGET_HEIGHT;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.FIXED_MAIN;
import static io.github.deathbeam.plugins.fixedhidechat.FixedHideChatConstants.TO_CONTRACT_WIDGETS;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Fixed Mode Hide Chat",
	description = "Hides chatbox in fixed mode and expands the view"
)
public class FixedHideChatPlugin extends Plugin implements KeyListener
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

	private int lastMenu = 0;
	private boolean hideChat = true;
	private boolean hideChatPrevious = hideChat;

	@Override
	protected void startUp() throws Exception
	{
		// Register listener
		keyManager.registerKeyListener(this);
	}

	@Override
	protected void shutDown() throws Exception
	{
		// Unregister listener
		keyManager.unregisterKeyListener(this);

		// Reset menu state
		hideChat = true;
		lastMenu = 0;

		// Reset widgets
		clientThread.invoke(this::resetWidgets);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		keyReleased(e);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		keyReleased(e);
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (!client.isResized() && e.getKeyCode() == KeyEvent.VK_ESCAPE && !hideChat)
		{
			hideChat = true;
			e.consume();
		}
	}

	@Subscribe
	public void onBeforeRender(final BeforeRender event)
	{
		if (client.isResized())
		{
			return;
		}

		// Bank container sometimes moves offscreen on resize and quick inputs, workaround
		final Widget bankWidget = client.getWidget(ComponentID.BANK_CONTAINER);
		if (bankWidget != null && !bankWidget.isSelfHidden())
		{
			// call [clientscript,bankmain_init] because otherwise the tag tabs don't extend properly
			// but don't call it every frame because then performance tanks
			// Causes a very slight flicker of the tag tab above the swap button sadly when opening the bag without the chat hidden
			if (hideChatPrevious != hideChat)
			{
				client.createScriptEvent(bankWidget.getOnLoadListener())
						.setSource(bankWidget)
						.run();
			}
			changeWidgetXY(bankWidget, BANK_X);
		}

		// The seed vault container sometimes moves offscreen on resize and quick inputs, workaround
		final Widget seedVaultWidget = client.getWidget(ComponentID.SEED_VAULT_INVENTORY_ITEM_CONTAINER);
		if (seedVaultWidget != null && !seedVaultWidget.isSelfHidden())
		{
			changeWidgetXY(seedVaultWidget, 6);
		}

		// Expand the view height
		setViewSizeTo(DEFAULT_VIEW_HEIGHT, EXPANDED_VIEW_HEIGHT);

		final Widget chatboxMessages = client.getWidget(ComponentID.CHATBOX_FRAME);

		if (chatboxMessages != null)
		{
			boolean found = !hideChat;

			// Check if any auto-expand interface is open
			if (!found)
			{
				for (final Map.Entry<Integer, Integer> widgets : AUTO_EXPAND_WIDGETS)
				{
					final Widget widget = widgets.getValue() == 0 ? client.getWidget(widgets.getKey()) : client.getWidget(widgets.getKey(), widgets.getValue());

					if (widget != null && !widget.isSelfHidden())
					{
						found = true;
						break;
					}
				}
			}

			// Resize some widgets that might interfere with having expanded chat
			setWidgetsSizeTo(
				found ? EXPANDED_VIEW_WIDGET_HEIGHT : DEFAULT_VIEW_WIDGET_HEIGHT,
				found ? DEFAULT_VIEW_WIDGET_HEIGHT : EXPANDED_VIEW_WIDGET_HEIGHT);

			// Hide/show chat messages
			chatboxMessages.setHidden(!found);
		}

		hideChatPrevious = hideChat;
	}

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked event)
	{
		if (!"Switch tab".equals(event.getMenuOption()))
		{
			return;
		}

		final Widget chatboxMessages = client.getWidget(ComponentID.CHATBOX_FRAME);
		final int newMenu = event.getParam1(); // Param1 is the same as getWidget().getId()
		hideChat = true;

		if (newMenu != lastMenu || (chatboxMessages != null && chatboxMessages.isHidden()))
		{
			hideChat = false;
			lastMenu = newMenu;
		}
	}

	private void changeWidgetXY(Widget widget, int xPosition)
	{
		widget.setOriginalX(xPosition);
		widget.setOriginalY(BANK_Y);
		widget.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		widget.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		widget.revalidateScroll();
	}

	private static void changeWidgetHeight(int originalHeight, int newHeight, Widget widget)
	{
		if (widget.getHeight() == originalHeight)
		{
			widget.setOriginalHeight(newHeight);
			widget.setHeightMode(WidgetSizeMode.ABSOLUTE);
			widget.revalidateScroll();

			final Widget[] nestedChildren = widget.getNestedChildren();

			if (nestedChildren != null)
			{
				for (final Widget nestedChild : nestedChildren)
				{
					if (nestedChild.getHeight() == originalHeight)
					{
						nestedChild.setOriginalHeight(newHeight);
						nestedChild.setHeightMode(WidgetSizeMode.ABSOLUTE);
						nestedChild.revalidateScroll();
					}
				}
			}

			final Widget[] dynamicChildren = widget.getDynamicChildren();

			if (dynamicChildren != null)
			{
				for (final Widget child : dynamicChildren)
				{
					if (child.getHeight() == originalHeight)
					{
						child.setOriginalHeight(newHeight);
						child.setHeightMode(WidgetSizeMode.ABSOLUTE);
						child.revalidateScroll();
					}
				}
			}
		}
	}

	private void setWidgetsSizeTo(final int originalHeight, final int newHeight)
	{
		for (final Map.Entry<Integer, Integer> widgets : TO_CONTRACT_WIDGETS)
		{
			final Widget widget = widgets.getValue() == 0 ? client.getWidget(widgets.getKey()) : client.getWidget(widgets.getKey(), widgets.getValue());
			if (widget != null && !widget.isSelfHidden())
			{
				changeWidgetHeight(originalHeight, newHeight, widget);
			}
		}
	}

	private void setViewSizeTo(final int originalHeight, final int newHeight)
	{
		final Widget viewport = client.getWidget(ComponentID.FIXED_VIEWPORT_FIXED_VIEWPORT);

		if (viewport != null)
		{
			viewport.setOriginalHeight(newHeight);
			viewport.setHeightMode(WidgetSizeMode.ABSOLUTE);
			viewport.revalidateScroll();
		}

		final Widget fixedMain = client.getWidget(FIXED_MAIN.getKey(), FIXED_MAIN.getValue());

		if (fixedMain != null && fixedMain.getHeight() == originalHeight)
		{
			fixedMain.setOriginalHeight(newHeight);
			fixedMain.setHeightMode(WidgetSizeMode.ABSOLUTE);
			fixedMain.revalidateScroll();

			final Widget[] staticChildren = fixedMain.getStaticChildren();

			// Expand all children of the main fixed view
			for (final Widget child : staticChildren)
			{
				changeWidgetHeight(originalHeight, newHeight, child);
			}
		}

	}

	private void resetWidgets()
	{
		if (client.isResized())
		{
			return;
		}

		// Contract the view if it is expanded
		setViewSizeTo(EXPANDED_VIEW_HEIGHT, DEFAULT_VIEW_HEIGHT);
		setWidgetsSizeTo(EXPANDED_VIEW_WIDGET_HEIGHT, DEFAULT_VIEW_WIDGET_HEIGHT);

		// Show the chat messages widget again
		final Widget chatboxMessages = client.getWidget(ComponentID.CHATBOX_FRAME);

		if (chatboxMessages != null)
		{
			chatboxMessages.setHidden(false);
		}
	}
}
