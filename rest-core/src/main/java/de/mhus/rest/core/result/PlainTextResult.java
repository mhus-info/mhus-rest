/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.rest.core.result;

import java.io.PrintWriter;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.api.RestResult;

public class PlainTextResult implements RestResult {

    private String contentType;
    private String text;
    private int returnCode = 0;

    public PlainTextResult(String text, String contentType) {
        if (MString.isEmpty(contentType)) contentType = MHttp.CONTENT_TYPE_TEXT;
        this.contentType = contentType;
        this.text = text;
    }

    @Override
    public void write(CallContext context, PrintWriter writer) throws Exception {
        writer.write(text);
    }

    @Override
    public String getContentType(CallContext context) {
        return contentType;
    }

    @Override
    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
