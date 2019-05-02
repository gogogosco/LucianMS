package com.lucianms.service;

import com.lucianms.nio.InterPacketOperation;
import com.lucianms.nio.receive.DirectPacketDecoder;
import com.lucianms.nio.send.DirectPacketEncoder;
import com.lucianms.nio.send.MaplePacketWriter;
import com.lucianms.nio.server.NettyDiscardClient;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.Server;
import com.lucianms.server.world.MapleWorld;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class InternalChannelCommunicationsHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalChannelCommunicationsHandler.class);

    private final String address;
    private final int port;
    private final NettyDiscardClient client;
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public InternalChannelCommunicationsHandler(String address, int port) throws Exception {
        this.address = address;
        this.port = port;

        client = new NettyDiscardClient(address, port, new NioEventLoopGroup(), this, DirectPacketDecoder.class, DirectPacketEncoder.class);
        client.run();
        attemptConnection();
    }

    public void close() throws Exception {
        client.close();
    }

    private void attemptConnection() {
        ChannelFuture connect = client.getBootstrap().connect(address, port);
        connect.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    LOGGER.info("Failed to connect to login server... Retrying in 5 seconds");
                    channelFuture.channel().eventLoop().schedule(() -> attemptConnection(), 5000, TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    public void sendMessage(String content) {
        MaplePacketWriter writer = new MaplePacketWriter(content.length() + 3);
        writer.write(InterPacketOperation.Message.ordinal());
        writer.writeMapleString(content);
        channels.writeAndFlush(writer.getPacket());
    }

    public void sendPacket(byte[] packet) {
        channels.writeAndFlush(packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
        LOGGER.info("Disconnected from login server");
        attemptConnection();

        for (MapleWorld world : Server.getWorlds()) {
            world.broadcastPacket(MaplePacketCreator.serverMessage("The server is currently going under maintenance. Please refrain from logging-out until further notice."));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
        LOGGER.info("Connected to login server");
        sendMessage("The server is now available.\r\nYou may login.");

        for (MapleWorld world : Server.getWorlds()) {
            world.broadcastPacket(MaplePacketCreator.serverMessage("Server is now operational. Thank you for your patience."));
        }
        TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                for (MapleWorld world : Server.getWorlds()) {
                    // empty message to remove scrolling notice
                    world.broadcastPacket(MaplePacketCreator.serverMessage(""));
                }
            }
        }, 10000);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            return;
        }
        cause.printStackTrace();
    }
}
