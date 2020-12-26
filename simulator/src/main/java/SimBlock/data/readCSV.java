package SimBlock.data;

import SimBlock.block.Block;

import java.io.*;
import java.util.*;

public class readCSV {

    public void readFile(String readpath, ArrayList<Map<String, Object>> Trxlist) {
        String filePath = this.getClass().getResource("/").getPath();
        File inFile = new File(filePath+readpath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            //判断是否到达文件末尾
            while (reader.ready()) {
                String line = reader.readLine();
                //返回一个","分隔的迭代器
                //这种方法不是很好，建议还是使用示例一中的方法
                StringTokenizer st = new StringTokenizer(line, ",");
                Map<String, Object> transaction = new HashMap<String, Object>();

                if (st.hasMoreTokens()) {
                    //Read Old ICBC201241201.csv file
                    /*transaction.put("SecurityCode", st.nextToken().trim());
                    transaction.put("RecordType", st.nextToken().trim());
                    transaction.put("TradeID", st.nextToken().trim());
                    transaction.put("Price", st.nextToken().trim());
                    transaction.put("Quantity", st.nextToken().trim());
                    transaction.put("TradeType", st.nextToken().trim());
                    transaction.put("TradeTime", st.nextToken().trim());*/
                    transaction.put("Timestamp", st.nextToken().trim());
                    transaction.put("Open", st.nextToken().trim());
                    transaction.put("High", st.nextToken().trim());
                    transaction.put("Low", st.nextToken().trim());
                    transaction.put("Close", st.nextToken().trim());
                    transaction.put("Volume_(BTC)", st.nextToken().trim());
                    transaction.put("Volume_(Currency)", st.nextToken().trim());
                    transaction.put("Weighted_Price", st.nextToken().trim());
                    //使用ArrayList接收数据
                    Trxlist.add(transaction);
                }
            }
            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}