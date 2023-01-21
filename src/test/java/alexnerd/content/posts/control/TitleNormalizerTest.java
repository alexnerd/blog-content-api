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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TitleNormalizerTest {
    private TitleNormalizer cut;

    @BeforeEach
    public void init() {
        this.cut = new TitleNormalizer();
        this.cut.titleSeparator = "-";
        this.cut.init();
    }

    public static TitleNormalizer create() {
        TitleNormalizerTest test = new TitleNormalizerTest();
        test.init();
        return test.cut;
    }

    @Test
    public void replaceInvalidCharacter() {
        String invalid = "hello%world";
        String expected = "hello-world";
        String actual = this.cut.normalize(invalid);
        assertEquals(expected, actual);
    }
}
