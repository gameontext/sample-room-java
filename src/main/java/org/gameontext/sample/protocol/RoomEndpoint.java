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
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.gameontext.sample.Log;
import org.gameontext.sample.RoomImplementation;

/**
 * This is the WebSocket endpoint for a room. Java EE WebSockets
 * use simple annotations for event driven methods. An instance of this class
 * will be created for every connected client.
 * https://book.gameontext.org/microservices/WebSocketProtocol.html
 */
@ServerEndpoint(value = "/room", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class RoomEndpoint {

    @Inject
    protected RoomImplementation roomImplementation;

    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        Log.log(Level.FINE, this, "A new connection has been made to the room.");

        // All we have to do in onOpen is send the acknowledgement
        sendMessage(session, Message.ACK_MSG);
    }

    @OnClose
    public void onClose(Session session, CloseReason r) {
        Log.log(Level.FINE, this, "A connection to the room has been closed with reason " + r);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        Log.log(Level.FINE, this, "A problem occurred on connection", t);

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
        roomImplementation.handleMessage(session, message, this);
    }

    /**
     * Simple broadcast: loop over all mentioned sessions to send the message
     * <p>
     * We are effectively always broadcasting: a player could be connected
     * to more than one device, and that could correspond to more than one connected
     * session. Allow topic filtering on the receiving side (Mediator and browser)
     * to filter out and display messages.
     *
     * @param session Target session (used to find all related sessions)
     * @param message Message to send
     * @see #sendRemoteTextMessage(Session, Message)
     */
    public void sendMessage(Session session, Message message) {
        for (Session s : session.getOpenSessions()) {
            sendMessageToSession(s, message);
        }
    }

    /**
     * Try sending the {@link Message} using
     * {@link Session#getBasicRemote()}, {@link Basic#sendObject(Object)}.
     *
     * @param session Session to send the message on
     * @param message Message to send
     * @return true if send was successful, or false if it failed
     */
    private boolean sendMessageToSession(Session session, Message message) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendObject(message);
                return true;
            } catch (EncodeException e) {
                // Something was wrong encoding this message, but the connection
                // is likely just fine.
                Log.log(Level.FINE, this, "Unexpected condition writing message", e);
            } catch (IOException ioe) {
                // An IOException, on the other hand, suggests the connection is
                // in a bad state.
                Log.log(Level.FINE, this, "Unexpected condition writing message", ioe);
                tryToClose(session, new CloseReason(CloseCodes.UNEXPECTED_CONDITION, trimReason(ioe.toString())));
            }
        }
        return false;
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
