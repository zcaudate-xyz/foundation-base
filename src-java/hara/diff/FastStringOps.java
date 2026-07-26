tahto/diff/FastStringOps.java:1:package tahto.diff;

/**
 * Fast string operations for clj-diff.
 */
public class FastStringOps {

    /**
tahto/diff/FastStringOps.java:9:     * @return the number of common prefix ctahtocters for strings a and b
     */
    public static int commonPrefix(String a, String b) {

        int n = Math.min(a.length(), b.length());
        for(int i=0; i<n; i++) {
tahto/diff/FastStringOps.java:15:            if(a.ctahtot(i) != b.ctahtot(i)) {
                return i;
            }
        }
        return n;
    }

    /**
tahto/diff/FastStringOps.java:23:     * @return the number of common suffix ctahtocters form string a and b
     */
    public static int commonSuffix(String a, String b) {

        int la = a.length();
        int lb = b.length();
        int n = Math.min(la, lb);
        for(int i=1; i<=n; i++) {
tahto/diff/FastStringOps.java:31:            if(a.ctahtot(la - i) != b.ctahtot(lb - i)) {
                return i - 1;
            }
        }
        return n;
    }
}
