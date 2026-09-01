package com.hfr.technology.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.hfr.clowder.Clowder;
import com.hfr.technology.Technology;
import com.hfr.technology.TechnologyManager;
import com.hfr.technology.TechnologyType;

public class GUIResearch extends GuiScreen {
    private static final ResourceLocation texture = new ResourceLocation("hfr:textures/gui/gui_research.png");
    private int x, y, w, h;
    private int scroll = 0;

    public void initGui() {
        super.initGui();
        this.w = 256;
        this.h = 220;
        this.x = (this.width - w) / 2;
        this.y = (this.height - h) / 2;
        this.buttonList.clear();
        int id = 0;
        this.buttonList.add(new GuiButton(9001, x + w - 25, y + 5, 20, 20, "X"));
        this.buttonList.add(new GuiButton(9002, x + 5, y + h - 25, 60, 20, "Up"));
        this.buttonList.add(new GuiButton(9003, x + 70, y + h - 25, 60, 20, "Down"));
        rebuildTechButtons();
    }

    private void rebuildTechButtons() {
        this.buttonList.removeIf(b -> b.id >= 0 && b.id < 1000);
        if (!TechnologyManager.isTreeAvailable()) return;
        List<Technology> techs = TechnologyManager.tree.technologies;
        for (int i = 0; i < techs.size(); i++) {
            int slot = i - scroll;
            if (slot < 0 || slot >= 8) continue;
            Technology tech = techs.get(i);
            int by = y + 35 + slot * 22;
            String label = tech.name + " [" + tech.type + "]";
            if (tech.researchCost > 0) label += " R:" + tech.researchCost;
            if (tech.purchaseCost > 0) label += " P:" + tech.purchaseCost;
            GuiButton btn = new GuiButton(i, x + 10, by, w - 20, 20, label);
            this.buttonList.add(btn);
        }
    }

    public void actionPerformed(GuiButton button) {
        if (button.id == 9001) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (button.id == 9002) {
            if (scroll > 0) { scroll--; rebuildTechButtons(); }
            return;
        }
        if (button.id == 9003) {
            if (TechnologyManager.isTreeAvailable() && scroll + 8 < TechnologyManager.tree.technologies.size()) {
                scroll++;
                rebuildTechButtons();
            }
            return;
        }
        if (button.id >= 0 && button.id < 1000 && TechnologyManager.isTreeAvailable()) {
            Technology tech = TechnologyManager.tree.technologies.get(button.id);
            mc.displayGuiScreen(new GUITechnologyInfo(tech));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(x, y, 0, 0, w, h);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("gui.hfr.technology.title"), x + 10, y + 8, 0x404040);
        if (!TechnologyManager.isConfigEnabled()) {
            String[] lines = new String[] {
                StatCollector.translateToLocal("gui.hfr.technology.disabled.1"),
                StatCollector.translateToLocal("gui.hfr.technology.disabled.2"),
                StatCollector.translateToLocal("gui.hfr.technology.disabled.3")
            };
            for (int i = 0; i < lines.length; i++)
                this.fontRendererObj.drawString(lines[i], x + 10, y + 35 + i * 12, 0x800000);
        } else if (!TechnologyManager.isTreeAvailable()) {
            String[] lines = new String[] {
                StatCollector.translateToLocal("gui.hfr.technology.notree.1"),
                StatCollector.translateToLocal("gui.hfr.technology.notree.2"),
                StatCollector.translateToLocal("gui.hfr.technology.notree.3")
            };
            for (int i = 0; i < lines.length; i++)
                this.fontRendererObj.drawString(lines[i], x + 10, y + 35 + i * 12, 0x800000);
        } else {
            String header = "Tree: " + TechnologyManager.tree.technologies.size()
                    + "  Currency: " + TechnologyManager.getPurchaseCurrency();
            this.fontRendererObj.drawString(header, x + 10, y + 22, 0x404040);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}