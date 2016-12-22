package stats.trueskill;

import static tools.maths.MathUtils.square;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


/**
 * @author Amaury Combes
 * source : https://github.com/nsp/JSkills/blob/master/src/main/java/jskills/numerics/GaussianDistribution.java
 */
public class GaussianDistribution {

    /**
     * The Gaussian representation of a flat line.
     **/
    public static final GaussianDistribution UNIFORM = fromPrecisionMean(0, 0);

    /** The peak of the Gaussian, μ **/
    private final double mean;

    /** The width of the Gaussian, σ, where the height drops to max/e **/
    private final double standardDeviation;

    /** The square of the standard deviation, σ^2 **/
    private final double variance;

    // Precision and PrecisionMean are used because they make multiplying and
    // dividing simpler (see the accompanying math paper for more details)

    /** 1/σ^2 **/
    private final double precision;

    /** Precision times mean, μ/σ^2 **/
    private final double precisionMean;

    /**
     * Private constructor that sets everything at once.
     * <p>
     * Only allow other constructors to use this because if the public were to
     * mess up the relationship between the parameters, who knows what would
     * happen?
     */
    private GaussianDistribution(double mean, double standardDeviation,
                                 double variance, double precision, double precisionMean) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.variance = variance;
        this.precision = precision;
        this.precisionMean = precisionMean;
    }

    public static GaussianDistribution fromPrecisionMean(double precisionMean,
                                                         double precision) {
        return new GaussianDistribution(precisionMean / precision,
                sqrt(1. / precision),
                1. / precision,
                precision,
                precisionMean);
    }

    /**
     * <pre>
     *               1          -(x)^2 / (2*stdDev^2)
     *   P(x) = ------------ * e
     *           sqrt(2*pi)
     * </pre>
     *
     * @param x
     *            the location to evaluate a normalized Gaussian at
     * @return the value at x of a normalized Gaussian centered at 0 of unit
     *         width.
     */
    public static double at(double x) { return at(x, 0, 1); }

    /**
     * <pre>
     *               1          -(x)^2 / (2*stdDev^2)
     *   P(x) = ------------ * e
     *           sqrt(2*pi)
     * </pre>
     *
     * @param x
     *            the location to evaluate a normalized Gaussian at
     * @return the value at x of a normalized Gaussian centered at 0 of unit
     *         width.
     */
    public static double at(double x, double mean, double standardDeviation) {
        double multiplier = 1.0/(standardDeviation*sqrt(2*PI));
        double expPart = exp((-1.0*pow(x - mean, 2.0))/(2*(standardDeviation*standardDeviation)));
        double result = multiplier*expPart;
        return result;
    }

    public static double cumulativeTo(double x, double mean, double standardDeviation) {
        double invsqrt2 = -0.707106781186547524400844362104;
        double result = errorFunctionCumulativeTo(invsqrt2*x);
        return 0.5*result;
    }

    public static double cumulativeTo(double x) {
        return cumulativeTo(x, 0, 1);
    }

    private static double errorFunctionCumulativeTo(double x) {
        // Derived from page 265 of Numerical Recipes 3rd Edition
        double z = abs(x);

        double t = 2.0/(2.0 + z);
        double ty = 4*t - 2;

        double[] coefficients = { -1.3026537197817094, 6.4196979235649026e-1,
                1.9476473204185836e-2, -9.561514786808631e-3,
                -9.46595344482036e-4, 3.66839497852761e-4, 4.2523324806907e-5,
                -2.0278578112534e-5, -1.624290004647e-6, 1.303655835580e-6,
                1.5626441722e-8, -8.5238095915e-8, 6.529054439e-9,
                5.059343495e-9, -9.91364156e-10, -2.27365122e-10,
                9.6467911e-11, 2.394038e-12, -6.886027e-12, 8.94487e-13,
                3.13092e-13, -1.12708e-13, 3.81e-16, 7.106e-15, -1.523e-15,
                -9.4e-17, 1.21e-16, -2.8e-17 };

        int ncof = coefficients.length;
        double d = 0.0;
        double dd = 0.0;

        for (int j = ncof - 1; j > 0; j--) {
            double tmp = d;
            d = ty * d - dd + coefficients[j];
            dd = tmp;
        }

        double ans = t*exp(-z*z + 0.5*(coefficients[0] + ty*d) - dd);
        return x >= 0.0 ? ans : (2.0 - ans);
    }

    private static double InverseErrorFunctionCumulativeTo(double p) {
        // From page 265 of numerical recipes

        if (p >= 2.0) return -100;
        if (p <= 0.0) return 100;

        double pp = (p < 1.0) ? p : 2 - p;
        double t = sqrt(-2*log(pp/2.0)); // Initial guess
        double x = -0.70711*((2.30753 + t*0.27061)/(1.0 + t*(0.99229 + t*0.04481)) - t);

        for (int j = 0; j < 2; j++) {
            double err = errorFunctionCumulativeTo(x) - pp;
            x += err / (1.12837916709551257 * exp(-(x * x)) - x * err); // Halley
        }

        return p < 1.0 ? x : -x;
    }

    public static double inverseCumulativeTo(double x, double mean, double standardDeviation) {
        // From numerical recipes, page 320
        return mean - sqrt(2)*standardDeviation*InverseErrorFunctionCumulativeTo(2*x);
    }

    public static double inverseCumulativeTo(double x) {
        return inverseCumulativeTo(x, 0, 1);
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getVariance() {
        return variance;
    }

    public double getPrecision() {
        return precision;
    }

    public double getPrecisionMean() {
        return precisionMean;
    }
}
