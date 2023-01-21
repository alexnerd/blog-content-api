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

import alexnerd.content.posts.entity.ContentType;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Storage {

    @Inject
    @ConfigProperty(name = "root.storage.dir")
    String baseDir;

    Path storageDirectoryPath;

    @PostConstruct
    public void init() {
        this.storageDirectoryPath = Path.of(baseDir);
    }

    public Path getContentDirectoryPath(Lang lang, ContentType contentType) {
        return Path.of(baseDir + "/" + lang + "/" + contentType.getBaseDir());
    }

    public Path getStorageDirectoryPath() {
        return this.storageDirectoryPath;
    }

    public void setStorageDir(String storageDir) {
        this.baseDir = storageDir;
    }


    public List<Path> getLastItemsPath(Lang lang, ContentType contentType, int searchDepth, int limit) {
        List<Path> lastCreated = new ArrayList<>();
        Path contentPath = this.getContentDirectoryPath(lang, contentType);
        try {
            this.getLastItems(contentPath, searchDepth, lastCreated, limit);
        } catch (IOException ex) {
            throw new StorageException("Can't fetch last posts", ex);
        }
        return lastCreated;
    }

    private void getLastItems(Path baseDir, int searchDepth, List<Path> lastCreated, int limit) throws IOException {
        List<Path> items = this.toList(baseDir);
        if (searchDepth != 0) {
            --searchDepth;
            Iterator<Path> iterator = items.iterator();
            while (iterator.hasNext()) {
                getLastItems(iterator.next(), searchDepth, lastCreated, limit);
                if (lastCreated.size() == limit) {
                    break;
                }
            }
        } else {
            List<Path> posts = this.toFiles(baseDir);
            posts.stream()
                    .limit(limit - lastCreated.size())
                    .forEach(lastCreated::add);
        }
    }

    private List<Path> toList(Path path) throws IOException {
        Comparator<Path> comparator = (Path p1, Path p2) -> Integer.valueOf(p2.getFileName().toString())
                .compareTo(Integer.valueOf(p1.getFileName().toString()));
        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.filter(Files::isDirectory)
                    .sorted(comparator)
                    //.forEachOrdered(l::add);
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ex) {
            throw new StorageException("Can't fetch last posts", ex);
        }
    }



    private List<Path> toFiles(Path path) {
        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(this::compareCreationTime)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ex) {
            throw new StorageException("Can't fetch last posts", ex);
        }
    }

    private FileTime getCreationTime(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException ex) {
            throw new RuntimeException("Can't read creation time from: " + path.getFileName(), ex);
        }
    }

    private int compareCreationTime(Path p1, Path p2) {
        return this.getCreationTime(p2).
                compareTo(this.getCreationTime(p1));
    }
}