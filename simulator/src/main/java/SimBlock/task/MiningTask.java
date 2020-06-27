/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.task;

import SimBlock.block.ProofOfWorkBlock;
import SimBlock.data.GolbalTrxPool;
import SimBlock.node.Node;

import java.math.BigInteger;
import java.util.*;

import SimBlock.block.MerkleTrees;
import SimBlock.data.readCSV;
import com.alibaba.fastjson.JSONObject;

import static SimBlock.simulator.Timer.getCurrentTime;

public class MiningTask extends AbstractMintingTask {
	private BigInteger difficulty;
	
	public MiningTask(Node minter, long interval, BigInteger difficulty) {
		super(minter, interval);
		this.difficulty = difficulty;
	}

	@Override
	public void run() {

		//Vincent
		//根據全局交易池，選擇需要的交易
		ArrayList TrxList = TrxSelection(GolbalTrxPool.TrxPool);
		//生成梅克爾二叉樹
		MerkleTrees merkleTrees = new MerkleTrees(TrxList);
		merkleTrees.constractTree();
		//打印根節點哈希值
		System.out.println("root : " + merkleTrees.getRoot());

		ProofOfWorkBlock createdBlock = new ProofOfWorkBlock((ProofOfWorkBlock)this.getParent(), this.getMinter(), getCurrentTime(), this.difficulty);
		this.getMinter().receiveBlock(createdBlock);
	}

	//Vincent
	//從全局交易池中選擇需要的交易
	public ArrayList TrxSelection(ArrayList<Map<String, Object>> TrxPool){

	    ArrayList<Map<String, Object>> selectedTrxList = new ArrayList<Map<String, Object>>();
	    Double trxFee = 0.02;

        for (int i = 0; i < TrxPool.size(); i++) {
            Map<String, Object> transaction = TrxPool.get(i);
            Double price = Double.parseDouble(transaction.get("Price").toString());
            Double quantity = Double.parseDouble(transaction.get("Quantity").toString());

            transaction.put("TransactionFee", price * quantity * trxFee);
            selectedTrxList.add(transaction);
        }
		return TrxPool;
	}
}
