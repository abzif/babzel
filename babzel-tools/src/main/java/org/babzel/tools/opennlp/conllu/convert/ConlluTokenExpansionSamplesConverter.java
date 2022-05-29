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
package org.babzel.tools.opennlp.conllu.convert;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import opennlp.tools.lemmatizer.LemmaSample;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.babzel.tools.opennlp.conllu.parser.ConlluWordLine;
import org.babzel.tools.opennlp.conllu.util.ConlluNormalizer;
import org.babzel.tools.opennlp.conllu.util.ConlluValidator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConlluTokenExpansionSamplesConverter implements ConlluSamplesConverter<LemmaSample> {
    @NonNull
    private final ConlluNormalizer normalizer;
    @NonNull
    private final ConlluValidator validator;

    @Override
    public Seq<LemmaSample> convert(@NonNull Seq<ConlluSentence> sentences, @NonNull String language) {
        return sentences
                .map(sentence -> normalizer.normalizeAfterTokenization(sentence, language))
                .filter(validator::isValidForTokenization)
                .map(this::convert);
    }

    private LemmaSample convert(ConlluSentence sentence) {
        var expansions = getExpansions(sentence.getWords());
        return new LemmaSample(
                expansions.map(Tuple2::_1).toJavaList(),
                Vector.fill(expansions.size(), "X").toJavaList(),
                expansions.map(Tuple2::_2).toJavaList());
    }

    private Seq<Tuple2<String, String>> getExpansions(Seq<ConlluWordLine> words) {
        return words.flatMap(this::getExpansions);
    }

    private Seq<Tuple2<String, String>> getExpansions(ConlluWordLine word) {
        return word.isCompound()
                ? getCompoundWordExpansions(word)
                : Vector.of(Tuple.of(word.getForm(), word.getForm()));
    }

    private Seq<Tuple2<String, String>> getCompoundWordExpansions(ConlluWordLine word) {
        return !word.getForm().equals(joinSubForms(word, ""))
                ? Vector.of(Tuple.of(word.getForm(), joinSubForms(word, " ")))
                : word.getSubWords().map(subWord -> Tuple.of(subWord.getForm(), subWord.getForm()));

    }

    private String joinSubForms(ConlluWordLine word, String delimiter) {
        return word.getSubWords().map(ConlluWordLine::getForm).mkString(delimiter);
    }
}
