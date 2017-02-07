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
package org.gameontext.sample;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.gameontext.sample.items.Items;
import org.gameontext.sample.jsr107lock.CacheBasedLock;
import org.gameontext.sample.jsr107secretCache.SecretDataBean;
import org.gameontext.sample.jsr107toggle.Toggle;
import org.gameontext.sample.map.client.MapClient;
import org.gameontext.sample.protocol.Message;
import org.gameontext.sample.protocol.RoomEndpoint;
import org.gameontext.sample.protocol.SessionSender;

/**
 * Here is where your room implementation lives. The WebSocket endpoint is
 * defined in {@link RoomEndpoint}, with {@link Message} as the text-based
 * payload being sent on the wire.
 * <p>
 * This is an ApplicationScoped CDI bean, which means it will be started when
 * the server/application starts, and stopped when it stops.
 *
 */
@ApplicationScoped
public class RoomImplementation {

    /**
     * The id of the room: you can retrieve this from the room editing view in
     * the UI
     */
    public static final String ROOM_ID = "bd66f1ad68a2993002364ebf1107478e";
    public static final String LOOK_UNKNOWN = "It doesn't look interesting";
    public static final String UNKNOWN_COMMAND = "This room is a basic model. It doesn't understand `%s`";
    public static final String UNSPECIFIED_DIRECTION = "You didn't say which way you wanted to go.";
    public static final String UNKNOWN_DIRECTION = "There isn't a door in that direction (%s)";
    public static final String GO_FORTH = "You head %s";
    public static final String HELLO_ALL = "%s is here";
    public static final String HELLO_USER = "Welcome!";
    public static final String GOODBYE_ALL = "%s has gone";
    public static final String GOODBYE_USER = "Bye!";

    @Inject
    protected MapClient mapClient;
    @Inject
    protected CacheBasedLock testLock;
    @Inject
    protected Items items;

    @Inject
    protected RoomDescription roomDescription;
    
    @Inject
    protected SecretDataBean secret;
    
    @Inject
    protected SessionSender sessionSender;
    
    @Inject
    protected Toggle toggle;

    @PostConstruct
    protected void postConstruct() {

        // Customize the room
        roomDescription.addCommand("/ping", "Does this work?");

        Log.log(Level.INFO, this, "Room initialized: {0}", roomDescription);
    }

    @PreDestroy
    protected void preDestroy() {
        Log.log(Level.FINE, this, "Room to be destroyed");
    }

    public void handleMessage(Message message) {

        // If this message isn't for this room, TOSS IT!
        if (!ROOM_ID.equals(message.getTargetId())) {
            Log.log(Level.FINEST, this, "Received message for the wrong room ({0}): {1}", message.getTargetId(),
                    message);
            return;
        }

        // Fetch the userId and the username of the sender.
        // The username can change overtime, so always use the sent username
        // when
        // constructing messages
        JsonObject messageBody = message.getParsedBody();
        String userId = messageBody.getString(Message.USER_ID);
        String username = messageBody.getString(Message.USERNAME);

        Log.log(Level.FINEST, this, "Received message from {0}({1}): {2}", username, userId, messageBody);

        // Who doesn't love switch on strings in Java 8?
        switch (message.getTarget()) {

        case roomHello:
            // roomHello,<roomId>,{
            // "username": "username",
            // "userId": "<userId>",
            // "version": 1|2
            // }
            // See RoomImplementationTest#testRoomHello*

            // Send location message
            sessionSender.sendMessage(Message.createLocationMessage(userId, roomDescription));

            // Say hello to a new person in the room
            sessionSender.sendMessage(Message.createBroadcastEvent(String.format(HELLO_ALL, username), userId, HELLO_USER));
            break;

        case roomJoin:
            // roomJoin,<roomId>,{
            // "username": "username",
            // "userId": "<userId>",
            // "version": 2
            // }
            // See RoomImplementationTest#testRoomJoin

            // Send location message
            sessionSender.sendMessage(Message.createLocationMessage(userId, roomDescription));

            break;

        case roomGoodbye:
            // roomGoodbye,<roomId>,{
            // "username": "username",
            // "userId": "<userId>"
            // }
            // See RoomImplementationTest#testRoomGoodbye

            // Say goodbye to person leaving the room
            sessionSender.sendMessage(Message.createBroadcastEvent(String.format(GOODBYE_ALL, username), userId, GOODBYE_USER));
            break;

        case roomPart:
            // room,<roomId>,{
            // "username": "username",
            // "userId": "<userId>"
            // }
            // See RoomImplementationTest#testRoomPart

            break;

        case room:
            // room,<roomId>,{
            // "username": "username",
            // "userId": "<userId>"
            // "content": "<message>"
            // }
            String content = messageBody.getString(Message.CONTENT);

            if (content.charAt(0) == '/') {
                // command
                processCommand(userId, username, content);
            } else {
                // See RoomImplementationTest#testHandleChatMessage

                // echo back the chat message
                sessionSender.sendMessage(Message.createChatMessage(username, content));
            }
            break;

        default:
            // unknown message type. discard, don't break.
            break;
        }
    }

    private void processCommand(String userId, String username, String content) {
        // Work mostly off of lower case.
        String contentToLower = content.toLowerCase(Locale.ENGLISH).trim();
        
        // let the items have first crack at parsing the command.
        boolean handled = items.processCommand(contentToLower,userId, username);
        
        if (!handled) {

            String firstWord;
            String remainder;
    
            int firstSpace = contentToLower.indexOf(' '); // find the first space
            if (firstSpace < 0 || contentToLower.length() <= firstSpace) {
                firstWord = contentToLower;
                remainder = null;
            } else {
                firstWord = contentToLower.substring(0, firstSpace);
                remainder = contentToLower.substring(firstSpace + 1);
            }

            switch (firstWord) {
            case "/go":
                // See RoomCommandsTest#testHandle*Go*
                // Always process the /go command.
                String exitId = getExitId(remainder);

                if (exitId == null) {
                    // Send error only to source session
                    if (remainder == null) {
                        sessionSender.sendMessage(Message.createSpecificEvent(userId, UNSPECIFIED_DIRECTION));
                    } else {
                        sessionSender.sendMessage(Message.createSpecificEvent(userId, String.format(UNKNOWN_DIRECTION, remainder)));
                    }
                } else {
                    // Allow the exit
                    sessionSender.sendMessage(Message.createExitMessage(userId, exitId,
                            String.format(GO_FORTH, prettyDirection(exitId))));
                }
                break;

            case "/look":
            case "/examine":
                // See RoomCommandsTest#testHandle*Look*

                // Treat look and examine the same (though you could make them
                // do different things)
                if (remainder == null || remainder.contains("room")) {
                    // This is looking at or examining the entire room. Send the
                    // player location message,
                    // which includes the room description and inventory
                    sessionSender.sendMessage(Message.createLocationMessage(userId, roomDescription));
                } else {
                    sessionSender.sendMessage(Message.createSpecificEvent(userId, LOOK_UNKNOWN));
                }
                break;

            case "/ping":
                // Custom command! /ping is added to the room description in the
                // @PostConstruct method
                // See RoomCommandsTest#testHandlePing*

                if (remainder == null) {
                    sessionSender.sendMessage(Message.createBroadcastEvent("Ping! Pong sent to " + username, userId, "Ping! Pong!"));
                } else {
                    sessionSender.sendMessage(Message.createBroadcastEvent(
                            "Ping! Pong sent to " + username + ": " + remainder, userId, "Ping! Pong! " + remainder));
                }

                break;
                
            case "/secret":
                if (remainder == null) {
                    String userSecret = secret.getSecretForUser(userId);
                    if (userSecret == null) {
                        sessionSender.sendMessage(Message.createSpecificEvent(userId, "You apparently don't have a secret at the moment. Maybe you should set one with /secret ilikepie"));
                    } else {
                        sessionSender.sendMessage(Message.createSpecificEvent(userId, "Your secret is currently '"+userSecret+"'"));
                    }
                } else {
                    secret.setSecretForUser(userId, remainder);
                    sessionSender.sendMessage(Message.createSpecificEvent(userId, "Your secret has been set to '"+remainder+"', it will expire in 5 minutes"));
                }
                break;
                
                
            case "/toggle":
                toggle.toggle();
                //no need for output, that should occur when the state listener hears the state change.
                break;

            case "/logs":
                sessionSender.sendMessage(Message.createSpecificEvent(userId, "LOGS::"));
                List<String> logs = Log.logPipe.getLogLines();
                for (String s : logs) {
                    sessionSender.sendMessage(Message.createSpecificEvent(userId, s));
                }
                break;

            default:
                sessionSender.sendMessage(Message.createSpecificEvent(userId, String.format(UNKNOWN_COMMAND, content)));
                break;
            }
        }
    }

    /**
     * Given a lower case string describing the direction someone wants to go
     * (/go N, or /go North), filter or transform that into a recognizable id
     * that can be used as an index into a known list of exits. Always valid are
     * n, s, e, w. If the string doesn't match a known exit direction, return
     * null.
     *
     * @param lowerDirection
     *            String read from the provided message
     * @return exit id or null
     */
    protected String getExitId(String lowerDirection) {
        if (lowerDirection == null) {
            return null;
        }

        switch (lowerDirection) {
        case "north":
        case "south":
        case "east":
        case "west":
            return lowerDirection.substring(0, 1);

        case "n":
        case "s":
        case "e":
        case "w":
            // Assume N/S/E/W are managed by the map service.
            return lowerDirection;

        default:
            // Otherwise unknown direction
            return null;
        }
    }

    /**
     * From the direction we used as a key
     * 
     * @param exitId
     *            The exitId in lower case
     * @return A pretty version of the direction for use in the exit message.
     */
    protected String prettyDirection(String exitId) {
        switch (exitId) {
        case "n":
            return "North";
        case "s":
            return "South";
        case "e":
            return "East";
        case "w":
            return "West";

        default:
            return exitId;
        }
    }
}
