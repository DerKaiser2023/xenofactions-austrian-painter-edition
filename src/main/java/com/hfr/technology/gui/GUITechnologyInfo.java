package com.hfr.technology.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GUITechnologyInfo extends GuiScreen {
    private static final ResourceLocation texture = new ResourceLocation("hfr:textures/gui/gui_tech_info.png");
    private int x, y, w, h;

    public void initGui() {
        super.initGui();
        this.x = (this.width - 256) / 2;
        this.y = (this.height - 220) / 2;
        this.w = 256;
        this.h = 220;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, x + 230, y + 5, 20, 20, "X"));
    }

    public void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(x, y, 0, 0, w, h);
        this.fontRendererObj.drawString("Technology Details", x + 10, y + 10, 0x404040);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}