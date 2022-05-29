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

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple4;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.io.Serializable;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import opennlp.tools.util.model.BaseModel;
import org.babzel.tools.opennlp.model.train.ModelEvaluator;
import org.babzel.tools.opennlp.model.train.ModelTrainer;
import org.babzel.tools.opennlp.model.util.EvalReportPersister;
import org.babzel.tools.opennlp.model.util.ModelPersister;
import org.babzel.tools.opennlp.model.util.SampleSplitter;

@RequiredArgsConstructor
@CommonsLog
public class ModelProcessor<M extends BaseModel, S extends Serializable> {
    @NonNull
    private final SampleSplitter splitter;
    @NonNull
    private final ModelTrainer<M, S> trainer;
    @NonNull
    private final ModelEvaluator<M, S> evaluator;
    @NonNull
    private final ModelPersister modelPersister;
    @NonNull
    private final EvalReportPersister evalReportPersister;

    public void processModel(
            @NonNull Seq<S> samples,
            @NonNull Seq<String> algorithms,
            @NonNull String language,
            @NonNull Path modelPath,
            @NonNull Path reportPath) {
        var splittedSamples = splitter.splitSamples(samples);
        var trainSamples = splittedSamples._1;
        var evalSamples = splittedSamples._2;
        var bestModelInfoOpt = trainEvaluateBestModel(algorithms, language, trainSamples, evalSamples);
        if (bestModelInfoOpt.isDefined()) {
            var algorithm = bestModelInfoOpt.get()._1;
            var model = bestModelInfoOpt.get()._2;
            var evaluationScore = bestModelInfoOpt.get()._3;
            var misclassifiedDetails = bestModelInfoOpt.get()._4;
            writeModel(model, modelPath);
            writeEvalReportPath(evaluationScore, trainSamples.size(), evalSamples.size(), algorithm, model, misclassifiedDetails, reportPath);
        } else {
            log.info("Insufficient training data to compute model");
        }
    }

    private Option<Tuple4<String, M, Double, String>> trainEvaluateBestModel(Seq<String> algorithms, String language, Seq<S> trainSamples, Seq<S> evalSamples) {
        var bestModelInfoOpt = Option.<Tuple4<String, M, Double, String>>none();
        for (var algorithm : algorithms) {
            var modelOpt = trainModel(algorithm, language, trainSamples);
            if (modelOpt.isDefined()) {
                var evaluationInfo = evaluateModel(modelOpt.get(), evalSamples);
                var evaluationScore = evaluationInfo._1;
                var misclassifiedDetails = evaluationInfo._2;
                if (bestModelInfoOpt.isEmpty() || evaluationScore > bestModelInfoOpt.get()._3) {
                    bestModelInfoOpt = Option.some(Tuple.of(algorithm, modelOpt.get(), evaluationScore, misclassifiedDetails));
                }
            }
        }
        return bestModelInfoOpt;
    }

    private Option<M> trainModel(String algorithm, String language, Seq<S> trainSamples) {
        log.info(String.format("Training model, language: '%s'", language));
        return trainer.trainModel(algorithm, language, trainSamples);
    }

    private Tuple2<Double, String> evaluateModel(M model, Seq<S> evalSamples) {
        log.info(String.format("Evaluating model"));
        return evaluator.evaluateModel(model, evalSamples);
    }

    private void writeModel(M model, Path modelPath) {
        log.info(String.format("Writing model to file: '%s'", modelPath.toAbsolutePath()));
        modelPersister.writeModel(model, modelPath);
    }

    private void writeEvalReportPath(double evaluationScore, int trainSamplesSize, int evalSamplesSize, String algorithm, M model, String misclassifiedDetails, Path reportPath) {
        log.info(String.format("Writing evaluation report to file: '%s'", reportPath.toAbsolutePath()));
        evalReportPersister.writeEvaluationReport(evaluationScore, trainSamplesSize, evalSamplesSize, algorithm, model, misclassifiedDetails, reportPath);
    }
}
