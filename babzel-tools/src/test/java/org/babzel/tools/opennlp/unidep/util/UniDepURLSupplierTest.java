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
package org.babzel.tools.opennlp.unidep.util;

import java.net.URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.babzel.tools.util.WebClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

@ExtendWith(MockitoExtension.class)
public class UniDepURLSupplierTest {
    @Mock
    private WebClient webClient;
    @InjectMocks
    private UniDepURLSupplier supplier;

    @Test
    public void supplyTreebankURLIncorrectMainPage() throws Exception {
        given(webClient.readContentAsBytes(any())).willReturn("".getBytes());

        assertThatThrownBy(() -> supplier.supplyTreebankURL());
    }

    @Test
    public void supplyTreebankURLIncorrectDownloadPage() throws Exception {
        given(webClient.readContentAsBytes(any())).willReturn(copyClassPathResourceAsBytes("uni-dep-main.html"), "".getBytes());

        assertThatThrownBy(() -> supplier.supplyTreebankURL());
    }

    @Test
    public void supplyTreebankURL() throws Exception {
        given(webClient.readContentAsBytes(any())).willReturn(copyClassPathResourceAsBytes("uni-dep-main.html"), copyClassPathResourceAsBytes("uni-dep-download.html"));

        var url = supplier.supplyTreebankURL();

        verify(webClient).readContentAsBytes(new URL("http://universaldependencies.org"));
        verify(webClient).readContentAsBytes(new URL("http://hdl.handle.net/11234/1-4611"));
        verifyNoMoreInteractions(webClient);
        assertThat(url).isEqualTo(new URL("https://lindat.mff.cuni.cz/repository/xmlui/bitstream/11234/1-4611/1/ud-treebanks-v2.9.tgz"));
    }

    private byte[] copyClassPathResourceAsBytes(String path) throws Exception {
        return FileCopyUtils.copyToByteArray(new ClassPathResource(path, getClass()).getInputStream());
    }
}
