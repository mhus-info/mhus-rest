/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.rest.core.result;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;

import de.mhus.lib.core.MFile;
import de.mhus.rest.core.api.RestResult;

public class BinaryResult implements RestResult {

    private InputStream is;
    private String contentType;
    private Reader reader;

    public BinaryResult(InputStream is, String contentType) {
        this.contentType = contentType;
        this.is = is;
    }

    public BinaryResult(Reader reader, String contentType) {
        this.contentType = contentType;
        this.reader = reader;
    }

    @Override
    public void write(PrintWriter writer) throws Exception {
        if (is != null) {
            while (true) {
                int b = is.read();
                if (b < 0) break;
                writer.write(b);
            }
            is.close();
            is = null;
        } else if (reader != null) {
            MFile.copyFile(reader, writer);
            reader.close();
            reader = null;
            onClose();
        }
    }

    protected void onClose() {}

    @Override
    public String getContentType() {
        return contentType;
    }
}
