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

import lombok.NonNull;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenizerModel;
import org.babzel.tools.opennlp.conllu.convert.ConlluTokenSamplesConverter;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentencesFactory;
import org.babzel.tools.opennlp.model.TokenizerModelProcessor;
import org.babzel.tools.util.FileUpToDateChecker;
import org.springframework.stereotype.Component;

@Component
public class ConlluTokenizerModelProcessor extends ConlluModelProcessor<TokenizerModel, TokenSample> {
    public ConlluTokenizerModelProcessor(
            @NonNull FileUpToDateChecker checker,
            @NonNull ConlluSentencesFactory sentencesFactory,
            @NonNull ConlluTokenSamplesConverter converter,
            @NonNull TokenizerModelProcessor modelProcessor) {
        super(checker, sentencesFactory, converter, modelProcessor);
    }
}
