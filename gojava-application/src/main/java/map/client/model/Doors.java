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
package map.client.model;

import java.util.HashMap;
import java.util.Map;

public class Doors {
    
    private Map<String, String> directions = new HashMap<String, String>();
    
    public String getN() {
        return directions.get("n");
    }

    public void setN(String n) {
        directions.put("n", n);
    }

    public String getS() {
        return directions.get("s");
    }

    public void setS(String s) {
        directions.put("s", s);
    }

    public String getE() {
        return directions.get("e");
    }

    public void setE(String e) {
        directions.put("e", e);
    }

    public String getW() {
        return directions.get("w");
    }

    public void setW(String w) {
        directions.put("w", w);
    }

    public String getU() {
        return directions.get("u");
    }

    public void setU(String u) {
        directions.put("u", u);
    }

    public String getD() {
        return directions.get("d");
    }

    public void setD(String d) {
        directions.put("d", d);
    }

    public String getDoor(String exitDirection) {
        return directions.get(exitDirection);
    }
}
