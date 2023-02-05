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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class TitleNormalizer {

    @Inject
    @ConfigProperty(name = "title.separator", defaultValue = "-")
    String titleSeparator;

    private int codePointSeparator;

    @PostConstruct
    public void init() {
        this.codePointSeparator = this.titleSeparator.codePoints()
                .findFirst()
                .orElseThrow();
    }

    public String normalize(String title) {
        return title.codePoints()
                .map(this::replaceDigitOrLetter)
                .collect(StringBuffer::new, StringBuffer::appendCodePoint, StringBuffer::append)
                .toString();
    }

    private int replaceDigitOrLetter(int codePoint) {
        if (Character.isLetterOrDigit(codePoint)) {
            return codePoint;
        } else {
            return this.codePointSeparator;
        }
    }
}
