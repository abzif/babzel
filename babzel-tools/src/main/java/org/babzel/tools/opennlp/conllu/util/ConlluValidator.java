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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConlluValidator {
    @NonNull
    private final ConlluValidatorHelper helper;

    public boolean isValidForTokenization(@NonNull ConlluSentence sentence) {
        return helper.notEmpty(sentence) && helper.formsValid(sentence) && helper.textEqualsConcatenatedForms(sentence);
    }

    public boolean isValidForLemmatization(@NonNull ConlluSentence sentence) {
        return isValidForTokenization(sentence) && helper.lemmasValid(sentence) && helper.posTagsValid(sentence);
    }
}
