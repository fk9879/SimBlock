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
package SimBlock.block;

import java.math.BigInteger;
import java.util.ArrayList;

import SimBlock.data.GolbalTrxPool;
import SimBlock.node.Node;
import SimBlock.task.MiningTask;

import static SimBlock.simulator.Simulator.*;


public class ProofOfWorkBlock extends Block {
	private BigInteger difficulty;
	private BigInteger totalDifficulty;
	private BigInteger nextDifficulty;
	private static BigInteger genesisNextDifficulty;

	public ProofOfWorkBlock(ProofOfWorkBlock parent, Node minter, long time, BigInteger difficulty, MerkleTrees merkleTrees) {
		super(parent, minter, time, merkleTrees);
		this.difficulty = difficulty;
		this.totalDifficulty = (parent == null ? BigInteger.ZERO : parent.getTotalDifficulty()).add(difficulty);
		this.nextDifficulty = (parent == null ? ProofOfWorkBlock.genesisNextDifficulty : parent.getNextDifficulty()); // TODO: difficulty adjustment
	}

	public BigInteger getDifficulty() {return this.difficulty;}
	public BigInteger getTotalDifficulty() {return this.totalDifficulty;}
	public BigInteger getNextDifficulty() {return this.nextDifficulty;}

	public static ProofOfWorkBlock genesisBlock(Node minter) {
		long totalMiningPower = 0;
		for(Node node : getSimulatedNodes()){
			totalMiningPower += node.getMiningPower();
		}
		//Vincent
		//根據全局交易池，選擇需要的交易，除了這個要修改外，還要修改MiningTask.java的run方法
		//ArrayList TrxList = MiningTask.TrxSelection(GolbalTrxPool.TrxPool);
		ArrayList TrxList = MiningTask.TrxVincentSelection(GolbalTrxPool.TrxPool);
		//生成梅克爾二叉樹
		MerkleTrees merkleTrees = new MerkleTrees(TrxList);
		merkleTrees.constractTree();

		genesisNextDifficulty = BigInteger.valueOf(totalMiningPower * getTargetInterval());
		return new ProofOfWorkBlock(null, minter, 0, BigInteger.ZERO,merkleTrees);
	}
}
