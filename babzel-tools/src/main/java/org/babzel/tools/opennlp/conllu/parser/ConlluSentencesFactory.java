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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.babzel.tools.opennlp.conllu.transformer.ConlluTransformers;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConlluSentencesFactory {
    @NonNull
    private final ConlluParser parser;
    @NonNull
    private final ConlluTransformers transformers;

    @SneakyThrows
    public Seq<ConlluSentence> createSentences(@NonNull Path conlluPath, @NonNull String language) {
        Seq<String> lineSeq = Vector.ofAll(Files.readAllLines(conlluPath, StandardCharsets.UTF_8));
        Seq<Seq<String>> groupedSeq = Vector.ofAll(lineSeq.slideBy(line -> line.isEmpty()));
        Seq<Seq<String>> paragraphSeq = groupedSeq.filter(lines -> !lines.forAll(line -> line.isEmpty()));
        Seq<ConlluSentence> parsedSeq = paragraphSeq.map(parser::parse);
        Seq<ConlluSentence> transformedSeq = parsedSeq.map(s -> transformers.transformSentence(s, language));
        return transformedSeq;
    }
}
