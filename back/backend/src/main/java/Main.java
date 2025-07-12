import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.customer.Customer;

public class Main {

    public static void main (String args[]){
        String idCustomerWebSafe="ahZzfnN0dW5uaW5nLWJhc2UtMTY0NDAyci4LEgRVc2VyIiRmMzgwYzc0NC0wZDdiLTQ5MjUtYWY2OS1iZmY5M2NlMGYzOGEM";
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        System.out.println(customerKey);
    }

}
