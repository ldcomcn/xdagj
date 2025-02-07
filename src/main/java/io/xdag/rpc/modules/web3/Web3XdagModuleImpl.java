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

import com.sun.jdi.LongValue;
import io.xdag.Kernel;
import io.xdag.config.Config;
import io.xdag.config.MainnetConfig;
import io.xdag.core.Block;
import io.xdag.core.Blockchain;
import io.xdag.core.XdagState;
import io.xdag.core.XdagStats;
import io.xdag.rpc.dto.BlockResultDTO;
import io.xdag.rpc.dto.StatusDTO;
import io.xdag.rpc.modules.xdag.XdagModule;
import io.xdag.utils.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

import static io.xdag.rpc.utils.TypeConverter.toQuantityJsonHex;
import static io.xdag.utils.BasicUtils.address2Hash;
import static io.xdag.utils.BasicUtils.amount2xdag;

public class Web3XdagModuleImpl implements Web3XdagModule{

    private static final Logger logger = LoggerFactory.getLogger(Web3XdagModuleImpl.class);

    class SyncingResult {
        public String currentBlock;
        public String highestBlock;
    }

    private final Blockchain blockchain;
    private final XdagModule xdagModule;
    private final Kernel kernel;

    public Web3XdagModuleImpl(XdagModule xdagModule, Kernel kernel) {
        this.blockchain = kernel.getBlockchain();
        this.xdagModule = xdagModule;
        this.kernel = kernel;
    }


    @Override
    public XdagModule getXdagModule() {
        return xdagModule;
    }

    @Override
    public String xdag_protocolVersion() {
        return null;
    }

    @Override
    public Object xdag_syncing() {
        long currentBlock = this.blockchain.getXdagStats().nmain;
        long highestBlock = Math.max(this.blockchain.getXdagStats().totalnmain, currentBlock);

        Config config = kernel.getConfig();
        if (config instanceof MainnetConfig) {
            if (kernel.getXdagState() != XdagState.SYNC){
                return false;
            }
        } else {
            if (kernel.getXdagState() != XdagState.STST) {
                return false;
            }
        }


        SyncingResult s = new SyncingResult();
        try {
            s.currentBlock = toQuantityJsonHex(currentBlock);
            s.highestBlock = toQuantityJsonHex(highestBlock);

            return s;
        } finally {
            logger.debug("xdag_syncing():current {}, highest {} ", s.currentBlock, s.highestBlock);
        }
    }

    @Override
    public String xdag_coinbase() {
        return Hex.toHexString(kernel.getPoolMiner().getAddressHash());
    }

    @Override
    public String xdag_blockNumber() {
        long b = blockchain.getXdagStats().nmain;
        logger.debug("xdag_blockNumber(): {}", b);

        return toQuantityJsonHex(b);
    }

    @Override
    public String xdag_getBalance(String address) throws Exception {
        byte[] hash;
        if (org.apache.commons.lang3.StringUtils.length(address) == 32) {
            hash = address2Hash(address);
        } else {
            hash = StringUtils.getHash(address);
        }
        byte[] key = new byte[32];
        System.arraycopy(Objects.requireNonNull(hash), 8, key, 8, 24);
        Block block = kernel.getBlockStore().getBlockInfoByHash(key);
        double balance = amount2xdag(block.getInfo().getAmount());
        return toQuantityJsonHex(balance);
    }

    @Override
    public String xdag_getTotalBalance() throws Exception {
        double balance = amount2xdag(kernel.getBlockchain().getXdagStats().getBalance());
        return toQuantityJsonHex(balance);
    }

    @Override
    public BlockResultDTO xdag_getBlockByNumber(String bnOrId, Boolean full) throws Exception {
        System.out.println(bnOrId);
        System.out.println(full);
        if (full) {
            System.out.println("hello");
        }
        BlockResultDTO blockResultDTO = new BlockResultDTO(Integer.parseInt(bnOrId));

        return blockResultDTO;
    }

    @Override
    public BlockResultDTO xdag_getBlockByHash(String blockHash, Boolean full) throws Exception {
        return null;
    }

    @Override
    public StatusDTO xdag_getStatus() throws Exception {
        XdagStats xdagStats = kernel.getBlockchain().getXdagStats();
        long nblocks = Math.max(xdagStats.getTotalnblocks(),xdagStats.getNblocks());
        long nmain = Math.max(xdagStats.getTotalnblocks(),xdagStats.getNmain());
        BigInteger diff = xdagStats.getDifficulty();
        double supply = amount2xdag(kernel.getBlockchain().getSupply(Math.max(xdagStats.nmain,xdagStats.totalnmain)));
        return new StatusDTO(nblocks,nmain,diff,supply);
    }
}
