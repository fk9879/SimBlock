package SimBlock.block;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStorageTree {

    // transaction List
    List<String> txList;
    // Merkle Root
    String root;

    /**
     * constructor
     * @param txList transaction List
     */
    public AbstractStorageTree(List<String> txList) {
        this.txList = txList;
        this.root = "";
    }

    public abstract void constractTree();

    public abstract List<String> getNewTxList(List<String> tempTxList);

    public abstract String getProofValue(String str);

    public abstract String getRoot();

}
