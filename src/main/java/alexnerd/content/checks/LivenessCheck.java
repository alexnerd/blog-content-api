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

package alexnerd.content.checks;

import alexnerd.content.metrics.PostsMetrics;
import alexnerd.content.posts.control.Lang;
import alexnerd.content.posts.control.PostStore;
import alexnerd.content.posts.control.Storage;
import alexnerd.content.posts.entity.ContentType;
import alexnerd.content.posts.entity.Post;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import java.nio.file.Files;

public class LivenessCheck {

    @Inject
    Storage helper;

    @Inject
    PostsMetrics postsMetrics;

    @Inject
    PostStore store;

    private final static String INITIAL_TITLE = "JavaNerd blog";
    private final static String INITIAL_DATE = "2016-1-1";

    @Inject
    @ConfigProperty(name = "minimum.storage.space", defaultValue = "50")
    int storageThreshold;

    @Produces
    @Liveness
    public HealthCheck checkPostsDirectoryExists() {
        return () -> HealthCheckResponse
                .named("posts-directory-exists")
                .status(Files.exists(this.helper.getStorageDirectoryPath()))
                .build();
    }

    @Produces
    @Liveness
    public HealthCheck checkEnoughSpace() {
        long size = postsMetrics.getPostsStorageSpaceInMB();
        boolean enoughSpace = size >= this.storageThreshold;
        return () -> HealthCheckResponse
                .named("posts-directory-has-space")
                .status(enoughSpace)
                .build();
    }

    @Produces
    @Liveness
    public HealthCheck initialExists() {
        return () -> HealthCheckResponse.named("initial-post-exists")
                .status(this.postsExist())
                .build();
    }

    public boolean postsExist() {
        try {
            Post post = this.store.read(Lang.ru, ContentType.POST, INITIAL_DATE, INITIAL_TITLE);
            return post.title().equalsIgnoreCase(INITIAL_TITLE);
        } catch (Exception ex) {
            return false;
        }
    }
}
