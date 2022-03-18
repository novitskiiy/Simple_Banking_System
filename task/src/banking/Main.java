package banking;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static String url="";

    public static void main(String[] args) {
        url = "jdbc:sqlite:"+args[1];

        createNewTable();
        menu();
    }

    //Создание таблицы
    public static void createNewTable() {
        String sql = "CREATE TABLE IF NOT EXISTS card(" +
                "id INTEGER," +
                "number TEXT," +
                "pin TEXT," +
                "balance INTEGER DEFAULT 0)";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    //Добавление в таблицу нового клиента
    public static void createAccount(){
        Client client=new Client();

        String insertCard = "INSERT INTO card VALUES(?,?,?,?)";
        String countId = "SELECT count(*) FROM card";

        int count=0;
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(countId);
            ResultSet rs = pstmt.executeQuery()){

            rs.next();
            count = rs.getInt(1);

        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertCard)) {

            pstmt.setInt(1, count + 1);
            pstmt.setString(2, client.getCardNumber());
            pstmt.setString(3, client.getPIN());
            pstmt.setInt(4, 0);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nYour card has been created");
        System.out.println(client);
    }

    //Поиск клиента в таблице
    public static Client selectClient(String number, String pin){
        String sql = "SELECT number, pin, balance FROM card WHERE number = ? AND pin = ?";
        Client client = null;

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            pstmt.setString(1,number);
            pstmt.setString(2,pin);
            ResultSet rs  = pstmt.executeQuery();
            if(rs.next()){
                client = new Client();
                client.setCardNumber(rs.getString("number"));
                client.setPIN(rs.getString("pin"));
                client.setBalance(rs.getInt("balance"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return client;
    }

    //Создан ли аккаунт у клиента по номеру карты
    public static boolean isClient(String number){
        String sql = "SELECT number, pin, balance FROM card WHERE number = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            pstmt.setString(1,number);
            ResultSet rs  = pstmt.executeQuery();
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    //Внести депозит
    public static void addIncome(String cardNumber, String pin, int deposit) {
        String sql = "UPDATE card SET balance = balance + ? WHERE number = ? AND pin = ? ";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deposit);
            pstmt.setString(2, cardNumber);
            pstmt.setString(3, pin);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Удаление аккаунта клиента
    public static void closeAccount(String cardNumber) {
        String sql = "DELETE FROM card WHERE number = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cardNumber);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Перевод средств другому клиенту
    public static void doTransfer(String numberReceiver, String numberSender, int amount) {
        String sqlAddMoney = "UPDATE card SET balance = balance + ? WHERE number = ?";
        String sqlTakeAwayMoney = "UPDATE card SET balance = balance - ? WHERE number = ?";

        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement pstmt1 = null, pstmt2 = null;

        try {
            conn = connect();
            if(conn == null)
                return;
            conn.setAutoCommit(false);
            pstmt1 = conn.prepareStatement(sqlAddMoney,
                    Statement.RETURN_GENERATED_KEYS);

            pstmt1.setInt(1, amount);
            pstmt1.setString(2, numberReceiver);
            int rowAffected = pstmt1.executeUpdate();

            rs = pstmt1.getGeneratedKeys();
            if (rowAffected != 1) {
                conn.rollback();
            }

            pstmt2 = conn.prepareStatement(sqlTakeAwayMoney);
            pstmt2.setInt(1, amount);
            pstmt2.setString(2, numberSender);
            pstmt2.executeUpdate();

            conn.commit();

        } catch (SQLException e1) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
            System.out.println(e1.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt1 != null) {
                    pstmt1.close();
                }
                if (pstmt2 != null) {
                    pstmt2.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e3) {
                System.out.println(e3.getMessage());
            }
        }
    }

    //Создан ли аккаунт
    public static boolean isAccountCreated(String cardNumber, String PIN){
        return selectClient(cardNumber, PIN) != null;
    }

    //Соответсвует ли номер карты алгоритму Луна
    public static boolean isCardLuhn(String cardNumber) {
        int sum=0;
        String[] strArray = cardNumber.split("");
        int[] numArray = new int[strArray.length];

        for (int i = 0; i < strArray.length; i++) {
            numArray[i] = Integer.parseInt(strArray[i]);
        }

        for(int i=0; i<numArray.length-1; i++){
            if((i+1) % 2 !=0)
                numArray[i] *=2;
            if(numArray[i] > 9)
                numArray[i] -= 9;
            sum += numArray[i];
        }
        return (sum + numArray[15]) % 10 == 0;
    }

    //Log into account
    public static void logInto(){
        Scanner scanner=new Scanner(System.in);
        String choose;
        boolean checker=true;
        Client client;

        System.out.println("\nEnter your card number:");
        String cardNumber=scanner.next();
        System.out.println("Enter your PIN:");
        String PIN=scanner.next();

        if(isAccountCreated(cardNumber, PIN)) {
            System.out.println("\nYou have successfully logged in!");
            while (checker) {
                client = selectClient(cardNumber, PIN);

                System.out.println("\n1. Balance");
                System.out.println("2. Add income");
                System.out.println("3. Do transfer");
                System.out.println("4. Close account");
                System.out.println("5. Log out");
                System.out.println("0. Exit");

                choose = scanner.next();
                switch(choose){
                    case "1":
                        System.out.println("\nBalance: "+client.getBalance());
                        break;
                    case "2":
                        System.out.println("Enter amount of deposit");
                        int deposit=scanner.nextInt();
                        addIncome(client.getCardNumber(), client.getPIN(), deposit);
                        break;
                    case "3":
                        System.out.println("Enter card number of receiver:");
                        String numberReceiver = scanner.next();
                        if(client.getCardNumber().equals(numberReceiver)) {
                            System.out.println("You can't transfer money to the same account!");
                            break;
                        }
                        else if (!isCardLuhn(numberReceiver)) {
                            System.out.println("Probably you made a mistake in the card number. Please try again!");
                            break;
                        }
                        else if (!isClient(numberReceiver)) {
                            System.out.println("Such a card does not exist.");
                            break;
                        }
                        else
                            System.out.println("Enter amount of transaction:");
                            int transfer = scanner.nextInt();
                            if(transfer > client.getBalance()) {
                                System.out.println("Not enough money!");
                                break;
                            }
                            else {
                                doTransfer(numberReceiver, client.getCardNumber(), transfer);
                                break;
                            }
                    case "4":
                        closeAccount(client.getCardNumber());
                        System.out.println("\nYour account has been closed!");
                        checker=false;
                        break;
                    case "5":
                        System.out.println("\nYou have successfully logged out!");
                        checker=false;
                        break;
                    case "0":
                        System.out.println("\nBye!");
                        System.exit(0);
                    default:
                        System.out.println("Wrong input!");
                        break;
                }
            }
        }
        else
            System.out.println("\nWrong card number or PIN!\n");
    }

    //Menu
    public static void menu(){
        int choose;
        Scanner scanner=new Scanner(System.in);

        while(true){

            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");

            choose = scanner.nextInt();
            switch(choose){
                case 1:
                    createAccount();
                    break;
                case 2:
                    logInto();
                    break;
                case 0:
                    System.out.println("\nBye!");
                    System.exit(0);
                default:
                    System.out.println("\nWrong input!");
                    break;
            }
        }
    }
}
