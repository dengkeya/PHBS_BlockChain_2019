# Homework 2

## Name : Deng Keya

## Summary

- For method 1 **BlockChain(Block genesisBlock)**

  First of all, I established a class BlockNode including block, height , parent, children , and utxopool in our class BlockChain. So,   BlockChain(Block genesisBlock) is just BlockNode(genesisBlock, null, upool). Its utxopool contains coinbaseutxo, height is 1. **One thing to stress, I set a HashMap<ByteArrayWrapper, BlockNode> H to record all the blocknode created.** 

- For method 2 **Block getMaxHeightBlock()**

  In my class I declared BlockNode MaxHeightBlockNode. Once we add a block, It would update MaxHeightBlockNode, and this update function is in my method addBlock(Block block). 

- For method 3 **UTXOPool getMaxHeightUTXOPool()**

  As the same to method 2, we can get utxopool from the MaxHeightBlockNode.

- For method 4 **TransactionPool getTransactionPool()**

  Since I declared a transactionPool as a varible, It can easily get the  transactionPool just use return.

- For method 5 **addBlock(Block block)**

  Before we add the block into the chain, there are some events we have to verify:

  - It is not a genesis block
  - Its parent  exsists or is valid
  - All txs in the block are valid
  - The utxopool should not be empty

  After we check all the requirements here,  we should add the valid block. 

  - Add coinbaseutxo into utxopool
  - Remove all txs in the block from txpool
  - Create a new blocknode with its parent is the prevblock
  - Update prevblock.children
  - Update maxheightblock
  - Add the block into hashmap H

  **One thing special, we have to satisfy condition CUT_OFF_AGE.**

  I declare ArrayList<BlockNode> heads to record all the blocknodes in the chain. When blocknode.height > CUT_OFF_AGE + 1, I use a new list newheads to record all children of blocknodes in heads. Compare heads and newheads, the different ones are the block we cannot add branch from. We remove it from our hashmap H,  so that blocks cannot find its hash to make it as their prevblock. 

- For method 6**addTransaction(Transaction tx)**

  I use the method provided in transactionPool.addTransaction(tx).

  ## Design of the test suite

- From transaction aspect
  - test_1: add  a block with valid transaction
  - test_2: test a block with double-spending txs cannot be added into the chain.                               (Since some invalid txs we have test in Homework_1, we only test double-spending problems in the test. )

- From block aspect

  - test_3: add an empty block with no tx  
  - test_4: add an other genesis block in the chain 
  - test_5: add a block with invalid prevhash

- From structure aspect

  - test_6: add a block into a chain with height 5

    ![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/test_61.png)

  - test_7: add a branch from genesis into a chain with height 16

    ![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/test_7.png)

  - test_8: Create a block after a valid transaction has been processed that uses a UTXO already claimed by a transaction in the longest valid branch

    ![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/test_8.png)

  - test_9: Construct two branches of approximately equal size, ensuring that blocks are always created on the proper branch

    ![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/test_9.png)

  - test_10: Construct two branches of approximately equal size, ensuring that blocks are always created on the proper branch

    ![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/test_10.png)

  - test_11:  complex structure with multiple branches

    ![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/test_11.png)

Here is my test results:

![](https://github.com/dengkeya/PHBS_BlockChain_2019/blob/master/Homework_2/testrrr.png)