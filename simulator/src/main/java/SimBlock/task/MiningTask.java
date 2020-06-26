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
import SimBlock.node.Node;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import SimBlock.block.MerkleTrees;

import static SimBlock.simulator.Timer.getCurrentTime;

public class MiningTask extends AbstractMintingTask {
	private BigInteger difficulty;

	//Vincent Start
	public List<Map<String, Object>> currentTransactions;

	public List<Map<String, Object>> getCurrentTransactions() {
		return currentTransactions;
	}

	public void newTransactions(String sender, String recipient, long amount) {
		Map<String, Object> transaction = new HashMap<String, Object>();
		transaction.put("sender", sender);
		transaction.put("recipient", recipient);
		transaction.put("amount", amount);
		getCurrentTransactions().add(transaction);
	}
	//Vincent Tail
	
	public MiningTask(Node minter, long interval, BigInteger difficulty) {
		super(minter, interval);
		this.difficulty = difficulty;
	}

	@Override
	public void run() {

		currentTransactions = new ArrayList<Map<String, Object>>();

		//Vincent Start
		newTransactions("sender","receipient",100);

		List<String> tempTxList = new ArrayList<String>();
		tempTxList.add("a");
		tempTxList.add("b");
		tempTxList.add("c");
		tempTxList.add("d");
		tempTxList.add("e");
		MerkleTrees merkleTrees = new MerkleTrees(currentTransactions);
		merkleTrees.constractTree();
		System.out.println("root : " + merkleTrees.getRoot());
		//Vincent Tail

		ProofOfWorkBlock createdBlock = new ProofOfWorkBlock((ProofOfWorkBlock)this.getParent(), this.getMinter(), getCurrentTime(), this.difficulty);
		this.getMinter().receiveBlock(createdBlock);
	}
}
