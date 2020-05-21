import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class Java2Redis {
	static Jedis j;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//Java2Redis obj = new Java2Redis();
        j = new Jedis("192.168.56.56");
        System.out.println(j.ping());
        readCSV();

	}
	
	public static void readCSV() throws Exception{
    	BufferedReader br1 = null;
    	BufferedReader br2 = null;
    	try {
    		String csvFile1 = "Z:\\CSVFILES\\Employees100000Records.CSV";
    		String csvFile2 = "Z:\\CSVFILES\\Salaries100000Records.CSV";
    		br1 = new BufferedReader(new FileReader(csvFile1));
    		br2 = new BufferedReader(new FileReader(csvFile2));
    		String line = "";
    		String csvSplitBy = ";";
            int count = 1;
    		while((line = br1.readLine()) != null) {
    			String[] values = line.split(csvSplitBy);
    			Map<String, String> val = new HashMap<String, String>();
    			val.put("Emp_No", values[0]);
    			val.put("Birth_Date", values[1]);
    			val.put("First_Name", values[2]);
    			val.put("Last_Name", values[3]);
    			val.put("Gender", values[4]);
    			val.put("Hire_Date", values[5]);
                String key = "Employees:"+values[0];
                j.hmset(key, val);
    		}
    		while((line=br2.readLine()) != null ) {
    			String[] values = line.split(csvSplitBy);
    			Map<String, String> val = new HashMap<String, String>();
    			val.put("Emp_No", values[0]);
    			val.put("Salary",values[1]);
    			val.put("From_Date",values[2]);
    			val.put("To_Date",values[3]);
    			String key = "Salaries:"+values[0];
    			j.hmset(key, val);
    		}
    		
            System.out.println("Complete");
    	}
    	catch(FileNotFoundException e) {
    		System.out.println(e);
    	}
    	catch(IOException e) {
    		System.out.println(e);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	finally {
    		if(br1 != null || br2 != null) {
    			try {br1.close(); br2.close();} catch(IOException e) {e.printStackTrace();}
    		}
    	}
    }

}
