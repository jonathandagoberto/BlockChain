package blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Representa una blockchain.
 */
public class Blockchain {
    private static final int INITIAL_FUNDS = 10_000_000; // Cantidad inicial de fondos en el bloque génesis
    private static ArrayList<Block> blockchain = new ArrayList<>();
    private static boolean isClosed = false;
    private static int fundsFromGenesisBlock = INITIAL_FUNDS;

    public static void main(String[] args) {
        createGenesisBlock();
        boolean continueLoop = true;
        Scanner scanner = new Scanner(System.in);

        while (continueLoop) {
            System.out.println("¿Qué deseas hacer?");
            System.out.println("1. Registrar una transacción");
            System.out.println("2. Consultar la dirección");
            System.out.println("3. Ver todos los bloques creados");
            System.out.println("4. Salir");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    handleTransactionRegistration(scanner);
                    break;

                case 2:
                    System.out.println("Dirección actual: " + getWalletAddress());
                    System.out.print("Ingresa la dirección wallet que quieres consultar: ");
                    String address = scanner.next();
                    showBlockByWalletAddress(address);
                    break;

                case 3:
                    showAllBlocks();
                    break;

                case 4:
                    continueLoop = false;
                    break;

                default:
                    System.out.println("Opción inválida. Por favor, intenta nuevamente.");
                    break;
            }
        }

        scanner.close();
    }

    /**
     * Crea el bloque génesis y distribuye los fondos iniciales.
     */
    public static void createGenesisBlock() {
        Block genesisBlock = new Block(1, "0");
        genesisBlock.addFunds(INITIAL_FUNDS);
        blockchain.add(genesisBlock);

        System.out.println("ID del bloque: " + genesisBlock.id);
        System.out.println("Nonce: " + genesisBlock.nonce);
        System.out.println("Hash previo: " + genesisBlock.previousHash);
        System.out.println("Hash: " + genesisBlock.hash);
        System.out.println("Dirección: " + genesisBlock.walletAddress);
        System.out.println("Fondos Disponibles: " + genesisBlock.getFundsAdded());
        System.out.println("¡Bloque génesis creado exitosamente!");

        // Distribuir los fondos del bloque génesis a los bloques que los necesiten
        distributeFundsFromGenesisBlock(genesisBlock);
    }

    /**
     * Cierra el bloque actual y crea uno nuevo.
     */
    public static void closeCurrentBlock() {
        Block currentBlock = blockchain.get(blockchain.size() - 1);
        currentBlock.closeBlock();
        isClosed = true;

        Block newBlock = new Block(currentBlock.id + 1, currentBlock.hash);
        blockchain.add(newBlock);

        System.out.println("ID del bloque: " + newBlock.id);
        System.out.println("Nonce: " + newBlock.nonce);
        System.out.println("Hash previo: " + newBlock.previousHash);
        System.out.println("Hash: " + newBlock.hash);
        System.out.println("Dirección: " + newBlock.walletAddress);
        System.out.println("------------------------");
        System.out.println("¡Nuevo bloque creado exitosamente!");
        System.out.println("------------------------");
        isClosed = false;
    }

    /**
     * Registra una transacción en el bloque actual.
     *
     * @param sender   Dirección wallet del remitente.
     * @param amount   Monto de la transacción.
     * @param receiver Dirección wallet del receptor.
     */
    public static void registerTransaction(String sender, int amount, String receiver) {
        Block currentBlock = blockchain.get(blockchain.size() - 1);
        String transaction = "Sender: " + sender + ", Amount: " + amount + ", Receiver: " + receiver;
        currentBlock.addData(transaction);
        currentBlock.subtractFunds(amount); // Resta los fondos del remitente

        // Distribuir los fondos de la transacción a los bloques anteriores que los necesiten
        for (int i = blockchain.size() - 2; i >= 0; i--) {
            Block block = blockchain.get(i);
            if (!block.isClosed() && block.getRemainingFunds() < amount) {
                int distributedFunds = Math.min(amount, block.getRemainingFunds());
                block.addFunds(distributedFunds);
                amount -= distributedFunds;
            }
        }

        fundsFromGenesisBlock -= amount; // Resta los fondos del bloque génesis
    }

    /**
     * Distribuye los fondos desde el bloque génesis a los bloques que los necesiten.
     *
     * @param genesisBlock Bloque génesis.
     */
    public static void distributeFundsFromGenesisBlock(Block genesisBlock) {
        for (int i = blockchain.size() - 2; i >= 0; i--) {
            Block block = blockchain.get(i);
            if (!block.isClosed() && block.getRemainingFunds() == 0) {
                int distributedFunds = Math.min(fundsFromGenesisBlock, block.getRemainingFunds());
                block.addFunds(distributedFunds);
                fundsFromGenesisBlock -= distributedFunds;
            }
        }
    }

    /**
     * Obtiene la dirección wallet del bloque actual.
     *
     * @return Dirección wallet del bloque actual.
     */
    public static String getWalletAddress() {
        return blockchain.get(blockchain.size() - 1).walletAddress;
    }

    /**
     * Muestra todos los bloques creados en la blockchain junto con sus transacciones.
     */
    public static void showAllBlocks() {
        System.out.println("------------------------");
        System.out.println("Mostrando todos los bloques creados:");
        System.out.println("------------------------");

        for (Block block : blockchain) {
            System.out.println("ID del bloque: " + block.id);
            System.out.println("Nonce: " + block.nonce);
            System.out.println("Hash previo: " + block.previousHash);
            System.out.println("Hash: " + block.hash);
            System.out.println("Dirección: " + block.walletAddress);
            System.out.println("Fondos gastados: " + block.getFundsAdded());
            System.out.println("Transacciones: ");
            for (String transaction : block.getData()) {
                System.out.println(transaction);
            }
            System.out.println();
        }

        // Descuenta lo gastado en los otros bloques del saldo del bloque génesis
        int spentFromGenesisBlock = 0;
        for (int i = 1; i < blockchain.size(); i++) {
            Block block = blockchain.get(i);
            spentFromGenesisBlock += block.getFundsAdded() - block.getRemainingFunds();
        }
        fundsFromGenesisBlock -= spentFromGenesisBlock;

        System.out.println("Saldo del bloque génesis: " + fundsFromGenesisBlock);
    }

    /**
     * Muestra el bloque que contiene la dirección wallet especificada.
     *
     * @param walletAddress Dirección wallet a buscar.
     */
    public static void showBlockByWalletAddress(String walletAddress) {
        System.out.println("------------------------");
        System.out.println("Mostrando información del bloque con dirección wallet: " + walletAddress);
        System.out.println("------------------------");

        for (Block block : blockchain) {
            if (block.walletAddress.equals(walletAddress)) {
                System.out.println("ID del bloque: " + block.id);
                System.out.println("Nonce: " + block.nonce);
                System.out.println("Hash previo: " + block.previousHash);
                System.out.println("Hash: " + block.hash);
                System.out.println("Dirección: " + block.walletAddress);
                System.out.println("Fondos gastados: " + block.getFundsAdded());
                System.out.println("Transacciones: ");
                for (String transaction : block.getData()) {
                    System.out.println(transaction);
                }
                System.out.println();
                return;
            }
        }

        System.out.println("No se encontró ningún bloque con la dirección wallet especificada: " + walletAddress);
    }

    /**
     * Maneja el registro de una transacción solicitando los datos al usuario.
     *
     * @param scanner Objeto Scanner para leer la entrada del usuario.
     */
    public static void handleTransactionRegistration(Scanner scanner) {
        if (blockchain.get(blockchain.size() - 1).getTransactionCount() < 3) {
            if (!isClosed) {
                System.out.println("------------------------");
                System.out.println("Ingresa las tres transacciones:");
                System.out.println("------------------------");
                for (int i = 0; i < 3; i++) {
                    System.out.println("Ingresa la dirección wallet del remitente: ");
                    String sender = scanner.next();
                    System.out.println("Ingresa el monto de la transacción: ");
                    int amount = scanner.nextInt();
                    System.out.println("Ingresa la dirección wallet del receptor: ");
                    String receiver = scanner.next();

                    registerTransaction(sender, amount, receiver);
                    System.out.println("------------------------");
                    System.out.println("¡Transacción registrada exitosamente!");
                    System.out.println("------------------------");
                }
                System.out.println("------------------------");
                System.out.println("¡Bloque cerrado exitosamente!");
                System.out.println("------------------------");
                closeCurrentBlock();
            } else {
                System.out.println("No puedes registrar más transacciones en el bloque actual. Cierra el bloque y crea uno nuevo.");
            }
        } else {
            System.out.println("El bloque actual ya contiene tres transacciones. Cierra el bloque y crea uno nuevo.");
        }
    }
}


