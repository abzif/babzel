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
package org.babzel.tools.opennlp.model.util;

import com.google.common.jimfs.Jimfs;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import java.nio.file.Path;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenizerModel;
import static org.assertj.core.api.Assertions.assertThat;
import org.babzel.tools.opennlp.model.ModelAlgorithm;
import org.babzel.tools.opennlp.model.train.TokenizerModelTrainer;
import org.junit.jupiter.api.Test;

public class EvalReportPersisterTest {
    private EvalReportPersister persister = new EvalReportPersister();

    @Test
    public void writeEvaluationReport() {
        Seq<TokenSample> samples = Vector.of(
                TokenSample.parse("a1 a2|.", "|"),
                TokenSample.parse("b1 b2|.", "|"),
                TokenSample.parse("c1 c2|.", "|"));
        TokenizerModelTrainer trainer = new TokenizerModelTrainer();
        TokenizerModel trainedModel = trainer.trainModel(ModelAlgorithm.MAXENT, "lx", samples).get();
        Path evalReportPath = Jimfs.newFileSystem().getPath("report.txt");

        persister.writeEvaluationReport(10.5, 500, 33, "A", trainedModel, "abc\nxyz", evalReportPath);

        assertThat(evalReportPath).exists();
        assertThat(evalReportPath).content()
                .contains("Evaluation-Score=10.5")
                .contains("Training-Sample-Size=500")
                .contains("Evaluation-Sample-Size=33")
                .contains("Training-Algorithm=A")
                .contains("Language=lx")
                .contains("abc\nxyz");
    }
}
