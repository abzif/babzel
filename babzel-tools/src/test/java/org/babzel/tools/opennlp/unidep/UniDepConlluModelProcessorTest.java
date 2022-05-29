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
package org.babzel.tools.opennlp.unidep;

import com.google.common.jimfs.Jimfs;
import io.vavr.collection.Vector;
import org.babzel.tools.opennlp.conllu.ConlluLemmatizerModelProcessor;
import org.babzel.tools.opennlp.conllu.ConlluPOSModelProcessor;
import org.babzel.tools.opennlp.conllu.ConlluSentenceModelProcessor;
import org.babzel.tools.opennlp.conllu.ConlluTokenExpanderModelProcessor;
import org.babzel.tools.opennlp.conllu.ConlluTokenizerModelProcessor;
import org.babzel.tools.opennlp.model.ModelAlgorithm;
import org.babzel.tools.opennlp.unidep.util.UniDepConlluModelFileSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UniDepConlluModelProcessorTest {
    @Mock
    private UniDepConlluModelFileSupplier fileSupplier;
    @Mock
    private UniDepConlluProcessor conlluTrainFileProcessor;
    @Mock
    private ConlluSentenceModelProcessor sentenceModelProcessor;
    @Mock
    private ConlluTokenizerModelProcessor tokenizerModelProcessor;
    @Mock
    private ConlluTokenExpanderModelProcessor tokenExpanderModelProcessor;
    @Mock
    private ConlluPOSModelProcessor posModelProcessor;
    @Mock
    private ConlluLemmatizerModelProcessor lemmatizerModelProcessor;
    @InjectMocks
    private UniDepConlluModelProcessor processor;

    @Test
    public void processUniDepConlluModel() {
        var rootPath = Jimfs.newFileSystem().getPath("");
        var treebankPath = rootPath.resolve("t.tgz");
        given(fileSupplier.getTreebankFile()).willReturn(treebankPath);
        var conlluPath = rootPath.resolve("lx.conllu");
        given(fileSupplier.getConlluFile(any())).willReturn(conlluPath);
        var sentenceModelPath = rootPath.resolve("lx-s.onlpm");
        given(fileSupplier.getSentenceModelFile(any())).willReturn(sentenceModelPath);
        var sentenceReportPath = rootPath.resolve("lx-s.txt");
        given(fileSupplier.getSentenceReportFile(any())).willReturn(sentenceReportPath);
        var tokenizerModelPath = rootPath.resolve("lx-t.onlpm");
        given(fileSupplier.getTokenizerModelFile(any())).willReturn(tokenizerModelPath);
        var tokenizerReportPath = rootPath.resolve("lx-t.txt");
        given(fileSupplier.getTokenizerReportFile(any())).willReturn(tokenizerReportPath);
        var tokenExpanderModelPath = rootPath.resolve("lx-te.onlpm");
        given(fileSupplier.getTokenExpanderModelFile(any())).willReturn(tokenExpanderModelPath);
        var tokenExpanderReportPath = rootPath.resolve("lx-te.txt");
        given(fileSupplier.getTokenExpanderReportFile(any())).willReturn(tokenExpanderReportPath);
        var posModelPath = rootPath.resolve("lx-p.onlpm");
        given(fileSupplier.getPOSModelFile(any())).willReturn(posModelPath);
        var posReportPath = rootPath.resolve("lx-p.txt");
        given(fileSupplier.getPOSReportFile(any())).willReturn(posReportPath);
        var lemmatizerModelPath = rootPath.resolve("lx-l.onlpm");
        given(fileSupplier.getLemmatizerModelFile(any())).willReturn(lemmatizerModelPath);
        var lemmatizerReportPath = rootPath.resolve("lx-l.txt");
        given(fileSupplier.getLemmatizerReportFile(any())).willReturn(lemmatizerReportPath);
        var tokenizerAlgorithms = Vector.of(ModelAlgorithm.MAXENT, ModelAlgorithm.MAXENT_QN, ModelAlgorithm.PERCEPTRON, ModelAlgorithm.NAIVE_BAYES);
        var posAlgorithms = Vector.of(ModelAlgorithm.MAXENT, ModelAlgorithm.PERCEPTRON, ModelAlgorithm.NAIVE_BAYES);
        var lemmatizerAlgorithms = Vector.of(ModelAlgorithm.MAXENT);

        processor.processUniDepConlluModel("lx");

        verify(fileSupplier).getTreebankFile();
        verify(fileSupplier).getConlluFile("lx");
        verify(fileSupplier).getSentenceModelFile("lx");
        verify(fileSupplier).getSentenceReportFile("lx");
        verify(fileSupplier).getTokenizerModelFile("lx");
        verify(fileSupplier).getTokenizerReportFile("lx");
        verify(fileSupplier).getTokenExpanderModelFile("lx");
        verify(fileSupplier).getTokenExpanderReportFile("lx");
        verify(fileSupplier).getPOSModelFile("lx");
        verify(fileSupplier).getPOSReportFile("lx");
        verify(fileSupplier).getLemmatizerModelFile("lx");
        verify(fileSupplier).getLemmatizerReportFile("lx");
        verify(conlluTrainFileProcessor).processUniDepConlluFile(treebankPath, "lx", conlluPath);
        verify(sentenceModelProcessor).processConlluModel(conlluPath, tokenizerAlgorithms, "lx", sentenceModelPath, sentenceReportPath);
        verify(tokenizerModelProcessor).processConlluModel(conlluPath, tokenizerAlgorithms, "lx", tokenizerModelPath, tokenizerReportPath);
        verify(tokenExpanderModelProcessor).processConlluModel(conlluPath, lemmatizerAlgorithms, "lx", tokenExpanderModelPath, tokenExpanderReportPath);
        verify(posModelProcessor).processConlluModel(conlluPath, posAlgorithms, "lx", posModelPath, posReportPath);
        verify(lemmatizerModelProcessor).processConlluModel(conlluPath, lemmatizerAlgorithms, "lx", lemmatizerModelPath, lemmatizerReportPath);
        verifyNoMoreInteractions(fileSupplier, conlluTrainFileProcessor, sentenceModelProcessor, tokenizerModelProcessor, posModelProcessor, lemmatizerModelProcessor);
    }
}
