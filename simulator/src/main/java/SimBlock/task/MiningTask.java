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

import SimBlock.block.Block;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	public static ArrayList TrxVincentSelection(ArrayList<Map<String, Object>> TrxPool){
		ArrayList<Map<String, Object>> selectedTrxList = new ArrayList<Map<String, Object>>();
		//將BlockSize除以TransactionSize然後向上取整
		Integer count = new Double(Math.ceil(BLOCKSIZE / TRANSACTIONSIZE)).intValue();
		Double totalTrxFee = new Double(0);
		//當交易池中還有交易未處理，並且一個Block還沒有填滿
		Map<String, Object> transaction = null;

		Boolean decayInd = true;
		Boolean spaceInd = true;
		//为每笔交易计算手续费时间衰减参数
		if(decayInd) {
			TimeSenProcess(GolbalTrxPool.TrxPool);
		}

		while (count > 0) {
			//transaction = getTrxByMaxFee(decayInd,TrxPool);
			transaction = getTrxByOrder(spaceInd,decayInd,TrxPool,count);
			totalTrxFee = totalTrxFee + Double.parseDouble(transaction.get("TransactionFee").toString());
			//將交易加入輸出List
			selectedTrxList.add(transaction);
			//減去這筆交易佔用的空間
			count = count - Integer.parseInt(transaction.get("SpaceFactor").toString());
			//TRXNUMS = TRXNUMS + 1;
			//如果是最后一笔纳入区块的交易，则记录总交易手续费，否则每笔交易的交易手续费设置为0
			//如果区块被填满，或者已经没有可以放入一个区块的交易，或者所有的交易都已经被放入区块
			if(count == 0 || TrxPool.size() ==0 || Boolean.parseBoolean(transaction.get("LastTrxInd").toString())) {
				transaction.put("TotalTransactionFee", totalTrxFee);
				OUT_CSV_FILE.println("SCALE:"+SCALE+" SHAPE:"+SHAPE+" POOLSIZE:"+TrxPool.size()+","+totalTrxFee+","+TRXNUMS);
				if(MINBLOCKTRXFEE > totalTrxFee) {
					MINBLOCKTRXFEE = totalTrxFee;
				}
				if(MAXBLOCKTRXFEE < totalTrxFee) {
					MAXBLOCKTRXFEE = totalTrxFee;
				}
				BLOCKHEIGHT = BLOCKHEIGHT + 1;
				System.out.println("Current Block Height is: " + BLOCKHEIGHT);
				System.out.println(new SimpleDateFormat("yyyy-MM-dd hhmmss").format(new Date()));
				REMAINTRX = new Double(TrxPool.size());
			}

			//CDF曲线画图
			try {
				PrintWriter CDF_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(Block.class.getClassLoader().getResource("simulator.conf").toURI().resolve("./output/CDFoutput.csv")))));
				Scanner scanner = new Scanner(System.in);
				System.out.println("开始....输入N");
				while (scanner.hasNextLine()) {
					double scale = scanner.nextDouble();
					double shape = scanner.nextDouble();

					LogNormalDistribution tmp = new LogNormalDistribution(scale, shape);

					XYSeries series = new XYSeries("xySeries");
					for (int x = 1; x <= 5000; x++) {
						double y = Math.ceil(tmp.cumulativeProbability(x) * 20);
						series.add(x, y);
						CDF_FILE.println(x + "," + y);
						CDF_FILE.flush();
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
				}
			}catch (Exception e){}
		}
		return selectedTrxList;
	}

	private static Map<String, Object> getTrxByOrder(Boolean spaceInd,Boolean decayInd,ArrayList<Map<String, Object>> TrxPool,int count){
		int i = 0;
		Map<String, Object> transaction = null;

		while(i < TrxPool.size()) {
			//從TrxPool中取出交易序號最小一筆
			transaction = TrxPool.get(i);

			//初始化衰减变量为1
			Double decayFactor = 1.0;
			//如果有衰减则计算衰减变量
			if (decayInd) {
				decayFactor = Double.parseDouble(transaction.get("DecayFactor").toString());
			}

			//計算带有衰减变量的交易手續費
			double trxFee = Double.parseDouble(transaction.get("Volume_(Currency)").toString()) * decayFactor * TRANSACTIONFEEPCT;
			transaction.put("TransactionFee", trxFee);

			//初始化CDF空间占用参数为1
			int spaceFactor = 1;
			//如果不是动态CDF则直接退出
			if (!spaceInd) {
				transaction.put("LastTrxInd", false);
				transaction.put("SpaceFactor", spaceFactor);
				break;
			}else{
				//根據交易手續費計算應該佔用的空間係數
				LogNormalDistribution LND = new LogNormalDistribution(SCALE, SHAPE);
				//根據Cumulative Probability計算出來的結果向上取整，即0.1=1
				spaceFactor = new Double(Math.ceil(LND.cumulativeProbability(trxFee) * 80)).intValue();
				//当CDF算出来占用空间是0的时候至少要占用一个存储空间
				if (spaceFactor == 0)
					spaceFactor = 1;
				//如果當前這筆交易不夠放到當前區塊中，選擇下一筆交易
				if (spaceFactor > count) {
					i = i + 1;
					if(i >= TrxPool.size()) {
						transaction.put("LastTrxInd", true);
					}
					continue;
				} else {
					transaction.put("LastTrxInd", false);
					//記錄通過LogNormalDistrbution計算出的交易佔用空間
					transaction.put("SpaceFactor", spaceFactor);
					break;
				}
			}
		}
		//將交易從交易池去掉
		TrxPool.remove(i);
		return transaction;
	}

	private static Map<String, Object> getTrxByMaxFee(Boolean decayInd,ArrayList<Map<String, Object>> TrxPool){
		Map<String, Object>  maxNum = TrxPool.get(0);
		Double maxFee = 0.0;
		if(decayInd) {
			maxFee = Double.parseDouble(maxNum.get("DecayFee").toString());
		}else{
			maxFee = Double.parseDouble(maxNum.get("Volume_(Currency)").toString());
		}
		int num = 0;
		Double currFee = 0.0;
		for (int i = 0; i < TrxPool.size(); i++) {
			//如果是手续费衰减，则比较衰减后的手续费值
			if(decayInd){
				currFee = Double.parseDouble(TrxPool.get(i).get("DecayFee").toString());
			}else{
				currFee = Double.parseDouble(TrxPool.get(i).get("Volume_(Currency)").toString());
			}
			if(maxFee < currFee) {
				maxFee = currFee;
				maxNum = TrxPool.get(i);
				num = i;
			}
		}
		TrxPool.remove(num);
		return maxNum;
	}

	private static void TimeSenProcess(ArrayList<Map<String, Object>> TrxPool) {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date day1 = df.parse("2019-12-06");
			for (int i = 0; i < TrxPool.size(); i++) {
				/*Date day2 = new Date(Long.parseLong(TrxPool.get(i).get("Timestamp").toString()) * 1000);
				String tmp1 = df.format(day1);
				String tmp2 = df.format(day2);
				Long days = daysBetween(day1, day2);
				TrxPool.get(i).put("TimeFactor", 1 * Math.pow(0.98, days));*/
				Double decayFactor = Double.parseDouble(TrxPool.get(i).get("DecayFactor").toString()) * DECAYPRECNET;
				TrxPool.get(i).put("DecayFactor", decayFactor);
				TrxPool.get(i).put("DecayFee", Double.parseDouble(TrxPool.get(i).get("Volume_(Currency)").toString()) * decayFactor);
			}
		}catch (ParseException e){

		}
	}

	private static long daysBetween(Date one, Date two) {
		long difference =  (one.getTime()-two.getTime())/86400000;
		return Math.abs(difference);
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
}
