package io.xdag.rpc.modules;

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
import io.xdag.rpc.dto.BlockResultDTO;
import io.xdag.rpc.dto.StatusDTO;
import io.xdag.rpc.dto.SyncingDTO;
import io.xdag.rpc.dto.TransactionReceiptDTO;
import io.xdag.rpc.modules.xdag.XdagModule;
import io.xdag.rpc.utils.TypeConverter;
import io.xdag.utils.Numeric;
import io.xdag.utils.XdagTime;
import io.xdag.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
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
import static io.xdag.core.ImportResult.IMPORTED_NOT_BEST;
import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_IN;
import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_OUT;
import static io.xdag.utils.BasicUtils.xdag2amount;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

@Slf4j
public class XdagModuleTest {

    static class MockBlockchain extends BlockchainImpl {
        public MockBlockchain(Kernel kernel) {
            super(kernel);
        }
        @Override
        public void startCheckMain() {
        }
    }

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

    public void createBlock() {
        long generateTime = 1600616700000L;
        ECKeyPair key = ECKeyPair.create(Numeric.toBigInt(SampleKeys.PRIVATE_KEY_STRING));
        MockBlockchain blockchain = new MockBlockchain(kernel);
        kernel.setBlockchain(blockchain);
        XdagTopStatus stats = blockchain.getXdagTopStatus();
        assertNotNull(stats);
        List<Address> pending = Lists.newArrayList();

        ImportResult result;
        log.debug("1. create 1 address block");
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
            log.debug("create No." + i + " extra block");
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

    @Test
    public void accountsTest() {
        assertEquals(100, xdagModule.accounts().length);
    }

    @Test
    public void syncingTest() {
        assertEquals(xdagModule.syncing(),false);
        kernel.setXdagState(XdagState.STST);
        assertEquals(xdagModule.syncing(), new SyncingDTO("0x63", "0x63"));
    }

    @Test
    public void getBlock() {
        BlockResultDTO expectedBrDTOByNum = new BlockResultDTO("0x3","0x82a2a6637545d166fe8457359ad8886b586009a704b45a85dca7f2689ea2368b","0x0000000000000000fe8457359ad8886b586009a704b45a85dca7f2689ea2368b",
                "0x40000000000","0x10d0902307","0x0","0x17d9de5ffff","","","63");
        BlockResultDTO expectedBrDTOByHash = new BlockResultDTO("0x3","0x82a2a6637545d166fe8457359ad8886b586009a704b45a85dca7f2689ea2368b","0x0000000000000000fe8457359ad8886b586009a704b45a85dca7f2689ea2368b",
                "0x40000000000","0x10d0902307","0x0","0x17d9de5ffff","","00000000000000003855000000000040ffffe59d7d0100000000000000000000750c78d293b5454c452427aef7d6cbf519a240a91d5f8e680000000000000000159e0808ec962e4ea8aa33b38fe2c2018aec149f92a67cb5f47ebd989d57f60109ea74273a40499c389b43c7dc44b769ecb3337de0b57e93fded0222075a8c84000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003a103a4e5729ad68c02a678ae39accfbc0ae208096437401b7ceab63cca0622f","63");
        BlockResultDTO brDTOByNum = xdagModule.getBlockByNumber(String.valueOf(3),false);
        BlockResultDTO brDTOByHash = xdagModule.getBlockByHash(brDTOByNum.getHashlow(),true);
        assertEquals(expectedBrDTOByNum,brDTOByNum);
        assertEquals(expectedBrDTOByHash,brDTOByHash);
    }

    @Test
    public void coinBase() {
        String expectedCoinBase = "0x00000000000000004b0f3b0c39b2b6cada1ec24f791510257cfed96ec4a6d2f6";
        assertEquals(expectedCoinBase,xdagModule.getCoinBase());
    }

    @Test
    public void sendRawTransaction() {
        Block bt = createTransaction();
        ImportResult importResult = IMPORTED_NOT_BEST;
        byte[] hash = TypeConverter.stringHexToByteArray("0x00000000000000002c230cb7d65fc7d35ff670e42f57af538e60634dab4ffc39");
        TransactionReceiptDTO transactionReceiptDTO = new TransactionReceiptDTO(hash,importResult);
        assertEquals(transactionReceiptDTO,xdagModule.sendRawTransaction(Hex.toHexString(bt.getXdagBlock().getData())));
    }

    @Test
    public void getStatus() {
        StatusDTO expectedStatusDTO = new StatusDTO(101,99, BigInteger.valueOf(4199877108476L),101,99,BigInteger.valueOf(4199877108476L),435406604599296L);
        assertEquals(expectedStatusDTO,xdagModule.getStatus());
    }


    public Block createTransaction() {
        // make one transaction(100 XDAG) block(from No.1 mainblock to address block)
        Address from  = new Address(kernel.getBlockchain().getBlockByHeight(2).getHashLow(), XDAG_FIELD_IN);
        Address to = new Address(kernel.getPoolMiner().getAddressHaashLow(), XDAG_FIELD_OUT);
        long xdagTime = XdagTime.getEndOfEpoch(XdagTime.msToXdagtimestamp(1600616700000L+100*64000));
        ECKeyPair poolKey = ECKeyPair.create(Numeric.toBigInt(SampleKeys.PRIVATE_KEY_STRING));
        return generateTransactionBlock(config, poolKey, xdagTime - 1, from, to, xdag2amount(100.00));
    }
}
