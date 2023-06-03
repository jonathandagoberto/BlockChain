package blockchain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representa un bloque en la blockchain.
 */
public class Block {
    public int id;
    public String previousHash;
    public String hash;
    public String walletAddress;
    public long timestamp;
    public int nonce;
    private ArrayList<String> data;
    private int transactionCount;
    private int funds;

    public Block(int id, String previousHash) {
        this.id = id;
        this.previousHash = previousHash;
        this.data = new ArrayList<String>();
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.hash = null;
        this.walletAddress = "WalletAddress-" + id;
        this.transactionCount = 0;
        this.funds = 0;
    }

    /**
     * Agrega datos al bloque.
     *
     * @param data Datos a agregar.
     */
    public void addData(String data) {
        this.data.add(data);
        transactionCount++;
    }

    /**
     * Cierra el bloque, calcula el hash y establece el valor final.
     */
    public void closeBlock() {
        this.hash = mineBlock();
    }

    /**
     * Calcula el hash del bloque.
     *
     * @return El hash calculado.
     */
    public String mineBlock() {
        String targetPrefix = "0000";
        String blockData = previousHash + Long.toString(timestamp) + Integer.toString(nonce) + data.toString() + walletAddress;
        String calculatedHash = Crypt.applySha256(blockData);

        while (!calculatedHash.startsWith(targetPrefix)) {
            nonce++;
            blockData = previousHash + Long.toString(timestamp) + Integer.toString(nonce) + data.toString() + walletAddress;
            calculatedHash = Crypt.applySha256(blockData);
        }

        return calculatedHash;
    }

    /**
     * Obtiene la cantidad de transacciones registradas en el bloque.
     *
     * @return Cantidad de transacciones.
     */
    public int getTransactionCount() {
        return transactionCount;
    }

    /**
     * Obtiene el hash del bloque.
     *
     * @return El hash del bloque.
     */
    public String getBlockHash() {
        return hash;
    }

    /**
     * Agrega fondos al bloque.
     *
     * @param amount Cantidad de fondos a agregar.
     */
    public void addFunds(int amount) {
        funds += amount;
    }

    /**
     * Resta fondos del bloque.
     *
     * @param amount Cantidad de fondos a restar.
     */
    public void subtractFunds(int amount) {
        funds -= amount;
    }

    /**
     * Obtiene la cantidad de fondos agregados al bloque.
     *
     * @return Cantidad de fondos agregados.
     */
    public int getFundsAdded() {
        return funds;
    }

    /**
     * Verifica si el bloque está cerrado.
     *
     * @return `true` si el bloque está cerrado, `false` en caso contrario.
     */
    public boolean isClosed() {
        return hash != null;
    }

    /**
     * Obtiene la cantidad de fondos restantes en el bloque.
     *
     * @return Cantidad de fondos restantes.
     */
    public int getRemainingFunds() {
        return funds;
    }

    /**
     * Obtiene los datos almacenados en el bloque.
     *
     * @return Lista de datos almacenados en el bloque.
     */
    public List<String> getData() {
        return data;
    }
}





