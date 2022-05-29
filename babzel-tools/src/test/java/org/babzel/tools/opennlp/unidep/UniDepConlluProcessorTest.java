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

import org.babzel.tools.opennlp.unidep.util.UniDepURLSupplier;
import org.babzel.tools.opennlp.unidep.util.UniDepConlluUncompressor;
import com.google.common.jimfs.Jimfs;
import java.net.URL;
import java.nio.file.Path;
import org.babzel.tools.util.FileDownloader;
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

@ExtendWith(MockitoExtension.class)
public class UniDepConlluProcessorTest {
    @Mock
    private FileUpToDateChecker checker;
    @Mock
    private UniDepURLSupplier uniDepURLSupplier;
    @Mock
    private FileDownloader fileDownloader;
    @Mock
    private UniDepConlluUncompressor uniDepConlluUncompressor;
    @InjectMocks
    private UniDepConlluProcessor processor;

    @Test
    public void processUniDepConlluFile_TreebankOutOfDate_ConlluOutOfDate() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("root");
        Path treebankPath = rootPath.resolve("t.tgz");
        Path conlluPath = rootPath.resolve("f.conllu");
        URL treebankURL = new URL("http://ud.org/treebank.tgz");
        given(uniDepURLSupplier.supplyTreebankURL()).willReturn(treebankURL);
        given(checker.isUpToDate(any(), any(URL.class))).willReturn(false);
        given(checker.isUpToDate(any(), any(Path.class))).willReturn(false);

        processor.processUniDepConlluFile(treebankPath, "lx", conlluPath);

        verify(uniDepURLSupplier).supplyTreebankURL();
        verify(checker).isUpToDate(treebankPath, treebankURL);
        verify(fileDownloader).downloadFile(treebankURL, treebankPath);
        verify(checker).isUpToDate(conlluPath, treebankPath);
        verify(uniDepConlluUncompressor).uncompressConlluFile(treebankPath, "lx", conlluPath);
        verifyNoMoreInteractions(checker, uniDepURLSupplier, fileDownloader, uniDepConlluUncompressor);
    }

    @Test
    public void processUniDepConlluFile_TreebankUpToDate_ConlluOutOfDate() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("root");
        Path treebankPath = rootPath.resolve("t.tgz");
        Path conlluPath = rootPath.resolve("f.conllu");
        URL treebankURL = new URL("http://ud.org/treebank.tgz");
        given(uniDepURLSupplier.supplyTreebankURL()).willReturn(treebankURL);
        given(checker.isUpToDate(any(), any(URL.class))).willReturn(true);
        given(checker.isUpToDate(any(), any(Path.class))).willReturn(false);

        processor.processUniDepConlluFile(treebankPath, "lx", conlluPath);

        verify(uniDepURLSupplier).supplyTreebankURL();
        verify(checker).isUpToDate(treebankPath, treebankURL);
        verify(checker).isUpToDate(conlluPath, treebankPath);
        verify(uniDepConlluUncompressor).uncompressConlluFile(treebankPath, "lx", conlluPath);
        verifyNoMoreInteractions(checker, uniDepURLSupplier, fileDownloader, uniDepConlluUncompressor);
    }

    @Test
    public void processUniDepConlluFile_TreebankUpToDate_ConlluUpToDate() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("root");
        Path treebankPath = rootPath.resolve("t.tgz");
        Path conlluPath = rootPath.resolve("f.conllu");
        URL treebankURL = new URL("http://ud.org/treebank.tgz");
        given(uniDepURLSupplier.supplyTreebankURL()).willReturn(treebankURL);
        given(checker.isUpToDate(any(), any(URL.class))).willReturn(true);
        given(checker.isUpToDate(any(), any(Path.class))).willReturn(true);

        processor.processUniDepConlluFile(treebankPath, "lx", conlluPath);

        verify(uniDepURLSupplier).supplyTreebankURL();
        verify(checker).isUpToDate(treebankPath, treebankURL);
        verify(checker).isUpToDate(conlluPath, treebankPath);
        verifyNoMoreInteractions(checker, uniDepURLSupplier, fileDownloader, uniDepConlluUncompressor);
    }
}
