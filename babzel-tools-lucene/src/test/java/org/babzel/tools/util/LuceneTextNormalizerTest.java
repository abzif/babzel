/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.babzel.tools.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class LuceneTextNormalizerTest {
    private LuceneTextNormalizer normalizer = new LuceneTextNormalizer();

    @Test
    public void normalizeBeforeTokenizer() {
        assertThat(normalizer.normalizeBeforeTokenizer("x-Æ-Ꜵ-ąęŁł-ÄØÖ", "lx")).isEqualTo("x-ae-ao-aell-aoo");
    }

    @Test
    public void normalizeAfterTokenizer() {
        assertThat(normalizer.normalizeAfterTokenizer("x-Æ-Ꜵ-ąęŁł-ÄØÖ", "lx")).isEqualTo("x-ae-ao-aell-aoo");
    }
}
