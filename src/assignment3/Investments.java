package assignment3;

/**
 * @author Gordon Restivo
 */
public class Investments {

    private String symbol;
    private String name;
    private int quantity;
    private double price;
    private double bookValue;

    /**
     * Non-default constructor. Requires all three instance fields to be
     * provided as input.
     *
     * @param symbol The title of a stock.
     * @param name The name of who the stock was purchased from.
     * @param quantity How many (the quantity of the) stocks you wish to
     * purchase.
     * @param price The price of one stock.
     * @param bookValue Current value of chosen stock.
     *
     */
    public Investments(String symbol, String name, String quantity, String price, double bookValue)
            throws symbolException, nameException, quantityException, priceException, bookValueException {
        if (symbol == null || symbol.equals("")) {
            throw new symbolException("Symbol cannot be an empty string.");
        } else {
            this.symbol = symbol;
        }
        if (name == null || name.equals("")) {
            throw new nameException("Name cannot be an empty value.");
        } else {
            this.name = name;
        }
        if (quantity == null || quantity.equals("") || !isNum(quantity)) {
            throw new quantityException("Quantity cannot be an empty value or <= 0.");
        } else {
            int numQuantity = Integer.parseInt(quantity);
            if (numQuantity > 0) {
                this.quantity = numQuantity;
            } else {
                throw new quantityException("Quantity cannot be an empty value or <= 0.");
            }
        }
        if (price == null || price.equals("") || !isNum(price)) {
            throw new priceException("Price cannot be an empty value or <= 0.");
        } else {
            double numPrice = Double.parseDouble(price);
            if (numPrice > 0) {
                this.price = numPrice;
            } else {
                throw new priceException("Price cannot be an empty value or <= 0.");
            }
        }
        if (this.bookValue < 0) {
            throw new bookValueException("Problem with BookValue");
        } else {
            this.bookValue = bookValue;
        }
    }

    /**
     * Default constructor. Assigns String fields the value of the empty string,
     * and integer fields the value 0.
     */
    public Investments() {
        this.symbol = "";
        this.name = "";
        this.quantity = 0;
        this.price = 0;
        this.bookValue = 0;
    }

    /**
     * Compares the fields of a Stock instance o to the fields of this instance.
     *
     * @param o The Object instance to be compared with this instance.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Investments)) {
            return false;
        } else {
            Investments d = (Investments) o;
            if (!(this.getSymbol().equals(d.getSymbol()))) {
                return false;
            }
            if (!(this.getName().equals(d.getName()))) {
                return false;
            }
            if (!(this.getQuantity() == (d.getQuantity()))) {
                return false;
            }
            if (!(this.getBookValue() == d.getBookValue())) {
                return false;
            }
            if (!(this.getPrice() == d.getPrice())) {
                return false;
            }
            return true;
        }
    }

    /**
     * Represents instance fields as members of a tuple (symbol, name, quantity,
     * price, bookValue).
     *
     * @return A string containing the instance fields.
     */
    @Override
    public String toString() {
        return "Returned investment = (" + this.symbol + ", " + this.name + ", " + this.quantity + ", " + this.price + ", " + this.bookValue + ")";
    }

    /**
     * mutator method to set symbol
     *
     * @param symbol symbol of company buying from
     */
    public void setSymbol(String symbol) throws symbolException {
        if (symbol == null || symbol.equals("")) {
            throw new symbolException("Symbol cannot be an empty string");
        } else {
            this.symbol = symbol;
        }
    }

    /**
     * mutator method to set name
     *
     * @param name name of company buying from
     */
    public void setName(String name) throws nameException {
        if (name == null || name.equals("")) {
            throw new nameException("Name cannot be an empty string");
        } else {
            this.symbol = symbol;
        }
        this.name = name;
    }

    /**
     * mutator method to set quantity
     *
     * @param quantity quantity of stock to purchase
     */
    public void setQuantity(int quantity) throws quantityException {
        if (quantity <= 0) {
            throw new quantityException("Quantity cannot be less than or equals to 0");
        } else {
            this.quantity = quantity;
        }
    }

    /**
     * mutator method to set price
     *
     * @param price price of one stock attribute
     */
    public void setPrice(double price) throws priceException {
        if (price <= 0) {
            throw new priceException("Price cannot be less than or equal to 0");
        } else {
            this.price = price;
        }
    }

    /**
     * mutator method to set bookValue
     *
     * @param bookValue value of investment
     */
    public void setBookValue(double bookValue) {
        this.bookValue = bookValue;
    }

    /**
     * @return The stock instances title.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return The stock instances name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The stock instances quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return The Stock instances book value .
     */
    public double getBookValue() {
        return bookValue;
    }

    /**
     * @return The stock instances price
     */
    public double getPrice() {
        return price;
    }

    public class symbolException extends Exception {
        public symbolException(String string) {
            super(string);
        }
    }

    public class nameException extends Exception {
        public nameException(String string) {
            super(string);
        }
    }

    public class quantityException extends Exception {
        public quantityException(String string) {
            super(string);
        }
    }

    public class priceException extends Exception {
        public priceException(String string) {
            super(string);
        }
    }

    public class bookValueException extends Exception {
        public bookValueException(String string) {
            super(string);
        }
    }

    public static boolean isNum(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
