import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matcher.*;

/** 
* TxHandler Tester. 
* 
* @author <Authors name> 
* @since <pre>9月 21, 2019</pre> 
* @version 1.0 
*/ 
public class TxHandlerTest {
    byte[] tx_origin_hasha;
    byte[] tx_origin_hashb;
    byte[] tx_origin_hashc;
    TxHandler test_Txhandler;
    //get participaters
    Map ma = Transaction.genKeyPair();
    RSAPrivateKey ska = (RSAPrivateKey) ma.get("PRIVATE_KEY");//获取公钥私钥
    RSAPublicKey pka = (RSAPublicKey) ma.get("PUBLIC_KEY");
    Map mb = Transaction.genKeyPair();
    RSAPrivateKey skb = (RSAPrivateKey) mb.get("PRIVATE_KEY");//获取公钥私钥
    RSAPublicKey pkb = (RSAPublicKey) mb.get("PUBLIC_KEY");
    Map mc = Transaction.genKeyPair();
    RSAPrivateKey skc = (RSAPrivateKey) mc.get("PRIVATE_KEY");//获取公钥私钥
    RSAPublicKey pkc = (RSAPublicKey) mc.get("PUBLIC_KEY");

    @Before
public void before() throws Exception {
        //get initial money，铸币交易的设定
        Transaction.Output outa = new Transaction().new Output(100,pka);
        Transaction.Output outb = new Transaction().new Output(100,pkb);
        Transaction.Output outc = new Transaction().new Output(100,pkc);
        tx_origin_hasha  = new String("origin_tx_hashA").getBytes();
        tx_origin_hashb  = new String("origin_tx_hashB").getBytes();
        tx_origin_hashc  = new String("origin_tx_hashC").getBytes();
        UTXOPool upool = new UTXOPool();
        upool.addUTXO(new UTXO(tx_origin_hasha,0),outa);
        upool.addUTXO(new UTXO(tx_origin_hashb,0),outb);
        upool.addUTXO(new UTXO(tx_origin_hashc,0),outc);
        test_Txhandler = new TxHandler(upool);
}
@After
public void after() throws Exception {
}

/*
verify a valid single tx
the following are all valid
 */
//test one in one out tx
@Test
public void testOneInOneOutIsValidTx() throws Exception {
    Transaction trans_a_to_b = new Transaction();
    trans_a_to_b.addInput(tx_origin_hasha,0);
    trans_a_to_b.addOutput(100,pkb);
    trans_a_to_b.GETsign(ska,0);
    trans_a_to_b.finalize();
    Transaction[] trans = {trans_a_to_b};
    Transaction[] expect = {trans_a_to_b};
    assertThat(test_Txhandler.handleTxs(trans), is(expect));
}
    //test one in multi out tx
    @Test
    public void testOneInMultiOutIsValidTx() throws Exception {
        Transaction trans_a_to_bc = new Transaction();
        trans_a_to_bc.addInput(tx_origin_hasha,0);
        trans_a_to_bc.addOutput(50,pkb);
        trans_a_to_bc.addOutput(50,pkc);
        trans_a_to_bc.GETsign(ska,0);
        trans_a_to_bc.finalize();
        Transaction[] trans = {trans_a_to_bc};
        Transaction[] expect = {trans_a_to_bc};
        assertThat(test_Txhandler.handleTxs(trans), is(expect));
    }

    //test multi in one out tx
    @Test
    public void testMultiInOneOutIsValidTx() throws Exception {
        Transaction trans_ac_to_b = new Transaction();
        trans_ac_to_b.addInput(tx_origin_hasha,0);
        trans_ac_to_b.addInput(tx_origin_hashc,0);
        trans_ac_to_b.addOutput(100,pkb);
        trans_ac_to_b.GETsign(ska,0);
        trans_ac_to_b.GETsign(skc,1);
        trans_ac_to_b.finalize();
        Transaction[] trans = {trans_ac_to_b};
        Transaction[] expect = {trans_ac_to_b};
        assertThat(test_Txhandler.handleTxs(trans), is(expect));
    }

    //test multi in multi out tx
    @Test
    public void testMultiInMultiOutIsValidTx() throws Exception {
    //for one tx,a to c,c to b
        Transaction trans_ac_to_cb = new Transaction();
        trans_ac_to_cb.addInput(tx_origin_hasha,0);
        trans_ac_to_cb.addInput(tx_origin_hashc,0);
        trans_ac_to_cb.addOutput(100,pkc);
        trans_ac_to_cb.addOutput(100,pkb);
        trans_ac_to_cb.GETsign(ska,0);
        trans_ac_to_cb.GETsign(skc,1);
        trans_ac_to_cb.finalize();
        Transaction[] trans = {trans_ac_to_cb};
        Transaction[] expect = {trans_ac_to_cb};
        assertThat(test_Txhandler.handleTxs(trans), is(expect));
    }

   /*
verify a tx with 5 conditions whitch are given in the requirements of "isValidTx"
the following are all invalid
 */

//test the signatures on each input of {@code tx} are invalid
     @Test
   public void testSignatureInValid() throws Exception {
       Transaction trans_a_to_b = new Transaction();
       trans_a_to_b.addInput(tx_origin_hasha,0);
       trans_a_to_b.addOutput(100,pkb);
       trans_a_to_b.GETsign(skc,0);
       trans_a_to_b.finalize();
       Transaction[] trans = {trans_a_to_b};
       Transaction[] expect = {};
       assertThat(test_Txhandler.handleTxs(trans), is(expect));
   }

    //test the output values are negative
    @Test
    public void testNegativeOutput() throws Exception {
        Transaction trans_a_to_b = new Transaction();
        trans_a_to_b.addInput(tx_origin_hasha,0);
        trans_a_to_b.addOutput(-100,pkb);
        trans_a_to_b.GETsign(ska,0);
        trans_a_to_b.finalize();
        Transaction[] trans = {trans_a_to_b};
        Transaction[] expect = {};
        assertThat(test_Txhandler.handleTxs(trans), is(expect));
    }

    //test the shorage of input values
    @Test
    public void testInputShorage() throws Exception {
        Transaction trans_a_to_b = new Transaction();
        trans_a_to_b.addInput(tx_origin_hasha,0);
        trans_a_to_b.addOutput(200,pkb);
        trans_a_to_b.GETsign(ska,0);
        trans_a_to_b.finalize();
        Transaction[] trans = {trans_a_to_b};
        Transaction[] expect = {};
        assertThat(test_Txhandler.handleTxs(trans), is(expect));
    }

    //test double spending with 1 tx
    @Test
    public void tesDoubleSpending1() throws Exception {
       //a to b with 50,and a to c with 70 while a only has 100
        Transaction trans_a_to_bc = new Transaction();
        trans_a_to_bc.addInput(tx_origin_hasha,0);
        trans_a_to_bc.addOutput(100,pkb);
        trans_a_to_bc.addOutput(100,pkc);
        trans_a_to_bc.GETsign(ska,0);
        trans_a_to_bc.finalize();
        Transaction[] trans = {trans_a_to_bc};
        Transaction[] expect = {};
        assertThat(test_Txhandler.handleTxs(trans), is(expect));
    }


  //test doublespending with 2tx using handleTxs to get the valid array of tx
@Test
public void tesDoubleSpending2() throws Exception {
    //tx a to b 100
    Transaction trans_a_to_b = new Transaction();
    trans_a_to_b.addInput(tx_origin_hasha,0);
    trans_a_to_b.addOutput(100,pkb);
    trans_a_to_b.GETsign(ska,0);
    trans_a_to_b.finalize();
    //tx a to c 100
    Transaction trans_a_to_c = new Transaction();
    trans_a_to_c.addInput(tx_origin_hasha,0);
    trans_a_to_c.addOutput(100,pkc);
    trans_a_to_c.GETsign(ska,0);
    trans_a_to_c.finalize();
    Transaction[] trans = {trans_a_to_b,trans_a_to_c};
    Transaction[] expect = {trans_a_to_b};
    assertThat(test_Txhandler.handleTxs(trans),is(expect));
}
}
