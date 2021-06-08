package io.xdag.rpc.modules.xdag;

import io.xdag.Kernel;
import io.xdag.core.Block;

import java.util.*;
import java.util.stream.Collectors;

import static io.xdag.rpc.utils.TypeConverter.toQuantityJsonHex;
import static io.xdag.rpc.utils.TypeConverter.toUnformattedJsonHex;

public class XdagModuleWalletEnabled implements XdagModuleWallet{

    final private Kernel kernel;

    public XdagModuleWalletEnabled(Kernel kernel) {
        this.kernel = kernel;
    }


    // TODO: account just contains the lowhash info.
    //  eg: 0000000000000000a54ddd7c7a7bdbb22366fa2516cf648f32641623580b67e3
    public String[] accounts(long num) {
        Set<Block> ours = new HashSet<>();
        kernel.getBlockStore().fetchOurBlocks(pair -> {
            Block block = pair.getValue();
            ours.add(block);
            return false;
        });

        List<Block> list = new ArrayList<>(ours);
        // 按balance降序排序
        List<String> blockList;
        if (num >= 0) {
            blockList = list.stream().sorted((o1, o2) -> (int) (o2.getInfo().getAmount() - o1.getInfo().getAmount())).map(block -> toUnformattedJsonHex(block.getHashLow()))
                    .limit(num).collect(Collectors.toList());
        } else {
            blockList = list.stream().sorted((o1, o2) -> (int) (o2.getInfo().getAmount() - o1.getInfo().getAmount())).map(block -> toUnformattedJsonHex(block.getHashLow()))
                    .collect(Collectors.toList());
        }

        return blockList.toArray(String[]::new);
    }

    @Override
    public String[] accounts() {
       return accounts(-1);
    }

    @Override
    public String sign(String addr, String data) {
        return null;
    }

    @Override
    public String getTotalBalance() {
        long balance = kernel.getBlockchain().getXdagStats().getBalance();
        return toQuantityJsonHex(balance);
    }
}
