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

import com.google.common.jimfs.Jimfs;
import io.vavr.collection.Vector;
import java.nio.file.Path;
import opennlp.tools.util.model.BaseModel;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentence;
import org.babzel.tools.opennlp.conllu.parser.ConlluSentencesFactory;
import org.babzel.tools.opennlp.model.ModelProcessor;
import org.babzel.tools.util.FileUpToDateChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.babzel.tools.opennlp.conllu.convert.ConlluSamplesConverter;

@ExtendWith(MockitoExtension.class)
public class ConlluModelProcessorTest {
    @Mock
    private FileUpToDateChecker checker;
    @Mock
    private ConlluSentencesFactory sentencesFactory;
    @Mock
    private ConlluSamplesConverter<String> converter;
    @Mock
    private ModelProcessor<BaseModel, String> modelProcessor;
    @InjectMocks
    private ConlluModelProcessor processor;

    @Test
    public void processConlluModel_ModelFileOutOfDate() {
        Path rootPath = Jimfs.newFileSystem().getPath("root");
        Path conlluPath = rootPath.resolve("file.conllu");
        Path modelPath = rootPath.resolve("model.bin");
        Path reportPath = rootPath.resolve("report.txt");
        given(checker.isUpToDate(any(), any(Path.class))).willReturn(false, true);
        given(sentencesFactory.createSentences(any(), any())).willReturn(Vector.of(
                new ConlluSentence("s1", Vector.empty()),
                new ConlluSentence("s2", Vector.empty()),
                new ConlluSentence("s3", Vector.empty())));
        given(converter.convert(any(), any())).willReturn(Vector.of("1", "3"));

        processor.processConlluModel(conlluPath, Vector.of("a", "b"), "lx", modelPath, reportPath);

        verify(checker).isUpToDate(modelPath, conlluPath);
        verify(sentencesFactory).createSentences(conlluPath, "lx");
        verify(converter).convert(Vector.of(
                new ConlluSentence("s1", Vector.empty()),
                new ConlluSentence("s2", Vector.empty()),
                new ConlluSentence("s3", Vector.empty())), "lx");
        verify(modelProcessor).processModel(Vector.of("1", "3"), Vector.of("a", "b"), "lx", modelPath, reportPath);
        verifyNoMoreInteractions(checker, sentencesFactory, converter, modelProcessor);
    }

    @Test
    public void processConlluModel_ReportFileOutOfDate() {
        Path rootPath = Jimfs.newFileSystem().getPath("root");
        Path conlluPath = rootPath.resolve("file.conllu");
        Path modelPath = rootPath.resolve("model.bin");
        Path reportPath = rootPath.resolve("report.txt");
        given(checker.isUpToDate(any(), any(Path.class))).willReturn(true, false);
        given(sentencesFactory.createSentences(any(), any())).willReturn(Vector.of(
                new ConlluSentence("s1", Vector.empty()),
                new ConlluSentence("s2", Vector.empty()),
                new ConlluSentence("s3", Vector.empty())));
        given(converter.convert(any(), any())).willReturn(Vector.of("1", "3"));

        processor.processConlluModel(conlluPath, Vector.of("a", "b"), "lx", modelPath, reportPath);

        verify(checker).isUpToDate(modelPath, conlluPath);
        verify(checker).isUpToDate(reportPath, conlluPath);
        verify(sentencesFactory).createSentences(conlluPath, "lx");
        verify(converter).convert(Vector.of(
                new ConlluSentence("s1", Vector.empty()),
                new ConlluSentence("s2", Vector.empty()),
                new ConlluSentence("s3", Vector.empty())), "lx");
        verify(modelProcessor).processModel(Vector.of("1", "3"), Vector.of("a", "b"), "lx", modelPath, reportPath);
        verifyNoMoreInteractions(checker, sentencesFactory, converter, modelProcessor);
    }

    @Test
    public void processConlluMode_ModelAndReportFilesUpToDate() {
        Path rootPath = Jimfs.newFileSystem().getPath("root");
        Path conlluPath = rootPath.resolve("file.conllu");
        Path modelPath = rootPath.resolve("model.bin");
        Path reportPath = rootPath.resolve("report.txt");
        given(checker.isUpToDate(any(), any(Path.class))).willReturn(true, true);

        processor.processConlluModel(conlluPath, Vector.of("a", "b"), "lx", modelPath, reportPath);

        verify(checker).isUpToDate(modelPath, conlluPath);
        verify(checker).isUpToDate(reportPath, conlluPath);
        verifyNoMoreInteractions(checker, sentencesFactory, converter, modelProcessor);
    }
}
