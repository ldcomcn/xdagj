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
package io.xdag.crypto;

import io.xdag.config.Config;
import io.xdag.config.DevnetConfig;
import io.xdag.crypto.jni.Native;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DFSJniTest {
    Config config;

    @Before
    public void setUp() throws Exception {
        config = new DevnetConfig();
        config.initKeys();
    }

    @Test
    public void testDfscrypted() throws DecoderException {
        // 加密前的512字节转成1024字节的hex的数据
        String uncryptHexData = "8b01000273cb2fbc2000000000000000000000000000000000000000000001006c755e8d9588cb67000000000000000000000000000000000000000000000000d7c558c3cf0300000000000000000000d7c558c3cf03000000000000000000007e000000000000007e000000000000007d000000000000007d000000000000000100000001000000e7ea7401000000007f000001421f0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        // 加密后的512字节转成1024字节的hex的数据
        String encryptHexData = "a743a6305ca30c4a2ca8932cda347aa4768ef29bd7299efb68df1ced16a83d6bff598dcaea318da4a6c7c7fcaddd7bfc65f60c637f4df79cedee020548257d62b157076c29df961a5e9b2004365e83cdc92e58640ae0d0f8273e4a96b760ee9bd613366b0f188a3aaa030ecc7284f1cadef8d48669444d3274c95a37b61e43dc5b747ec5d78974c8a1bc70c2b090afa9723f6d77411e5c529612b0f534d8ddfdb8dbad6b8f47028d143bc5a9a5499d63d530c5b6f0dd510b9f0dbf18cf242478d333630b132e8d76dc404bbb6981a174180c45ba45371742341927d5b5384e6c1ffa3097ab8f50f1a9923b337e884d4636dc6d98c1d477caa31740cc163ee64f640439964771343f910e3143eba8836f00fc3976772dada9e6ef09596667bd1842e7c82e7d5584d54f5762218dbafc18982c75fa62759a9a0d972c1c38bea5a8a3db2f9dfe4063a6e315d061ee303c352f1498aa81b42f65a263da518dc4c6b45ea6a425489c049a45c2eacf928467122976505295bc31c4a0250ad34b9e15c71356e56baae9eda7129593b097088e0a834b5d857c73e768b6b837f5d49c18795e763d476cff5799c04b0a79e2cfa1cab904f8e3a4167e547d94e2b9a8253dc202749b23d1a4b83bead3b115e7b0e5d3110d4396121155ec3c4219bbafe0d9f07759c45760f3f617962aabfcf39625f6537acd30989acffb73b4521704d42884";

        byte[] rawByte = Hex.decodeHex(uncryptHexData);
        byte[] encryptedBytes = Native.dfslib_encrypt_byte_sector(rawByte, rawByte.length, 1);
        Assert.assertTrue(
                "encrypt ok.", StringUtils.equals(Hex.encodeHexString(encryptedBytes), encryptHexData));

        byte[] uncryptedBytes = Native.dfslib_uncrypt_byte_sector(encryptedBytes, encryptedBytes.length, 1);

        Assert.assertTrue(
                "uncrypt ok.", StringUtils.equals(Hex.encodeHexString(uncryptedBytes), uncryptHexData));
    }
}
