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

import SimBlock.block.MerkleTrees;
import SimBlock.block.ProofOfWorkBlock;
import SimBlock.data.GolbalTrxPool;
import SimBlock.data.readCSV;
import SimBlock.node.Node;
import SimBlock.settings.SimulationConfiguration;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import static SimBlock.settings.SimulationConfiguration.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import static SimBlock.simulator.Main.OUT_CSV_FILE;
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
		//根據全局交易池，選擇需要的交易，除了這個要修改外，還要修改ProofOfWorkBlock.java的genesisBlock方法
		//ArrayList TrxList = TrxSelection(GolbalTrxPool.TrxPool);
		//载入TransactionPool 额外2500笔，即1.7天的交易数据。
		new readCSV().readFile("BitcoinTransactions.csv", 2100, GolbalTrxPool.TrxPool);
		ArrayList TrxList = TrxVincentSelection(GolbalTrxPool.TrxPool);
		//生成梅克爾二叉樹
		MerkleTrees merkleTrees = new MerkleTrees(TrxList);
		merkleTrees.constractTree();
		//打印根節點哈希值
		//System.out.println("root : " + merkleTrees.getRoot());

		ProofOfWorkBlock createdBlock = new ProofOfWorkBlock((ProofOfWorkBlock)this.getParent(), this.getMinter(), getCurrentTime(), this.difficulty, merkleTrees);
		//System.out.println("x1");
		this.getMinter().receiveBlock(createdBlock);
		//System.out.println("x2");
	}

	//Vincent
	//從全局交易池中選擇需要的交易
	public static ArrayList TrxSelection(ArrayList<Map<String, Object>> TrxPool){

	    ArrayList<Map<String, Object>> selectedTrxList = new ArrayList<Map<String, Object>>();
	    //將BlockSize除以TransactionSize然後向上取整
	    Integer count = new Double(Math.ceil(BLOCKSIZE / TRANSACTIONSIZE)).intValue();

		//當交易池中還有交易未處理，並且一個Block還沒有填滿
		for (int i = 0; i < TrxPool.size() && count > 0; i++) {
        	//從TrxPool中取出交易序號最小一筆
			Map<String, Object> transaction = TrxPool.get(i);
			Double price = Double.parseDouble(transaction.get("Price").toString());
			Double quantity = Double.parseDouble(transaction.get("Quantity").toString());
			//計算交易手續費
			double trxFee = price * quantity * TRANSACTIONFEEPCT;
			transaction.put("TransactionFee", trxFee);
			int spaceFactor = 1;
			//記錄通過LogNormalDistrbution計算出的交易佔用空間
			transaction.put("SpaceFactor", spaceFactor);
			//如果當前這筆交易不夠放到當前區塊中，選擇下一筆交易
			if(spaceFactor > count) {
				continue;
			}else {
				//將交易加入輸出List
				selectedTrxList.add(transaction);
				//將交易序號最小一筆交易從交易池去掉
				TrxPool.remove(i);
				//將交易池序號減少一個
				i = i - 1;
				//減去這筆交易佔用的空間
				count = count - spaceFactor;
			}
        }
		return selectedTrxList;
	}


	//Vincent
	//從全局交易池中選擇需要的交易
	public static ArrayList TrxVincentSelection(ArrayList<Map<String, Object>> TrxPool){
		ArrayList<Map<String, Object>> selectedTrxList = new ArrayList<Map<String, Object>>();
		//將BlockSize除以TransactionSize然後向上取整
		Integer count = new Double(Math.ceil(BLOCKSIZE / TRANSACTIONSIZE)).intValue();
		Double totalTrxFee = new Double(0);
		//當交易池中還有交易未處理，並且一個Block還沒有填滿
		for (int i = 0; TrxPool.size() != 0 && i < TrxPool.size() && count > 0; i++) {
			//從TrxPool中取出交易序號最小一筆
			Map<String, Object> transaction = TrxPool.get(i);
			String temp = transaction.get("Volume_(Currency)").toString();
			Double Volume_Currency = Double.parseDouble(temp);
			//計算交易手續費
			double trxFee = Volume_Currency * TRANSACTIONFEEPCT;

			//Strategy 1
			int spaceFactor = 1;
			totalTrxFee = totalTrxFee + trxFee;
			transaction.put("TransactionFee", trxFee);
			//將交易加入輸出List
			selectedTrxList.add(transaction);
			//將交易從交易池去掉
			TrxPool.remove(i);
			//將交易池序號減少一個
			i = i - 1;
			//減去這筆交易佔用的空間
			count = count - spaceFactor;
			transaction.put("SpaceFactor", spaceFactor);

			//Strategy 6-10
			/*//根據交易手續費計算應該佔用的空間係數
			LogNormalDistribution LND = new LogNormalDistribution(SCALE,SHAPE);
			//根據Cumulative Probability計算出來的結果向上取整，即0.1=1
			int spaceFactor = new Double(Math.ceil(LND.cumulativeProbability(trxFee)*27)).intValue();
			//当CDF算出来占用空间是0的时候至少要占用一个存储空间
			if (spaceFactor == 0)
				spaceFactor = 1;
			//記錄通過LogNormalDistrbution計算出的交易佔用空間
			transaction.put("SpaceFactor", spaceFactor);
			//如果當前這筆交易不夠放到當前區塊中，選擇下一筆交易
			if(spaceFactor > count) {
				continue;
			}else {
				//将当前交易的手续费累加到区块的总交易手续费
				totalTrxFee = totalTrxFee + trxFee;
				transaction.put("TransactionFee", trxFee);
				//將交易加入輸出List
				selectedTrxList.add(transaction);
				//將交易從交易池去掉
				TrxPool.remove(i);
				//將交易池序號減少一個
				i = i - 1;
				//減去這筆交易佔用的空間
				count = count - spaceFactor;
			}*/
			//如果是最后一笔纳入区块的交易，则记录总交易手续费，否则每笔交易的交易手续费设置为0
			//如果区块被填满，或者已经没有可以放入一个区块的交易，或者所有的交易都已经被放入区块
			if(count == 0 || i>= TrxPool.size() || TrxPool.size() ==0) {
				transaction.put("TotalTransactionFee", totalTrxFee);
				OUT_CSV_FILE.println("SCALE:"+SCALE+" SHAPE:"+SHAPE+" POOLSIZE:"+TrxPool.size()+","+totalTrxFee);
				if(MINBLOCKTRXFEE > totalTrxFee) {
					MINBLOCKTRXFEE = totalTrxFee;
				}
				if(MAXBLOCKTRXFEE < totalTrxFee) {
					MAXBLOCKTRXFEE = totalTrxFee;
				}
				BLOCKHEIGHT = BLOCKHEIGHT + 1;
				REMAINTRX = new Double(TrxPool.size());
			}else{
				transaction.put("TotalTransactionFee", 0);
			}

			//CDF曲线画图
			/*Scanner scanner = new Scanner(System.in);
			System.out.println("开始....输入N");
			while (scanner.hasNextLine()) {
				double scale = scanner.nextDouble();
				double shape = scanner.nextDouble();

				LogNormalDistribution tmp = new LogNormalDistribution(scale,shape);

				XYSeries series = new XYSeries("xySeries");
				for (int x = 1; x <= 2500; x++) {
					double y =  Math.ceil(tmp.cumulativeProbability(x*20)*30);
					series.add(x, y);
				}
				XYSeriesCollection dataset = new XYSeriesCollection();
				dataset.addSeries(series);
				JFreeChart chart = ChartFactory.createXYLineChart(
						"y = x^2", // chart title
						"x", // x axis label
						"x^2", // y axis label
						dataset, // data
						PlotOrientation.VERTICAL,
						false, // include legend
						false, // tooltips
						false // urls
				);

				ChartFrame frame = new ChartFrame("my picture", chart);
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}*/
		}
		//TRXNUMS = TrxPool.size();
		return selectedTrxList;
	}
}
