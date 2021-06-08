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
package io.xdag.rpc.dto;


import io.xdag.core.Block;
import io.xdag.utils.BytesUtils;
import lombok.Data;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Objects;

import static io.xdag.rpc.utils.TypeConverter.*;

@Data
//TODO: Return xdagblock info
public class BlockResultDTO {

    // rawData
    private String height;
    // blockInfo
    private String rawdata;
    private String hash;
    private String hashlow;
    private String amount;
    private String difficulty;
    private String fee;
    private String timestamp;
    private String remark;
    private String flags;

    // TODO: if block not found, what should we return?
    public BlockResultDTO() {

    }

    public BlockResultDTO(String height, String hash, String hashlow, String amount, String difficulty, String fee, String timestamp, String remark, String rawdata, String flags) {
        this.height = height;
        this.hash = hash;
        this.hashlow = hashlow;
        this.amount = amount;
        this.difficulty = difficulty;
        this.fee = fee;
        this.timestamp = timestamp;
        this.remark = remark;
        this.rawdata = rawdata;
        this.flags = flags;
    }



    public static BlockResultDTO fromBlock(Block block, boolean full) {
        if (block == null) {
            return new BlockResultDTO();
        }
        String height = block.getInfo().getHeight() == 0?"":toQuantityJsonHex(block.getInfo().getHeight());
        String hash = toUnformattedJsonHex(block.getInfo().getHash());
        String hashlow = toUnformattedJsonHex(block.getInfo().getHashlow());
        String amount = toQuantityJsonHex(block.getInfo().getAmount());
        String difficulty = toQuantityJsonHex(block.getInfo().getDifficulty());
        String fee = toQuantityJsonHex(block.getInfo().getFee());
        String timestamp = toQuantityJsonHex(block.getInfo().getTimestamp());
        String remark = block.getInfo().getRemark() == null?"":new String(block.getInfo().getRemark());
        String flags = Integer.toString(block.getInfo().getFlags());
        String rawdata = "";
        if (full) {
            rawdata = BytesUtils.toHexString(block.getXdagBlock().getData());
        }
        return new BlockResultDTO(height,hash,hashlow,amount,difficulty,fee,timestamp,remark,rawdata,flags);
    }
}
