import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);//把对象复制过来
    }

    public UTXOPool getUtxoPool() {
        return utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        double sumin = 0;
        double sumout = 0;//交易里输出脚本的value是double型
        Set<UTXO> used = new HashSet<>();
            for (int i = 0 ; i < tx.numInputs() ; i++) {//get info of input from tx
                Transaction.Input input = tx.getInput(i);
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);//construct utxo，当前交易的钱的来源集合，交易的hash和上一次的输出编号
                if (!utxoPool.contains(utxo)) return false;//check（1）本次使用的钱是否在utxopool
                Transaction.Output output = utxoPool.getTxOutput(utxo);//get info of output from utxo,utxo的输出
                if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature))
                    return false;//check（2）交易的输出output.address里存放着pk，pk says msg
                if (!used.add(utxo)) return false;//check（3）,因为hashset有个特点，不接受重复元素，add如果判断为TRUE，则会插入，否则返回FALSE
                sumin += output.value;//上一次的输出value就是本次的输入value
            }
            for (int i = 0; i < tx.getOutputs().size(); i++) {
                Transaction.Output output = tx.getOutput(i);
                if (output.value < 0) return false;//check（4）
                sumout += output.value;
            }
            if (sumin < sumout) return false;//check（5）
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    /**
     *  乱序的交易情况下，每判定加入一个交易，就要对utxopool进行一次更新
     *  所以我们需要又从头开始对每个交易进行检验
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Set<Transaction> rtx= new HashSet<>();
            for(Transaction tx : possibleTxs) {
                if (rtx.contains(tx)) continue;//如果待检验的tx本身就在有效交易集合里，就跳过该交易进入下一个交易
                if (isValidTx(tx))
                    rtx.add(tx);
                    for (int i = 0; i < tx.numOutputs();i++){
                        utxoPool.addUTXO(new UTXO(tx.getHash(),i), tx.getOutput(i));//本次有效交易对应的输出全部加入utxo
                    }
                    for(Transaction.Input input : tx.getInputs()){
                        utxoPool.removeUTXO(new UTXO(input.prevTxHash , input.outputIndex));//把本次交易对应的输入从utxopool中删除
                    }
            }
        Transaction[] result = rtx.toArray( new Transaction[]{});//因为最后要求返回的是array
        return result;
    }
    public UTXOPool getUTXOPool() {
        // IMPLEMENT THIS
        return utxoPool;
    }
}
