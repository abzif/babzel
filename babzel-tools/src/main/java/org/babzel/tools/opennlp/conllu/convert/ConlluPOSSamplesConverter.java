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
import opennlp.tools.postag.POSSample;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.babzel.tools.opennlp.conllu.parser.ConlluWordLine;
import org.babzel.tools.opennlp.conllu.util.ConlluNormalizer;
import org.babzel.tools.opennlp.conllu.util.ConlluValidator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConlluPOSSamplesConverter implements ConlluSamplesConverter<POSSample> {
    @NonNull
    private final ConlluNormalizer normalizer;
    @NonNull
    private final ConlluValidator validator;

    @Override
    public Seq<POSSample> convert(@NonNull Seq<ConlluSentence> sentences, @NonNull String language) {
        return sentences
                .map(sentence -> normalizer.normalizeAfterTokenization(sentence, language))
                .filter(validator::isValidForLemmatization)
                .map(this::convert);
    }

    private POSSample convert(ConlluSentence sentence) {
        var posTags = getPOSTags(sentence.getWords());
        return new POSSample(
                posTags.map(Tuple2::_1).toJavaList(),
                posTags.map(Tuple2::_2).toJavaList());
    }

    private Seq<Tuple2<String, String>> getPOSTags(Seq<ConlluWordLine> words) {
        return words.flatMap(this::getPOSTags);
    }

    private Seq<Tuple2<String, String>> getPOSTags(ConlluWordLine word) {
        return word.isCompound()
                ? word.getSubWords().map(subWord -> Tuple.of(subWord.getForm(), subWord.getPosTag()))
                : Vector.of(Tuple.of(word.getForm(), word.getPosTag()));
    }
}
