package io.xdag.rpc.dto;

import lombok.Data;

@Data
public class SyncingDTO {
    public String currentBlock;
    public String highestBlock;

    public SyncingDTO() {
    }

    public  SyncingDTO(String currentBlock, String highestBlock) {
        this.currentBlock = currentBlock;
        this.highestBlock = highestBlock;
    }


}
