import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class QueryParser {
    static Jedis j;
    public static String action, starColumns,tableName;
    public static String QueryWords[];
    public static String keyWords[] = {"Select","From",";","join","as","on","."};
    public static String QueryTables[] = new String[10];    
    public static String QueryColumnNames[] = new String[20];
    public static String arr[];
    public static int tableCount,fromPosition,toPosition,columnCount,whereCount;
    public static boolean selectFlag,starFlag,tableFlag,columnFlag,whereFlag,joinFlag,outputFlag; 
    public static String where = new String("");
    public static String employeesTable[];
    public static String salariesTable[];
    public static long time1,time2;
    
    
    public static String result[];
    public static String resultData[][] = new String[100][100];
    //public static Map<String,String> dataMap;
    public static Map<String,Integer> ident = new HashMap<>();
    public static String temp1;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    	String Q1 = "SELECT * FROM employees";
    	String Q2 = "SELECT * FROM salaries";
    	String Q3 = "SELECT Emp_No,Birth_Date,First_Name,Last_Name,Gender,Hire_Date FROM Employees";
    	String Q4 = "SELECT Emp_No,Salary,From_Date,To_Date FROM salaries";
    	String Q5 = "Select Emp_No, Birth_Date, First_Name, Last_Name, Gender, Hire_Date, Salary, From_Date, To_Date from Employees inner join Salaries on Employees.Emp_No = Salaries.Emp_No";
    	String Q6 = "Select * from Employees inner join Salaries on Employees.Emp_No = Salaries.Emp_No";
    	
    	
    	
        time1= System.currentTimeMillis();
        j = new Jedis("192.168.56.56");
        analyzeQuery(Q1);       
        time2 = System.currentTimeMillis();
       System.out.println("Time for selecting data from Redis by converting SQL Queries to Redis Commands");
       System.out.println(time2-time1);
    }

    public static void analyzeQuery(String Q)
    {
    	tableCount = 0;
    	columnCount = 0;
    	whereCount = 0;
    	QueryWords = Q.split("\\s|,");
        int x = QueryWords.length;
        String Query[] = new String[x+1];
        Query[x]=";";
        for (int i=0;i<QueryWords.length;i++)
        {
        	Query[i]=QueryWords[i];        	
        } 
        
        //for (int i=0;i<Query.length;i++)
        //{        	
        	//System.out.println(Query[i]);
        //}      
        
        
        for(int i=0;i<Query.length;i++)
        {
        	if (Query[i].equalsIgnoreCase("where"))
        	{
        		whereFlag = true;
        		tableFlag = false;
        	}
        	if(Query[i].equalsIgnoreCase(";"))
    		{
    			tableFlag = false;
    			whereFlag = false;
    		}
        	if(Query[i].equalsIgnoreCase("join") || (Query[i].equalsIgnoreCase("Inner join")))
        	{
        		joinFlag = true;
        	}
        	if(tableFlag == true)
        	{
        		if(!Query[i].equalsIgnoreCase("join") && !Query[i].equalsIgnoreCase("on") && !Query[i].contains(".") && !Query[i].equalsIgnoreCase("=") && !Query[i].equalsIgnoreCase("as"))
        		{
        			QueryTables[tableCount] = Query[i].substring(0, 1).toUpperCase() + Query[i].substring(1);
        			//System.out.println(QueryTables[tableCount]);
        			tableCount++;
        		}
        	}
        		 
        	if(Query[i].equalsIgnoreCase("from"))
        	{
        		tableFlag = true;
        		columnFlag = false;
        	}
        	
        	if(columnFlag == true)
        	{
        		QueryColumnNames[columnCount] = Query[i];
        		columnCount++;
        	}
        	
        	if(Query[i].equalsIgnoreCase("Select"))
        	{
        		columnFlag = true;
        	}
        	if(whereFlag == true)
        	{
        		Query[i+1] = Query[i+1].replaceAll("\'", "");
        		if(!Query[i+1].equalsIgnoreCase("And") && !Query[i+1].equalsIgnoreCase(";"))
        		{
        			where =  where+ Query[i+1].substring(0, 1).toUpperCase() + Query[i+1].substring(1);        			
        		}
        	}
        		       		        	
        }
        
        
        
        if(Query[0].equalsIgnoreCase("Select"))
        {
        	if(Query[1].equalsIgnoreCase("*"))
        	starFlag = true;
        	else
        		starFlag = false;
        }        
        execute();
        System.out.println(where);
    }
    
    public static void getKeys() {
    	Integer setCount;
        Set<String> keys;
        keys = j.keys("*");
        
        arr = new String[keys.size()];
        int i=0;
        for(String s: keys)
            {
        		//System.out.println(s.substring(s.indexOf(":")));
        		setCount = ident.get(s.substring(s.indexOf(":")));
        		if(setCount == null)
        		ident.put(s.substring(s.indexOf(":")), 1);
        		else
        			ident.put(s.substring(s.indexOf(":")), ++setCount);
        		arr[i++] = s;        		
            }
        
        Arrays.toString(arr);
        Arrays.sort(arr);        
        //for(int k=0; k<arr.length-1; k++) {
            //System.out.println(arr[k]);
        //}
        
    }
    
    public static void execute()
    {
    	getKeys();
    	if(starFlag == true)
    	{    		
    		starQuery();
    	}
    	else if(starFlag == false)
    	{
    		nonStarQuery();
    	}    	
    }
    
    public static void nonStarQuery()
    {
    	int count = 0;
    	List<String> columns = new ArrayList<String>(QueryColumnNames.length);
    	for(String s:QueryColumnNames)
    	{
    		if(s!= null && s.length() >0)
    		columns.add(s);
    	}
    	String tableColumns[] = columns.toArray(new String[columns.size()]);
    	
    	do {
    	for(int i=0;i<arr.length;i++)
        {
            if(arr[i].contains(QueryTables[count]))
            {
            	if(joinFlag == true)
            	{            		
            		if(ident.get(arr[i].substring(arr[i].indexOf(":"))) > 1)
            		{
            			System.out.println(j.hmget(arr[i], tableColumns));
            		}
            	}
            	else
            	{            		            	
            		System.out.println(j.hmget(arr[i], tableColumns));
            	}
                //System.out.println(j.hmget(arr[i], "FirstName", "LastName"));
            	//System.out.println(j.hmget(arr[i], QueryColumnNames[0], QueryColumnNames[1]));
            	//System.out.println(j.hmget(arr[i], tableColumns));
            }
        }
    	count++;
    	}
    	while (QueryTables[count] != null);
    }
    
    public static void starQuery()
    {
    	String temp;
    	int count = 0;    	
    	do
    	{
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i].contains(QueryTables[count]))
            {
            	if(joinFlag == true)
            	{
            		//System.out.println(ident.get(arr[i].substring(arr[i].indexOf(":"))));
            		if(ident.get(arr[i].substring(arr[i].indexOf(":"))) > 1)
            		{
            			System.out.println(j.hgetAll(arr[i]));
            		}
            	}
            	else
            	{
            		//temp = j.hgetAll(arr[i]).toString();            	
            		//System.out.println(j.hgetAll(arr[i]));        		
            		
            		System.out.println(j.hgetAll(arr[i]));
            		//j.hgetAll(arr[i]);
            	}
            }
        }
        count++;        
    	}
    	while (QueryTables[count] != null);    	
    }
}