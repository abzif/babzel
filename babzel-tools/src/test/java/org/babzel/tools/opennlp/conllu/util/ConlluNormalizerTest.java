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
package org.babzel.tools.opennlp.conllu.util;

import io.vavr.collection.Vector;
import static org.assertj.core.api.Assertions.assertThat;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.babzel.tools.opennlp.conllu.parser.ConlluWordLine;
import org.babzel.tools.util.TextNormalizer;
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
public class ConlluNormalizerTest {
    @Mock
    private TextNormalizer textNormalizer;
    @InjectMocks
    private ConlluNormalizer normalizer;

    @Test
    public void normalizeBeforeTokenization() {
        given(textNormalizer.normalizeBeforeTokenizer(any(), any())).willAnswer(answer -> answer.getArgument(0).toString().replace("x", "X"));
        var sentence = new ConlluSentence("x sentence x", Vector.of(
                new ConlluWordLine(1, 2, "fx12", Vector.of(
                        new ConlluWordLine(1, "fx1", "lx1", "ADJ"),
                        new ConlluWordLine(2, "fx2", "lx2", "ADV"))),
                new ConlluWordLine(3, "fx3", "lx3", "VERB")));

        var actual = normalizer.normalizeBeforeTokenization(sentence, "lx");

        verify(textNormalizer, times(8)).normalizeBeforeTokenizer(any(), eq("lx"));
        verifyNoMoreInteractions(textNormalizer);
        assertThat(actual).isEqualTo(new ConlluSentence("X sentence X", Vector.of(
                new ConlluWordLine(1, 2, "fX12", Vector.of(
                        new ConlluWordLine(1, "fX1", "lX1", "ADJ"),
                        new ConlluWordLine(2, "fX2", "lX2", "ADV"))),
                new ConlluWordLine(3, "fX3", "lX3", "VERB"))));
    }

    @Test
    public void normalizeAfterTokenization() {
        given(textNormalizer.normalizeAfterTokenizer(any(), any())).willAnswer(answer -> answer.getArgument(0).toString().replace("x", "X"));
        var sentence = new ConlluSentence("x sentence x", Vector.of(
                new ConlluWordLine(1, 2, "fx12", Vector.of(
                        new ConlluWordLine(1, "fx1", "lx1", "ADJ"),
                        new ConlluWordLine(2, "fx2", "lx2", "ADV"))),
                new ConlluWordLine(3, "fx3", "lx3", "VERB")));

        var actual = normalizer.normalizeAfterTokenization(sentence, "lx");

        verify(textNormalizer, times(8)).normalizeAfterTokenizer(any(), eq("lx"));
        verifyNoMoreInteractions(textNormalizer);
        assertThat(actual).isEqualTo(new ConlluSentence("X sentence X", Vector.of(
                new ConlluWordLine(1, 2, "fX12", Vector.of(
                        new ConlluWordLine(1, "fX1", "lX1", "ADJ"),
                        new ConlluWordLine(2, "fX2", "lX2", "ADV"))),
                new ConlluWordLine(3, "fX3", "lX3", "VERB"))));
    }
}
