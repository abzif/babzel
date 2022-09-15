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

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.charfilter.MappingCharFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.icu.ICUNormalizer2CharFilterFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.springframework.stereotype.Component;

@Component
public class LuceneTextNormalizer implements TextNormalizer {
    private final Analyzer analyzer = createAnalyzer();

    @SneakyThrows
    private Analyzer createAnalyzer() {
        var builder = CustomAnalyzer.builder();
        builder = appendCharFilters(builder);
        builder = appendTokenizer(builder);
        return builder.build();
    }

    @SneakyThrows
    private CustomAnalyzer.Builder appendCharFilters(CustomAnalyzer.Builder builder) {
        return builder
                // lowercase
                .addCharFilter(ICUNormalizer2CharFilterFactory.class, "name", "nfkc_cf", "mode", "compose")
                // normalize, not perfect, some characters and ligatures are not handled
                .addCharFilter(ICUNormalizer2CharFilterFactory.class, "name", "nfc", "mode", "decompose")
                // normalize remaining characters and ligatures
                .addCharFilter(MappingCharFilterFactory.class, "mapping", "org/babzel/tools/util/fold-to-ascii.txt")
                // strip accents
                .addCharFilter(PatternReplaceCharFilterFactory.class, "pattern", "\\p{InCombiningDiacriticalMarks}", "replacement", "");
    }

    @SneakyThrows
    private CustomAnalyzer.Builder appendTokenizer(CustomAnalyzer.Builder builder) {
        return builder.withTokenizer(StandardTokenizerFactory.class);
    }

    @Override
    public String normalizeText(String text, String language) {
        return analyzer.normalize("", text).utf8ToString();
    }
}
