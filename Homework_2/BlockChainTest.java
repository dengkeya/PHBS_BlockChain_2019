import static org.hamcrest.Matcher.*;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.lang.reflect.Array;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import sun.security.rsa.RSAPublicKeyImpl;
import sun.security.rsa.RSAPrivateKeyImpl;
import java.security.*;
import java.security.Signature;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;

/** 
* BlockChain Tester. 
* 
* @author <Authors name> 
* @since <pre>10月 18, 2019</pre> 
* @version 1.0 
*/ 
public class BlockChainTest {
    BlockChain chain;
    BlockHandler blockHandler;
    Block genesisblock;
    int N = 5;
    KeyPairGenerator GEN = KeyPairGenerator.getInstance("RSA");
    ArrayList<KeyPair> users_key = new ArrayList<KeyPair>();
    Signature[] users_sign = new  Signature[N];




//    KeyPair pk_a = KeyPairGenerator.getInstance("RSA").generateKeyPair();
//    KeyPair pk_b = KeyPairGenerator.getInstance("RSA").generateKeyPair();
//    KeyPair pk_c = KeyPairGenerator.getInstance("RSA").generateKeyPair();
//    Signature signature_a = Signature.getInstance("SHA256withRSA");
//    Signature signature_b = Signature.getInstance("SHA256withRSA");
//    Signature signature_c = Signature.getInstance("SHA256withRSA");





    public BlockChainTest() throws NoSuchAlgorithmException {
    }

    @Before
public void before() throws Exception {
        for(int i = 0; i < N; i++){
            users_key.add(GEN.generateKeyPair());
            users_sign[i] = Signature.getInstance("SHA256withRSA");
            users_sign[i].initSign((RSAPrivateKey)GEN.generateKeyPair().getPrivate());
        }
        genesisblock = new Block(null, users_key.get(0).getPublic());
        genesisblock.finalize();
        chain = new BlockChain(genesisblock);
        blockHandler = new BlockHandler(chain);
} 

@After
public void after() throws Exception { 
} 


@Test
//交易角度：一个block包含有效交易
public void test1() throws Exception {
    Transaction tx = new Transaction();
    tx.addInput(genesisblock.getCoinbase().getHash(),0);
    tx.addOutput(1,users_key.get(1).getPublic());
    users_sign[0].update(tx.getRawDataToSign(0));
    tx.addSignature(users_sign[0].sign(), 0);
    tx.finalize();
    blockHandler.processTx(tx);
    //    block
    Block block = blockHandler.createBlock(users_key.get(0).getPublic());
    block.finalize();
    assertThat(blockHandler.processBlock(block), is(true));
}
    @Test
    //交易角度：简单的情况在homework1里已经验证，这里仅仅验证一个block里包含双花问题即可
    public void test2() throws Exception {
        Transaction tx_1 = new Transaction();
        tx_1.addInput(genesisblock.getCoinbase().getHash(), 0);
        tx_1.addOutput(25,users_key.get(1).getPublic());
        users_sign[0].update(tx_1.getRawDataToSign(0));
        tx_1.addSignature(users_sign[0].sign(),0);
        tx_1.finalize();
        blockHandler.processTx(tx_1);
        Transaction tx_2 = new Transaction();
        tx_2.addInput(genesisblock.getCoinbase().getHash(), 0);
        tx_2.addOutput(25,users_key.get(2).getPublic());
        users_sign[0].update(tx_2.getRawDataToSign(0));
        tx_2.addSignature(users_sign[0].sign(),0);
        tx_2.finalize();
        blockHandler.processTx(tx_2);
        Block block = new Block(genesisblock.getHash(), users_key.get(0).getPublic());
        block.addTransaction(tx_1);
        block.addTransaction(tx_2);
        block.finalize();
        assertThat(blockHandler.processBlock(block), is(false));
    }

    @Test
    //区块角度：无交易空白块
    public void test3() throws Exception {
        Block block = new Block(genesisblock.getHash(), users_key.get(0).getPublic());
        block.finalize();
        assertThat(blockHandler.processBlock(block), is(true));
    }

    @Test
    //区块角度：新加入创世块
    public void test4() throws Exception {
        Block block = new Block(null, users_key.get(0).getPublic());
        block.finalize();
        assertThat(blockHandler.processBlock(block), is(false));
    }

    @Test
    //区块角度：invalid prevhash
    public void test5() throws Exception {
        byte[] hash = genesisblock.getHash();
        byte[] hashCopy = Arrays.copyOf(hash, hash.length);
        hashCopy[0]++;
        Block block = new Block(hashCopy, users_key.get(1).getPublic());
        block.finalize();
        assertThat(blockHandler.processBlock(block), is(false));
    }

    @Test
    //结构角度：可以形成一条链,height = 5
    public void test6() throws Exception {
        Block prevBlock = genesisblock;
        for (int i = 0; i < 5; i++) {
            Transaction tx = new Transaction();
            tx.addInput(prevBlock.getCoinbase().getHash() , 0);
            tx.addOutput(25, users_key.get(1).getPublic());
            users_sign[0].update(tx.getRawDataToSign(0));
            tx.addSignature(users_sign[0].sign() , 0);
            tx.finalize();
            blockHandler.processTx(tx);
            Block block = blockHandler.createBlock(users_key.get(0).getPublic());
            block.finalize();
            assertThat(blockHandler.processBlock(block), is(true));
            prevBlock = block;
        }
        assertThat(chain.height, is(6));
    }

    @Test
    //结构角度：一条链，当个数超过cutoff_age之后(height = 16)，有一个块接入创世块后面，分叉接入失败
    public void test7() throws Exception {
        Block prevBlock = genesisblock;
        for (int i = 1; i < 10 + 6; i++) {
            Transaction tx = new Transaction();
            tx.addInput(prevBlock.getCoinbase().getHash() , 0);
            tx.addOutput(25, users_key.get(1).getPublic());
            users_sign[0].update(tx.getRawDataToSign(0));
            tx.addSignature(users_sign[0].sign() , 0);
            tx.finalize();
            blockHandler.processTx(tx);
            Block block = blockHandler.createBlock(users_key.get(0).getPublic());
            block.finalize();
            //assertThat(blockHandler.processBlock(block), is(true));
            prevBlock = block;
            //System.out.println(i);
        }//以上是一条合法链
        Block block = new Block(genesisblock.getHash(), users_key.get(0).getPublic());
        block.finalize();
        assertThat(chain.height, is(16));
        assertThat(blockHandler.processBlock(block), is(false));
    }

    @Test
    //结构角度： //Create a block after a valid transaction has been processed
    //    //that uses a UTXO already claimed by a transaction
    //    //in the longest valid branch   新接入的branch里有交易用了已经接入主链里交易的utxo的情况
    public void test8() throws Exception {
        Block prevBlock = genesisblock;
        for (int i = 1; i < 5 ; i++) {
            Transaction tx = new Transaction();
            tx.addInput(prevBlock.getCoinbase().getHash() , 0);
            tx.addOutput(25, users_key.get(1).getPublic());
            users_sign[0].update(tx.getRawDataToSign(0));
            tx.addSignature(users_sign[0].sign() , 0);
            tx.finalize();
            blockHandler.processTx(tx);
            Block block = blockHandler.createBlock(users_key.get(0).getPublic());
            block.finalize();
            //assertThat(blockHandler.processBlock(block), is(true));
            prevBlock = block;
            //System.out.println(i);
        }//以上是一条合法链
        //tx: 1 to 2 with 25块钱
        Transaction tx_1_to_2 = new Transaction();
        tx_1_to_2.addInput(genesisblock.getCoinbase().getHash(),0);
        tx_1_to_2.addOutput(25 ,users_key.get(2).getPublic());
        users_sign[0].update(tx_1_to_2.getRawDataToSign(0));
        tx_1_to_2.addSignature(users_sign[1].sign(),0);
        tx_1_to_2.finalize();
        blockHandler.processTx(tx_1_to_2);
        Block block_mainchain = blockHandler.createBlock(users_key.get(0).getPublic());
        block_mainchain.finalize();
        assertThat(chain.height, is(6));

        //tx: 1 to 3 with 25块钱
        Transaction tx_1_to_3 = new Transaction();
        tx_1_to_3.addInput(genesisblock.getCoinbase().getHash(),0);
        tx_1_to_3.addOutput(25 ,users_key.get(3).getPublic());
        users_sign[1].update(tx_1_to_3.getRawDataToSign(0));
        tx_1_to_3.addSignature(users_sign[1].sign(),0);
        tx_1_to_3.finalize();
        blockHandler.processTx(tx_1_to_3);
        Block block_branch = new Block(genesisblock.getHash() , users_key.get(1).getPublic());
        block_branch.addTransaction(tx_1_to_3);
        block_branch.finalize();
        assertThat(blockHandler.processBlock(block_branch), is(false));

    }

    @Test
    //结构角度：相同长度的链，会选先创建的那一条作为主链//Construct two branches of approximately equal size,
    //    //ensuring that blocks are always created on the proper branch
    public void test9() throws Exception {
        //创建一个长度为5的chain
        for (int i = 0; i < 5 ; i++) {
            Block block_early = blockHandler.createBlock(users_key.get(0).getPublic());
            block_early.finalize();
        }//以上是一条合法链
        assertThat(chain.height, is(6));

        Block prevBlock = genesisblock;
        for (int i = 0; i < 5 ; i++) {
            Block block_late = new Block(prevBlock.getHash() , users_key.get(1).getPublic());
            block_late.finalize();
            blockHandler.processBlock(block_late);
            prevBlock = block_late;
        }//以上是一条合法链
        assertThat(chain.getMaxHeightBlock() != prevBlock, is(true));
    }

    @Test
    //结构角度：a txpool with valid txs and invalid txs
    public void test10() throws Exception {
        //valid tx: 0 to 2 with 25块钱
        Transaction tx_0_to_2 = new Transaction();
        tx_0_to_2.addInput(genesisblock.getCoinbase().getHash(),0);
        tx_0_to_2.addOutput(25 ,users_key.get(2).getPublic());
        users_sign[0].update(tx_0_to_2.getRawDataToSign(0));
        tx_0_to_2.addSignature(users_sign[0].sign(),0);
        tx_0_to_2.finalize();
        //valid tx: 2 to 3 with 25块钱
        Transaction tx_2_to_3 = new Transaction();
        tx_2_to_3.addInput(tx_0_to_2.getHash(),0);
        tx_2_to_3.addOutput(25 ,users_key.get(3).getPublic());
        users_sign[2].update(tx_2_to_3.getRawDataToSign(0));
        tx_2_to_3.addSignature(users_sign[2].sign(),0);
        tx_2_to_3.finalize();
        //invalid tx: 3 to 4 with 40块钱
        Transaction tx_3_to_4 = new Transaction();
        tx_3_to_4.addInput(tx_2_to_3.getHash(),0);
        tx_3_to_4.addOutput(40 ,users_key.get(4).getPublic());
        users_sign[3].update(tx_3_to_4.getRawDataToSign(0));
        tx_3_to_4.addSignature(users_sign[3].sign(),0);
        tx_3_to_4.finalize();
        blockHandler.processTx(tx_0_to_2);
        blockHandler.processTx(tx_2_to_3);
        blockHandler.processTx(tx_3_to_4);
//        Block block = new Block(genesisblock.getHash() , users_key.get(1).getPublic());
        Block block = blockHandler.createBlock(users_key.get(0).getPublic());
        block.finalize();
        //blockHandler.processBlock(block);
        assertThat(chain.getTransactionPool().getTransactions().size() != block.getTransactions().size(), is(true));
    }

    @Test
    //结构角度：多个分叉的复杂结构
    public void test11() throws Exception {
        //height=16的一条主链
        for (int i = 1; i < 10 + 6 ; i++) {
            Transaction tx = new Transaction();
            tx.addInput(genesisblock.getCoinbase().getHash() , 0);
            tx.addOutput(25, users_key.get(1).getPublic());
            users_sign[0].update(tx.getRawDataToSign(0));
            tx.addSignature(users_sign[0].sign() , 0);
            tx.finalize();
            blockHandler.processTx(tx);
            Block block_mainchain = blockHandler.createBlock(users_key.get(0).getPublic());
            block_mainchain.finalize();
        }
        assertThat(chain.height, is(16));

        //长度为5的branch_1，接在主链倒数第二个块上
        byte[] prevBlockHash = chain.getMaxHeightBlock().getPrevBlockHash();
        for (int i = 1; i < 5 ; i++) {
            Block block_branch_1 = new Block(prevBlockHash, users_key.get(1).getPublic());
            block_branch_1.finalize();
            blockHandler.processBlock(block_branch_1);
            prevBlockHash = block_branch_1.getHash();
            assertThat(blockHandler.processBlock(block_branch_1), is(true));
        }

        //长度为3的branch_2，接在主链创世块上，失败
        prevBlockHash = genesisblock.getHash();
        for (int i = 1; i < 3 ; i++) {
            Block block_branch_2 = new Block(prevBlockHash, users_key.get(1).getPublic());
            block_branch_2.finalize();
            blockHandler.processBlock(block_branch_2);
            prevBlockHash = block_branch_2.getHash();
            assertThat(blockHandler.processBlock(block_branch_2), is(false));
        }

        //单块接入最后一个块，2个单块 with valid tx ，1个单块 with invalid tx
        //valid tx: 0 to 2 with 25块钱
        Transaction tx_0_to_2 = new Transaction();
        tx_0_to_2.addInput(genesisblock.getCoinbase().getHash(),0);
        tx_0_to_2.addOutput(25 ,users_key.get(2).getPublic());
        users_sign[0].update(tx_0_to_2.getRawDataToSign(0));
        tx_0_to_2.addSignature(users_sign[0].sign(),0);
        tx_0_to_2.finalize();
        blockHandler.processTx(tx_0_to_2);

        Block block_1 = blockHandler.createBlock(users_key.get(0).getPublic());
        block_1.finalize();

        //valid tx: 2 to 3 with 25块钱
        Transaction tx_2_to_3 = new Transaction();
        tx_2_to_3.addInput(tx_0_to_2.getHash(),0);
        tx_2_to_3.addOutput(25 ,users_key.get(3).getPublic());
        users_sign[2].update(tx_2_to_3.getRawDataToSign(0));
        tx_2_to_3.addSignature(users_sign[2].sign(),0);
        tx_2_to_3.finalize();
        blockHandler.processTx(tx_2_to_3);

        Block block_2 = blockHandler.createBlock(users_key.get(0).getPublic());
        block_2.finalize();


        //invalid tx: 3 to 4 with 40块钱
        Transaction tx_3_to_4 = new Transaction();
        tx_3_to_4.addInput(tx_2_to_3.getHash(),0);
        tx_3_to_4.addOutput(40 ,users_key.get(4).getPublic());
        users_sign[3].update(tx_3_to_4.getRawDataToSign(0));
        tx_3_to_4.addSignature(users_sign[3].sign(),0);
        tx_3_to_4.finalize();
        blockHandler.processTx(tx_3_to_4);

        Block block_3 = new Block(prevBlockHash, users_key.get(0).getPublic());
        block_3.addTransaction(tx_3_to_4);
        block_3.finalize();

        assertThat(blockHandler.processBlock(block_1), is(true));
        assertThat(blockHandler.processBlock(block_2), is(true));
        assertThat(blockHandler.processBlock(block_3), is(false));

    }
}




