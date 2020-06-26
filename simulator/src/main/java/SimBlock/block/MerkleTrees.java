package SimBlock.block;

/*import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;*/

import com.alibaba.fastjson.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MerkleTrees extends AbstractStorageTree {
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
            tempTxList.add(new JSONObject().toJSONString(this.txList.get(i)));
        }

        List<String> newTxList = getNewTxList(tempTxList);

        //执行循环，直到只剩下一个hash值
        while (newTxList.size() != 1) {
            newTxList = getNewTxList(newTxList);
        }

        this.root = newTxList.get(0);
    }

    @Override
    public List<String> getNewTxList(List<String> tempTxList) {

        List<String> newTxList = new ArrayList<String>();
        int index = 0;
        while (index < tempTxList.size()) {
            // left
            String left = tempTxList.get(index);
            index++;
            // right
            String right = "";
            if (index != tempTxList.size()) {
                right = tempTxList.get(index);
            }
            // sha2 hex value
            String sha2HexValue = getProofValue(left + right);
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

}
