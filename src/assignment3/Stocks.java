package assignment3;

/**
 * @author Gordon Restivo
 */
public class Stocks extends Investments {

    public Stocks() throws Investments.symbolException, Investments.nameException, Investments.quantityException,
            Investments.priceException, Investments.bookValueException {
        super("", "", "", "", 0.0);
    }

    public Stocks(String symbol, String name, String quantity, String price, double bookValue)
            throws Investments.symbolException, Investments.nameException, Investments.quantityException,
            Investments.priceException, Investments.bookValueException {
            super(symbol, name, quantity, price, bookValue);
        
    }

    // ~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~_~
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o
    ) {
        if (o == this) {
            return true;
        }
        if (!(this.getClass().equals(o.getClass()))) {
            return false;
        }

        Stocks cd = (Stocks) o;
        //return this.getNumProblems() == (cd.getNumProblems()) && super.equals(o);  
        return super.equals(o);

    }
}
