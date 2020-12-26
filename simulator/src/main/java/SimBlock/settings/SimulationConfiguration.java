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
package SimBlock.settings;

public class SimulationConfiguration {
	public static final int NUM_OF_NODES = 600;//600;//800;//6000;

	public static final String TABLE = "SimBlock.node.routingTable.BitcoinCoreTable";
	//public static final String ALGO = "SimBlock.node.consensusAlgo.SampleProofOfStake";;
	public static final String ALGO = "SimBlock.node.consensusAlgo.ProofOfWork";

	// Unit: millisecond
	public static final long INTERVAL = 1000*60*10;//1000*60;//1000*30*5;//1000*60*10;

	// Mining power is the number of mining (hash calculation) executed per millisecond.
	public static final int AVERAGE_MINING_POWER = 400000;
	public static final int STDEV_OF_MINING_POWER = 100000;

	public static final int AVERAGE_COINS = 4000;
	public static final int STDEV_OF_COINS = 2000;

	public static final double STAKING_REWARD = 0.01;

	public static final int ENDBLOCKHEIGHT = 1200;

	// Unit: byte
	public static final long BLOCKSIZE = 6110;//6110;//8000;//535000;//0.5MB

	//Vincent
	// Unit: byte
	public static final long TRANSACTIONSIZE = 220;//250
	// Unit: 100 percentage
	public static final double TRANSACTIONFEEPCT = 0.2;
	// Log Normal Distribution Scale
	public static Double SCALE = 10.0;
	// Log Normal Distribution Shape
	public static Double SHAPE = 1.0;
	// Maximum Transaction fee for one block
	public static Double MAXBLOCKTRXFEE = 0.0;
	// Minimum Transaction fee for one block
	public static Double MINBLOCKTRXFEE = 999999999.9;
	// Block Height
	public static Double BLOCKHEIGHT = 0.0;
	// Remaining Transactions
	public static Double REMAINTRX = 0.0;
}
