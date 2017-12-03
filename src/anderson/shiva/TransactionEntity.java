package anderson.shiva;

import com.microsoft.azure.storage.table.TableServiceEntity;

import java.util.Date;

public class TransactionEntity extends TableServiceEntity {
    public TransactionEntity(String partition_key, String transaction_id, String user_id, String seller_id, String product_name, Double sale_price, String transaction_date) {

        this.partitionKey = partition_key;

        this.rowKey = transaction_id;

        this.user_id = user_id;
        this.seller_id = seller_id;

        this.product_name = product_name;
        this.sale_price = sale_price;

        this.transaction_date = transaction_date;

    }

    public TransactionEntity() { }

    String user_id;
    String seller_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public Double getSale_price() {
        return sale_price;
    }

    public void setSale_price(Double sale_price) {
        this.sale_price = sale_price;
    }

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }

    String product_name;

    Double sale_price;
    String transaction_date;

}