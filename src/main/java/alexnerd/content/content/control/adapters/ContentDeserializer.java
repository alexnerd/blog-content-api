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
import alexnerd.content.content.entity.enums.ContentType;

public interface ContentDeserializer {

    static Content deserialize(String content, ContentType type) {
        return switch (type) {
            case POST, ARTICLE, ARTICLE_TEASER -> JsonMapper.load(content);
            case LAST_ARTICLES -> {
                Content post = JsonMapper.load(content);
                yield new Content(post.title(), ContentType.LAST_ARTICLES, null, null,
                        post.createDate(), post.link());
            }
            default -> throw new IllegalStateException("Unsupported content type: " + type);
        };
    }
}
