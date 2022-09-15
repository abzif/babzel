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

import io.vavr.collection.Vector;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.util.Span;
import static org.assertj.core.api.Assertions.assertThat;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.babzel.tools.opennlp.conllu.util.ConlluNormalizer;
import org.babzel.tools.opennlp.conllu.util.ConlluValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConlluSentenceSamplesConverterTest {
    @Mock
    private ConlluNormalizer normalizer;
    @Mock
    private ConlluValidator validator;
    @InjectMocks
    private ConlluSentenceSamplesConverter converter;

    @Test
    public void convert() {
        var sentences = Vector.of(
                new ConlluSentence("1", Vector.empty()),
                new ConlluSentence("2", Vector.empty()),
                new ConlluSentence("3", Vector.empty()),
                new ConlluSentence("4", Vector.empty()),
                new ConlluSentence("5", Vector.empty()),
                new ConlluSentence("6", Vector.empty()),
                new ConlluSentence("7", Vector.empty()),
                new ConlluSentence("8", Vector.empty()),
                new ConlluSentence("9", Vector.empty()),
                new ConlluSentence("10", Vector.empty()),
                new ConlluSentence("11", Vector.empty()),
                new ConlluSentence("12", Vector.empty()),
                new ConlluSentence("13", Vector.empty()));
        given(normalizer.normalizeSentence(any(), any()))
                .willAnswer(answer -> new ConlluSentence("n" + answer.getArgument(0, ConlluSentence.class).getText(), Vector.empty()));
        given(validator.isValidForTokenization(any())).willReturn(true);

        var sample = converter.convert(sentences, "lx");

        verify(normalizer, times(13)).normalizeSentence(any(), eq("lx"));
        verify(validator, times(13)).isValidForTokenization(any());
        verifyNoMoreInteractions(normalizer, validator);
        assertThat(sample).isEqualTo(Vector.of(
                new SentenceSample("n1 n2 n3 n4 n5 n6 n7 n8 n9 n10",
                        new Span(0, 2), new Span(3, 5), new Span(6, 8), new Span(9, 11), new Span(12, 14),
                        new Span(15, 17), new Span(18, 20), new Span(21, 23), new Span(24, 26), new Span(27, 30)),
                new SentenceSample("n11 n12 n13",
                        new Span(0, 3), new Span(4, 7), new Span(8, 11))));
    }

    @Test
    public void convertIncorrect() {
        var sentence1 = new ConlluSentence("1", Vector.empty());
        var normSentence1 = new ConlluSentence("n1", Vector.empty());
        var sentence2 = new ConlluSentence("2", Vector.empty());
        var normSentence2 = new ConlluSentence("n2", Vector.empty());
        given(normalizer.normalizeSentence(any(), any())).willReturn(normSentence1, normSentence2);
        given(validator.isValidForTokenization(any())).willReturn(false);

        var samples = converter.convert(Vector.of(sentence1, sentence2), "lx");

        verify(normalizer).normalizeSentence(sentence1, "lx");
        verify(normalizer).normalizeSentence(sentence2, "lx");
        verify(validator).isValidForTokenization(normSentence1);
        verify(validator).isValidForTokenization(normSentence2);
        verifyNoMoreInteractions(normalizer, validator);
        assertThat(samples).isEqualTo(Vector.empty());
    }
}
