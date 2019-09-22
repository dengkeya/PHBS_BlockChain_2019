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

Before the test started, I initialized the whole test and generated 3 people, each giving 100 yuan.

#### For valid transactions:

- testOneInOneOutIsvalidTx()

  This is a common transaction with one in one out. It is expected to be true.

- testOneInMultiOutIsValidTx()

  This is a common transaction with one in one out. It is expected to be true.

- testMultiInOneOutIsValidTx()

  This is a common transaction with multi-in one out. It is expected to be true.

- testMultiInMultiOutIsValidTx()

  This is a common transaction with multi-in multi-out. It is expected to be true.

#### For invalid transactions:

- testSignatureInValid()

  For a transaction a to b using 100 yuan,instead of  the signature was signed by a, I designed it was signed by c. 

- testNegativeOutput()

  For a transaction a to b, I designed that a should pay b -100 yuan, which causes the output of the transaction is negative.

- testInputShorage()

  There is  a transaction a to b using 200 yuan, while a only has 100 yuan, which causes the input shortage.

- testDoubleSpending1()

  There  is  a transaction, a gives 100 yuan to b and c respectively. However, a only has 100 yuan. I designed one input with two outputs.

- tesDoubleSpending2()

  There are two transactions. First, a gives b 100 yuan. And then, a gives c 100 yuan. It is a doublespending problem.  