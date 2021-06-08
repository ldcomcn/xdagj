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

import io.xdag.Kernel;
import io.xdag.config.Config;
import io.xdag.config.MainnetConfig;
import io.xdag.core.*;
import io.xdag.rpc.dto.BlockResultDTO;
import io.xdag.rpc.dto.StatusDTO;
import io.xdag.rpc.dto.SyncingDTO;
import io.xdag.rpc.utils.TypeConverter;
import io.xdag.utils.BytesUtils;
import io.xdag.utils.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Objects;

import static io.xdag.rpc.utils.TypeConverter.toQuantityJsonHex;
import static io.xdag.rpc.utils.TypeConverter.toUnformattedJsonHex;
import static io.xdag.utils.BasicUtils.address2Hash;

public class XdagModuleChainBase implements XdagModuleChain {

    private final Blockchain blockchain;
    private final Kernel kernel;
    private final String coinBase;

    public XdagModuleChainBase(Kernel kernel) {
        this.kernel = kernel;
        this.blockchain = kernel.getBlockchain();
        this.coinBase = toUnformattedJsonHex(kernel.getPoolMiner().getAddressHaashLow());
    }

    @Override
    public Object syncing() {
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

        SyncingDTO s = new SyncingDTO();
        s.currentBlock = toQuantityJsonHex(currentBlock);
        s.highestBlock = toQuantityJsonHex(highestBlock);
        return s;
    }

    @Override
    public String getCoinBase() {
        return coinBase;
    }

    @Override
    public BlockResultDTO getBlockByHash(String hash, boolean full) {
        byte[] bh = new byte[32];
        System.arraycopy(Hex.decode(TypeConverter.removeZeroX(hash)),8,bh,8,24);
        Block block = blockchain.getBlockByHash(bh,full);
        return BlockResultDTO.fromBlock(block,full);
    }

    @Override
    public BlockResultDTO getBlockByNumber(String bnOrId, boolean full) {
        Block block = blockchain.getBlockByHeight(Long.parseLong(bnOrId));
        return BlockResultDTO.fromBlock(block,false);
    }

    @Override
    public StatusDTO getStatus() {
        XdagTopStatus xdagTopStatus = kernel.getBlockchain().getXdagTopStatus();
        XdagStats xdagStats = kernel.getBlockchain().getXdagStats();

        BigInteger diff = xdagTopStatus.getTopDiff()!=null?xdagTopStatus.getTopDiff():BigInteger.ZERO;
        BigInteger netDiff = xdagStats.getMaxdifficulty()!=null?xdagStats.getMaxdifficulty():BigInteger.ZERO;
        BigInteger tDiff = netDiff.max(diff);

        long nblocks = xdagStats.getNblocks();
        long tNblocks = Math.max(xdagStats.getTotalnblocks(),xdagStats.getNblocks());
        long nmain = xdagStats.getNmain();
        long tNmain = Math.max(xdagStats.getTotalnmain(),xdagStats.getNmain());
        long supply = kernel.getBlockchain().getSupply(Math.max(xdagStats.nmain,xdagStats.totalnmain));

        return new StatusDTO(nblocks,nmain,diff,tNblocks, tNmain, tDiff,supply);
    }

    @Override
    public String getBalance(String address) {
        byte[] hash;
        if (org.apache.commons.lang3.StringUtils.length(address) == 32) {
            hash = address2Hash(address);
        } else {
            hash = Hex.decode(TypeConverter.removeZeroX(address));
        }
        byte[] key = new byte[32];
        System.arraycopy(Objects.requireNonNull(hash), 8, key, 8, 24);
        Block block = kernel.getBlockStore().getBlockInfoByHash(key);
        long balance = block.getInfo().getAmount();
        return toQuantityJsonHex(balance);
    }
}
