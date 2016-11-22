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
package org.gameontext.sample.map.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class MapResponseReader implements MessageBodyReader<MapData> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(MapData.class);
    }

    /**
     * We require a significant subset of the response from the Map service.
     * Look only at name, fullName, and description
     */
    @Override
    public MapData readFrom(Class<MapData> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                    throws IOException, WebApplicationException {

        JsonReader rdr = null;
        try {
            rdr = Json.createReader(entityStream);
            JsonStructure json = rdr.read();
            JsonObject returnedJson = (JsonObject) json;
            JsonObject info = returnedJson.getJsonObject("info");

            MapData data = new MapData();
            data.setName(sanitiseNull(info.getJsonString("name")));
            data.setFullName(sanitiseNull(info.getJsonString("fullName")));
            data.setDescription(sanitiseNull(info.getJsonString("description")));

            return data;
        } finally {
            if (rdr != null) {
                rdr.close();
            }
        }
    }

    private String sanitiseNull(JsonString jsonString) {
        return (jsonString == null) ? null : jsonString.getString();
    }

}
