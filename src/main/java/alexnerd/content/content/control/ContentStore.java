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

import alexnerd.content.content.control.adapters.ContentDeserializer;
import alexnerd.content.content.control.enums.Lang;
import alexnerd.content.content.entity.Content;
import alexnerd.content.content.entity.enums.ContentType;
import alexnerd.content.metrics.ContentMetrics;
import jakarta.inject.Inject;

import java.io.FileNotFoundException;
import java.util.List;

public class ContentStore {

    @Inject
    private TitleNormalizer normalizer;

    @Inject
    private ContentMetrics contentMetrics;

    @Inject
    private Storage storage;

    public Content read(Lang lang, ContentType type, String date, String title) {
        String fileName = normalizer.normalize(title);
        try {
            String stringified = storage.getContent(lang, type, date, fileName);
            contentMetrics.increaseHitCounter(title);
            return ContentDeserializer.deserialize(stringified, type);
        } catch (FileNotFoundException ex) {
            contentMetrics.increaseNotExistingContentCounter();
            throw new StorageException(404, "Can't fetch content: " + fileName);
        }
    }

    public List<Content> readLast(Lang lang, ContentType type, int limit) {
        return storage.getLastContent(lang, type, limit)
                .stream()
                .map(string -> ContentDeserializer.deserialize(string, type))
                .toList();
    }
}
