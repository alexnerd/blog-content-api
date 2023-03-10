/*
 * Copyright 2023 Aleksey Popov <alexnerd.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alexnerd.content.content.control.adapters;

import alexnerd.content.content.entity.Content;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

public interface JsonMapper {
    JsonbConfig config = new JsonbConfig()
            .withFormatting(true)
            .withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());

    static String save(Content content) {
        try (Jsonb jsonb = JsonbBuilder.create(config)) {
            return jsonb.toJson(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Content load(String stringified) {
        try (Jsonb jsonb = JsonbBuilder.create(config)) {
            return jsonb.fromJson(stringified, Content.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
