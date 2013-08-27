import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException; 
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * The ATM client. 
 * It performance various of operations supported by 
 * the services provided by the bank server.  
 * 
 * Author: Dongpu Jin
 * Date: 4/13/2013
 */
public class ATM{
    // reference of the remote object 
    static BankInterface obj = null; 
    
	/**
	 * rpc to begin the transaction
	 */ 
	public void begin_transaction(int cutm) throws RemoteException, InterruptedException {
		System.out.println(obj.begin_transaction(cutm)); 
	}
	
    /**
     * rpc to deposit operation 
     */ 
    public void deposit(int cutm, int account, int amount) throws RemoteException, InterruptedException{
        System.out.println(obj.deposit(cutm, account, amount));
    }
    
    /**
     * rpc to withdraw operation
     */
    public void withdraw(int cutm, int account, int amount) throws RemoteException, InterruptedException{
        System.out.println(obj.withdraw(cutm, account, amount));
    }
    
    /**
     * rpc to inquiry operation
     */ 
    public void inquiry(int cutm, int account) throws RemoteException, InterruptedException{ 
        System.out.println(obj.inquiry(cutm, account)); 
    }
	
	/**
	 * rpc to normally end a transaction
	 */ 
	public void end_transaction(int cutm) throws RemoteException, InterruptedException {
		System.out.println(obj.end_transaction(cutm)); 
	}
	
	/**
	 * rpc to abort a transaction
	 */
	public void abort_transaction(int cutm) throws RemoteException, InterruptedException {
		System.out.println(obj.abort_transaction(cutm)); 
	}
	
    public static void main (String args[]) throws Exception{
        String server_address = null; 
        int server_port = 0; 
        String operation = null; 
        int account = 0; 
        int amount = 0; 
		int cutm = 0; // customer id
        
        // check arguments
        if (args.length == 4) { // begin, end, abort
            if (!(args[2].equals("begin_transaction") || 
				args[2].equals("end_transaction") ||
				args[2].equals("abort_transaction"))
			){
                System.out.println("Invalid arguments. Exit.");
                System.exit(1); 
            } 
            
            try {
                server_address = args[0]; 
                server_port = Integer.parseInt(args[1]); 
                operation = args[2]; 
                cutm = Integer.parseInt(args[3]);  
            } catch (Exception e){
                System.out.println("Invalid arguments"); 
                e.printStackTrace(); 
            }
        } 
		else if(args.length == 5){ // inquiry
			if (!args[2].equals("inquiry")){
                System.out.println("Invalid arguments. Exit.");
                System.exit(1); 
            }
			
			try {
                server_address = args[0]; 
                server_port = Integer.parseInt(args[1]); 
                operation = args[2]; 
				cutm = Integer.parseInt(args[3]); 
                account = Integer.parseInt(args[4]);  
            } catch (Exception e){
                System.out.println("Invalid arguments"); 
                e.printStackTrace(); 
            }
		}
        else if (args.length == 6) { // deposit or withdraw
            if(!(args[2].equals("deposit") ||
				args[2].equals("withdraw"))
			){
                System.out.println("Invalid arguments. Exit."); 
                System.exit(1); 
            }
            
            try {
                server_address = args[0]; 
                server_port = Integer.parseInt(args[1]); 
                operation = args[2];
				cutm = Integer.parseInt(args[3]); 
                account = Integer.parseInt(args[4]);
                amount = Integer.parseInt(args[5]); 
            }
            catch (Exception e) {
                System.out.println("Invalid arguments"); 
                e.printStackTrace();
            }
        } 
        else { // invalid arguments 
            System.out.println("Invalid arguments. Exit."); 
            System.exit(1); 
        } 
        
        // create and install a security manager
        // if (System.getSecurityManager() == null){
        //    System.setSecurityManager(new RMISecurityManager()); 
        // }

        // instantiate a client obj
        ATM ATMClient = new ATM();
        
        // look up server 
        try {
            String name = "rmi://" + server_address + ":" + server_port + "/Bank"; 
			obj = (BankInterface)Naming.lookup(name);
        } catch (Exception e){ 
            System.out.println("ATM client exception: " + e); 
            e.printStackTrace(); 
        }
        
		if (operation.equals("begin_transaction")){
			ATMClient.begin_transaction(cutm); 
		}
        else if (operation.equals("inquiry")) {
            ATMClient.inquiry(cutm, account);
        } 
        else if (operation.equals("deposit")) {
            ATMClient.deposit(cutm, account, amount); 
        } 
        else if (operation.equals("withdraw")) {
            ATMClient.withdraw(cutm, account, amount); 
        }
		else if(operation.equals("end_transaction")){
			ATMClient.end_transaction(cutm); 
		}
		else if(operation.equals("abort_transaction")){
			ATMClient.abort_transaction(cutm); 
		}
         
    }
}
