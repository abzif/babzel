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
package org.babzel.tools.opennlp.conllu.parser;

import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import lombok.NonNull;
import lombok.Value;

@Value
public class ConlluSentence {
    @NonNull
    private final String text;
    @NonNull
    private final Seq<ConlluWordLine> words;

    public ConlluSentence flattenWords() {
        return new ConlluSentence(text, flattenWords(words));
    }

    private Seq<ConlluWordLine> flattenWords(Seq<ConlluWordLine> words) {
        return words.flatMap(this::flattenWord);
    }

    private Seq<ConlluWordLine> flattenWord(ConlluWordLine word) {
        return word.isCompound() && formEqualsJoinedSubForms(word)
                ? word.getSubWords()
                : Vector.of(word);
    }

    private boolean formEqualsJoinedSubForms(ConlluWordLine word) {
        var form = word.getForm();
        var joinedSubForms = word.getSubWords().map(ConlluWordLine::getForm).mkString();
        return form.equals(joinedSubForms);
    }
}
