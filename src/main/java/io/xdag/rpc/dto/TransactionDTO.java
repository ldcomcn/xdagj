package io.xdag.rpc.dto;

import lombok.Data;

import static io.xdag.config.Constants.*;
import static io.xdag.config.Constants.BI_MAIN_REF;

@Data
public class TransactionDTO {
    private final String hash;
    private final String state;

    public TransactionDTO(String hash, String state) {
        this.hash = hash;
        this.state = state;
    }

    public static TransactionDTO getTransactionDTO(String hash, String flags) {
        String state = getStateByFlags(Integer.parseInt(flags));
        return new TransactionDTO(hash,state);
    }

    private static String getStateByFlags(int flags) {
        int flag = flags & ~(BI_OURS | BI_REMARK |BI_MAIN |BI_MAIN_CHAIN);
        // 1C
        if (flag == (BI_REF | BI_MAIN_REF | BI_APPLIED)) {
            return "Accepted";
        }
        // 18
        if (flag == (BI_REF | BI_MAIN_REF)) {
            return "Rejected";
        }
        return "Pending";
    }
}
