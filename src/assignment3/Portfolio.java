package assignment3;

import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;

/**
 * @author Gordon Restivo Date last modified 11/29/2017
 *
 * This class manipulates investments (either stocks or mutual funds) by giving
 * the user several manipulation options. A command menu runs to give the user 6
 * different options. The options are buy, sell, update, getGain, search and
 * quit. When the program ends the current state of the investment ArrayList is
 * written to file, and when the program is loaded any investments on file are
 * loaded into the investment ArrayList
 * 
 * This class also utilizes Java Swing to create a GUI interface that allows the user
 * to switch between different screens with the different command menu options
 */
public class Portfolio {

    JFrame frame = new JFrame("Investment Portfolio");
    JPanel mainPane = new JPanel();
    JPanel introPane = new JPanel(new GridLayout(2, 1));

    CardLayout cardLayout = new CardLayout();
    private JTextArea area;

    private static String fileName;
    private int investmentType;
    private String returnedString;
    private String userInput;
    private String symbol;
    private String name;
    private int quantity;
    private double price;
    private double bookValue;

    private int currentLocation = 0;

    private final ArrayList<Investments> investment = new ArrayList<>();//ArrayList that holds all current investments
    Scanner keyboard = new Scanner(System.in);
    HashMap<String, ArrayList<Integer>> hmap = new HashMap<>();//hash map that holds all tokenized names from investmens with relative addresses
    ArrayList<Integer> intArray = new ArrayList<>();//ArrayList that holds intgers used as the value for the hash map above
    public static int investmentNum = 0;// Static variable that keeps track of the number of made investments

    /**
     * buyInvestments method allows the user to buy a specific quantity of a new
     * investment or mutual fund or update an existing investment by adding more
     * quantity This method will also modify the name hash map if a new
     * investment is made
     *
     * @param type Tells the method if the user is buying a investment or mutual
     * @param symbol the symbol of the investment to be updated or purchased
     * @param name the name of the investments to be purchased
     * @param tempQuantity the number of units to purchase
     * @param tempPrice the price of each unit
     * 
     * @return a message saying whether the purchase was successful, if it was updated
     * or if the inputs were invalid
     */
    private String buyInvestments(int type, String symbol, String name, String tempQuantity, String tempPrice) {
        String tempToken;
        int tempInt = -1, check = 0, symbolCheck = 0, existCheck = 0;

        try {
            quantity = Integer.parseInt(tempQuantity);
            price = Double.parseDouble(tempPrice);
        } catch (Exception e) {
            return "Integer and quantity must be proper numbers!";
        }

        //This loop gets the companies symbol from the user 
        for (int i = 0; i < investment.size(); i++) {
            if (investment.get(i).getClass().getCanonicalName().equals("assignment3.MutualFunds") && type == 1) {
                if (symbol.equals(investment.get(i).getSymbol())) {
                    check = 1;
                }
            } else if (investment.get(i).getClass().getCanonicalName().equals("assignment3.Stocks") && type == 2) {
                if (symbol.equals(investment.get(i).getSymbol())) {
                    check = 1;
                }
            }
        }
        if (check == 1) {
            return "Investment exists in opposite type! please enter a new symbol: ";
        }

        //for loop to check if the symbol aleady exists, if true then the name portion will be skipped
        for (int i = 0; i < investment.size(); i++) {
            if (investment.get(i).getSymbol().equalsIgnoreCase(symbol)) {
                symbolCheck = 1;
            }
        }

        /*This while loop gets the companies name name from user, tokenizes it,
         * and modifies the hash map of names accordingly
         */
        if (symbolCheck == 0) {
            StringTokenizer splitSentence = new StringTokenizer(name, ", ");
            while (splitSentence.hasMoreTokens()) {
                tempToken = splitSentence.nextToken();
                String lowerCase = tempToken.toLowerCase();
                if (hmap.containsKey(lowerCase)) { //the hash map already contains the name
                    intArray = hmap.get(tempToken);//gets current int array form hashmap key
                    intArray.add(investmentNum);
                } else {
                    intArray = new ArrayList<>();
                    intArray.add(investmentNum);
                    hmap.put(tempToken, intArray); //adds new key to hash map
                }
            }
        } else {
            existCheck = 1;
        }

        //These series of statements create a new investment or updates an old investment
        tempInt = buyUpdate(type, symbol, quantity, price);
        bookValue = computeBookValue(type, quantity, price);
        if (tempInt == -1) {
            if (type == 1) {
                Stocks newInvestments;
                try {
                    newInvestments = new Stocks(symbol, name, tempQuantity, tempPrice, bookValue);
                } catch (Investments.symbolException | Investments.nameException | Investments.quantityException | Investments.priceException | Investments.bookValueException ex) {
                    return ex.getMessage();
                }
                investment.add(newInvestments);
                return "New Stock Investment added. Book value is: " + bookValue;
            } else if (type == 2) {
                MutualFunds newInvestments;
                try {
                    newInvestments = new MutualFunds(symbol, name, tempQuantity, tempPrice, bookValue);
                } catch (Investments.symbolException | Investments.nameException | Investments.quantityException | Investments.priceException | Investments.bookValueException ex) {
                    return ex.getMessage();
                }
                investment.add(newInvestments);
                return "New Mutual Fund Investment added. Book value is: " + bookValue;
            }
        } else {
            if (existCheck == 1) {
                return "Previous investment updated. New book value is: " + bookValue;
            }
        }
        investmentNum++;
        return "";
    }

    /**
     * computeBookValue method computes the book value by multiplying the
     * quantity by the price (and adding 9.99 if it is a investment)
     *
     * @param type tells the method if the user is purchasing a investment or
     * mutual fund
     * @param quantity is how many attributes of the investment you wish to
     * purchase
     * @param price is the price of the new investment
     * 
     * @return currentBookValue is the book value computed by the method
     */
    private double computeBookValue(int type, int quantity, double price) {
        double currentBookValue = 0.0;

        if (type == 1) {/*investment*/
            currentBookValue = quantity * price + 9.99;
        } else if (type == 2) {/*mutual fund*/
            currentBookValue = quantity * price;
        }

        return currentBookValue;
    }

    /**
     * buyUpdate method checks to see if the user has already made an investment
     * with the same symbol If yes : the quantity and book value are updated
     * accordingly If no: -1 is returned and a new investment is created
     *
     * @param type is if the investment is a investment or mutual fund
     * @param symbol is the symbol of the given investment
     * @param quantity is the amount of attributes for the given investment
     * @param price is the price of the given investment
     * @return 0 if investment is found, 1 if mutual fund is found and -1 if
     * nothing is found
     */
    private int buyUpdate(int type, String symbol, int quantity, double price) {
        int prevQuantity, newQuantity;
        double oldBookValue, newBookValue;

        for (int i = 0; i < investment.size(); i++) {
            if (symbol.equals(investment.get(i).getSymbol())) {
                prevQuantity = investment.get(i).getQuantity();
                newQuantity = prevQuantity + quantity;

                oldBookValue = investment.get(i).getBookValue();
                newBookValue = computeBookValue(type, quantity, price);

                if (investment.get(i).getClass().getCanonicalName().equals("assignmente.Stocks")) {
                    newBookValue = newBookValue + oldBookValue + 9.99;
                } else {
                    newBookValue = newBookValue + oldBookValue;
                }

                try {
                    investment.get(i).setQuantity(newQuantity);
                } catch (Investments.quantityException ex) {
                    ex.getMessage();
                }
                try {
                    investment.get(i).setPrice(price);
                } catch (Investments.priceException ex) {
                    ex.getMessage();
                }

                investment.get(i).setBookValue(newBookValue);
                return 0;
            }
        }
        return -1;
    }

    /**
     * sellInvestments method sells a specified quantity of a given investment
     * The user is asked for the symbol of the investment they wish to sell from
     * If it exists: The user is asked to give a quantity to be sold (less than
     * owned quantity and >= to 0) and a price to be sold at The money made is
     * printed to output and the quantity and book value are adjusted
     * accordingly if new quantity == 0 then the investment is deleted If it
     * does not exist: The user is told the investment does not exist and is
     * brought back to the original command prompts
     *
     * This method will also modify the hash map accordingly when investments
     * are deleted
     */
    private String sellInvestments(String symbol, String givenQuantity, String givenPrice) {
        int prevQuantity = 0, newQuantity = 0, exists = 0, location = 0;
        double newBookValue, amountPaid = 0;

        if (symbol.isEmpty() || givenQuantity.isEmpty() || givenPrice.isEmpty()) {
            return "You cannot enter empty values! Please try again.";
        }
        if (!isNum(givenQuantity) || !isNum(givenPrice)) {
            return "Quantity and price must be numeric values! Please try again.";
        }
        price = Double.parseDouble(givenPrice);
        quantity = Integer.parseInt(givenQuantity);
        if (price <= 0 || quantity <= 0) {
            return "Price and quantity must be values greater than 0! Please try again.";
        }

        //For loop to see if investment exists
        for (int i = 0; i < investment.size(); i++) {
            if (symbol.equals(investment.get(i).getSymbol())) {
                prevQuantity = investment.get(i).getQuantity();
                location = i;
                exists = 1;
            }
        }

        //If nothing is found prints that the investment doesnt exist
        if (exists != 1) {
            return "Investment does not exist.";
        } else { //Gets quantity to sell and adjusts to new quantity and book value
            newQuantity = prevQuantity - quantity;
            try {
                investment.get(location).setQuantity(newQuantity);
            } catch (Investments.quantityException ex) {
                return ex.getMessage();
            }
            newBookValue = investment.get(location).getBookValue();
            newBookValue = newBookValue * ((prevQuantity - quantity) / prevQuantity);
            investment.get(location).setBookValue(newBookValue);
            try {
                investment.get(location).setPrice(price);
            } catch (Investments.priceException ex) {
                return ex.getMessage();
            }
            if (investment.get(location).getClass().getCanonicalName().equals("assignment3.Stocks")) {
                amountPaid = quantity * price - 9.99;
            } else {
                amountPaid = quantity * price - 45;
            }

            //If statement that deletes the investment and updates the hash map accordingly
            if (newQuantity == 0) {
                String name = investment.get(location).getName();
                investment.remove(location);//delete from array list
                StringTokenizer splitSentence = new StringTokenizer(name, ", ");
                while (splitSentence.hasMoreTokens()) {
                    String tempToken = splitSentence.nextToken();
                    intArray = hmap.get(tempToken);//gets current int array
                    int arrayLength = intArray.size();
                    if (arrayLength != 0) {
                        for (int i = 0; i < arrayLength; i++) {//for loop that removes instance of deleted investment from hash map
                            if ((int) intArray.get(i) == location) {
                                intArray.remove(i);
                                break;
                            }
                        }
                        if (intArray.isEmpty()) {//removes hash map key if associated value is empty
                            hmap.remove(tempToken);
                        }
                    } else {
                        hmap.remove(tempToken);//removes hash map key if associated value is empty
                    }
                }

                //For each loop that decrements all key values > the deleted investment value
                for (Map.Entry pair : hmap.entrySet()) {
                    intArray = (ArrayList<Integer>) pair.getValue();
                    for (int i = 0; i < intArray.size(); i++) {
                        if (intArray.get(i) > location) {
                            intArray.set(i, intArray.get(i) - 1);
                        }
                    }
                }
            }
        }
        return "Previous Quantity: " + prevQuantity + "\nNew Quantity: " + newQuantity + "\nYou sold " + quantity + " for " + amountPaid + " dollars.";
    }

    /**
     * update method allows the user to update any or all of the previously made
     * investments It iterates through all investments and asked the user to
     * give the investment a new price If not previous investments have been
     * made the user is alerted
     * 
     * @param price is the new price to update the investment with 
     */
    private void update(double price) {
        double newBookValue;

        if (investment.get(currentLocation).getClass().getCanonicalName().equals("assignment3.Stocks")) {
            newBookValue = (investment.get(currentLocation).getQuantity()) * price - 9.99;
        } else {
            newBookValue = (investment.get(currentLocation).getQuantity()) * price;
        }
        try {
            investment.get(currentLocation).setPrice(price);
        } catch (Investments.priceException ex) {
            ex.getMessage();
        }

        String stringNewBookValue = Double.toString(newBookValue);
        for (int i = 0; i < stringNewBookValue.length(); i++) {
            if (stringNewBookValue.charAt(i) == '.') {
                try {
                    stringNewBookValue = stringNewBookValue.substring(0, i + 3);
                } catch (Exception e) {
                }
                break;
            }
        }
    }

    /**
     * getTotalGain and getSepGain methods calculate the total gain made on all previously made
     * investments. The gain made from each investment is calculated by
     * multiplying the current quantity by the current price The gain off each
     * individual investment is printed along with the total gain over all
     * investments at the end
     * 
     * @return a string with appropriate response message
     */
    private String getTotGain() {
        double newGain, totalGain = 0;

        //checks if there are any investments and prints total gain if there is. 
        for (int i = 0; i < investment.size(); i++) {
            price = investment.get(i).getPrice();
            bookValue = investment.get(i).getBookValue();
            quantity = investment.get(i).getQuantity();
            if (investment.get(i).getClass().getCanonicalName().equals("assignment3.Stocks")) {
                newGain = (price * quantity) - bookValue;
            } else {
                newGain = (price * quantity - 45) - bookValue;
            }
            totalGain = totalGain + newGain;
        }
        String stringTotalGain = Double.toString(totalGain);
        for (int i = 0; i < stringTotalGain.length(); i++) {
            if (stringTotalGain.charAt(i) == '.') {
                try {
                    stringTotalGain = stringTotalGain.substring(0, i + 3);
                } catch (Exception e) {
                }
                break;
            }
        }
        return stringTotalGain;
    }

    
    private String getSepGain() {
        double newGain;
        String output = "";

        //checks if there are any investments and prints total gain if there is. 
        for (int i = 0; i < investment.size(); i++) {
            symbol = investment.get(i).getSymbol();
            name = investment.get(i).getName();
            price = investment.get(i).getPrice();
            bookValue = investment.get(i).getBookValue();
            quantity = investment.get(i).getQuantity();
            if (investment.get(i).getClass().getCanonicalName().equals("assignment3.Stocks")) {
                newGain = (price * quantity) - bookValue;
            } else {
                newGain = (price * quantity - 45) - bookValue;
            }
            String stringNewGain = Double.toString(newGain);
            for (int j = 0; j < stringNewGain.length(); j++) {
                if (stringNewGain.charAt(j) == '.') {
                    try {
                        stringNewGain = stringNewGain.substring(0, j + 3);
                    } catch (Exception e) {
                    }
                    break;
                }
            }
            output += "Gain for " + symbol + " = " + stringNewGain + "\n";
        }

        return output;
    }

    /**
     * search method searches for matching investment or mutual fund instances
     * based upon given input by the user
     *
     * The given inputs can be all, none or some of symbol, name or price. Based
     * on the combination of the given inputs the method will display all of the
     * matches, only if it matches all given inputs
     *
     * @param symbol the symbol of the investment to search for, can be empty
     * string
     * @param nameKeywords the name keywords of the investment to search for,
     * can be empty string
     * @param lowPrice the lower price bound for the investment search, can be
     * empty string
     * @param highPrice the higher price bound for the investment search, can be
     * empty string
     *
     * @return is the returned investments or an error message for incorrect
     * input
     */
    private String search(String symbol, String nameKeywords, String lowPrice, String highPrice) {
        String returnOutput;
        double lowRange = -1, highRange = -1;
        int check1 = 0, check2 = 0, check3 = 0, rangeType = 0;

        //ensures proper input (if any) for the low price range
        if (!lowPrice.equals("")) {
            if (!isNum(lowPrice)) {
                return "Invalid price input! Try again.";
            }
            lowRange = Double.parseDouble(lowPrice);
            check3 = 1;
        }
        //ensures proper input (if any) for the high price range
        if (!highPrice.equals("")) {
            if (!isNum(highPrice)) {
                return "Invalid price input! Try again.";
            }
            highRange = Double.parseDouble(highPrice);
            check3 = 1;
        }

        //If price range values are invalid an error message is returned
        if (lowRange != -1 && lowRange < 0) {
            return "Invalid price input, must be positive! Try again.";
        }
        if (highRange != -1 && highRange < 0) {
            return "Invalid price input, must be positive! Try again.";
        }
        if (highRange != -1 && lowRange != -1 && lowRange > highRange) {
            return "Invalid price range! Low price must be less than high price. Try Again";
        }

        //Check if symbol and name keywords are empty
        if (!symbol.equals("")) {
            check1 = 1;
        }
        if (!nameKeywords.equals("")) {
            check2 = 1;
        }

        //If price ranges exist the set designated price range type
        if (highRange != -1 && lowRange != -1) {
            rangeType = 4;
        } else if (highRange != -1 && lowRange == -1) {
            rangeType = 3;
        } else {
            rangeType = 2;
        }

        String tempNames, tempSymbol;
        int tempQuantity;
        double tempPrice, tempBookValue;

        ArrayList<Investments> searchInvestments = new ArrayList<>(investment.size());//new ArrayList to store investments that can be searched through and deleted
        ArrayList<Integer> searchArray = new ArrayList<>();//ArrayList that holds values that are returned from the hasp map
        ArrayList<Integer> finalSearchArray = new ArrayList<>();//ArrayList that holds values that are returned from the hasp map
        ArrayList<Integer> deleteArray = new ArrayList<>();//ArrayList that holds values that do not match the search criteria 

        //For loop that copies values from original investment ArrayList into new search ArrayList
        if (check2 != 1) {
            for (int i = 0; i < investment.size(); i++) {
                tempSymbol = investment.get(i).getSymbol();
                tempNames = investment.get(i).getName();
                tempQuantity = investment.get(i).getQuantity();
                tempPrice = investment.get(i).getPrice();
                tempBookValue = investment.get(i).getBookValue();
                if (investment.get(i).getClass().getCanonicalName().equals("assignment3.Stocks")) {
                    Stocks newInvestments;
                    try {
                        String quantity = Integer.toString(tempQuantity);
                        String price = Double.toString(tempPrice);
                        newInvestments = new Stocks(tempSymbol, tempNames, quantity, price, tempBookValue);
                    } catch (Investments.symbolException | Investments.nameException | Investments.quantityException | Investments.priceException | Investments.bookValueException ex) {
                        return ex.getMessage();
                    }
                    searchInvestments.add(newInvestments);
                } else {
                    MutualFunds newInvestments;
                    try {
                        String quantity = Integer.toString(tempQuantity);
                        String price = Double.toString(tempPrice);
                        newInvestments = new MutualFunds(tempSymbol, tempNames, quantity, price, tempBookValue);
                    } catch (Investments.symbolException | Investments.nameException | Investments.quantityException | Investments.priceException | Investments.bookValueException ex) {
                        return ex.getMessage();
                    }
                    searchInvestments.add(newInvestments);
                }
            }
        }

        //Print out all investments because no fields were filled
        if (check1 == 0 && check2 == 0 && check3 == 0) {
            returnOutput = "Returned investments: \n\n";
            for (int i = 0; i < investment.size(); i++) {
                returnOutput += "symbol = " + searchInvestments.get(i).getSymbol() + "\n";
                returnOutput += "name = " + searchInvestments.get(i).getName() + "\n";
                returnOutput += "quantity = " + searchInvestments.get(i).getQuantity() + "\n";
                returnOutput += "price = " + searchInvestments.get(i).getPrice() + "\n";
                returnOutput += "bookValue = " + searchInvestments.get(i).getBookValue() + "\n\n";
            }
            return returnOutput;
        } else {//Print out investments based on specific input fields
            int removeCheck, nameCheck, equalCheck = 0, doubleArrayCheck = 0, searchCheck = 0;

            /*If the user inputted key words for the search this block will return a list of integers that match the search criteria
             *If the user does input key words then there are seperate statements for symbol search and price range search that only
             *search the list of integers given from the hash map*/
            if (check2 == 1) {
                nameCheck = 0;
                StringTokenizer splitSentence = new StringTokenizer(nameKeywords, ", ");
                while (splitSentence.hasMoreTokens()) {
                    String tempToken = splitSentence.nextToken();
                    if (hmap.containsKey(tempToken.toLowerCase())) {//if the hashmap contains the given key word
                        if (nameCheck == 0) { //if first keyword to be found in hash map
                            intArray = hmap.get(tempToken);//gets current int array
                            for (int i = 0; i < intArray.size(); i++) {
                                searchArray.add(intArray.get(i));
                            }
                            nameCheck = 1;
                        } else { //if second key word or greater to be found in hash map
                            intArray = hmap.get(tempToken); //set intArray equal to ArrayList at key word
                            for (int i = 0; i < searchArray.size(); i++) { //for the size of the intArray ArrayList
                                for (int j = 0; j < intArray.size(); j++) {
                                    if (Objects.equals(searchArray.get(i), intArray.get(j))) {
                                        searchCheck = 1;
                                    }
                                }
                                if (searchCheck != 1) {
                                    searchArray.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                }
                if (check1 == 1) {//Check if symbol matches name keyword instances
                    for (int i = 0; i < searchArray.size(); i++) {
                        if (!investment.get(searchArray.get(i)).getSymbol().equalsIgnoreCase(symbol)) {
                            searchArray.remove(i);
                            i--;
                        }
                    }
                }
                if (check3 == 1) {//check if price range matches nam keyword instances
                    for (int i = 0; i < searchArray.size(); i++) {
                        switch (rangeType) {
                            case 2:
                                if (investment.get(searchArray.get(i)).getPrice() < lowRange) {
                                    searchArray.remove(i);
                                }
                                break;
                            case 3:
                                if (investment.get(searchArray.get(i)).getPrice() > highRange || investment.get(searchArray.get(i)).getPrice() < 0) {
                                    searchArray.remove(i);
                                }
                                break;
                            case 4:
                                if (investment.get(searchArray.get(i)).getPrice() < lowRange || investment.get(searchArray.get(i)).getPrice() > highRange) {
                                    searchArray.remove(i);
                                }
                                break;
                        }
                    }
                }
            } else {//if key words were not entered, you still must search through investment ArrayList
                for (int i = 0; i < searchInvestments.size(); i++) {
                    removeCheck = 0;

                    if (check1 == 1) {//if the user inputted a symbol
                        if (!searchInvestments.get(i).getSymbol().equals(symbol)) {
                            removeCheck = 1;
                        }
                    }
                    if (check3 == 1) {//if the user inputted a price range
                        switch (rangeType) {
                            case 2:
                                if (searchInvestments.get(i).getPrice() < lowRange) {
                                    removeCheck = 1;
                                }
                                break;
                            case 3:
                                if (searchInvestments.get(i).getPrice() > highRange || searchInvestments.get(i).getPrice() < 0) {
                                    removeCheck = 1;
                                }
                                break;
                            case 4:
                                if (searchInvestments.get(i).getPrice() < lowRange || searchInvestments.get(i).getPrice() > highRange) {
                                    removeCheck = 1;
                                }
                                break;
                        }
                    }
                    if (removeCheck == 1) {
                        searchInvestments.remove(i);
                        i--;
                    }
                }
            }

            //If no search input was given then all investments are displayed and user is given this message
            if (searchInvestments.isEmpty() && searchArray.isEmpty()) {//Gives user error message if no investments matched the search 
                return "No returned investments.";
            } else if (check2 == 1) {
                returnOutput = "Returned investments: \n\n";
                for (int i = 0; i < searchArray.size(); i++) {//Prints out investments that the hash map returned if only key words were inputted
                    returnOutput += "symbol = " + investment.get(searchArray.get(i)).getSymbol() + "'\n";
                    returnOutput += "name = " + investment.get(searchArray.get(i)).getName() + "'\n";
                    returnOutput += "quantity = " + investment.get(searchArray.get(i)).getQuantity() + "'\n";
                    returnOutput += "price = " + investment.get(searchArray.get(i)).getPrice() + "'\n";
                    returnOutput += "bookValue = " + investment.get(searchArray.get(i)).getBookValue() + "'\n\n";

                }
            } else {
                returnOutput = "Returned investments: \n\n";
                for (int i = 0; i < searchInvestments.size(); i++) {//prints any investments returned in the search
                    returnOutput += "symbol = " + searchInvestments.get(i).getSymbol() + "\n";
                    returnOutput += "name = " + searchInvestments.get(i).getName() + "\n";
                    returnOutput += "quantity = " + searchInvestments.get(i).getQuantity() + "\n";
                    returnOutput += "price = " + searchInvestments.get(i).getPrice() + "\n";
                    returnOutput += "bookValue = " + searchInvestments.get(i).getBookValue() + "\n\n";
                }
            }
            return returnOutput;
        }
    }

    /**
     * isNum method returns true if the given string is a number or false if it
     * is not
     *
     * @param str is the given string which is to be check to see if it is a
     * integer or a double
     *
     * @return false if it *is not an integer or double, true if it is
     */
    public static boolean isNum(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * writeInvestmentsToFile method will run every time the program is closed
     * properly and will save the state of the investment ArrayList to a file
     */
    public void writeInvestmentsToFile() {
        // Write to a file
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            for (int i = 0; i < investment.size(); i++) {
                if (investment.get(i).getClass().getCanonicalName().equals("assignment3.Stocks")) {
                    writer.write("type = (S)");
                } else if (investment.get(i).getClass().getCanonicalName().equals("assignment3.MutualFunds")) {
                    writer.write("type = (M)");
                }
                writer.newLine();
                writer.write("symbol = (" + investment.get(i).getSymbol() + ")");
                writer.newLine();
                writer.write("name = (" + investment.get(i).getName() + ")");
                writer.newLine();
                writer.write("quantity = (" + investment.get(i).getQuantity() + ")");
                writer.newLine();
                writer.write("price = (" + investment.get(i).getPrice() + ")");
                writer.newLine();
                writer.write("bookValue = (" + investment.get(i).getBookValue() + ")");
                writer.newLine();
                writer.newLine();
            }
            writer.close();
            return;
        } catch (IOException e) {
            return;
        }
    }

    /**
     * readInvestmentsFromFile method will run every time the program is opened
     * and will read any investments in the file into the investment ArrayList
     */
    public String readInvestmentsFromFile()
            throws Investments.symbolException, Investments.nameException, Investments.quantityException, Investments.priceException, Investments.bookValueException {
        {
            String backUpFileName = "output.txt", type = "", tempQuantity = "", tempPrice = "";
            int count = 0;

            //runs a switch loop that parses the file input into investments
            BufferedReader reader;
            try {
                File f = new File(fileName);//Checks if file exists if command line arg was given
                if (f.exists()) {
                    reader = new BufferedReader(new FileReader(fileName));
                } else {//else creates a dummy file name
                    fileName = backUpFileName;
                    reader = new BufferedReader(new FileReader(fileName));
                }
                String line = reader.readLine();
                while (line != null) {//Reads in file line by line adding investments accordingly
                    count++;
                    if (count != 0) {//parses input based from brother and sister brackets
                        line = line.substring(line.indexOf("(") + 1);
                        line = line.substring(0, line.indexOf(")"));
                    }
                    switch (count) {
                        case 1:
                            type = line;
                            break;
                        case 2:
                            symbol = line;
                            break;
                        case 3:
                            name = line;
                            StringTokenizer splitSentence = new StringTokenizer(name, ", ");
                            while (splitSentence.hasMoreTokens()) {//adds new key to hash map or modifies old key value
                                String tempToken = splitSentence.nextToken();
                                String lowerCase = tempToken.toLowerCase();
                                if (hmap.containsKey(lowerCase)) {
                                    intArray = hmap.get(tempToken);//gets current int array
                                    intArray.add(investmentNum);
                                } else {
                                    intArray = new ArrayList<>();
                                    intArray.add(investmentNum);
                                    hmap.put(tempToken, intArray);
                                }
                            }
                            investmentNum++;
                            break;
                        case 4:
                            tempQuantity = line;
                            break;
                        case 5:
                            tempPrice = line;
                            break;
                        case 6:
                            bookValue = Double.parseDouble(line);
                            count = 7;
                            break;
                        default:
                            break;
                    }
                    if (count == 7) {
                        if (type.equals("S")) {
                            Stocks newInvestments;
                            try {
                                newInvestments = new Stocks(symbol, name, tempQuantity, tempPrice, bookValue);
                            } catch (Investments.symbolException | Investments.nameException | Investments.quantityException | Investments.priceException | Investments.bookValueException ex) {
                                return ex.getMessage();
                            }
                            investment.add(newInvestments);
                        } else if (type.equals("M")) {
                            MutualFunds newInvestments;
                            try {
                                newInvestments = new MutualFunds(symbol, name, tempQuantity, tempPrice, bookValue);
                            } catch (Investments.symbolException | Investments.nameException | Investments.quantityException | Investments.priceException | Investments.bookValueException ex) {
                                return ex.getMessage();
                            }

                            investment.add(newInvestments);
                        }
                        count = -1;
                    }

                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
            }
            return "";
        }
    }

    /**
     * Method that sets up GUI and runs all GUI command such as button action
     * command, menu option commands and text field commands
     *
     * This method interacts with all other method in this .java file to
     * effectively run a GUI interface for an investment portfolio
     */
    public Portfolio() {
        //Set up different fonts
        Font font1 = new Font("SansSerif", Font.BOLD, 13);
        Font font2 = new Font("MonoSpaced Plain", Font.BOLD, 18);
        Font font4 = new Font("MonoSpaced Plain", Font.BOLD, 20);
        Font font3 = new Font("SansSerif", Font.BOLD, 16);
        Font font5 = new Font("MonoSpaced Plain", Font.BOLD, 22);

        //creates a JPanel with a card layout to easily switch between panels
        mainPane.setLayout(cardLayout);

////////////////////////////////////////////////////////////////////////////////
//Load the intro panel with the introductory message and portfolio gif
        this.area = new JTextArea(100, 60);
        area.setPreferredSize(new Dimension(300, 220));
        area.setFont(font5);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setText("\n Welcome to your Investment Portfolio\n\n\n"
                + " Choose a command from the \"Commands\" menu to buy or sell\n"
                + " an investment, update prices for all investments, get gain for\n"
                + " the portfolio, search for relevant investments, or quit the\n"
                + " program.");

        JPanel introBottom = new JPanel(new GridLayout(1, 1));
        JLabel introBottomLabel = new JLabel("If this sentence appears, the gif that was included with the files is in the wrong file location.");
        ImageIcon introBottomPic = new ImageIcon("portfolioPic.gif");
        introBottomLabel.setIcon(introBottomPic);
        introBottom.add(introBottomLabel);
        introPane.add(area);
        introPane.add(introBottom);

        mainPane.add(introPane, "intro");
        cardLayout.show(mainPane, "intro");

////////////////////////////////////////////////////////////////////////////////
//Initialize all text feilds
        JTextField symbolArea = new JTextField(20);
        symbolArea.setFont(font3);
        symbolArea.setEditable(true);

        JTextField nameArea = new JTextField(40);
        nameArea.setFont(font3);
        nameArea.setEditable(true);

        JTextField quantityArea = new JTextField(10);
        quantityArea.setFont(font3);
        quantityArea.setEditable(true);

        JTextField priceArea = new JTextField(10);
        priceArea.setFont(font3);
        priceArea.setEditable(true);

        JTextField totGainArea = new JTextField(20);
        totGainArea.setFont(font3);
        totGainArea.setEditable(false);

        JTextField highPriceArea = new JTextField(20);
        highPriceArea.setFont(font3);
        highPriceArea.setEditable(true);

////////////////////////////////////////////////////////////////////////////////
//creates combo box for buy screen
        String[] comboOptions = new String[]{"Stock", "Mutual Fund"};
        JComboBox<String> buyComboBox = new JComboBox<>(comboOptions);
        buyComboBox.setPreferredSize(new Dimension(200, 40));
        buyComboBox.setFont(font4);

        buyComboBox.setSelectedItem(comboOptions[0]);

////////////////////////////////////////////////////////////////////////////////
//Create all labels for different screens
        JLabel symbolLabel = new JLabel("Symbol");
        symbolLabel.setFont(font2);
        JLabel keywordsLabel = new JLabel("Keyword(s)");
        keywordsLabel.setFont(font2);
        JLabel lowLabel = new JLabel("Low price");
        lowLabel.setFont(font2);
        JLabel highLabel = new JLabel("High price");
        highLabel.setFont(font2);
        JLabel searchInvestmentLabel = new JLabel("Searching investments");
        searchInvestmentLabel.setFont(font2);
        JLabel searchResultsLabel = new JLabel("    Search results");
        searchResultsLabel.setFont(font2);
        JLabel inGainLabel = new JLabel("     Individual gains");
        inGainLabel.setFont(font2);
        JLabel totalGainLabel = new JLabel("Total gain");
        totalGainLabel.setFont(font2);
        JLabel getTotalGainLabel = new JLabel("Getting total gain");
        getTotalGainLabel.setFont(font2);
        JLabel messageLabel = new JLabel("     Messages");
        messageLabel.setFont(font2);
        JLabel priceLabel = new JLabel("Price");
        priceLabel.setFont(font2);
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(font2);
        JLabel updateInvestmentLabel = new JLabel("Updating investments");
        updateInvestmentLabel.setFont(font2);
        JLabel quantityLabel = new JLabel("Quantity");
        quantityLabel.setFont(font2);
        JLabel sellInvestmentLabel = new JLabel("Selling an investment");
        sellInvestmentLabel.setFont(font2);
        JLabel buyInvestmentLabel = new JLabel("Buying an investment");
        buyInvestmentLabel.setFont(font2);
        JLabel typeLabel = new JLabel("Type");
        typeLabel.setFont(font2);

////////////////////////////////////////////////////////////////////////////////
//Create and initilize all button options
        /*When pressed calls the search function with arguments contained in
         *specified areas, return is prints to message area*/
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                returnedString = search(symbolArea.getText(), nameArea.getText(), priceArea.getText(), highPriceArea.getText());
                area.setText(returnedString);
            }
        });

        //Button that clears all text spaces
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                buyComboBox.setSelectedItem(comboOptions[0]);
                symbolArea.setText("");
                nameArea.setText("");
                quantityArea.setText("");
                priceArea.setText("");
                highPriceArea.setText("");
                area.setText("");
            }
        });

        /*Button that calls sell method with specified text area inputs, the 
        return is printed to the message area*/
        JButton sellButton = new JButton("Sell");
        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                returnedString = sellInvestments(symbolArea.getText(), quantityArea.getText(), priceArea.getText());
                area.setText(returnedString);
                symbolArea.setText("");
                quantityArea.setText("");
                priceArea.setText("");
            }
        });

        /*Button that calls buy method with specified text area inputs, the 
        return is printed to the message area*/
        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int type;
                if (buyComboBox.getSelectedItem().equals("Stock")) {
                    type = 1;
                } else {
                    type = 2;
                }
                returnedString = buyInvestments(type, symbolArea.getText(), nameArea.getText(), quantityArea.getText(), priceArea.getText());
                area.setText(returnedString);
            }
        });

        /*Buttons that go forwards or backwards through the list of investments
        displaying them to the correct text areas*/
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Prev");
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                prevButton.setEnabled(true);

                if (currentLocation != 0) {
                    if (currentLocation == 1) {
                        prevButton.setEnabled(false);
                    }
                    currentLocation--;
                    if (currentLocation < investment.size() - 1) {
                        nextButton.setEnabled(true);
                    }
                    symbolArea.setText(investment.get(currentLocation).getSymbol());
                    nameArea.setText(investment.get(currentLocation).getName());
                    priceArea.setText("" + investment.get(currentLocation).getPrice());
                }
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                nextButton.setEnabled(true);

                if (currentLocation != investment.size() - 1) {
                    if (currentLocation == investment.size() - 2) {
                        nextButton.setEnabled(false);
                    }
                    currentLocation++;
                    if (currentLocation > 0) {
                        prevButton.setEnabled(true);
                    }
                    symbolArea.setText(investment.get(currentLocation).getSymbol());
                    nameArea.setText(investment.get(currentLocation).getName());
                    priceArea.setText("" + investment.get(currentLocation).getPrice());
                }
            }
        });

        //Button that saves updated price of investments
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (priceArea.getText().equals("") || !isNum(priceArea.getText())) {
                    priceArea.setText("");
                    area.setText("Invalid price input! Please try gain.");
                } else {
                    price = Double.parseDouble(priceArea.getText());
                    if (price <= 0) {
                        priceArea.setText("");
                        area.setText("Invalid price input! Please try gain.");
                    } else {
                        update(price);
                        symbol = investment.get(currentLocation).getSymbol();
                        name = investment.get(currentLocation).getName();
                        area.setText("The price for investment '" + symbol + "' has been updated to $" + price + ".");
                        priceArea.setText("");
                    }
                }
            }
        });

        //set dimensions of all buttons
        resetButton.setPreferredSize(new Dimension(90, 45));
        buyButton.setPreferredSize(new Dimension(90, 45));
        sellButton.setPreferredSize(new Dimension(90, 45));
        prevButton.setPreferredSize(new Dimension(90, 45));
        nextButton.setPreferredSize(new Dimension(90, 45));
        saveButton.setPreferredSize(new Dimension(90, 45));
        searchButton.setPreferredSize(new Dimension(90, 45));

////////////////////////////////////////////////////////////////////////////////
//Creates Command bar and sets actions for each command option
        JMenuBar menuBar = new JMenuBar();
        JMenu commandMenu = new JMenu("Commands");

        //Sets JPanel for buy screen
        JMenuItem menuBuy = new JMenuItem("Buy Investment");
        menuBuy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                area.setText("");
                area.setFont(font1);

                symbolArea.setText("");
                nameArea.setText("");
                quantityArea.setText("");
                priceArea.setText("");

                JPanel buyPane = new JPanel(new GridBagLayout());
                mainPane.add(buyPane, "1");
                JPanel inputPane = new JPanel(new GridBagLayout());
                JPanel buttonPane = new JPanel(new GridBagLayout());
                JPanel messagePane = new JPanel(new GridBagLayout());
                inputPane.setBackground(Color.PINK);
                messagePane.setBackground(Color.PINK);
                buttonPane.setBackground(Color.PINK);

                symbolArea.setEditable(true);
                nameArea.setEditable(true);

                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                GridBagConstraints gbc = new GridBagConstraints();
                GridBagConstraints gbc2 = new GridBagConstraints();
                GridBagConstraints gbc3 = new GridBagConstraints();

                gbc.ipadx = 700;
                gbc.ipady = 160;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.insets = new Insets(0, 20, 0, 0);
                messagePane.add(scrollPane, gbc);

                gbc.ipadx = 1;
                gbc.ipady = 10;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.anchor = GridBagConstraints.LINE_START;
                messagePane.add(messageLabel, gbc);

                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.gridheight = 1;
                gbc.ipadx = 55;
                gbc.ipady = 330;
                gbc.gridx = 0;
                gbc.gridy = 0;
                buyPane.add(inputPane, gbc);

                gbc.ipadx = 200;
                gbc.ipady = 260;
                gbc.gridx = 1;
                gbc.gridy = 0;
                buyPane.add(buttonPane, gbc);

                gbc.ipadx = 100;
                gbc.ipady = 400;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                buyPane.add(messagePane, gbc);

                gbc3.gridx = 0;
                gbc3.gridy = 0;
                gbc3.insets = new Insets(30, 0, 30, 50);
                gbc3.anchor = GridBagConstraints.CENTER;
                buttonPane.add(resetButton, gbc3);

                gbc3.gridx = 0;
                gbc3.gridy = 1;
                gbc3.insets = new Insets(30, 0, 0, 50);
                buttonPane.add(buyButton, gbc3);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.weightx = 1;
                gbc2.weighty = 1;
                gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc2.insets = new Insets(0, 10, 0, 0);
                inputPane.add(buyInvestmentLabel, gbc2);

                gbc2.insets = new Insets(40, 45, 0, 0);
                gbc2.gridx = 0;
                gbc2.gridy = 0;
                inputPane.add(typeLabel, gbc2);

                gbc2.insets = new Insets(80, 45, 0, 0);
                inputPane.add(symbolLabel, gbc2);

                gbc2.insets = new Insets(120, 45, 0, 0);
                inputPane.add(nameLabel, gbc2);

                gbc2.insets = new Insets(160, 45, 0, 0);
                inputPane.add(quantityLabel, gbc2);

                gbc2.insets = new Insets(200, 45, 0, 0);
                inputPane.add(priceLabel, gbc2);

                gbc2.insets = new Insets(35, 150, 0, 0);
                gbc2.ipadx = 160;
                gbc2.ipady = 1;
                inputPane.add(buyComboBox, gbc2);

                gbc2.weightx = 0;
                gbc2.weighty = 0;
                gbc2.ipadx = 300;
                gbc2.ipady = 10;

                gbc2.insets = new Insets(75, 150, 0, 0);
                inputPane.add(symbolArea, gbc2);

                gbc2.ipadx = 350;
                gbc2.insets = new Insets(115, 150, 0, 0);
                inputPane.add(nameArea, gbc2);

                gbc2.ipadx = 200;
                gbc2.insets = new Insets(155, 150, 0, 0);
                inputPane.add(quantityArea, gbc2);

                gbc2.ipadx = 200;
                gbc2.insets = new Insets(200, 150, 0, 0);
                inputPane.add(priceArea, gbc2);

                cardLayout.show(mainPane, "1");
            }
        });

        //sets JPanel for sell screen
        JMenuItem menuSell = new JMenuItem("Sell Investment");
        menuSell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                sellButton.setEnabled(true);

                symbolArea.setText("");
                quantityArea.setText("");
                priceArea.setText("");

                area.setText("");
                area.setFont(font1);
                JPanel inputPane = new JPanel(new GridBagLayout());
                JPanel buttonPane = new JPanel(new GridBagLayout());
                JPanel messagePane = new JPanel(new GridBagLayout());
                JPanel sellPane = new JPanel(new GridBagLayout());
                mainPane.add(sellPane, "2");

                inputPane.setBackground(Color.MAGENTA);
                messagePane.setBackground(Color.MAGENTA);
                buttonPane.setBackground(Color.MAGENTA);

                symbolArea.setEditable(true);
                nameArea.setEditable(true);

                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                GridBagConstraints gbc = new GridBagConstraints();
                GridBagConstraints gbc2 = new GridBagConstraints();
                GridBagConstraints gbc3 = new GridBagConstraints();

                gbc.ipadx = 700;
                gbc.ipady = 160;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.insets = new Insets(0, 20, 0, 0);
                messagePane.add(scrollPane, gbc);

                gbc.ipadx = 1;
                gbc.ipady = 10;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.anchor = GridBagConstraints.LINE_START;
                messagePane.add(messageLabel, gbc);

                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.gridheight = 1;
                gbc.ipadx = 55;
                gbc.ipady = 330;
                gbc.gridx = 0;
                gbc.gridy = 0;
                sellPane.add(inputPane, gbc);

                gbc.ipadx = 200;
                gbc.ipady = 260;
                gbc.gridx = 1;
                gbc.gridy = 0;
                sellPane.add(buttonPane, gbc);

                gbc.ipadx = 100;
                gbc.ipady = 400;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                sellPane.add(messagePane, gbc);

                gbc3.gridx = 0;
                gbc3.gridy = 0;
                gbc3.insets = new Insets(30, 0, 30, 50);
                gbc3.anchor = GridBagConstraints.CENTER;
                buttonPane.add(resetButton, gbc3);

                gbc3.gridx = 0;
                gbc3.gridy = 1;
                gbc3.insets = new Insets(30, 0, 0, 50);
                buttonPane.add(sellButton, gbc3);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.weightx = 1;
                gbc2.weighty = 1;
                gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc2.insets = new Insets(0, 10, 0, 0);
                inputPane.add(sellInvestmentLabel, gbc2);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.insets = new Insets(60, 45, 0, 0);
                inputPane.add(symbolLabel, gbc2);

                gbc2.insets = new Insets(120, 45, 0, 0);
                inputPane.add(quantityLabel, gbc2);

                gbc2.insets = new Insets(180, 45, 0, 0);
                inputPane.add(priceLabel, gbc2);

                gbc2.ipadx = 160;
                gbc2.ipady = 1;
                gbc2.weightx = 0;
                gbc2.weighty = 0;
                gbc2.ipadx = 300;
                gbc2.ipady = 10;
                gbc2.insets = new Insets(65, 150, 0, 0);
                inputPane.add(symbolArea, gbc2);

                gbc2.ipadx = 350;
                gbc2.ipadx = 200;
                gbc2.insets = new Insets(125, 150, 0, 0);
                inputPane.add(quantityArea, gbc2);

                gbc2.ipadx = 200;
                gbc2.insets = new Insets(185, 150, 0, 0);
                inputPane.add(priceArea, gbc2);

                cardLayout.show(mainPane, "2");

                if (investment.isEmpty()) {
                    area.setText("No investments made at this time.");
                    sellButton.setEnabled(false);
                } else {
                    String output = "";
                    for (int i = 0; i < investment.size(); i++) {
                        output += "symbol = " + investment.get(i).getSymbol() + "\n";
                        output += "quantity = " + investment.get(i).getQuantity() + "\n";
                        output += "price = " + investment.get(i).getPrice() + "\n\n";
                    }
                    area.setText("Current Investments for reference: \n\n" + output);
                }
            }
        });

        //set JPanel for getGain screen
        JMenuItem menuGetGain = new JMenuItem("Get Investment Gains");
        menuGetGain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                area.setText("");
                area.setFont(font1);

                totGainArea.setText("");

                JPanel inputPane = new JPanel(new GridBagLayout());
                JPanel messagePane = new JPanel(new GridBagLayout());
                JPanel updatePane = new JPanel(new GridBagLayout());
                mainPane.add(updatePane, "3");

                inputPane.setBackground(Color.YELLOW);
                messagePane.setBackground(Color.YELLOW);
                updatePane.setBackground(Color.YELLOW);

                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                GridBagConstraints gbc = new GridBagConstraints();
                GridBagConstraints gbc2 = new GridBagConstraints();

                gbc.ipadx = 730;
                gbc.ipady = 270;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.insets = new Insets(0, 20, 0, 0);
                messagePane.add(scrollPane, gbc);

                gbc.ipadx = 1;
                gbc.ipady = 0;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.anchor = GridBagConstraints.LINE_START;
                messagePane.add(inGainLabel, gbc);

                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.gridheight = 1;
                gbc.ipadx = 55;
                gbc.ipady = 280;
                gbc.gridx = 0;
                gbc.gridy = 0;
                updatePane.add(inputPane, gbc);

                gbc.ipadx = 100;
                gbc.ipady = 400;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                updatePane.add(messagePane, gbc);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.weightx = 1;
                gbc2.weighty = 1;
                gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc2.insets = new Insets(0, 10, 0, 0);
                inputPane.add(getTotalGainLabel, gbc2);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.insets = new Insets(60, 45, 0, 0);
                inputPane.add(totalGainLabel, gbc2);

                gbc2.ipadx = 160;
                gbc2.ipady = 1;
                gbc2.weightx = 0;
                gbc2.weighty = 0;
                gbc2.ipadx = 200;
                gbc2.ipady = 10;
                gbc2.insets = new Insets(57, 150, 0, 0);
                inputPane.add(totGainArea, gbc2);

                cardLayout.show(mainPane, "3");

                if (!investment.isEmpty()) {
                    totGainArea.setText(getTotGain());
                    area.setText(getSepGain());
                } else {
                    area.setText("No investments at this time.");
                }
            }
        });

        //set JPanel for update screen
        JMenuItem menuUpdate = new JMenuItem("Update Investments");
        menuUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                area.setText("");
                area.setFont(font1);
                nextButton.setEnabled(true);
                prevButton.setEnabled(false);
                saveButton.setEnabled(true);

                JPanel inputPane = new JPanel(new GridBagLayout());
                JPanel buttonPane = new JPanel(new GridBagLayout());
                JPanel messagePane = new JPanel(new GridBagLayout());
                JPanel updatePane = new JPanel(new GridBagLayout());
                mainPane.add(updatePane, "4");

                inputPane.setBackground(Color.RED);
                messagePane.setBackground(Color.RED);
                buttonPane.setBackground(Color.RED);

                symbolArea.setEditable(false);
                nameArea.setEditable(false);

                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                GridBagConstraints gbc = new GridBagConstraints();
                GridBagConstraints gbc2 = new GridBagConstraints();
                GridBagConstraints gbc3 = new GridBagConstraints();

                gbc.ipadx = 700;
                gbc.ipady = 160;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.insets = new Insets(0, 20, 0, 0);
                messagePane.add(scrollPane, gbc);

                gbc.ipadx = 1;
                gbc.ipady = 10;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.anchor = GridBagConstraints.LINE_START;
                messagePane.add(messageLabel, gbc);

                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.gridheight = 1;
                gbc.ipadx = 55;
                gbc.ipady = 330;
                gbc.gridx = 0;
                gbc.gridy = 0;
                updatePane.add(inputPane, gbc);

                gbc.ipadx = 200;
                gbc.ipady = 260;
                gbc.gridx = 1;
                gbc.gridy = 0;
                updatePane.add(buttonPane, gbc);

                gbc.ipadx = 100;
                gbc.ipady = 400;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                updatePane.add(messagePane, gbc);

                gbc3.gridx = 0;
                gbc3.gridy = 0;
                gbc3.insets = new Insets(30, 0, 15, 50);
                gbc3.anchor = GridBagConstraints.CENTER;
                buttonPane.add(prevButton, gbc3);

                gbc3.gridx = 0;
                gbc3.gridy = 1;
                gbc3.insets = new Insets(10, 0, 15, 50);
                buttonPane.add(nextButton, gbc3);

                gbc3.gridx = 0;
                gbc3.gridy = 2;
                gbc3.insets = new Insets(10, 0, 0, 50);
                buttonPane.add(saveButton, gbc3);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.weightx = 1;
                gbc2.weighty = 1;
                gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc2.insets = new Insets(0, 10, 0, 0);
                inputPane.add(updateInvestmentLabel, gbc2);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.insets = new Insets(60, 45, 0, 0);
                inputPane.add(symbolLabel, gbc2);

                gbc2.insets = new Insets(120, 45, 0, 0);
                inputPane.add(nameLabel, gbc2);

                gbc2.insets = new Insets(180, 45, 0, 0);
                inputPane.add(priceLabel, gbc2);

                gbc2.ipadx = 160;
                gbc2.ipady = 1;
                gbc2.weightx = 0;
                gbc2.weighty = 0;
                gbc2.ipadx = 300;
                gbc2.ipady = 10;
                gbc2.insets = new Insets(60, 150, 0, 0);
                inputPane.add(symbolArea, gbc2);

                gbc2.ipadx = 350;
                gbc2.insets = new Insets(120, 150, 0, 0);
                inputPane.add(nameArea, gbc2);

                gbc2.ipadx = 200;
                gbc2.insets = new Insets(180, 150, 0, 0);
                inputPane.add(priceArea, gbc2);

                cardLayout.show(mainPane, "4");

                currentLocation = 0;
                if (investment.isEmpty()) {
                    area.setText("No investments to update at this time.");
                    priceArea.setEditable(false);
                    nextButton.setEnabled(false);
                    prevButton.setEnabled(false);
                    saveButton.setEnabled(false);
                } else {
                    if (investment.size() == 1) {
                        nextButton.setEnabled(false);
                    }
                    symbolArea.setText(investment.get(0).getSymbol());
                    nameArea.setText(investment.get(0).getName());
                    priceArea.setText(investment.get(0).getPrice() + "");
                }
            }
        });

        //set JPanel for search screen
        JMenuItem menuSearch = new JMenuItem("Search for Investments");
        menuSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                area.setText("");
                area.setFont(font1);
                searchButton.setEnabled(true);

                symbolArea.setText("");
                nameArea.setText("");
                priceArea.setText("");
                highPriceArea.setText("");

                JPanel searchPane = new JPanel(new GridBagLayout());
                mainPane.add(searchPane, "5");
                JPanel inputPane = new JPanel(new GridBagLayout());
                JPanel buttonPane = new JPanel(new GridBagLayout());
                JPanel messagePane = new JPanel(new GridBagLayout());

                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                symbolArea.setEditable(true);
                nameArea.setEditable(true);
                priceArea.setEditable(true);

                GridBagConstraints gbc = new GridBagConstraints();
                GridBagConstraints gbc2 = new GridBagConstraints();
                GridBagConstraints gbc3 = new GridBagConstraints();

                gbc.ipadx = 700;
                gbc.ipady = 160;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.insets = new Insets(0, 20, 0, 0);
                messagePane.add(scrollPane, gbc);

                gbc.ipadx = 1;
                gbc.ipady = 10;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.anchor = GridBagConstraints.LINE_START;
                messagePane.add(searchResultsLabel, gbc);

                gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc.gridheight = 1;
                gbc.ipadx = 55;
                gbc.ipady = 330;
                gbc.gridx = 0;
                gbc.gridy = 0;
                searchPane.add(inputPane, gbc);

                gbc.ipadx = 200;
                gbc.ipady = 260;
                gbc.gridx = 1;
                gbc.gridy = 0;
                searchPane.add(buttonPane, gbc);

                gbc.ipadx = 100;
                gbc.ipady = 400;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                searchPane.add(messagePane, gbc);

                gbc3.gridx = 0;
                gbc3.gridy = 0;
                gbc3.insets = new Insets(30, 0, 30, 50);
                gbc3.anchor = GridBagConstraints.CENTER;
                buttonPane.add(resetButton, gbc3);

                gbc3.gridx = 0;
                gbc3.gridy = 1;
                gbc3.insets = new Insets(30, 0, 0, 50);
                buttonPane.add(searchButton, gbc3);

                gbc2.gridx = 0;
                gbc2.gridy = 0;
                gbc2.weightx = 1;
                gbc2.weighty = 1;
                gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
                gbc2.insets = new Insets(0, 10, 0, 0);
                inputPane.add(searchInvestmentLabel, gbc2);

                gbc2.insets = new Insets(50, 45, 0, 0);
                inputPane.add(symbolLabel, gbc2);

                gbc2.insets = new Insets(95, 45, 0, 0);
                inputPane.add(nameLabel, gbc2);

                gbc2.insets = new Insets(115, 45, 0, 0);
                inputPane.add(keywordsLabel, gbc2);

                gbc2.insets = new Insets(155, 45, 0, 0);
                inputPane.add(lowLabel, gbc2);

                gbc2.insets = new Insets(195, 45, 0, 0);
                inputPane.add(highLabel, gbc2);

                gbc2.weightx = 0;
                gbc2.weighty = 0;
                gbc2.ipadx = 300;
                gbc2.ipady = 10;
                gbc2.insets = new Insets(50, 150, 0, 0);
                inputPane.add(symbolArea, gbc2);

                gbc2.ipadx = 350;
                gbc2.insets = new Insets(100, 150, 0, 0);
                inputPane.add(nameArea, gbc2);

                gbc2.ipadx = 200;
                gbc2.insets = new Insets(153, 150, 0, 0);
                inputPane.add(priceArea, gbc2);

                gbc2.ipadx = 200;
                gbc2.insets = new Insets(193, 150, 0, 0);
                inputPane.add(highPriceArea, gbc2);

                cardLayout.show(mainPane, "5");

                if (investment.isEmpty()) {
                    searchButton.setEnabled(false);
                    area.setText("No investments to search at this time.");
                }
            }
        });

        //Sets exit protocol for quit menu option
        JMenuItem menuQuit = new JMenuItem("Quit");
        menuQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String ObjButtons[] = {"Yes", "No"};
                int promptResult = JOptionPane.showOptionDialog(menuQuit, "Are you sure you want to Quit?", "Quit conformation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons, ObjButtons[1]);
                if (promptResult == 0) {
                    writeInvestmentsToFile();
                    System.exit(0);
                }
            }
        });

        //Initialize size and font of all command menu options
        menuBuy.setPreferredSize(new Dimension(783, 30));
        menuBuy.setFont(font2);
        commandMenu.add(menuBuy);

        menuSell.setPreferredSize(new Dimension(783, 30));
        menuSell.setFont(font2);
        commandMenu.add(menuSell);

        menuGetGain.setPreferredSize(new Dimension(783, 30));
        menuGetGain.setFont(font2);
        commandMenu.add(menuGetGain);

        menuUpdate.setPreferredSize(new Dimension(783, 30));
        menuUpdate.setFont(font2);
        commandMenu.add(menuUpdate);

        menuSearch.setPreferredSize(new Dimension(783, 30));
        menuSearch.setFont(font2);
        commandMenu.add(menuSearch);

        menuQuit.setPreferredSize(new Dimension(783, 30));
        menuQuit.setFont(font2);
        commandMenu.add(menuQuit);

        commandMenu.setFont(font3);
        commandMenu.setPreferredSize(new Dimension(801, 30));

        menuBar.add(commandMenu);
        menuBar.setBackground(Color.GRAY);
        menuBar.setPreferredSize(new Dimension(801, 30));

////////////////////////////////////////////////////////////////////////////////        
//Set up frame starting position        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setJMenuBar(menuBar);
        frame.add(mainPane);
        frame.setVisible(true);
    }

    /**
     * main method to run program and create object of portfolio
     *
     * @param args takes in command line arguments if needed (not needed in this
     * program)
     */
    public static void main(String[] args) { //0 is arg 1
        String returnFileName;

        Portfolio investmentPortfolio = new Portfolio();
        if (args.length == 0) {//if a command line argument was not given sends an empty file name
            fileName = "";
            try {
                investmentPortfolio.readInvestmentsFromFile();
            } catch (Investments.symbolException ex) {
                ex.getMessage();
            } catch (Investments.nameException ex) {
                ex.getMessage();
            } catch (Investments.quantityException ex) {
                ex.getMessage();
            } catch (Investments.priceException ex) {
                ex.getMessage();
            } catch (Investments.bookValueException ex) {
                ex.getMessage();
            }
        } else {//sends command line argument
            fileName = args[0];
            try {
                investmentPortfolio.readInvestmentsFromFile();
            } catch (Investments.symbolException ex) {
                ex.getMessage();
            } catch (Investments.nameException ex) {
                ex.getMessage();
            } catch (Investments.quantityException ex) {
                ex.getMessage();
            } catch (Investments.priceException ex) {
                ex.getMessage();
            } catch (Investments.bookValueException ex) {
                ex.getMessage();
            }
        }
    }
}
