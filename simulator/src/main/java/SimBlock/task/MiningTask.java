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
import SimBlock.node.Node;
import SimBlock.settings.SimulationConfiguration;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import static SimBlock.settings.SimulationConfiguration.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

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
	    //將BlockSize除以TransactionSize然後向上取整
	    Integer count = new Double(Math.ceil(BLOCKSIZE / TRANSACTIONSIZE)).intValue();

        for (int i = 0; i < TrxPool.size() && count > 0; i++) {
        	//從TrxPool中取出交易
			Map<String, Object> transaction = TrxPool.get(i);
			Double price = Double.parseDouble(transaction.get("Price").toString());
			Double quantity = Double.parseDouble(transaction.get("Quantity").toString());
			//計算交易手續費
			double trxFee = price * quantity * TRANSACTIONFEEPCT;
			transaction.put("TransactionFee", trxFee);
			//根據交易手續費計算應該佔用的空間係數
			LogNormalDistribution LND = new LogNormalDistribution(SCALE,SHAPE);
			int spaceFactor = new Double(LND.cumulativeProbability(trxFee)).intValue();
			//記錄通過LogNormalDistrbution計算出的交易佔用空間
			transaction.put("SpaceFactor", spaceFactor);
			//將交易加入輸出List
			selectedTrxList.add(transaction);
			//將交易從交易池去掉
			TrxPool.remove(i);
			//減去這筆交易佔用的空間
			count = count - spaceFactor;


			/*Scanner scanner = new Scanner(System.in);
			System.out.println("开始....输入N");
			while (scanner.hasNext()) {
				double scale = scanner.nextDouble();
				double shape = scanner.nextDouble();

				LogNormalDistribution tmp = new LogNormalDistribution(5,1);

				XYSeries series = new XYSeries("xySeries");
				for (int x = 1; x <= 1000; x++) {
					double y = tmp.cumulativeProbability(x);
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
		return selectedTrxList;
	}
}
