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
package org.babzel.tools.opennlp.conllu;

import io.vavr.collection.Seq;
import java.io.Serializable;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import opennlp.tools.util.model.BaseModel;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentencesFactory;
import org.babzel.tools.opennlp.model.ModelProcessor;
import org.babzel.tools.util.FileUpToDateChecker;
import org.babzel.tools.opennlp.conllu.convert.ConlluSamplesConverter;

@RequiredArgsConstructor
@CommonsLog
public class ConlluModelProcessor<M extends BaseModel, S extends Serializable> {
    @NonNull
    private final FileUpToDateChecker checker;
    @NonNull
    private final ConlluSentencesFactory sentencesFactory;
    @NonNull
    private final ConlluSamplesConverter<S> converter;
    @NonNull
    private final ModelProcessor<M, S> modelProcessor;

    public void processConlluModel(
            @NonNull Path conlluPath,
            @NonNull Seq<String> algorithms,
            @NonNull String language,
            @NonNull Path modelPath,
            @NonNull Path reportPath) {
        if (!checker.isUpToDate(modelPath, conlluPath) || !checker.isUpToDate(reportPath, conlluPath)) {
            log.info(String.format("Processing model, language: '%s', conllu file: '%s'", language, conlluPath.toAbsolutePath()));
            var sentences = sentencesFactory.createSentences(conlluPath, language);
            var totalSentenceCount = sentences.size();
            var samples = converter.convert(sentences, language);
            var correctSentenceCount = samples.size();
            log.info(String.format("Sentences total: %d, correct: %d, correct percent: %.02f%%", totalSentenceCount, correctSentenceCount, 100.0 * correctSentenceCount / totalSentenceCount));
            modelProcessor.processModel(samples, algorithms, language, modelPath, reportPath);
        } else {
            log.info(String.format("Skip processing model, language: '%s', conllu file: '%s'", language, conlluPath.toAbsolutePath()));
            log.info(String.format("Model file: '%s' is up to date", modelPath.toAbsolutePath()));
            log.info(String.format("Evaluation report file: '%s' is up to date", reportPath.toAbsolutePath()));
        }
    }
}
