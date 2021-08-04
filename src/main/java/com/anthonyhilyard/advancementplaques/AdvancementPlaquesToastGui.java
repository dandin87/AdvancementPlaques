package com.anthonyhilyard.advancementplaques;

import java.util.Arrays;
import java.util.Deque;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraftforge.fml.ModList;

public class AdvancementPlaquesToastGui extends ToastComponent
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();

	private final AdvancementPlaque[] plaques = new AdvancementPlaque[3];
	private final Deque<AdvancementToast> advancementToastsQueue = Queues.newArrayDeque();
	private final Minecraft mc;
	private final CustomItemRenderer itemRenderer;

	public AdvancementPlaquesToastGui(Minecraft mcIn)
	{
		super(mcIn);
		mc = mcIn;
		itemRenderer = new CustomItemRenderer(mc.getTextureManager(), mc.getModelManager(), mc.getItemColors(), mc.getItemRenderer().getBlockEntityRenderer(), mc);
	}

	@Override
	public void addToast(Toast toastIn)
	{
		if (toastIn instanceof AdvancementToast)
		{
			AdvancementToast advancementToast = (AdvancementToast)toastIn;
			DisplayInfo displayInfo = advancementToast.advancement.getDisplay();
			if ((displayInfo.getFrame() == FrameType.TASK && AdvancementPlaquesConfig.INSTANCE.tasks.get()) ||
				(displayInfo.getFrame() == FrameType.GOAL && AdvancementPlaquesConfig.INSTANCE.goals.get()) ||
				(displayInfo.getFrame() == FrameType.CHALLENGE && AdvancementPlaquesConfig.INSTANCE.challenges.get()) ||
				AdvancementPlaquesConfig.INSTANCE.whitelist.get().contains(advancementToast.advancement.getId().toString()))
			{
				// Special logic for advancement toasts.  Store them seperately since they will be displayed seperately.
				advancementToastsQueue.add((AdvancementToast)toastIn);
				return;
			}
		}

		super.addToast(toastIn);
	}

	@Override
	public void render(PoseStack stack)
	{
		if (!mc.options.hideGui)
		{
			// Do toasts.
			super.render(stack);

			try
			{
				// If Waila/Hwyla/Jade is installed, turn it off while the plaque is drawing if configured to do so.
				if (AdvancementPlaquesConfig.INSTANCE.hideWaila.get() && ModList.get().isLoaded("waila"))
				{
					boolean anyPlaques = false;
					for (int i = 0; i < plaques.length; i++)
					{
						if (plaques[i] != null)
						{
							anyPlaques = true;
							break;
						}
					}


					if (anyPlaques)
					{
						Class.forName("com.anthonyhilyard.advancementplaques.WailaHandler").getMethod("disableWaila").invoke(null);
					}
					else
					{
						Class.forName("com.anthonyhilyard.advancementplaques.WailaHandler").getMethod("enableWaila").invoke(null);
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error(e);
			}

			// Do plaques.
			for (int i = 0; i < plaques.length; ++i)
			{
				AdvancementPlaque toastinstance = plaques[i];

				if (toastinstance != null && toastinstance.render(mc.getWindow().getGuiScaledWidth(), i, stack))
				{
					plaques[i] = null;
				}

				if (plaques[i] == null && !advancementToastsQueue.isEmpty())
				{
					plaques[i] = new AdvancementPlaque(advancementToastsQueue.removeFirst(), mc, itemRenderer);
				}
			}
		}
	}

	@Override
	public void clear()
	{
		super.clear();
		Arrays.fill(plaques, null);
		advancementToastsQueue.clear();
	}
}
