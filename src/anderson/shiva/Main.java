package anderson.shiva;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import java.util.*;


import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;
import com.microsoft.azure.storage.table.TableQuery.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Main {

    public static void main(String[] args) throws Exception {

        // get connection information from cmd args.
        String table_storage_connection_string = "DefaultEndpointsProtocol=https;AccountName=andersonshivafinal;AccountKey=hxGuXVK6wkF0zL/JYB3TC9TwTmm65++R5xO4rULaMEj0L82DiZwKOJ5SQL93LKyGXVSx/zjZPz/XFK5ICXSW9Q==;EndpointSuffix=core.windows.net";
        String service_bus_connection_string = "Endpoint=sb://andersonshivafinal.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xuJftJUuoMH25818xxQ8W2N8qf8YbjF0wRi0sNyenRY=";
        String queue_name = "locustrequests";

        try {


            IMessageReceiver receiver;

            receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder( service_bus_connection_string, queue_name), ReceiveMode.RECEIVEANDDELETE);



            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(table_storage_connection_string);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create the table if it doesn't exist.
            String tableName = "transactions";
            CloudTable cloudTable = tableClient.getTableReference(tableName);
            cloudTable.createIfNotExists();


            while(true){

                /* fetch new batch of messages. */
                Collection<IMessage> messages = receiver.receiveBatch(100);

                /* create a new batch operation to storage messages in azure table storage. */
                TableBatchOperation good_batchOperation = new TableBatchOperation();
                TableBatchOperation bad_batchOperation = new TableBatchOperation();

                /* information for debugging. */
                System.out.println("INFO: Processing new batch of messages.");

                int bad_count = 0;
                int good_count = 0;

                /* ensure that there are messages to process. */
                if(messages != null){
                    for(IMessage m : messages){

                        try {
                            /* introduce failure into receiver with a 4% failure rate. */
                            String partition_key = "save_success";

                            /* convert back to json */
                            byte[] body = m.getBody();
                            System.out.println(new String(body));
                            JSONParser parser = new JSONParser();
                            JSONObject obj = (JSONObject) parser.parse(new String(body));

                            /* get the individual fields to write to azure table store. */
                            String trans_id = Long.toString((Long) obj.get("transaction_id"));

                            String user_id = (String) obj.get("user_id");
                            String seller_id = (String) obj.get("seller_id");

                            String product_name = (String) obj.get("product_name");

                            Double sale_price = (Double) obj.get("sale_price");

                            String trans_date = (String) obj.get("transaction_date");

                            /* consider this a failure. */
                            if (product_name.equals("Error") || sale_price < 0) {
                                partition_key = "save_failed";

                                TransactionEntity t = new TransactionEntity(partition_key, trans_id, user_id, seller_id, product_name, sale_price, trans_date);
                                bad_batchOperation.insertOrReplace(t);
                                bad_count++;
                            }
                            else{
                                TransactionEntity t = new TransactionEntity(partition_key, trans_id, user_id, seller_id, product_name, sale_price, trans_date);
                                good_batchOperation.insertOrReplace(t);
                                good_count++;
                            }


//                            UUID id = m.getLockToken();
//                            receiver.complete(id);

                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                    /* execute batch operation. */
                    if(good_count > 0){
                        cloudTable.execute(good_batchOperation);
                    }

                    if(bad_count > 0){
                        cloudTable.execute(bad_batchOperation);
                    }

                }
            } // while true;

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
