import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccountServiceImpl implements IService{
	   IAccountDao accountDao;
	   
	   @Autowired
	   public void setDao(IAccountDao dao) {
	      this.accountDao = dao;
	   }
	   
	   @Override
	   public Account logIn(String accountId, String password) throws Exception {
	    			Account account = accountDao.getAccountById(accountId);
	    			if(account != null && password.equals(account.getPassword())) {
	    				return account; 
	    			}else {
	    				throw new InvalidAccountException("Wrong password with this account");
	    			}
	    }
	   
	    @Override
	    public Account deposit(String accountId, BigDecimal amount) throws Exception {
	        if (amount.compareTo(new BigDecimal(0) <=0)){  //Should something like currencyUtil.zeroNum
	            throw new InvalidOperationException("Invalid Deposit amount");
	        }
	        accountDao.updateAccountBalance(accountId,amount.setScale(4, RoundingMode.HALF_EVEN));
	        Account account = updateHistory(accountId, "deposit", LocalDate.now(), amount);
	        return account;
	        
	    }
	    
	    @Override
	    public Account withdraw(String accountId, BigDecimal amount) throws Exception {  
	    		Account account = accountDao.getAccountById(accountId);
	        if (amount.compareTo(new BigDecimal(0)) <=0 && account.getBalance().compareTo(amount) < 0){
	            throw new InvalidOperationException("Invalid withdraw amount");
	        }
	        BigDecimal delta = amount.negate();
	        accountDao.updateAccountBalance(accountId,delta.setScale(4, RoundingMode.HALF_EVEN));
	        Account account = updateHistory(accountId, "withdraw", LocalDate.now(), delta);

	        	return account;
	        	
	    }
	    
	    @Override
	    public List<OperationHistory> getAllHistory(String accountId) throws Exception {  
	    		Account account = accountDao.getAccountById(accountId);
		    List<OperationHistory> history= account.getOperationsHistory();

	        	return history;
	        	
	    }
	    
	    private Account updateHistory(String accountId, String type, LocalDate date, BigDecimal amount) {
	        Account account = accountDao.getAccountById(accountId);
	        List<OperationHistory> history= account.getOperationsHistory();	
			
	        OperationHistory operation = ApplicationContextProvider.getApplicationContext().getBean("operationHistory");
			operation.setType(type);
			operation.setDate(date);
			operation.setAmount(amount);
			operation.setBalance(account.getBalance());
			
	        history.add(operation);
	        account = accountDao.updateAccountOperationsHistory(account);
	        return account;
	    }

}
