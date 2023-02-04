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

package alexnerd.content.content.control;

import alexnerd.content.content.control.adapters.Mapper;
import alexnerd.content.content.entity.Content;
import alexnerd.content.content.entity.ContentType;
import alexnerd.content.metrics.ContentMetrics;
import jakarta.inject.Inject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContentStore {

    @Inject
    TitleNormalizer normalizer;

    @Inject
    ContentMetrics contentMetrics;

    @Inject
    Storage storage;

    public ContentStore() {
    }

    public ContentStore(TitleNormalizer normalizer, ContentMetrics contentMetrics, Storage storage) {
        this.normalizer = normalizer;
        this.contentMetrics = contentMetrics;
        this.storage = storage;
    }

    public Content read(Lang lang, ContentType type, String date, String title) {
        String fileName = this.normalizer.normalize(title);
        contentMetrics.increaseHitCounter(title);

        try {
            String stringified = storage.getContent(lang, type, date, fileName);

            return this.deserializeContent(stringified, type);
        } catch (FileNotFoundException ex) {
            this.contentMetrics.increaseNotExistingContentCounter();
            throw new StorageException(404, "Can't fetch content: " + fileName);
        }
    }

    public List<Content> readLast(Lang lang, ContentType type, int limit) {
        return this.storage.getLastContent(lang, type, limit)
                .stream()
                .map(string -> this.deserializeContent(string, type))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    Content deserializeContent(String content, ContentType type) {
        return switch (type) {
            case POST, ARTICLE, ARTICLE_TEASER -> Mapper.load(content);
            case LAST_ARTICLES -> {
                Content post = Mapper.load(content);
                yield new Content(post.title(), ContentType.LAST_ARTICLES, null, null,
                        post.createDate(), post.link());
            }
            default -> throw new StorageException(422, "Unsupported content type: " + type);
        };
    }
}
