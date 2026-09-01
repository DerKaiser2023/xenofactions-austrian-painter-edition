package com.hfr.technology.gui;

import com.hfr.technology.Technology;
import com.hfr.technology.TechnologyManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GUITechnologyInfo extends GuiScreen {
    private static final ResourceLocation texture = new ResourceLocation("hfr:textures/gui/gui_tech_info.png");
    private int x, y, w, h;
    private final Technology tech;

    public GUITechnologyInfo(Technology tech) {
        this.tech = tech;
    }

    public void initGui() {
        super.initGui();
        this.w = 256;
        this.h = 220;
        this.x = (this.width - w) / 2;
        this.y = (this.height - h) / 2;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, x + w - 25, y + 5, 20, 20, "X"));
        this.buttonList.add(new GuiButton(1, x + 10, y + h - 25, 100, 20, "Back"));
        if (tech != null && tech.researchCost > 0) {
            this.buttonList.add(new GuiButton(2, x + 130, y + h - 25, 100, 20, "Research"));
        }
        if (tech != null && tech.purchaseCost > 0) {
            this.buttonList.add(new GuiButton(3, x + 130, y + h - 50, 100, 20, "Purchase"));
        }
    }

    public void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (button.id == 1) {
            this.mc.displayGuiScreen(new GUIResearch());
            return;
        }
        if (button.id == 2 && tech != null) {
            net.minecraft.client.Minecraft.getMinecraft().thePlayer.sendChatMessage("/xc research research " + tech.id);
            this.mc.displayGuiScreen(null);
            return;
        }
        if (button.id == 3 && tech != null) {
            net.minecraft.client.Minecraft.getMinecraft().thePlayer.sendChatMessage("/xc research purchase " + tech.id);
            this.mc.displayGuiScreen(null);
            return;
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(x, y, 0, 0, w, h);
        if (tech == null) {
            this.fontRendererObj.drawString("Technology Details", x + 10, y + 10, 0x404040);
        } else {
            this.fontRendererObj.drawString(tech.name, x + 10, y + 10, 0x404040);
            this.fontRendererObj.drawString("ID: " + tech.id, x + 10, y + 25, 0x303030);
            this.fontRendererObj.drawString("Type: " + tech.type, x + 10, y + 40, 0x303030);
            this.fontRendererObj.drawString("Tier: " + tech.tier, x + 10, y + 55, 0x303030);
            if (tech.researchCost > 0)
                this.fontRendererObj.drawString("Research cost: " + tech.researchCost + " " + TechnologyManager.getResearchCurrency(), x + 10, y + 70, 0x303030);
            if (tech.purchaseCost > 0)
                this.fontRendererObj.drawString("Purchase cost: " + tech.purchaseCost + " " + TechnologyManager.getPurchaseCurrency(), x + 10, y + 85, 0x303030);
            if (!tech.prerequisites.isEmpty())
                this.fontRendererObj.drawString("Requires: " + String.join(", ", tech.prerequisites), x + 10, y + 100, 0x303030);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}