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
package io.xdag.rpc.modules.web3;

import io.xdag.rpc.Web3;
import io.xdag.rpc.dto.BlockResultDTO;
import io.xdag.rpc.dto.StatusDTO;
import io.xdag.rpc.dto.TransactionDTO;
import io.xdag.rpc.dto.TransactionReceiptDTO;
import io.xdag.rpc.modules.xdag.XdagModule;


public interface Web3XdagModule {

    String xdag_chainId();

    XdagModule getXdagModule();

    String xdag_protocolVersion();

    default String[] xdag_accounts() {
        return getXdagModule().accounts();
    }

    default String xdag_sign(String addr, String data) {
        return getXdagModule().sign(addr, data);
    }

    default Object xdag_syncing(){
        return getXdagModule().syncing();
    }

    default String xdag_coinbase() {
        return getXdagModule().getCoinBase();
    }

    default String xdag_blockNumber() {
        return getXdagModule().getStatus().getNblocks();
    }

    default String xdag_getBalance(String address) throws Exception {
        return getXdagModule().getBalance(address);
    }

    default String xdag_getTotalBalance() throws Exception {
        return getXdagModule().getTotalBalance();
    }

    default TransactionDTO xdag_getTransactionByHash(String hash, Boolean full)throws Exception{

        return TransactionDTO.getTransactionDTO(hash,xdag_getBlockByHash(hash,full).getFlags());
    }

    default BlockResultDTO xdag_getBlockByNumber(String bnOrId, Boolean full) throws Exception {
        return getXdagModule().getBlockByNumber(bnOrId,full);
    }

    default String xdag_sendRawTransaction(String rawData) {
        return getXdagModule().sendRawTransaction(rawData).getTransactionHash();
    }

    default String xdag_sendTransaction(Web3.CallArguments args) {
        return getXdagModule().sendTransaction(args).getTransactionHash();
    }

    default BlockResultDTO xdag_getBlockByHash(String blockHash, Boolean full) throws Exception {
        return getXdagModule().getBlockByHash(blockHash,full);
    }

    default StatusDTO xdag_getStatus() throws Exception {
        return getXdagModule().getStatus();
    }
}
