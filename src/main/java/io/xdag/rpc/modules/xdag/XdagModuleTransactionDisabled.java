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
 */
package io.xdag.rpc.modules.xdag;

import io.xdag.core.Blockchain;
import io.xdag.rpc.Web3;
import io.xdag.rpc.dto.TransactionReceiptDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.xdag.rpc.exception.XdagJsonRpcRequestException.invalidParamError;

public class XdagModuleTransactionDisabled extends XdagModuleTransactionBase{

    private static final Logger logger = LoggerFactory.getLogger(XdagModuleTransactionDisabled.class);

    public XdagModuleTransactionDisabled(Blockchain blockchain) {
        super(blockchain);
    }

    @Override
    public TransactionReceiptDTO sendTransaction(Web3.CallArguments args) {
        logger.debug("xdag_sendTransaction({}): {}", args, null);
        throw invalidParamError("Local wallet is disabled in this node");
    }

    @Override
    public TransactionReceiptDTO sendRawTransaction(String rawData) {
        logger.debug("xdag_sendRawTransaction({}): {}", rawData, null);
        throw invalidParamError("Local wallet is disabled in this node");
    }
}
