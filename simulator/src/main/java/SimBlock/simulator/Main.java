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
package SimBlock.simulator;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Network.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Timer.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import SimBlock.block.Block;
import SimBlock.node.Node;
import SimBlock.task.AbstractMintingTask;

public class Main {
	public static Random random = new Random(10);
	public static long time1 = 0;//a value to know the simulation time.

	public static URI CONF_FILE_URI;
	public static URI OUT_FILE_URI;
	//設置輸入及輸出文件參數
	static {
		try {
			//CONF_FILE_URI = ClassLoader.getSystemResource("simulator.conf").toURI();
			System.out.println(Block.class.getClassLoader().getResource("").toURI());
			CONF_FILE_URI = Block.class.getClassLoader().getResource("simulator.conf").toURI();
			OUT_FILE_URI = CONF_FILE_URI.resolve(new URI("./output/"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static PrintWriter OUT_JSON_FILE;
	public static PrintWriter STATIC_JSON_FILE;
	//定義輸出文件
	static {
		try{
			OUT_JSON_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./output.json")))));
			STATIC_JSON_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./static.json")))));
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		long start = System.currentTimeMillis();
		setTargetInterval(INTERVAL);

		OUT_JSON_FILE.print("["); //start json format
		OUT_JSON_FILE.flush();

		//把NetworkConfiguration中定義的地區信息先輸出到json文件
		printRegion();

		//根據以下信息來構建 SimulationCOnfiguration 裡面定義的 NUM_OF_NODES 個區塊鏈節點網絡
		// NetworkConfiguration地區劃分，
		// NetworkConfiguration級別劃分，
		// 算力，
		// 網絡連接Table SimBlock.node.routingTable.BitcoinCoreTable，
		// 算法 SimBlock.node.consensusAlgo.SampleProofOfStake
		// 1. 創建節點
		// 2. 把節點加入網絡，加到節點List裡面去
		// 3. 獲得創世區塊
		// 	3.1. 根據不同共識協議，獲得創世區塊 Node.java  genesisBlock()
		// 	3.2. 收到創世區塊 Node.java  receiveBlock()
		//		3.2.1. 如果新區塊是有效的
		//		3.2.2. 如果當前節點挖出自己的區塊，收到區塊與自己的區塊不能同時存在於區塊鏈，則將自己的區塊設為孤兒區塊
		//		3.2.3. 把創世區塊加入區塊鏈 Node.java  receiveBlock()  addToChain()
		//		3.2.4. 挖礦，將挖礦任務放入隊列中 Node.java  receiveBlock()  minting()
		//		3.2.5. 把區塊八卦到臨近節點 Node.java  receiveBlock()  sendInv()
		//  3.2. 收到新的區塊 Node.java  receiveBlock()
		//		3.2.1. 如果新區塊是有效的
		//		3.2.2. 如果當前節點挖出自己的區塊，收到區塊與自己的區塊不能同時存在於區塊鏈，則將自己的區塊設為孤兒區塊
		//		3.2.3. 把區塊加入區塊鏈 Node.java  receiveBlock()  addToChain()
		//		3.2.4. 挖礦，將挖礦任務放入隊列中 Node.java  receiveBlock()  minting()
		//		3.2.5. 把區塊八卦到臨近節點 Node.java  receiveBlock()  sendInv()

		constructNetworkWithAllNode(NUM_OF_NODES);

		//	當創世區塊及八卦任務加入Task隊列並啟動任務運行後
		//	不斷會有其他節點收到八卦信息，並啟動挖礦任務，並生成新節點，並啟動自己的八卦任務。
		//	Task隊列的內容會不斷增加，整個區塊鏈網絡的活動就逐漸啟動了
		int j=1;
		while(getTask() != null){
			//啟動所有的挖礦任務
			if(getTask() instanceof AbstractMintingTask){
				AbstractMintingTask task = (AbstractMintingTask) getTask();
				if(task.getParent().getHeight() == j) j++;
				if(j > ENDBLOCKHEIGHT){break;}
				if(j%100==0 || j==2) writeGraph(j);
			}
			runTask();
		}

		//打印所有的傳播信息
		printAllPropagation();

		System.out.println();

		Set<Block> blocks = new HashSet<Block>();
		Block block  = getSimulatedNodes().get(0).getBlock();

		while(block.getParent() != null){
			blocks.add(block);
			block = block.getParent();
		}
		//整理所有Orphan節點信息
		Set<Block> orphans = new HashSet<Block>();
		int averageOrhansSize =0;
		for(Node node :getSimulatedNodes()){
			orphans.addAll(node.getOrphans());
			averageOrhansSize += node.getOrphans().size();
		}
		averageOrhansSize = averageOrhansSize/getSimulatedNodes().size();

		blocks.addAll(orphans);

		ArrayList<Block> blockList = new ArrayList<Block>();
		blockList.addAll(blocks);
		Collections.sort(blockList, new Comparator<Block>(){
	        @Override
	        public int compare(Block a, Block b){
	          int order = Long.signum(a.getTime() - b.getTime());
	          if(order != 0) return order;
	          order = System.identityHashCode(a) - System.identityHashCode(b);
			  return order;
	        }
	    });
		//將Orphan節點信息在Systemout打印出來
		for(Block orphan : orphans){
			System.out.println(orphan+ ":" +orphan.getHeight());
		}
		System.out.println(averageOrhansSize);
		//將節點信息打印到blockList.txt
		try {
			FileWriter fw = new FileWriter(new File(OUT_FILE_URI.resolve("./blockList.txt")), false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

            for(Block b:blockList){
    			if(!orphans.contains(b)){
    				pw.println("OnChain : "+b.getHeight()+" : "+b);
    			}else{
    				pw.println("Orphan : "+b.getHeight()+" : "+b);
    			}
            }
            pw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

		OUT_JSON_FILE.print("{");
		OUT_JSON_FILE.print(	"\"kind\":\"simulation-end\",");
		OUT_JSON_FILE.print(	"\"content\":{");
		OUT_JSON_FILE.print(		"\"timestamp\":" + getCurrentTime());
		OUT_JSON_FILE.print(	"}");
		OUT_JSON_FILE.print("}");
		OUT_JSON_FILE.print("]"); //end json format
		OUT_JSON_FILE.close();
		long end = System.currentTimeMillis();
		time1 += end -start;
		System.out.println(time1);

	}


	//TODO　计划在下面加载初始生成的方案
	//创建一个要加入节点的任务（将节点加入与开始链接的任务分开）
	//在方案文件中，将上面的参与任务插入Timer中

	public static ArrayList<Integer> makeRandomList(double[] distribution ,boolean facum){
		ArrayList<Integer> list = new ArrayList<Integer>();
		int index=0;

		if(facum){
			for(; index < distribution.length ; index++){
				while(list.size() <= NUM_OF_NODES * distribution[index]){
					list.add(index);
				}
			}
			while(list.size() < NUM_OF_NODES){
				list.add(index);
			}
		}else{
			double acumulative = 0.0;
			for(; index < distribution.length ; index++){
				acumulative += distribution[index];
				while(list.size() <= NUM_OF_NODES * acumulative){
					list.add(index);
				}
			}
			while(list.size() < NUM_OF_NODES){
				list.add(index);
			}
		}

		Collections.shuffle(list, random);
		return list;
	}

	public static int genMiningPower(){
		double r = random.nextGaussian();

		return  Math.max((int)(r * STDEV_OF_MINING_POWER + AVERAGE_MINING_POWER),1);
	}
	public static void constructNetworkWithAllNode(int numNodes){
		//List<String> regions = new ArrayList<>(Arrays.asList("NORTH_AMERICA", "EUROPE", "SOUTH_AMERICA", "ASIA_PACIFIC", "JAPAN", "AUSTRALIA", "OTHER"));
		double[] regionDistribution = getRegionDistribution();
		List<Integer> regionList  = makeRandomList(regionDistribution,false);
		double[] degreeDistribution = getDegreeDistribution();
		List<Integer> degreeList  = makeRandomList(degreeDistribution,true);

		for(int id = 1; id <= numNodes; id++){
			//根據地區劃分，級別劃分，算力，網絡連接Table，算法來構建區塊鏈節點網絡
			Node node = new Node(id,degreeList.get(id-1)+1,regionList.get(id-1), genMiningPower(),TABLE,ALGO);
			addNode(node);

			OUT_JSON_FILE.print("{");
			OUT_JSON_FILE.print(	"\"kind\":\"add-node\",");
			OUT_JSON_FILE.print(	"\"content\":{");
			OUT_JSON_FILE.print(		"\"timestamp\":0,");
			OUT_JSON_FILE.print(		"\"node-id\":" + id + ",");
			OUT_JSON_FILE.print(		"\"region-id\":" + regionList.get(id-1));
			OUT_JSON_FILE.print(	"}");
			OUT_JSON_FILE.print("},");
			OUT_JSON_FILE.flush();

		}

		for(Node node: getSimulatedNodes()){
			//初始化節點的Routing Table
			node.joinNetwork();
		}
		//獲得創世節點
		getSimulatedNodes().get(0).genesisBlock();
	}

	public static void writeGraph(int j){
		try {
			FileWriter fw = new FileWriter(new File(OUT_FILE_URI.resolve("./graph/"+ j +".txt")), false);
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

            for(int index =1;index<=getSimulatedNodes().size();index++){
    			Node node = getSimulatedNodes().get(index-1);
    			for(int i=0;i<node.getNeighbors().size();i++){
    				Node neighter = node.getNeighbors().get(i);
    				pw.println(node.getNodeID()+" " +neighter.getNodeID());
    			}
            }
            pw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

}
