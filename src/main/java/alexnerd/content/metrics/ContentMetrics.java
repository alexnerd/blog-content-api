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

package alexnerd.content.metrics;

import alexnerd.content.content.control.StorageException;
import alexnerd.content.content.control.Storage;
import jakarta.inject.Inject;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import java.io.IOException;
import java.nio.file.Files;

public class ContentMetrics {
    @Inject
    private Storage storage;
    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    private MetricRegistry registry;

    public void increaseNotExistingContentCounter() {
        this.registry.counter("fetch_content_with_not_existing_title").inc();
    }

    @Gauge(unit = "mb")
    public long getContentStorageSpaceInMB() {
        try {
            return Files.getFileStore(this.storage.getStorageDirectoryPath()).getUsableSpace() / 1024 / 1024;
        } catch (IOException e) {
            throw new StorageException("Cannot fetch size information from " + this.storage.getStorageDirectoryPath(), e);
        }
    }

    public void increaseHitCounter(String title) {
        this.registry.counter("content_hits_" + title).inc();
    }
}
