package SimBlock.block;

/*import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;*/

import com.alibaba.fastjson.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerkleTrees extends AbstractStorageTree {
    public Map<String, Object> merkleTree = new HashMap<>();
    /**
     * constructor
     *
     * @param txList transaction List
     */
    public MerkleTrees(List<Map<String, Object>> txList) {
        super(txList);
    }

    @Override
    public void constractTree() {

        List<String> tempTxList = new ArrayList<String>();

        for (int i = 0; i < this.txList.size(); i++) {
            for (int j =0; j < Integer.parseInt(this.txList.get(i).get("SpaceFactor").toString()); j++) {
                tempTxList.add(new JSONObject().toJSONString(this.txList.get(i)));
            }
        }

        //Vincent
        //int level = 0;
        //List<String> newTxList = getNewTxList(tempTxList,level);

        //执行循环，直到只剩下一个hash值
        //while (newTxList.size() != 0 && newTxList.size() != 1) {
        //    level++;
        //    newTxList = getNewTxList(newTxList,level);
        //}

        //如果沒有交易了就不執行
        //if(newTxList.size() != 0)
        //    this.root = newTxList.get(0);
    }

    @Override
    public List<String> getNewTxList(List<String> tempTxList, int level) {

        List<String> newTxList = new ArrayList<String>();
        int index = 0;
        while (index < tempTxList.size()) {
            //Vincent
            Map<String, Object> leaf = new HashMap<>();
            // left
            String left = tempTxList.get(index);
            index++;
            // right
            String right = "";
            if (index != tempTxList.size()) {
                right = tempTxList.get(index);
            }
            // sha2 hex value
            String sha2HexValue = getProofValue(left + right + index);

            //Vincent
            //Leaf 節點保存交易信息
            if(level == 0) {
                leaf.put("left", left);
                leaf.put("right", right);
                leaf.put("level", level);
                leaf.put("type", "leaf");
                merkleTree.put(sha2HexValue, leaf);
            }else{
                if(tempTxList.size() == 2) {
                    //Root 節點保存Hash信息
                    leaf.put("type", "root");
                    leaf.put("level", level);
                    leaf.put("left", left);
                    leaf.put("right", right);
                    leaf.put("parent", "root");
                    ((Map<String,Object>)merkleTree.get(left)).put("parent",sha2HexValue);
                    merkleTree.put(sha2HexValue, leaf);
                    merkleTree.put("root", sha2HexValue);
                    merkleTree.put("hashalgo", "sha256");
                    merkleTree.put("leaves", "24");
                    merkleTree.put("levels", "4");
                }else{
                    //Node 節點保存Hash信息
                    leaf.put("left", left);
                    leaf.put("right", right);
                    leaf.put("level", level);
                    leaf.put("type", "node");
                    ((Map<String,Object>)merkleTree.get(left)).put("parent",sha2HexValue);
                    merkleTree.put(sha2HexValue, leaf);
                }
            }

            newTxList.add(sha2HexValue);
            index++;
        }

        return newTxList;
    }

    @Override
    public String getProofValue(String str) {
        byte[] cipher_byte;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes());
            cipher_byte = md.digest();
            StringBuilder sb = new StringBuilder(2 * cipher_byte.length);
            for (byte b : cipher_byte) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public String getRoot() {
        return this.root;
    }

    @Override
    public Map<String, Object> getData() {
        return this.merkleTree;
    }

}
