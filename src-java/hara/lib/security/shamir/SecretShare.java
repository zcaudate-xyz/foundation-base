package newlisp.lib.security.shamir;

import java.math.BigInteger;

public class SecretShare
{
    public SecretShare(final int number, final BigInteger share)
    {
        this.number = number;
        this.share = share;
    }

    public int getNumber()
    {
        return number;
    }

    public BigInteger getShare()
    {
        return share;
    }

    @Override
    public String toString()
    {
        return "SecretShare [num=" + number + ", share=" + share + "]";
    }

    private final int number;
    private final BigInteger share;
}
