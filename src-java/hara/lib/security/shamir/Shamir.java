package hara.lib.security.shamir;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public final class Shamir
{
    public static SecretShare[] split(final BigInteger secret, int needed, int available, BigInteger prime, Random random)
    {
        // System.out.println("Prime Number: " + prime);

        final BigInteger[] coeff = new BigInteger[needed];
        coeff[0] = secret;
        for (int i = 1; i < needed; i++)
        {
            BigInteger r;
            while (true)
            {
                r = new BigInteger(prime.bitLength(), random);
                if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(prime) < 0)
                {
                    break;
                }
            }
            coeff[i] = r;
        }

        final SecretShare[] shares = new SecretShare[available];
        for (int x = 1; x <= available; x++)
        {
            BigInteger accum = secret;

            for (int exp = 1; exp < needed; exp++)
            {
                accum = accum.add(coeff[exp].multiply(BigInteger.valueOf(x).pow(exp).mod(prime))).mod(prime);
            }
            shares[x - 1] = new SecretShare(x, accum);
            // System.out.println("Share " + shares[x - 1]);
        }

        return shares;
    }

    public static BigInteger combine(final SecretShare[] shares, final BigInteger prime)
    {
        BigInteger accum = BigInteger.ZERO;

        for(int formula = 0; formula < shares.length; formula++)
        {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for(int count = 0; count < shares.length; count++)
            {
                if(formula == count)
                    continue; // If not the same value

                int startposition = shares[formula].getNumber();
                int nextposition = shares[count].getNumber();

                numerator = numerator.multiply(BigInteger.valueOf(nextposition).negate()).mod(prime); // (numerator * -nextposition) % prime;
                denominator = denominator.multiply(BigInteger.valueOf(startposition - nextposition)).mod(prime); // (denominator * (startposition - nextposition)) % prime;
            }
            BigInteger value = shares[formula].getShare();
            BigInteger tmp = value.multiply(numerator) . multiply(modInverse(denominator, prime));
            accum = prime.add(accum).add(tmp) . mod(prime); //  (prime + accum + (value * numerator * modInverse(denominator))) % prime;
        }

        // System.out.println("The secret is: " + accum + "\n");

        return accum;
    }

    private static BigInteger[] gcdD(BigInteger a, BigInteger b)
    { 
        if (b.compareTo(BigInteger.ZERO) == 0)
            return new BigInteger[] {a, BigInteger.ONE, BigInteger.ZERO}; 
        else
        { 
            BigInteger n = a.divide(b);
            BigInteger c = a.mod(b);
            BigInteger[] r = gcdD(b, c); 
            return new BigInteger[] {r[0], r[2], r[1].subtract(r[2].multiply(n))};
        }
    }

    private static BigInteger modInverse(BigInteger k, BigInteger prime)
    { 
        k = k.mod(prime);
        BigInteger r = (k.compareTo(BigInteger.ZERO) == -1) ? (gcdD(prime, k.negate())[2]).negate() : gcdD(prime,k)[2];
        return prime.add(r).mod(prime);
    }

    public static void main(final String[] args)
    {
        final int CERTAINTY = 256;
        final SecureRandom random = new SecureRandom();

        final BigInteger secret = new BigInteger("123");

        // prime number must be longer then secret number
        final BigInteger prime = new BigInteger(secret.bitLength() + 1, CERTAINTY, random);

        // 2 - at least 2 secret parts are needed to view secret
        // 5 - there are 5 persons that get secret parts
        final SecretShare[] shares = Shamir.split(secret, 2, 5, prime, random);


        // we can use any combination of 2 or more parts of secret
        SecretShare[] sharesToViewSecret = new SecretShare[] {shares[0],shares[1]}; // 0 & 1
        BigInteger result = Shamir.combine(sharesToViewSecret, prime);

        sharesToViewSecret = new SecretShare[] {shares[1],shares[4]}; // 1 & 4
        result = Shamir.combine(sharesToViewSecret, prime);

        sharesToViewSecret = new SecretShare[] {shares[0],shares[1],shares[3]}; // 0 & 1 & 3
        result = Shamir.combine(sharesToViewSecret, prime);
    }
}
