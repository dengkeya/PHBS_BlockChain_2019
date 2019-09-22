# Homework 1    

## Name : Deng Keya

## Summary

- **For method 1 TxHandler(UTXOPool utxoPool)**

  I  make a copy of utxoPool which has been declared before.

- **For method 2 isValidTx(Transaction tx)**

  I declare a hashset to store utxo  claimed before. As .add(utxo) can return false if the utxo has already been in the hashset, I call it to check if the utxo has been claimed multiple times. For judging whether the 5 requirements  are satisfied or not, I apply  traversal method to judge each input of the transaction. 

- **For method 3 handleTxs(Transaction[] possibleTxs)**

  If the transaction has already in the valid transaction set, I will skip it. If it is valid, I would remove the utxo corresponding to inputs of the  transaction, and add the utxo created by this transaction into the utxopool.

## Design of the test suite

I designed a series of transactions. For valid transactions, I designed single-in-single-out, single-in-multiple-out, multiple-in-single-out, multiple-in-multiple-out, respectively, to verify them, and output the valid transaction array. For invalid transactions, I designed 5 transactions that violated the 5 given requirements, and tested them one by one. In addition, I have designed 2  problems about double spending, one is 1 transaction, the other is 2 transactions.

In the beginning, I generated 3 people using my keypairgenerator, which is wrriten in my Class Transaction. Before the test started, I initialized the whole test and gave 3 people each of them 100 yuan. 

**There is a interesting trick.** I did not construct the whole transaction from input to output. I just gave the outputs, and using method .getByte() to create hash of these transactions about creating coins, as follows:**(PS: This trick is from my instructor 沈廷威， who helps me a lot in Java. I think it's great, so I also used this trick here.)**

```java
Transaction.Output outa = new Transaction().new Output(100,pka);
Transaction.Output outb = new Transaction().new Output(100,pkb);
Transaction.Output outc = new Transaction().new Output(100,pkc);
tx_origin_hasha  = new String("origin_tx_hashA").getBytes();
tx_origin_hashb  = new String("origin_tx_hashB").getBytes();
tx_origin_hashc  = new String("origin_tx_hashC").getBytes();
upool.addUTXO(new UTXO(tx_origin_hasha,0),outa);
upool.addUTXO(new UTXO(tx_origin_hashb,0),outb);
upool.addUTXO(new UTXO(tx_origin_hashc,0)  
```



- First of all, I verify several simple **valid** transactions to see if my method can work.
  - testOneInOneOutIsvalidTx()
  - testOneInMultiOutIsValidTx()
  - testMultiInOneOutIsValidTx()
  - testMultiInMultiOutIsValidTx()



- Then, I  designed 5 **invalid** transactions that violated the 5 given requirements, and tested them one by one.

  - testSignatureInValid()

  - testNegativeOutput()

  - testInputShorage()

  - testDoubleSpending1()

    There  is  a transaction, a gives 100 yuan to b and c respectively. However, a only has 100 yuan. I designed one input with two outputs.

  - tesDoubleSpending2()

    There are two transactions. First, a gives b 100 yuan. And then, a gives c 100 yuan. It is a doublespending problem.  

    

- Finally, **a relatively complex transaction network** is constructed as an application test of my methods. I also regard it as a challenge for me, **a newcomer to Java**. 

  The following is a series transactions. My aim is to build the transaction net, and use my method to find the valid array of transactions.

![](E:\blockchainhomeworkrepo\微信图片_20190923001437.png)

In the test, a_b_c, d_e, f_g_h, i ,assume they pay each other with their initial 100 yuan, and for other transactions  related with them, they would use the money they get later. 

In the end, I get the valid array of transactions successfully. And the following is the result I get. The red ones are valid transaction.

![](E:\blockchainhomeworkrepo\微信图片_20190923001431.png)



