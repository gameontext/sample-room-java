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

import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import org.gameontext.sample.protocol.Message;
import org.gameontext.sample.protocol.RoomEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class RoomImplementationTest {

    public static final String ROOM_ID = "roomId";
    public static final String TEST_ID = "testId";
    public static final String TEST_USERNAME = "testUser";

    @Rule
    public TestName testName = new TestName();

    private RoomImplementation roomImpl;
    private RoomDescription roomDescription;

    @Before
    public void before() {
        System.out.println(" ===== " + testName.getMethodName());

        roomImpl = new RoomImplementation();

        // Might need a mock of map client for postConstruct..

        roomImpl.postConstruct();
        roomDescription = roomImpl.roomDescription;
    }

    @Test
    public void testHandleMessageForDifferentRoom(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomMessage("NotMyId", TEST_ID, TEST_USERNAME, "Just chatting");

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            endpoint.sendMessage(session, message); times = 0;
        }};
    }

    @Test
    public void testHandleChatMessage(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomMessage(ROOM_ID, TEST_ID, TEST_USERNAME, "Just chatting");

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            Message m1;
            endpoint.sendMessage(session, m1 = withCapture()); times = 1;

            String s = m1.toString();
            System.out.println(s);

            Assert.assertTrue("Message should be directed to all users, and of type chat: " + s,
                    s.startsWith("player,*,{\"type\":\"chat\""));

            Assert.assertTrue("Message should contain simple chat content: " + s,
                    s.contains("\"content\":\"Just chatting\""));

            Assert.assertTrue("Message should include username: " + s,
                    s.contains("\"username\":\"testUser\""));

            Assert.assertFalse("Message should NOT include user id: " + s,
                    s.contains(TEST_ID));
        }};
    }

    @Test
    public void testRoomHelloV1(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomHello(ROOM_ID, TEST_ID, TEST_USERNAME, 1);

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            List<Message> messages = new ArrayList<>();
            endpoint.sendMessage(session, withCapture(messages)); times = 2;

            String s = messages.get(0).toString();
            System.out.println(s);
            RoomImplementationTest.assertLocationResponse(s, roomDescription);

            s = messages.get(1).toString();
            System.out.println(s);

            Assert.assertTrue("Message should be directed to all users, and of type event: " + s,
                    s.startsWith("player,*,{\"type\":\"event\""));

            Assert.assertTrue("Message should include content for all: " + s,
                    s.contains("\"*\":\"" + String.format(RoomImplementation.HELLO_ALL, TEST_USERNAME)));

            Assert.assertTrue("Message should include content for user: " + s,
                    s.contains("\"testId\":\"" + RoomImplementation.HELLO_USER));

        }};
    }

    @Test
    public void testRoomHelloV2(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomHello(ROOM_ID, TEST_ID, TEST_USERNAME, 2);

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            List<Message> messages = new ArrayList<>();
            endpoint.sendMessage(session, withCapture(messages)); times = 2;

            String s = messages.get(0).toString();
            System.out.println(s);
            RoomImplementationTest.assertLocationResponse(s, roomDescription);

            s = messages.get(1).toString();
            System.out.println(s);

            Assert.assertTrue("Message should be directed to all users, and of type event: " + s,
                    s.startsWith("player,*,{\"type\":\"event\""));

            Assert.assertTrue("Message should include content for all: " + s,
                    s.contains("\"*\":\"" + String.format(RoomImplementation.HELLO_ALL, TEST_USERNAME)));

            Assert.assertTrue("Message should include content for user: " + s,
                    s.contains("\"testId\":\"" + RoomImplementation.HELLO_USER));
        }};
    }

    @Test
    public void testRoomGoodbye(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomGoodbye(ROOM_ID, TEST_ID, TEST_USERNAME);

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            Message m1;
            endpoint.sendMessage(session, m1 = withCapture()); times = 1;

            String s = m1.toString();
            System.out.println(s);

            Assert.assertTrue("Message should be directed to all users, and of type event: " + s,
                    s.startsWith("player,*,{\"type\":\"event\""));

            Assert.assertTrue("Message should include content for all: " + s,
                    s.contains("\"*\":\"" + String.format(RoomImplementation.GOODBYE_ALL, TEST_USERNAME)));

            Assert.assertTrue("Message should include content for user: " + s,
                    s.contains("\"testId\":\"" + RoomImplementation.GOODBYE_USER));
        }};
    }

    @Test
    public void testRoomJoin(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomJoin(ROOM_ID, TEST_ID, TEST_USERNAME, 2);

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            Message m1;
            endpoint.sendMessage(session, m1 = withCapture()); times = 1;

            String s = m1.toString();
            System.out.println(s);

            RoomImplementationTest.assertLocationResponse(s, roomDescription);
        }};
    }

    @Test
    public void testRoomPart(@Mocked Session session, @Mocked RoomEndpoint endpoint) {
        Message message = Message.createRoomPart(ROOM_ID, TEST_ID, TEST_USERNAME);

        roomImpl.handleMessage(session, message, endpoint);

        new Verifications() {{
            endpoint.sendMessage(session, message); times = 0;
        }};
    }

    public static void assertLocationResponse(String s, RoomDescription roomDescription) {
        Assert.assertTrue("Message should be directed to specific user, and of type location: " + s,
                s.startsWith("player,testId,{\"type\":\"location\""));

        Assert.assertTrue("Message should contain room name: " + s,
                s.contains("\"name\":\"" + roomDescription.getName()));

        Assert.assertTrue("Message should contain room full name: " + s,
                s.contains("\"fullName\":\"" + roomDescription.getFullName()));

        Assert.assertTrue("Message should contain room description: " + s,
                s.contains("\"description\":\"" + roomDescription.getDescription()));

        if ( roomDescription.getCommands().isEmpty() ) {
            Assert.assertFalse("Message should NOT contain room commands: " + s,
                    s.contains("\"commands\""));
        } else {
            String commands = roomDescription.getCommands().toString();

            Assert.assertTrue("Message should contain room commands: " + s + ", expected=\"commands\":" + commands,
                    s.contains("\"commands\":"+commands));
        }

        if ( roomDescription.getInventory().isEmpty() ) {
            Assert.assertFalse("Message should NOT contain room inventory: " + s,
                    s.contains("\"roomInventory\""));
        } else {
            String items = roomDescription.getInventory().toString();

            Assert.assertTrue("Message should contain room commands: " + s + ", expected=\"roomInventory\":" + items,
                    s.contains("\"roomInventory\":"+items));
        }

        Assert.assertFalse("Message should not contain wildcard: " + s,
                s.contains("*"));
    }

}
