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

import alexnerd.content.content.control.enums.Lang;
import alexnerd.content.content.entity.enums.ContentType;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.FileNotFoundException;
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
    private String baseDir;

    private Path storageDirectoryPath;

    private final static int SEARCH_DEPTH = 3;


    @PostConstruct
    public void init() {
        this.storageDirectoryPath = Path.of(baseDir);
    }

    public Path getContentDirectoryPath(Lang lang, ContentType contentType) {
        return Path.of(baseDir)
                .resolve(lang.name())
                .resolve(contentType.getBaseDir());
    }

    public Path getStorageDirectoryPath() {
        return this.storageDirectoryPath;
    }

    String getContent(Lang lang, ContentType type, String date, String fileName) throws FileNotFoundException {
        Path contentPath = this.constructContentPath(lang, type, date, fileName);

        if (Files.notExists(contentPath) || !Files.isRegularFile(contentPath)) {
            throw new FileNotFoundException("Can't fetch content: " + fileName);
        }

        return this.readContent(contentPath);
    }

    List<String> getLastContent(Lang lang, ContentType contentType, int limit) {
        Path contentPath = this.getContentDirectoryPath(lang, contentType);

        return this.getLastContentPath(contentPath, SEARCH_DEPTH, limit).stream()
                .map(this::readContent)
                .collect(Collectors.toList());
    }

    private List<Path> getLastContentPath(Path currentDir, int searchDepth, int limit) {
        List<Path> lastCreated = new ArrayList<>();
        List<Path> directories = this.getDirectories(currentDir);
        if (searchDepth != 0) {
            --searchDepth;
            Iterator<Path> iterator = directories.iterator();
            while (iterator.hasNext()) {
                lastCreated.addAll(getLastContentPath(iterator.next(), searchDepth, limit - lastCreated.size()));
                if (lastCreated.size() == limit) {
                    break;
                }
            }
        } else {
            List<Path> content = this.getFiles(currentDir);
            lastCreated.addAll(content.stream()
                    .limit(limit - lastCreated.size())
                    .collect(Collectors.toList()));
        }
        return lastCreated;
    }

    private List<Path> getDirectories(Path path) {
        Comparator<Path> comparator = (Path p1, Path p2) -> Integer.valueOf(p2.getFileName().toString())
                .compareTo(Integer.valueOf(p1.getFileName().toString()));
        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.filter(Files::isDirectory)
                    .sorted(comparator)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ex) {
            throw new StorageException("Can't get directories from path: " + path, ex);
        }
    }

    private List<Path> getFiles(Path path) {
        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted((p1, p2) -> this.getCreationTime(p2).compareTo(this.getCreationTime(p1)))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ex) {
            throw new StorageException("Can't get files from path: " + path, ex);
        }
    }

    private FileTime getCreationTime(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException ex) {
            throw new StorageException("Can't read creation time from: " + path.getFileName(), ex);
        }
    }

    private String readContent(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            throw new StorageException("Can't read content from file: " + path.getFileName(), ex);
        }
    }

    private Path constructContentPath(Lang lang, ContentType type, String date, String fileName) {
        Path path = this.getStorageDirectoryPath()
            .resolve(lang.name())
            .resolve(type.getBaseDir());
        String[] split = date.split("-");
        for (String s : split) {
            path = path.resolve(s);
        }
        return path.resolve(fileName + ".json");
    }
}