import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.Vector; 
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Queue;
import java.util.LinkedList; 
import java.util.concurrent.Semaphore;

/**
 * The bank server. 
 * It has the actual implementation of the methods. 
 * 
 * Author: Dongpu Jin
 * Date: 4/13/2013
 */
public class Bank extends UnicastRemoteObject implements BankInterface
{	
	/**
	 * Inner class for private work space
	 */ 
	class PWS {
		int account; 
		int balance; 
		
		public PWS(int account, int balance){
			this.account = account; 
			this.balance = balance; 
		}
		
		int getAccount(){
			return this.account; 
		}
		
		void setAccount(int account){
			this.account = account; 
		}
		
		int getBalance(){
			return this.balance; 
		}
		
		void setBalance(int balance){
			this.balance = balance; 
		}
	}
	
	/**
	 * Inner class represents each account
	 */ 
	class Account{
		int account; 
		int balance;  
		private final Semaphore s; 

		public Account(int account){
			this.account = account; 
			this.balance = 0; 
			this.s = new Semaphore(1, true); 
		}
		
		public Account(int account, int balance){
			this.account = account; 
			this.balance = balance; 
			this.s = new Semaphore(1, true); 
		}
		
		int getAccount(){
			return this.account;
		}
		
		int getBalance(){
			return this.balance; 
		}
		
		void setBalance(int balance){
			this.balance = balance; 
		}
		
		Semaphore getSem(){
			return this.s; 
		}
	}; 
	
	/**
	 * Inner class represents each customer
	 */ 
	class Customer {
		int cutm; 
		int accnt;  // curr accessing account
		PWS pws;  // private workspace 
		
		public Customer(int cutm){
			this.cutm = cutm; 
			this.accnt = -1; 
			this.pws = null; 
		}
		
		public Customer(int cutm, int accnt){
			this.cutm = cutm; 
			this.accnt = accnt; 
			this.pws = null; 
		}
		
		int getCutm(){
			return this.cutm; 
		}
		
		int getAccnt(){
			return this.accnt; 
		}
		
		void setAccnt(int accnt){
			this.accnt = accnt; 
		}
		
		PWS getPWS(){
			return this.pws; 
		}
		
		void setPWS(PWS pws){
			this.pws = pws;
		}
	}; 
	
	private ArrayList<Account> userAccount;    // current active accnt
	private ArrayList<Customer> activeCutm;  // current active cutm

	/**
	 * Default constructor. Initialization. 
	 */ 
    public Bank() throws RemoteException{
        userAccount = new ArrayList<Account>(); 
		activeCutm = new ArrayList<Customer>();
		userAccount.add(new Account(100, 1000));
		userAccount.add(new Account(200, 2000)); 
    }

	/**
	 * Begin the transaction for the given customer
	 */ 
	public String begin_transaction(int cutm) throws RemoteException{
		int index = -1; 
		index = checkCutm(cutm); 
		
		if (index != -1) {
			return "WARNING: The transaction for customer " + cutm + " has already started. "; 
		}
		else{
			Customer customer = new Customer(cutm); 
			activeCutm.add(customer); 
			return "Customer " + cutm + " starts the transaction. "; 
		} 
	}
	
    /**
     * Deposit to the given account with the specific amount
     */
    public String deposit(int cutm, int account, int amount) throws RemoteException, InterruptedException{
		int cutmIdx = checkCutm(cutm);  
		if (cutmIdx == -1) {
			return "WARNING: The transaction for customer " + cutm + " has not been started. "; 
		}
		else{ 
			int accntIdx = checkAccnt(account);
			if (accntIdx == -1) {  // create account if does not exist
				Account newAccnt = new Account(account);
				userAccount.add(newAccnt); 
				accntIdx = checkAccnt(account); 
			}
			
			Customer currCutm = activeCutm.get(cutmIdx); 
			Account currAccnt = userAccount.get(accntIdx); 
			
			if (currCutm.getAccnt() == -1) { // first timer
				currCutm.setAccnt(account);  // update customer accnt
				
				currAccnt.getSem().acquire();  // acquire the semaphore
				
				PWS pws = currCutm.getPWS(); 
				if (pws == null) {  // create new pws if not exist
					currCutm.setPWS(new PWS(currAccnt.getAccount(), currAccnt.getBalance())); 
					pws = currCutm.getPWS(); 
				}
				
				int newBalance = pws.getBalance() + amount; 
				pws.setBalance(newBalance);
				return "Successfully deposit $" + amount + " to account " + account;
			}
			else if(currCutm.getAccnt() == account) {  // still accessing the same account  
				PWS pws = currCutm.getPWS(); 
				if (pws == null) {
					currCutm.setPWS(new PWS(currAccnt.getAccount(), currAccnt.getBalance())); 
					pws = currCutm.getPWS(); 
				}
				
				int newBalance = pws.getBalance() + amount; 
				pws.setBalance(newBalance);
				return "Successfully deposit $" + amount + " to account " + account;	
			}
			else {  // accessing a different account 
				return "WARNING: Deposit to a different account " + account + " is not allowed in this transaction. ";
			}
		} 
    }

    /**
     * withdraw from the given account with the specific amount
     */ 
    public String withdraw(int cutm, int account, int amount) throws RemoteException, InterruptedException{
		int cutmIdx = checkCutm(cutm); 
		int accntIdx = checkAccnt(account);		
		if (cutmIdx == -1) {
			return "WARNING: The transaction for customer " + cutm + " has not been started. "; 
		}
		else if (accntIdx == -1){
			return "WARNING: The account " + account + " does not exist. Action canceled."; 
		}
		else{  // both cutm and accnt exist
			Customer currCutm = activeCutm.get(cutmIdx); 
			Account currAccnt = userAccount.get(accntIdx); 
			
			if (currCutm.getAccnt() == -1) { // first timer
				currCutm.setAccnt(account);  // update customer accnt
				
				currAccnt.getSem().acquire();  // acquire the semaphore
				
				PWS pws = currCutm.getPWS(); 
				if (pws == null) {  // create new pws if not exist
					currCutm.setPWS(new PWS(currAccnt.getAccount(), currAccnt.getBalance())); 
					pws = currCutm.getPWS(); 
				}
				
				if (pws.getBalance() < amount) {
					return "Balance not enough. Action canceled.";
				}
				
				int newBalance = pws.getBalance() - amount; 
				pws.setBalance(newBalance);
				return "Successfully withdraw $" + amount + " from account " + account;
			}
			else if(currCutm.getAccnt() == account) {  // still accessing the same account  
				PWS pws = currCutm.getPWS(); 
				if (pws == null) {
					currCutm.setPWS(new PWS(currAccnt.getAccount(), currAccnt.getBalance())); 
					pws = currCutm.getPWS(); 
				}
				
				if (pws.getBalance() < amount) {
					return "Balance not enough. Action canceled.";
				}
				
				int newBalance = pws.getBalance() - amount; 
				pws.setBalance(newBalance);
				return "Successfully withdraw $" + amount + " from account " + account;	
			}
			else {  // accessing a different account 
				return "WARNING: withdraw to a different account " + account + " is not allowed in this transaction. ";
			}
		}
    }

	/**
	 * Inquiry the given account
	 */ 
    public String inquiry(int cutm, int account) throws RemoteException, InterruptedException{
		int cutmIdx = checkCutm(cutm); 
		int accntIdx = checkAccnt(account); 
		
		if (cutmIdx == -1) {
			return "WARNING: The transaction for customer " + cutm + " has not been started. "; 
		}
		else if(accntIdx == -1) {
			return "WARNING: The account " + account + " does not exist. ";
		}
		else{  // both cutm and accnt exist
			Customer currCutm = activeCutm.get(cutmIdx); 
			Account currAccnt = userAccount.get(accntIdx); 
			
			if (currCutm.getAccnt() == -1) { // first timer
				currCutm.setAccnt(account);  // update customer accnt
				
				currAccnt.getSem().acquire();  // acquire the semaphore
				
				PWS pws = currCutm.getPWS(); 
				if (pws == null) {  // create new pws if not exist
					currCutm.setPWS(new PWS(currAccnt.getAccount(), currAccnt.getBalance())); 
					pws = currCutm.getPWS(); 
				}
				
				int balance = pws.getBalance(); 
				return "The current balance for account " + account + " is $" + balance;
			}
			else if(currCutm.getAccnt() == account) {  // still accessing the same account  
				PWS pws = currCutm.getPWS(); 
				if (pws == null) {  // create new pws if not exist
					currCutm.setPWS(new PWS(currAccnt.getAccount(), currAccnt.getBalance())); 
					pws = currCutm.getPWS(); 
				}
				
				int balance = pws.getBalance();
				return "The current balance for account " + account + " is $" + balance;	
			}
			else {  // accessing a different account 
				return "WARNING: Inquiry a different account " + account + " is not allowed in this transaction. ";
			}
		} 
    }

	/**
	 * End the given transaction 
	 */ 
	public String end_transaction(int cutm) throws RemoteException, InterruptedException{
		int index = -1; 
		index = checkCutm(cutm); 
		
		if (index == -1) {
			return "WARNING: The transaction for customer " + cutm + " has not been started. "; 
		}
		else{
			Customer ctemp = activeCutm.get(index); 
			int accntIdx = checkAccnt(ctemp.getAccnt());
			if (accntIdx != -1){
				Account atemp = userAccount.get(accntIdx); 
				if(ctemp.getPWS() != null) {
					atemp.setBalance(ctemp.getPWS().getBalance());  // sync actual accnt with pws
				}
				
				atemp.getSem().release(); // release semaphore
			}
			activeCutm.remove(index);  
			return "Customer " + cutm + " commits the transaction. "; 
		}
	}
	
	/** 
	 * Abort the given transaction 
	 */ 
	public String abort_transaction(int cutm) throws RemoteException, InterruptedException{
		int index = -1; 
		index = checkCutm(cutm); 
		
		if (index == -1) {
			return "WARNING: The transaction for customer " + cutm + " has not been started. "; 
		}
		else{
			Customer ctemp = activeCutm.get(index); 
			int accntIdx = checkAccnt(ctemp.getAccnt());
			if (accntIdx != -1){
				userAccount.get(accntIdx).getSem().release(); // release semaphore
			}
			activeCutm.remove(index); 
			return "Customer " + cutm + " aborts the transaction. "; 
		}
	}
    
	/**
	 * Check whether the given cutm already started. 
	 */ 
	private int checkCutm(int cutm) {
		int index = -1;  
		for (Customer temp : activeCutm){
			if(temp.getCutm() == cutm) {
				index = activeCutm.indexOf(temp); 
				break; 
			}
		}	
		return index; 
	}
	
	/**
	 * Check whether the given cutm already started. 
	 */ 
	private int checkAccnt(int accnt) {
		int index = -1;  
		for (Account temp : userAccount){
			if(temp.getAccount() == accnt) {
				index = userAccount.indexOf(temp); 
				break; 
			}
		}	
		return index; 
	}
	
	public static void main(String args[]) throws Exception{
        // default server port number
        int serverport = 1099;
        
        //get port number of argument
        if (args.length == 1){
            try {
                serverport = Integer.parseInt(args[0]);
				System.out.println("Using port: " + serverport); 
            } catch (Exception e) {
                e.printStackTrace(); 
            }
        } 
        else{
            System.out.println("No port provided. Using default port: 1099"); 
        }
        
        // create and install a security manager 
        //if(System.getSecurityManager() == null){
        //    System.setSecurityManager(new RMISecurityManager());
        //}
        
		Registry registry = null; 
        try {
            registry = LocateRegistry.createRegistry(serverport); 
        } catch (RemoteException e) {
            System.out.println("java RMI registry on port " + serverport + " already exists");
			registry = LocateRegistry.getRegistry(); 
        }
        
        //init Bank server
		Bank bankobj  = new Bank();
		
		// bind this obj instance to the name "Bank"
        try {
            String name = "rmi://localhost" + ":" + serverport + "/Bank"; 
            Naming.rebind(name, bankobj);
            System.out.println("Server ready"); 
        } catch (Exception e){
            e.printStackTrace();  
        }
    }
}
