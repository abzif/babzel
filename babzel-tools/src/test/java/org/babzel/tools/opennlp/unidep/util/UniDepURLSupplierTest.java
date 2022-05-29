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

import com.gargoylesoftware.htmlunit.MockWebConnection;
import java.net.URL;
import static org.assertj.core.api.Assertions.assertThat;
import org.babzel.tools.ToolsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

public class UniDepURLSupplierTest {
    private MockWebConnection mockWebConnection;
    private UniDepURLSupplier supplier;

    @BeforeEach
    public void setUp() {
        mockWebConnection = new MockWebConnection();
        var webClient = ToolsConfig.createSimpleWebClient();
        webClient.setWebConnection(mockWebConnection);
        supplier = new UniDepURLSupplier(() -> webClient);
    }

    @Test
    public void supplyTreebankURL() throws Exception {
        mockWebConnection.setResponse(new URL("http://universaldependencies.org/"), copyClassPathResourceToString("uni-dep-main.html"));
        mockWebConnection.setResponse(new URL("http://hdl.handle.net/11234/1-4611"), copyClassPathResourceToString("uni-dep-download.html"));

        var url = supplier.supplyTreebankURL();

        assertThat(url).isEqualTo(new URL("http://hdl.handle.net/repository/xmlui/bitstream/handle/11234/1-4611/ud-treebanks-v2.9.tgz?sequence=1&isAllowed=y"));
    }

    private String copyClassPathResourceToString(String path) throws Exception {
        return new String(FileCopyUtils.copyToByteArray(new ClassPathResource(path, getClass()).getInputStream()));
    }
}
