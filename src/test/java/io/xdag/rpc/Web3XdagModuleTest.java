package io.xdag.rpc;

import com.google.common.collect.Lists;
import io.xdag.Kernel;
import io.xdag.config.Config;
import io.xdag.config.DevnetConfig;
import io.xdag.core.*;
import io.xdag.crypto.ECKeyPair;
import io.xdag.crypto.SampleKeys;
import io.xdag.crypto.jni.Native;
import io.xdag.db.DatabaseFactory;
import io.xdag.db.DatabaseName;
import io.xdag.db.rocksdb.RocksdbFactory;
import io.xdag.db.store.BlockStore;
import io.xdag.db.store.OrphanPool;
import io.xdag.mine.miner.Miner;
import io.xdag.rpc.dto.SyncingDTO;
import io.xdag.rpc.dto.TransactionDTO;
import io.xdag.rpc.modules.XdagModuleTest;
import io.xdag.rpc.modules.web3.Web3XdagModule;
import io.xdag.rpc.modules.web3.Web3XdagModuleImpl;
import io.xdag.rpc.modules.xdag.XdagModule;
import io.xdag.rpc.modules.xdag.XdagModuleTransactionEnabled;
import io.xdag.rpc.modules.xdag.XdagModuleWalletDisabled;
import io.xdag.utils.BytesUtils;
import io.xdag.utils.Numeric;
import io.xdag.utils.XdagTime;
import io.xdag.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.xdag.BlockBuilder.*;
import static io.xdag.core.ImportResult.IMPORTED_BEST;
import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_IN;
import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_OUT;
import static io.xdag.utils.BasicUtils.xdag2amount;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

public class Web3XdagModuleTest {

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    Config config = new DevnetConfig();
    Wallet wallet;
    String pwd;
    Kernel kernel;
    DatabaseFactory dbFactory;
    XdagModule xdagModule;

    @Before
    public void setUp() throws Exception {
        config.getNodeSpec().setStoreDir(root.newFolder().getAbsolutePath());
        config.getNodeSpec().setStoreBackupDir(root.newFolder().getAbsolutePath());

        Native.init(config);
        if (Native.dnet_crypt_init() < 0) {
            throw new Exception("dnet crypt init failed");
        }
        pwd = "password";
        wallet = new Wallet(config);
        wallet.unlock(pwd);
        ECKeyPair key = ECKeyPair.create(Numeric.toBigInt(SampleKeys.PRIVATE_KEY_STRING));
        wallet.setAccounts(Collections.singletonList(key));
        wallet.flush();

        kernel = new Kernel(config);
        dbFactory = new RocksdbFactory(config);

        BlockStore blockStore = new BlockStore(
                dbFactory.getDB(DatabaseName.INDEX),
                dbFactory.getDB(DatabaseName.TIME),
                dbFactory.getDB(DatabaseName.BLOCK));

        blockStore.reset();
        OrphanPool orphanPool = new OrphanPool(dbFactory.getDB(DatabaseName.ORPHANIND));
        orphanPool.reset();

        kernel.setBlockStore(blockStore);
        kernel.setOrphanPool(orphanPool);
        kernel.setWallet(wallet);
        createBlock();
    }

    @Test
    public void syncingTest() throws Exception {
        Web3XdagModule web3XdagModule = createWeb3XdagModule(xdagModule,"0.4.3",2021);

        assertEquals(Integer.toString(2021),web3XdagModule.xdag_chainId());
        assertEquals("0.4.3",web3XdagModule.xdag_protocolVersion());
        assertEquals(100,web3XdagModule.xdag_accounts().length);
        assertEquals("0x65",web3XdagModule.xdag_blockNumber());
        assertEquals("0x00000000000000004b0f3b0c39b2b6cada1ec24f791510257cfed96ec4a6d2f6",web3XdagModule.xdag_coinbase());
        assertEquals("0x40000000000",web3XdagModule.xdag_getBalance(web3XdagModule.xdag_coinbase()));

        assertEquals(false,web3XdagModule.xdag_syncing());
        kernel.setXdagState(XdagState.STST);
        assertEquals(new SyncingDTO("0x63", "0x63"),web3XdagModule.xdag_syncing());

        assertEquals("0x18c0000000000",web3XdagModule.xdag_getTotalBalance());

        Block tx = createTransaction();

        TransactionDTO expectedTransactionDTO = new TransactionDTO("0x00000000000000002c230cb7d65fc7d35ff670e42f57af538e60634dab4ffc39","Pending");
        assertEquals(expectedTransactionDTO,web3XdagModule.xdag_getTransactionByHash(web3XdagModule.xdag_sendRawTransaction(BytesUtils.toHexString(tx.getXdagBlock().getData())),true));

    }

    public Block createTransaction() {
        // make one transaction(100 XDAG) block(from No.1 mainblock to address block)
        Address from  = new Address(kernel.getBlockchain().getBlockByHeight(2).getHashLow(), XDAG_FIELD_IN);
        Address to = new Address(kernel.getPoolMiner().getAddressHaashLow(), XDAG_FIELD_OUT);
        long xdagTime = XdagTime.getEndOfEpoch(XdagTime.msToXdagtimestamp(1600616700000L+100*64000));
        ECKeyPair poolKey = ECKeyPair.create(Numeric.toBigInt(SampleKeys.PRIVATE_KEY_STRING));
        return generateTransactionBlock(config, poolKey, xdagTime - 1, from, to, xdag2amount(100.00));
    }

    static class MockBlockchain extends BlockchainImpl {
        public MockBlockchain(Kernel kernel) {
            super(kernel);
        }
        @Override
        public void startCheckMain() {
        }
    }

    private Web3XdagModule createWeb3XdagModule(XdagModule module,String version,long chainID) {
        return new Web3XdagModuleImpl(module,version,Long.toString(chainID));
    }


    public void createBlock() {
        long generateTime = 1600616700000L;
        ECKeyPair key = ECKeyPair.create(Numeric.toBigInt(SampleKeys.PRIVATE_KEY_STRING));
        MockBlockchain blockchain = new MockBlockchain(kernel);
        kernel.setBlockchain(blockchain);
        XdagTopStatus stats = blockchain.getXdagTopStatus();
        assertNotNull(stats);
        List<Address> pending = Lists.newArrayList();

        ImportResult result;
        Block addressBlock = generateAddressBlock(config, key, generateTime);


        kernel.setPoolMiner(new Miner(addressBlock.getHash()));
        xdagModule = new XdagModule(kernel);

        // 1. add address block
        result = blockchain.tryToConnect(addressBlock);
        assertSame(result, IMPORTED_BEST);
        assertArrayEquals(addressBlock.getHashLow(), stats.getTop());
        List<Block> extraBlockList = Lists.newLinkedList();
        byte[] ref = addressBlock.getHashLow();
        // 2. create 100 mainblocks
        for(int i = 1; i <= 100; i++) {
            generateTime += 64000L;
            pending.clear();
            pending.add(new Address(ref, XDAG_FIELD_OUT));
            long time = XdagTime.msToXdagtimestamp(generateTime);
            long xdagTime = XdagTime.getEndOfEpoch(time);
            Block extraBlock = generateExtraBlock(config, key, xdagTime, pending);
            result = blockchain.tryToConnect(extraBlock);
            assertSame(result, IMPORTED_BEST);
            assertArrayEquals(extraBlock.getHashLow(), stats.getTop());
            Block storedExtraBlock = blockchain.getBlockByHash(stats.getTop(), false);
            assertArrayEquals(extraBlock.getHashLow(), storedExtraBlock.getHashLow());
            ref = extraBlock.getHashLow();
            extraBlockList.add(extraBlock);
        }
    }
}
