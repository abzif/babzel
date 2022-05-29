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
package org.babzel.tools.opennlp.model;

import lombok.NonNull;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import org.babzel.tools.opennlp.model.train.POSModelEvaluator;
import org.babzel.tools.opennlp.model.train.POSModelTrainer;
import org.babzel.tools.opennlp.model.util.EvalReportPersister;
import org.babzel.tools.opennlp.model.util.ModelPersister;
import org.babzel.tools.opennlp.model.util.SampleSplitter;
import org.springframework.stereotype.Component;

@Component
public class POSModelProcessor extends ModelProcessor<POSModel, POSSample> {
    public POSModelProcessor(
            @NonNull SampleSplitter splitter,
            @NonNull POSModelTrainer trainer,
            @NonNull POSModelEvaluator evaluator,
            @NonNull ModelPersister modelPersister,
            @NonNull EvalReportPersister evalReportPersister) {
        super(splitter, trainer, evaluator, modelPersister, evalReportPersister);
    }
}
