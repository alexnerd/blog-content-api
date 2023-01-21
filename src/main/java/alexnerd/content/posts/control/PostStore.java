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

package alexnerd.content.posts.control;

import alexnerd.content.metrics.PostsMetrics;
import alexnerd.content.posts.control.adapters.Mapper;
import alexnerd.content.posts.entity.ContentType;
import alexnerd.content.posts.entity.Post;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostStore {

    @Inject
    TitleNormalizer normalizer;

    @Inject
    PostsMetrics postsMetrics;

    @Inject
    Storage storage;

    private final static int SEARCH_DEPTH = 3;

    public PostStore() {
    }

    public PostStore(TitleNormalizer normalizer, PostsMetrics postsMetrics, Storage storage) {
        this.normalizer = normalizer;
        this.postsMetrics = postsMetrics;
        this.storage = storage;
    }

    public Post read(Lang lang, ContentType type, String date, String title) {
        String fileName = this.normalizer.normalize(title);
        postsMetrics.increaseHitCounter(title);

        String postPath = this.getPostPath(lang, type, date, fileName);

        if (!this.fileExists(postPath)) {
            this.postsMetrics.increaseNotExistingPostCounter();
            throw new StorageException(404, "Can't fetch post: " + fileName);
        }
        String stringified = this.readString(postPath);
        return this.deserializeContent(stringified, type);
    }

    public List<Post> readLast(Lang lang, ContentType type, int limit) {
        return this.storage.getLastItemsPath(lang, type, SEARCH_DEPTH, limit)
                .stream()
                .map(this::readString)
                .map(s -> this.deserializeContent(s, type))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    boolean fileExists(String fileName) {
        Path fqn = this.storage.getStorageDirectoryPath().resolve(fileName);
        return Files.exists(fqn);
    }

    Post deserializeContent(String stringified, ContentType type) {
        return switch (type) {
            case POST, ARTICLE, ARTICLE_TEASER -> Mapper.load(stringified);
            case LAST_ARTICLES -> {
                Post post = Mapper.load(stringified);
                yield new Post(post.title(), ContentType.LAST_ARTICLES, null, null,
                        post.createDate(), post.link());
            }
            default -> throw new IllegalStateException("Unsupported content type: " + type);
        };
    }

    String readString(String fileName) {
        Path path = this.storage.getStorageDirectoryPath().resolve(fileName);
        return this.readString(path);
    }

    String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            throw new StorageException("Can't read post " + path.getFileName(), ex);
        }
    }

    String getPostPath(Lang lang, ContentType type, String date, String fileName) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(lang);
        strBuilder.append("/");
        strBuilder.append(type.getBaseDir());
        strBuilder.append("/");
        String[] split = date.split("-");
        for (String s : split) {
            strBuilder.append(s);
            strBuilder.append("/");
        }
        strBuilder.append(fileName);
        strBuilder.append(".json");
        return strBuilder.toString();
    }
}
