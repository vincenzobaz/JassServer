package stats.trueskill;


import static stats.trueskill.GaussianDistribution.at;
import static stats.trueskill.GaussianDistribution.cumulativeTo;

/**
 * @author Amaury Combes
 * source : https://github.com/nsp/JSkills/blob/master/src/main/java/jskills/trueskill/TruncatedGaussianCorrectionFunctions.java
 */
public class TruncatedGaussianCorrectionFunctions {

    private TruncatedGaussianCorrectionFunctions() {}

    /**
     * The "V" function where the team performance difference is greater than the draw margin.
     * <remarks>In the reference F# implementation, this is referred to as "the additive
     * correction of a single-sided truncated Gaussian with unit variance."</remarks>
     *
     * @param teamPerformanceDifference
     * @param drawMargin In the paper, it's referred to as just "ε".
     * @returns
     */
    public static double vExceedsMargin(double teamPerformanceDifference, double drawMargin, double c) {
        return vExceedsMargin(teamPerformanceDifference / c, drawMargin / c);
    }

    public static double vExceedsMargin(double teamPerformanceDifference, double drawMargin) {
        double denominator = cumulativeTo(teamPerformanceDifference - drawMargin);

        if (denominator < 2.222758749e-162)
            return -teamPerformanceDifference + drawMargin;

        return at(teamPerformanceDifference - drawMargin)/denominator;
    }

    /**
     * The "W" function where the team performance difference is greater than the draw margin.
     * <remarks>In the reference F# implementation, this is referred to as "the multiplicative
     * correction of a single-sided truncated Gaussian with unit variance."</remarks>
     *
     * @param teamPerformanceDifference
     * @param drawMargin
     * @param c
     * @returns
     */
    public static double wExceedsMargin(double teamPerformanceDifference, double drawMargin, double c) {
        return wExceedsMargin(teamPerformanceDifference / c, drawMargin / c);
        //var vWin = vExceedsMargin(teamPerformanceDifference, drawMargin, c);
        //return vWin * (vWin + (teamPerformanceDifference - drawMargin) / c);
    }

    public static double wExceedsMargin(double teamPerformanceDifference, double drawMargin) {
        double denominator = cumulativeTo(teamPerformanceDifference - drawMargin);

        if (denominator < 2.222758749e-162) {
            if (teamPerformanceDifference < 0.0)
                return 1.0;
            else
                return 0.0;
        }

        double vWin = vExceedsMargin(teamPerformanceDifference, drawMargin);
        return vWin*(vWin + teamPerformanceDifference - drawMargin);
    }

    // from F#:
    public static double vWithinMargin(double teamPerformanceDifference, double drawMargin) {
        double teamPerformanceDifferenceAbsoluteValue = Math.abs(teamPerformanceDifference);
        double denominator = cumulativeTo(drawMargin - teamPerformanceDifferenceAbsoluteValue) -
                cumulativeTo(-drawMargin - teamPerformanceDifferenceAbsoluteValue);

        if (denominator < 2.222758749e-162) {
            if (teamPerformanceDifference < 0.0) {
                return -teamPerformanceDifference - drawMargin;
            }

            return -teamPerformanceDifference + drawMargin;
        }

        double numerator = at(-drawMargin - teamPerformanceDifferenceAbsoluteValue) -
                at(drawMargin - teamPerformanceDifferenceAbsoluteValue);

        if (teamPerformanceDifference < 0.0) {
            return -numerator/denominator;
        }

        return numerator/denominator;
    }

    // From F#:
    public static double wWithinMargin(double teamPerformanceDifference, double drawMargin) {
        double teamPerformanceDifferenceAbsoluteValue = Math.abs(teamPerformanceDifference);
        double denominator = cumulativeTo(drawMargin - teamPerformanceDifferenceAbsoluteValue) -
                cumulativeTo(-drawMargin - teamPerformanceDifferenceAbsoluteValue);

        if (denominator < 2.222758749e-162) {
            return 1.0;
        }

        double vt = vWithinMargin(teamPerformanceDifferenceAbsoluteValue, drawMargin);

        return vt*vt +
                (
                        (drawMargin - teamPerformanceDifferenceAbsoluteValue)
                                *
                                at(
                                        drawMargin - teamPerformanceDifferenceAbsoluteValue)
                                - (-drawMargin - teamPerformanceDifferenceAbsoluteValue)
                                *
                                at(-drawMargin - teamPerformanceDifferenceAbsoluteValue))/denominator;
    }
}