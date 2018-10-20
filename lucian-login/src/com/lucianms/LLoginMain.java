package com.lucianms;

import com.lucianms.events.*;
import com.lucianms.io.Config;
import com.lucianms.nio.InternalLoginCommunicationsHandler;
import com.lucianms.nio.ReceivePacketState;
import com.lucianms.nio.RecvOpcode;
import com.lucianms.nio.receive.DirectPacketDecoder;
import com.lucianms.nio.send.DirectPacketEncoder;
import com.lucianms.nio.server.MapleServerInboundHandler;
import com.lucianms.nio.server.NettyDiscardServer;
import com.lucianms.server.Server;
import com.lucianms.server.handlers.login.PickCharHandler;
import com.lucianms.server.handlers.login.ViewCharHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author izarooni
 */
public class LLoginMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(LLoginMain.class);

    public static void main(String[] args) {
        initReceiveHeaders();
        Server.createServer();
        Config config = Server.getConfig();

        try {
            String address = config.getString("ServerHost");
            Long port = config.getNumber("LoginBasePort");

            NettyDiscardServer interServer = new NettyDiscardServer(address, port.intValue() + 1, new InternalLoginCommunicationsHandler(), new NioEventLoopGroup(), new DirectPacketDecoder(), new DirectPacketEncoder());
            interServer.run();

            MapleServerInboundHandler handler = new MapleServerInboundHandler(ReceivePacketState.LoginServer, address, port.intValue(), new NioEventLoopGroup());
            Server.setServerHandler(handler);
            LOGGER.info("Login server open on {}", port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void initReceiveHeaders() {
        RecvOpcode.LOGIN_PASSWORD.clazz = AccountLoginEvent.class;
        RecvOpcode.SERVERLIST_REREQUEST.clazz = WorldListEvent.class;
        RecvOpcode.CHARLIST_REQUEST.clazz = AccountChannelSelectEvent.class;
        RecvOpcode.SERVERSTATUS_REQUEST.clazz = WorldStatusCheckEvent.class;
        RecvOpcode.ACCEPT_TOS.clazz = AccountToSResultEvent.class;
        RecvOpcode.SET_GENDER.clazz = AccountGenderSetEvent.class;
        RecvOpcode.AFTER_LOGIN.clazz = AccountPostLoginEvent.class;
        RecvOpcode.REGISTER_PIN.clazz = AccountPINSetEvent.class;
        RecvOpcode.SERVERLIST_REQUEST.clazz = WorldListEvent.class;
        RecvOpcode.VIEW_ALL_CHAR.clazz = ViewCharHandler.class;
        RecvOpcode.PICK_ALL_CHAR.clazz = PickCharHandler.class;
        RecvOpcode.CHAR_SELECT.clazz = AccountPlayerSelectEvent.class;
        RecvOpcode.CHECK_CHAR_NAME.clazz = AccountPlayerCreateUsernameCheckEvent.class;
        RecvOpcode.CREATE_CHAR.clazz = AccountPlayerCreateEvent.class;
        RecvOpcode.DELETE_CHAR.clazz = AccountPlayerDeleteEvent.class;
        RecvOpcode.RELOG.clazz = AccountRelogEvent.class;
        RecvOpcode.REGISTER_PIC.clazz = AccountRegisterPICEvent.class;
        RecvOpcode.CHAR_SELECT_WITH_PIC.clazz = AccountSelectPlayerPICEvent.class;
    }
}
