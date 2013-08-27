import java.rmi.Remote; 
import java.rmi.RemoteException; 

/**
 * The interface for the distributed banking system. 
 * This interface provides method interfaces that 
 * are to be implemented by the server. 
 * 
 * Author: Dongpu Jin
 * Date: 4/13/2013
 */
public interface BankInterface extends Remote{
	public String begin_transaction(int cutm) throws RemoteException, InterruptedException;
    public String deposit(int cutm, int account, int amount) throws RemoteException, InterruptedException; 
    public String withdraw(int cutm, int account, int amount) throws RemoteException, InterruptedException; 
    public String inquiry(int cutm, int account) throws RemoteException, InterruptedException; 
	public String end_transaction(int cutm) throws RemoteException, InterruptedException;
	public String abort_transaction(int cutm) throws RemoteException, InterruptedException;
}
