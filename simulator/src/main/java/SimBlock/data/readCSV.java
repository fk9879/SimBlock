package SimBlock.data;

import SimBlock.block.Block;

import java.io.*;
import java.util.*;

import static SimBlock.settings.SimulationConfiguration.TRXNUMS;

public class readCSV {

    public void readFile(String readpath, int recordsnum, ArrayList<Map<String, Object>> Trxlist) {
        String filePath = this.getClass().getResource("/").getPath();
        File inFile = new File(filePath+readpath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            int count = 0;
            //判断是否到达文件末尾
            while (reader.ready()) {
                String line = reader.readLine();
                count = count + 1;
                //返回一个","分隔的迭代器
                //这种方法不是很好，建议还是使用示例一中的方法
                //取mempool中大于等于TRXNUMS（当前已取到的记录编号）的记录
                if(count >= TRXNUMS && Trxlist.size() <= 16000) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    Map<String, Object> transaction = new HashMap<String, Object>();

                    if (st.hasMoreTokens()) {
                        transaction.put("Timestamp", st.nextToken().trim());
                        transaction.put("Open", st.nextToken().trim());
                        transaction.put("High", st.nextToken().trim());
                        transaction.put("Low", st.nextToken().trim());
                        transaction.put("Close", st.nextToken().trim());
                        transaction.put("Volume_(BTC)", st.nextToken().trim());
                        transaction.put("Volume_(Currency)", st.nextToken().trim());
                        transaction.put("Weighted_Price", st.nextToken().trim());
                        transaction.put("DecayFactor","1");
                        //使用ArrayList接收数据
                        Trxlist.add(transaction);
                    }
                }
                //if(Trxlist.size() == TRXNUMS + recordsnum)
                if(Trxlist.size() == 16000)
                    break;
            }

            TRXNUMS = count;
            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}