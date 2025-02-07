
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */package io.xdag.rpc.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"jsonrpc", "id", "method", "result", "params", "error"})
public abstract class JsonRpcMessage {
    private final JsonRpcVersion version;

    public JsonRpcMessage(JsonRpcVersion version) {
        this.version = verifyVersion(version);
    }

    @JsonProperty("jsonrpc")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public JsonRpcVersion getVersion() {
        return version;
    }

    private static JsonRpcVersion verifyVersion(JsonRpcVersion version) {
        if (version != JsonRpcVersion.V2_0) {
            throw new IllegalArgumentException(
                    String.format("JSON-RPC version should always be %s, but was %s.", JsonRpcVersion.V2_0, version)
            );
        }

        return version;
    }
}
