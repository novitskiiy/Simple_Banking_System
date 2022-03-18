package banking;

import java.util.Random;

public class Client {
    private String cardNumber;
    private String PIN;
    private int balance=0;

    public Client(){
        this.cardNumber=randomCardNumber();
        this.PIN=randomPIN();
    }

    // Generation random card number based on Luhn algorithm
    public String randomCardNumber(){
        Random random=new Random();
        int sum=0;
        String result="";
        int MII=4;
        int[] BIN = {MII, 0, 0, 0, 0, 0};
        int[] accIdent= new int[9];
        int checksum=0;
        int[] woChecksum = new int[15];

        for(int i = 0; i < accIdent.length; i++){
            accIdent[i]= random.nextInt(10);
        }

        for(int i=0; i<woChecksum.length; i++){
            if(i<6)
                woChecksum[i] = BIN[i];
            else
                woChecksum[i] = accIdent[i-6];
            result+=Integer.toString(woChecksum[i]);
        }

        for(int i=0; i<woChecksum.length; i++){
            if((i+1) % 2 !=0)
                woChecksum[i] *=2;
            if(woChecksum[i] > 9)
                woChecksum[i] -= 9;
            sum += woChecksum[i];
        }

        for(int i=0; i<10; i++){
            if((sum+i) % 10 == 0)
                checksum=i;
        }
        result+=Integer.toString(checksum);
        return result;
    }

    //Generation PIN
    public String randomPIN(){
        int[] pin=new int[4];
        String pinResult="";
        Random random=new Random();

        for(int i = 0; i < pin.length; i++){
            pin[i]= random.nextInt(10);
            pinResult+=pin[i];
        }

        return pinResult;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Your card number:\n" + cardNumber + "\n" +
                "Your PIN:\n" + PIN + "\n";
    }
}
