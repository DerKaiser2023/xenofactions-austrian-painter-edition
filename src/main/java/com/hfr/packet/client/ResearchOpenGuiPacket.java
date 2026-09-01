package com.hfr.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import com.hfr.technology.gui.GUIResearch;

public class ResearchOpenGuiPacket implements IMessage {

    public ResearchOpenGuiPacket() { }

    @Override
    public void fromBytes(ByteBuf buf) { }

    @Override
    public void toBytes(ByteBuf buf) { }

    public static class Handler implements IMessageHandler<ResearchOpenGuiPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ResearchOpenGuiPacket m, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GUIResearch());
            return null;
        }
    }
}