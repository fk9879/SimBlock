package SimBlock.block;

import java.util.List;
import java.util.Map;

public abstract class AbstractStorageTree {

    // transaction List
    List<Map<String, Object>> txList;
    // Merkle Root
    String root;

    /**
     * constructor
     * @param txList transaction List
     */
    public AbstractStorageTree(List<Map<String, Object>> txList) {
        this.txList = txList;
        this.root = "";
    }

    public abstract void constractTree();

    public abstract List<String> getNewTxList(List<String> tempTxList);

    public abstract String getProofValue(String str);

    public abstract String getRoot();

}
