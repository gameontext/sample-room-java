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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Exits {
    private Exit n;
    private Exit s;
    private Exit e;
    private Exit w;
    private Exit u;
    private Exit d;

    public Exit getN() {
        return n;
    }

    public void setN(Exit n) {
        this.n = n;
    }

    public Exit getS() {
        return s;
    }

    public void setS(Exit s) {
        this.s = s;
    }

    public Exit getE() {
        return e;
    }

    public void setE(Exit e) {
        this.e = e;
    }

    public Exit getW() {
        return w;
    }

    public void setW(Exit w) {
        this.w = w;
    }

    public Exit getU() {
        return u;
    }

    public void setU(Exit u) {
        this.u = u;
    }

    public Exit getD() {
        return d;
    }

    public void setD(Exit d) {
        this.d = d;
    }

    public Exit getExit(String direction) {
        switch (direction.toLowerCase()) {
            case "n": {
                return getN();
            }
            case "s": {
                return getS();
            }
            case "e": {
                return getE();
            }
            case "w": {
                return getW();
            }
            case "u": {
                return getU();
            }
            case "d": {
                return getD();
            }
            default: {
                // unknown exit.. return null;
                return null;
            }
        }
    }

    public JsonObject toSimpleJsonList() {

        JsonObjectBuilder content = Json.createObjectBuilder();
        if ( n != null) {
            content.add("N", n.getDoor());
        }

        if ( s != null) {
            content.add("S", s.getDoor());
        }

        if ( e != null ) {
            content.add("E", e.getDoor());
        }

        if ( w != null ) {
            content.add("W", w.getDoor());
        }

        if ( u != null ) {
            content.add("U", u.getDoor());
        }

        if ( d != null ) {
            content.add("D", d.getDoor());
        }

        return content.build();
    }

    @Override
    public String toString()  {
      StringBuilder sb = new StringBuilder();
      sb.append("Exits=");
      sb.append("[ n=").append(n);
      sb.append(", w=").append(w);
      sb.append(", s=").append(s);
      sb.append(", e=").append(e);
      sb.append(", u=").append(u);
      sb.append(", d=").append(d);
      sb.append("]");
      return sb.toString();
    }

}
