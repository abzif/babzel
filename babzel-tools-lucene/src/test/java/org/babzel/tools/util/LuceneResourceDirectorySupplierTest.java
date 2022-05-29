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
package org.babzel.tools.util;

import com.google.common.jimfs.Jimfs;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LuceneResourceDirectorySupplierTest {
    @Mock
    private RootDirectorySupplier rootDirectorySupplier;
    @InjectMocks
    private LuceneResourceDirectorySupplier supplier;

    @Test
    public void getResourceDirName() {
        var rootPath = Jimfs.newFileSystem().getPath("");
        given(rootDirectorySupplier.getRootDirectory()).willReturn(rootPath);

        var resPath = supplier.getResourceDirectory("lx");

        assertThat(resPath).isEqualTo(rootPath.resolve("lx-lucene"));
    }
}
