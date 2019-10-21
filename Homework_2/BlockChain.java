// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private BlockNode genesisBlockNode;
    private ArrayList<BlockNode> heads = new ArrayList<BlockNode>();
    private TransactionPool transactionPool;
    public int height;
    public BlockNode MaxHeightBlockNode;
    public HashMap<ByteArrayWrapper, BlockNode> H;

    private class BlockNode{
        public Block block;
        public int height;
        public BlockNode parent;
        public List<BlockNode> children;
        private UTXOPool utxoPool;

        public BlockNode(Block block, BlockNode parent,  UTXOPool utxoPool){
            this.block = block;
            this.parent = parent;
            this.utxoPool = utxoPool;
            children = new ArrayList<BlockNode>();
            if(parent != null){
                this.height = parent.height + 1;
            }else{
                this.height = 1;
            }
        }

        public UTXOPool getUtxoPoolCopy() {
            return new UTXOPool(utxoPool);
        }
    }

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        UTXOPool upool = new UTXOPool();
        Transaction coinbase = genesisBlock.getCoinbase();
        UTXO utxocoinbase = new UTXO(coinbase.getHash(), 0);

        upool.addUTXO(utxocoinbase, coinbase.getOutput(0));//获取创世快的utxopool，进而构造创世node
        BlockNode genesisBlockNode = new BlockNode(genesisBlock, null, upool);
        //创世块节点加入链的node数组
        heads.add(genesisBlockNode);
        //构建一个map,确保可以通过hash去找到block的node
        H = new HashMap<ByteArrayWrapper, BlockNode>();
        H.put(new ByteArrayWrapper(genesisBlock.getHash()), genesisBlockNode);
        height = 1;
        MaxHeightBlockNode = genesisBlockNode;
        // add coinbase into txpool
        this.transactionPool = new TransactionPool();
        transactionPool.addTransaction(genesisBlock.getCoinbase());
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return MaxHeightBlockNode.block;

    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        //return H.get(this.getMaxHeightBlock().getHash() ).utxoPool;
        return MaxHeightBlockNode.utxoPool;
    }

    //用了个类似递归的算法去找最大height对应的blocknode
//    private  BlockNode getMaxHeightBlockNode(BlockNode node){
//        if(node.children.isempty()){
//            return node;
//        }else{
//            height = node.height;
//            for(BlockNode nd : node.children){
//                nd = getMaxHeightBlockNode(nd);
//                if(nd.height > height){
//                    node = nd;
//                    height = nd.height;
//                }
//            }
//            return node;
//        }
//    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        //make sure it is not a genesisblock
        if(block.getPrevBlockHash() == null){
            System.out.println("no prevhash");
            return false;
        }
        //check the parent exsists
        BlockNode parent = H.get(new ByteArrayWrapper(block.getPrevBlockHash()));
        if(parent == null){
            System.out.println("no parent");
            return false;
        }

        //add coinbaseutxo into utxopool
        UTXOPool upool = parent.getUtxoPoolCopy();
        TxHandler Txhandler = new TxHandler(upool);
        upool = Txhandler.getUTXOPool();
        if (upool.getAllUTXO().size() == 0) {
            return false;
        }
        Transaction coinbase  = block.getCoinbase();
        UTXO coinbaseUTXO = new UTXO(coinbase.getHash(), 0);
        upool.addUTXO(coinbaseUTXO, coinbase.getOutput(0));
        //verify all txs in block is valid
        Transaction[] BlockTxs = block.getTransactions().toArray(new Transaction[block.getTransactions().size()]);
        if(BlockTxs.length != Txhandler.handleTxs(BlockTxs).length){
            System.out.println("invalid");
//            System.out.println(BlockTxs.length);
//            System.out.println(Txhandler.handleTxs(BlockTxs).length);
            return false;
        }

        //remove txs in block from txspool
        ArrayList<Transaction> txs = block.getTransactions();
        for(Transaction tx : txs){
            transactionPool.removeTransaction(tx.getHash());
        }
        //creat new blocknode
        BlockNode blockNode = new BlockNode(block, parent, upool);
        parent.children.add(blockNode);
        H.put(new ByteArrayWrapper(block.getHash()), blockNode);
        heads.add(blockNode);
        //update maxheightblock
        if(blockNode.height > this.height){
            this.height = blockNode.height;
            MaxHeightBlockNode = blockNode;
        }
        //update heads
        if(this.height > CUT_OFF_AGE + 1){//对于已经超出范围的节点，我们将之移除
            ArrayList<BlockNode> newheads = new ArrayList<BlockNode>();
            //ArrayList<BlockNode> delectheads = new ArrayList<BlockNode>();
            for(BlockNode oldheads : heads) {
                for (BlockNode oldheadschild : oldheads.children) {
                    newheads.add(oldheadschild);
                }
            }
            for(BlockNode oldheads : heads){
                if(!newheads.contains(oldheads)){
                    H.remove(new ByteArrayWrapper(oldheads.block.getHash()));//把老节点从map里面删除，想连上去的新块相当于找不到他们地址了
                }
            }
            heads = newheads;
        }
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
    }
}