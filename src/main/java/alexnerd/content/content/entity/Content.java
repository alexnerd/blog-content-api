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

package alexnerd.content.content.entity;

import alexnerd.content.content.control.adapters.PrivateVisibilityStrategy;
import alexnerd.content.content.entity.enums.ContentType;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbVisibility;

@JsonbVisibility(PrivateVisibilityStrategy.class)
public record Content(String title, ContentType type, String content, String rubric, String createDate, String link) {
    @JsonbCreator
    public static Content create(@JsonbProperty("title") String title, @JsonbProperty("type") ContentType type,
                                 @JsonbProperty("content") String content, @JsonbProperty("rubric") String rubric,
                                 @JsonbProperty("createDate") String createDate, @JsonbProperty("link") String link) {
        return new Content(title, type, content, rubric, createDate, link);
    }
}
