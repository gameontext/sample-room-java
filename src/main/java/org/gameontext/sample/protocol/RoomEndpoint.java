/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.gameontext.sample.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.gameontext.sample.Log;
import org.gameontext.sample.RoomImplementation;

/**
 * This is the WebSocket endpoint for a room. Java EE WebSockets
 * use simple annotations for event driven methods. An instance of this class
 * will be created for every connected client.
 * https://book.game-on.org/microservices/WebSocketProtocol.html
 */
@ServerEndpoint(value = "/room",
decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class RoomEndpoint {

    @Inject
    private RoomImplementation roomImplementation;
    
    @Inject
    private SessionSender sessionSender;
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        Log.log(Level.FINE, this, "A new connection has been made to the room.");

        sessionSender.addSession(session);
        
        // All we have to do in onOpen is send the acknowledgement
        sessionSender.sendMessage(Message.ACK_MSG);
        
    }

    @OnClose
    public void onClose(Session session, CloseReason r) {
        Log.log(Level.FINE, this, "A connection to the room has been closed with reason " + r);
        
        sessionSender.removeSession(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        Log.log(Level.FINE, this, "A problem occurred on connection", t);
        
        sessionSender.removeSession(session);

        // TODO: Careful with what might revealed about implementation details!!
        // We're opting for making debug easy..
        tryToClose(session,
                new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                        trimReason(t.getClass().getName())));
    }

    /**
     * The hook into the interesting room stuff.
     * @param session
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void receiveMessage(Session session, Message message) throws IOException {
        roomImplementation.handleMessage(message);
    }

    /**
     * @param message String to trim
     * @return a string no longer than 123 characters (limit of value length for {@code CloseReason})
     */
    private String trimReason(String message) {
        return message.length() > 123 ? message.substring(0, 123) : message;
    }

    /**
     * Try to close the WebSocket session and give a reason for doing so.
     *
     * @param s  Session to close
     * @param reason {@link CloseReason} the WebSocket is closing.
     */
    public void tryToClose(Session s, CloseReason reason) {
        try {
            s.close(reason);
        } catch (IOException e) {
            tryToClose(s);
        }
    }

    /**
     * Try to close a {@code Closeable} (usually once an error has already
     * occurred).
     *
     * @param c Closable to close
     */
    public void tryToClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e1) {
            }
        }
    }
}
