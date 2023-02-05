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

import alexnerd.content.content.control.ContentStore;
import alexnerd.content.content.control.Lang;
import alexnerd.content.content.control.Storage;
import alexnerd.content.content.entity.Content;
import alexnerd.content.content.entity.ContentType;
import alexnerd.content.metrics.ContentMetrics;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import java.nio.file.Files;

public class LivenessCheck {

    @Inject
    private Storage helper;

    @Inject
    private ContentMetrics contentMetrics;

    @Inject
    private ContentStore store;

    private final static String INITIAL_TITLE = "JavaNerd blog";
    private final static String INITIAL_DATE = "2016-1-1";

    @Inject
    @ConfigProperty(name = "minimum.storage.space", defaultValue = "50")
    private int storageThreshold;

    @Produces
    @Liveness
    public HealthCheck checkContentDirectoryExists() {
        return () -> HealthCheckResponse
                .named("content-directory-exists")
                .status(Files.exists(this.helper.getStorageDirectoryPath()))
                .build();
    }

    @Produces
    @Liveness
    public HealthCheck checkEnoughSpace() {
        long size = contentMetrics.getContentStorageSpaceInMB();
        boolean enoughSpace = size >= this.storageThreshold;
        return () -> HealthCheckResponse
                .named("content-directory-has-space")
                .status(enoughSpace)
                .build();
    }

    @Produces
    @Liveness
    public HealthCheck initialExists() {
        return () -> HealthCheckResponse.named("initial-post-exists")
                .status(this.postExist())
                .build();
    }

    private boolean postExist() {
        try {
            Content content = this.store.read(Lang.ru, ContentType.POST, INITIAL_DATE, INITIAL_TITLE);
            return content.title().equalsIgnoreCase(INITIAL_TITLE);
        } catch (Exception ex) {
            return false;
        }
    }
}
