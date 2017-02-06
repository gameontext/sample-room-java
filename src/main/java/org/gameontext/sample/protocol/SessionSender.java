package org.gameontext.sample.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.gameontext.sample.Log;

@ApplicationScoped
public class SessionSender {

    private final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    public void addSession(Session session){
        sessions.add(session);
    }

    public void removeSession(Session session){
        sessions.remove(session);
    }
    
    /**
     * Utility method to simplify sending responses to rooms/users.
     * @param messageForUser
     * @param messageForRoom
     */
    public void sendResponseToRoom(String messageForUser, String messageForRoom, String userId){
        Message toSend = null;
        if(messageForRoom!=null )
        {
            if(messageForUser!=null){
                toSend = Message.createBroadcastEvent(messageForRoom, userId, messageForUser);
            }else{
                toSend = Message.createBroadcastEvent(messageForRoom);
            }
        }else{
            if(messageForUser!=null){
                toSend = Message.createSpecificEvent(userId, messageForUser);
            }else{
                throw new IllegalArgumentException("Cannot have both user and room message as null!!");
            }
            
        }
        sendMessage(toSend);
    }

    /**
     * Simple broadcast: loop over all mentioned sessions to send the message
     * <p>
     * We are effectively always broadcasting: a player could be connected to
     * more than one device, and that could correspond to more than one
     * connected session. Allow topic filtering on the receiving side (Mediator
     * and browser) to filter out and display messages.
     *
     * @param session
     *            Target session (used to find all related sessions)
     * @param message
     *            Message to send
     * @see #sendRemoteTextMessage(Session, Message)
     */
    public void sendMessage(Message message) {
        if (sessions.isEmpty())
            return;
        else {
            Session session = sessions.iterator().next();
            if (session != null) {
                for (Session s : session.getOpenSessions()) {
                    sendMessageToSession(s, message);
                }
            }
        }
    }

    /**
     * Try sending the {@link Message} using {@link Session#getBasicRemote()},
     * {@link Basic#sendObject(Object)}.
     *
     * @param session
     *            Session to send the message on
     * @param message
     *            Message to send
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
                Log.log(Level.FINE, RoomEndpoint.class, "Unexpected condition writing message", e);
            } catch (IOException ioe) {
                // An IOException, on the other hand, suggests the connection is
                // in a bad state.
                Log.log(Level.FINE, RoomEndpoint.class, "Unexpected condition writing message", ioe);
                tryToClose(session, new CloseReason(CloseCodes.UNEXPECTED_CONDITION, trimReason(ioe.toString())));
            }
        }
        return false;
    }

    /**
     * @param message
     *            String to trim
     * @return a string no longer than 123 characters (limit of value length for
     *         {@code CloseReason})
     */
    private String trimReason(String message) {
        return message.length() > 123 ? message.substring(0, 123) : message;
    }

    /**
     * Try to close the WebSocket session and give a reason for doing so.
     *
     * @param s
     *            Session to close
     * @param reason
     *            {@link CloseReason} the WebSocket is closing.
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
     * @param c
     *            Closable to close
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
