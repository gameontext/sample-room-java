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

import org.gameontext.sample.RoomDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * What goes in, must come out?
 *
 */
public class MessageTest {

    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() {
        System.out.println(" ===== " + testName.getMethodName());
    }

    @Test
    public void testCreateSpecificEventMessage() throws Exception {
        Message m1 = Message.createSpecificEvent("user1", "Message for user1");
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"event\",\"content\":{\"user1\""));
        Assert.assertFalse(s, s.contains("*"));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateBroadcastEventMessage() throws Exception {
        Message m1 = Message.createBroadcastEvent("EVERYTHING", "user1", "Message for user1");
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,*,{\"type\":\"event\",\"content\":{"));
        Assert.assertTrue(s, s.contains("\"*\":\"EVERYTHING\""));
        Assert.assertTrue(s, s.contains("\"user1\":\"Message for user1\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateBroadcastEventMessageGeneralOnly() throws Exception {
        Message m1 = Message.createBroadcastEvent("EVERYTHING");
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,*,{\"type\":\"event\",\"content\":{"));
        Assert.assertTrue(s, s.contains("\"*\":\"EVERYTHING\""));
        Assert.assertFalse(s, s.contains("\"user1\":\"Message for user1\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateBroadcastEventMessageMismatch() throws Exception {
        Message m1 = Message.createBroadcastEvent("EVERYTHING","user1");
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,*,{\"type\":\"event\",\"content\":{"));
        Assert.assertTrue(s, s.contains("\"*\":\"EVERYTHING\""));
        Assert.assertFalse(s, s.contains("\"user1\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateChatMessage() throws Exception {
        Message m1 = Message.createChatMessage("userName", "Message from userName");
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,*,{\"type\":\"chat\""));
        Assert.assertTrue(s, s.contains("\"content\":\"Message from userName\""));
        Assert.assertTrue(s, s.contains("\"username\":\"userName\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateLocationMessageMinimal() throws Exception {
        RoomDescription roomDescription = new RoomDescription();

        Message m1 = Message.createLocationMessage("user1", roomDescription);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"location\""));
        Assert.assertTrue(s, s.contains("\"name\":\""+roomDescription.getName()+"\""));
        Assert.assertTrue(s, s.contains("\"fullName\":\""+roomDescription.getFullName()+"\""));
        Assert.assertTrue(s, s.contains("\"description\":\""+roomDescription.getDescription()+"\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateLocationMessageFailNullName() throws Exception {
        RoomDescription data = new RoomDescription();
        data.setName(null);
        data.setFullName("b");
        data.setDescription("c");

        Message m1 = Message.createLocationMessage("user1", data);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"location\""));
        Assert.assertTrue(s, s.contains("\"name\":\""+data.getName()+"\""));
        Assert.assertTrue(s, s.contains("\"fullName\":\"b\""));
        Assert.assertTrue(s, s.contains("\"description\":\"c\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateLocationMessageFailNullFullName() throws Exception {
        RoomDescription data = new RoomDescription();
        data.setName("a");
        data.setFullName(null);
        data.setDescription("c");

        Message m1 = Message.createLocationMessage("user1", data);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"location\""));
        Assert.assertTrue(s, s.contains("\"name\":\"a\""));
        Assert.assertTrue(s, s.contains("\"fullName\":\""+data.getFullName()+"\""));
        Assert.assertTrue(s, s.contains("\"description\":\"c\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test
    public void testCreateLocationMessageFailNullDescription() throws Exception {
        RoomDescription data = new RoomDescription();
        data.setName("a");
        data.setFullName("b");
        data.setDescription(null);

        Message m1 = Message.createLocationMessage("user1", data);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"location\""));
        Assert.assertTrue(s, s.contains("\"name\":\"a\""));
        Assert.assertTrue(s, s.contains("\"fullName\":\"b\""));
        Assert.assertTrue(s, s.contains("\"description\":\""+data.getDescription()+"\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLocationMessageBadCommands() throws Exception {
        RoomDescription roomDescription = new RoomDescription();
        roomDescription.addCommand("/command", null);
    }

    @Test
    public void testCreateLocationMessageCommands() throws Exception {
        RoomDescription roomDescription = new RoomDescription();
        roomDescription.addCommand("/command", "description");

        Message m1 = Message.createLocationMessage("user1", roomDescription);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"location\""));
        Assert.assertTrue(s, s.contains("\"name\":\""+roomDescription.getName()+"\""));
        Assert.assertTrue(s, s.contains("\"fullName\":\""+roomDescription.getFullName()+"\""));
        Assert.assertTrue(s, s.contains("\"description\":\""+roomDescription.getDescription()+"\""));
        Assert.assertTrue(s, s.contains("\"commands\":{\"/command\":\"description\"}"));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }


    @Test
    public void testCreateLocationMessageInventory() throws Exception {
        RoomDescription roomDescription = new RoomDescription();
        roomDescription.addItem("Squashy Chair");

        Message m1 = Message.createLocationMessage("user1", roomDescription);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("player,user1,{\"type\":\"location\""));
        Assert.assertTrue(s, s.contains("\"name\":\""+roomDescription.getName()+"\""));
        Assert.assertTrue(s, s.contains("\"fullName\":\""+roomDescription.getFullName()+"\""));
        Assert.assertTrue(s, s.contains("\"description\":\""+roomDescription.getDescription()+"\""));
        Assert.assertTrue(s, s.contains("\"roomInventory\":[\"Squashy Chair\"]"));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }


    @Test
    public void testCreatePlayerLocationMessage() throws Exception {
        Message m1 = Message.createExitMessage("user1", "N", "So long, and thanks for all the fish");
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("playerLocation,user1,{\"type\":\"exit\""));
        Assert.assertTrue(s, s.contains("\"content\":\"So long, and thanks for all the fish\""));
        Assert.assertTrue(s, s.contains("\"exitId\":\"N\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePlayerLocationMessageNullDirection() throws Exception {
        Message.createExitMessage("user1", null, "So long, and thanks for all the fish");
    }


    @Test
    public void testCreatePlayerLocationMessageNullMessage() throws Exception {
        Message m1 = Message.createExitMessage("user1", "N", null);
        String s = m1.encode();
        System.out.println(s);

        Assert.assertTrue(s, s.startsWith("playerLocation,user1,{\"type\":\"exit\""));
        Assert.assertTrue(s, s.contains("\"content\":\"Fare thee well\""));
        Assert.assertTrue(s, s.contains("\"exitId\":\"N\""));

        Message m2 = new Message(s);
        Assert.assertEquals(m1, m2);
    }
}
