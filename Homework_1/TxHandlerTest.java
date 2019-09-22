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
* @since <pre>9�� 21, 2019</pre> 
* @version 1.0 
*/ 
public class TxHandlerTest {
    byte[] tx_origin_hasha;
    byte[] tx_origin_hashb;
    byte[] tx_origin_hashc;
    TxHandler test_Txhandler;
    //get participaters
    Map ma = Transaction.genKeyPair();
    RSAPrivateKey ska = (RSAPrivateKey) ma.get("PRIVATE_KEY");//��ȡ��Կ˽Կ
    RSAPublicKey pka = (RSAPublicKey) ma.get("PUBLIC_KEY");
    Map mb = Transaction.genKeyPair();
    RSAPrivateKey skb = (RSAPrivateKey) mb.get("PRIVATE_KEY");//��ȡ��Կ˽Կ
    RSAPublicKey pkb = (RSAPublicKey) mb.get("PUBLIC_KEY");
    Map mc = Transaction.genKeyPair();
    RSAPrivateKey skc = (RSAPrivateKey) mc.get("PRIVATE_KEY");//��ȡ��Կ˽Կ
    RSAPublicKey pkc = (RSAPublicKey) mc.get("PUBLIC_KEY");

    @Before
public void before() throws Exception {
        //get initial money�����ҽ��׵��趨
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

//test transaction net as a challege for myself
    @Test
    public void testTxNet() throws Exception {
        byte[] tx_origin_hashd;
        byte[] tx_origin_hashe;
        byte[] tx_origin_hashf;
        byte[] tx_origin_hashg;
        byte[] tx_origin_hashh;
        byte[] tx_origin_hashi;
        //get participaters
        Map md = Transaction.genKeyPair();
        RSAPrivateKey skd = (RSAPrivateKey) md.get("PRIVATE_KEY");
        RSAPublicKey pkd = (RSAPublicKey) md.get("PUBLIC_KEY");
        Map me = Transaction.genKeyPair();
        RSAPrivateKey ske = (RSAPrivateKey) me.get("PRIVATE_KEY");
        RSAPublicKey pke = (RSAPublicKey) me.get("PUBLIC_KEY");
        Map mf = Transaction.genKeyPair();
        RSAPrivateKey skf = (RSAPrivateKey) mf.get("PRIVATE_KEY");
        RSAPublicKey pkf = (RSAPublicKey) mf.get("PUBLIC_KEY");
        Map mg = Transaction.genKeyPair();
        RSAPrivateKey skg = (RSAPrivateKey) mg.get("PRIVATE_KEY");
        RSAPublicKey pkg = (RSAPublicKey) mg.get("PUBLIC_KEY");
        Map mh = Transaction.genKeyPair();
        RSAPrivateKey skh = (RSAPrivateKey) mh.get("PRIVATE_KEY");
        RSAPublicKey pkh = (RSAPublicKey) mh.get("PUBLIC_KEY");
        Map mi = Transaction.genKeyPair();
        RSAPrivateKey ski = (RSAPrivateKey) mi.get("PRIVATE_KEY");
        RSAPublicKey pki = (RSAPublicKey) mi.get("PUBLIC_KEY");
        //get money
        Transaction.Output outd = new Transaction().new Output(100,pkd);
        Transaction.Output oute = new Transaction().new Output(100,pke);
        Transaction.Output outf = new Transaction().new Output(100,pkf);
        Transaction.Output outg = new Transaction().new Output(100,pkg);
        Transaction.Output outh = new Transaction().new Output(100,pkh);
        Transaction.Output outi = new Transaction().new Output(100,pki);
        tx_origin_hashg  = new String("origin_tx_hashG").getBytes();
        tx_origin_hashh  = new String("origin_tx_hashH").getBytes();
        tx_origin_hashi  = new String("origin_tx_hashI").getBytes();
        tx_origin_hashd  = new String("origin_tx_hashD").getBytes();
        tx_origin_hashe  = new String("origin_tx_hashE").getBytes();
        tx_origin_hashf  = new String("origin_tx_hashF").getBytes();
        upool.addUTXO(new UTXO(tx_origin_hashd,0),outd);
        upool.addUTXO(new UTXO(tx_origin_hashe,0),oute);
        upool.addUTXO(new UTXO(tx_origin_hashf,0),outf);
        upool.addUTXO(new UTXO(tx_origin_hashd,0),outg);
        upool.addUTXO(new UTXO(tx_origin_hashe,0),outh);
        upool.addUTXO(new UTXO(tx_origin_hashf,0),outi);
        //a to b, a to d
        Transaction trans_a_to_b = new Transaction();
        trans_a_to_b.addInput(tx_origin_hasha,0);
        trans_a_to_b.addOutput(100,pkb);
        trans_a_to_b.GETsign(ska,0);
        trans_a_to_b.finalize();
        Transaction trans_a_to_d = new Transaction();
        trans_a_to_d.addInput(tx_origin_hasha,0);
        trans_a_to_d.addOutput(100,pkd);
        trans_a_to_d.GETsign(ska,0);
        trans_a_to_d.finalize();
        //b to c, b to g
        Transaction trans_b_to_c = new Transaction();
        trans_b_to_c.addInput(tx_origin_hashb,0);
        trans_b_to_c.addOutput(100,pkc);
        trans_b_to_c.GETsign(skb,0);
        trans_b_to_c.finalize();
        Transaction trans_b_to_g = new Transaction();
        trans_b_to_g.addInput(trans_a_to_b.getHash(),0);
        trans_b_to_g.addOutput(100,pkg);
        trans_b_to_g.GETsign(skb,0);
        trans_b_to_g.finalize();
        //d to e, d to f, d to i
        Transaction trans_d_to_e = new Transaction();
        trans_d_to_e.addInput(tx_origin_hashd,0);
        trans_d_to_e.addOutput(100,pke);
        trans_d_to_e.GETsign(skd,0);
        trans_d_to_e.finalize();
        Transaction trans_d_to_f = new Transaction();
        trans_d_to_f.addInput(trans_a_to_d.getHash(),0);
        trans_d_to_f.addOutput(100,pkf);
        trans_d_to_f.GETsign(skd,0);
        trans_d_to_f.finalize();
        Transaction trans_d_to_i = new Transaction();
        trans_d_to_i.addInput(trans_a_to_d.getHash(),0);
        trans_d_to_i.addOutput(100,pki);
        trans_d_to_i.GETsign(skd,0);
        trans_d_to_i.finalize();
        //e to h, e to i
        Transaction trans_e_to_h = new Transaction();
        trans_e_to_h.addInput(tx_origin_hashe,0);
        trans_e_to_h.addOutput(100,pkh);
        trans_e_to_h.GETsign(ske,0);
        trans_e_to_h.finalize();
        Transaction trans_e_to_i = new Transaction();
        trans_e_to_i.addInput(trans_d_to_e.getHash(),0);
        trans_e_to_i.addOutput(100,pkf);
        trans_e_to_i.GETsign(ske,0);
        trans_e_to_i.finalize();
        //f to g
        Transaction trans_f_to_g = new Transaction();
        trans_f_to_g.addInput(tx_origin_hashf,0);
        trans_f_to_g.addOutput(100,pkg);
        trans_f_to_g.GETsign(skf,0);
        trans_f_to_g.finalize();
        //g to h
        Transaction trans_g_to_h = new Transaction();
        trans_g_to_h.addInput(tx_origin_hashg,0);
        trans_g_to_h.addOutput(100,pkh);
        trans_g_to_h.GETsign(skg,0);
        trans_g_to_h.finalize();
        Transaction[] trans = {trans_a_to_b, trans_a_to_d,trans_b_to_c,trans_b_to_g,trans_d_to_e,trans_d_to_f,
                trans_d_to_i,trans_e_to_h,trans_e_to_i,trans_f_to_g,trans_g_to_h};
        Transaction[] expect = {};
        for(int i=0;i<trans.length;i++) {
            for(int j=0;j<test_Txhandler.handleTxs(trans).length;j++) {
                if (trans[i] == test_Txhandler.handleTxs(trans)[j]) {
                    System.out.print(trans[i]+"\n");
                }
            }
        }
    }
}
