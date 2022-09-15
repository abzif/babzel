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
import io.vavr.Tuple3;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import opennlp.tools.lemmatizer.LemmaSample;
import static org.assertj.core.api.Assertions.assertThat;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.babzel.tools.opennlp.conllu.parser.ConlluWordLine;
import org.babzel.tools.opennlp.conllu.util.ConlluNormalizer;
import org.babzel.tools.opennlp.conllu.util.ConlluValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConlluLemmaSamplesConverterTest {
    @Mock
    private ConlluNormalizer normalizer;
    @Mock
    private ConlluValidator validator;
    @InjectMocks
    private ConlluLemmaSamplesConverter converter;

    @Test
    public void convert() {
        var sentence = new ConlluSentence("", Vector.empty());
        var normSentence = new ConlluSentence("Some example sentence ten example.", Vector.of(
                new ConlluWordLine(1, 2, "Some", Vector.of(
                        new ConlluWordLine(1, "Somt", "l1", "ADJ"),
                        new ConlluWordLine(2, "mex", "l2", "ADV"))),
                new ConlluWordLine(3, "example", "l3", "VERB"),
                new ConlluWordLine(4, 6, "sentence", Vector.of(
                        new ConlluWordLine(4, "sen", "l4", "NOUN"),
                        new ConlluWordLine(5, "ten", "l5", "PROPN"),
                        new ConlluWordLine(6, "ce", "l6", "AUX"))),
                new ConlluWordLine(7, "ten", "l7", "NOUN"),
                new ConlluWordLine(8, "example", "l8", "VERB"),
                new ConlluWordLine(9, ".", "l9", "PUNCT")));
        given(normalizer.normalizeSentence(any(), any())).willReturn(normSentence);
        given(validator.isValidForLemmatization(any())).willReturn(true);

        var samples = converter.convert(Vector.of(sentence), "lx");

        verify(normalizer).normalizeSentence(sentence, "lx");
        verify(validator).isValidForLemmatization(normSentence);
        verifyNoMoreInteractions(normalizer, validator);
        assertThat(samples).isEqualTo(Vector.of(createSample(Vector.of(
                Tuple.of("Somt mex", "ADJ ADV", "l1 l2"),
                Tuple.of("example", "VERB", "l3"),
                Tuple.of("sen", "NOUN", "l4"),
                Tuple.of("ten", "PROPN", "l5"),
                Tuple.of("ce", "AUX", "l6"),
                Tuple.of("ten", "NOUN", "l7"),
                Tuple.of("example", "VERB", "l8"),
                Tuple.of(".", "PUNCT", "l9")))));
    }

    @Test
    public void convertIncorrect() {
        var sentence1 = new ConlluSentence("1", Vector.empty());
        var normSentence1 = new ConlluSentence("n1", Vector.empty());
        var sentence2 = new ConlluSentence("2", Vector.empty());
        var normSentence2 = new ConlluSentence("n2", Vector.empty());
        given(normalizer.normalizeSentence(any(), any())).willReturn(normSentence1, normSentence2);
        given(validator.isValidForLemmatization(any())).willReturn(false);

        var samples = converter.convert(Vector.of(sentence1, sentence2), "lx");

        verify(normalizer).normalizeSentence(sentence1, "lx");
        verify(normalizer).normalizeSentence(sentence2, "lx");
        verify(validator).isValidForLemmatization(normSentence1);
        verify(validator).isValidForLemmatization(normSentence2);
        verifyNoMoreInteractions(normalizer, validator);
        assertThat(samples).isEqualTo(Vector.empty());
    }

    private LemmaSample createSample(Seq<Tuple3<String, String, String>> lemmas) {
        return new LemmaSample(
                lemmas.map(Tuple3::_1).toJavaList(),
                lemmas.map(Tuple3::_2).toJavaList(),
                lemmas.map(Tuple3::_3).toJavaList());
    }
}
