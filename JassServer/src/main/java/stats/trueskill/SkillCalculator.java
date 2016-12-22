package stats.trueskill;


import java.util.ArrayList;
import java.util.List;

import tools.maths.MathUtils;

import static stats.trueskill.SkillCalculator.OutCome.OTHERTEAM;
import static stats.trueskill.SkillCalculator.OutCome.USERTEAM;

/**
 * @author Amaury Combes
 */
public class SkillCalculator {

    public enum OutCome {
        USERTEAM, OTHERTEAM
    }

    public SkillCalculator() {

    }

    public static Rank calculateNewRatings(GameInfo gameInfo, List<Rank> players, int winner) {
        List<Rank> team1 = new ArrayList<>();
        team1.add(players.get(0));
        team1.add(players.get(1));

        List<Rank> team2 = new ArrayList<>();
        team2.add(players.get(2));
        team2.add(players.get(3));


        OutCome whoWon = (winner == 0) ? USERTEAM : OTHERTEAM;

        return updateRating(gameInfo, team1, team2, whoWon);
    }

    private static Rank updateRating(GameInfo gameInfo, List<Rank> userTeam, List<Rank> otherTeam, OutCome winner) {
        double drawMargin = DrawMargin.GetDrawMarginFromDrawProbability(gameInfo.getDrawProbability(), gameInfo.getBeta());
        double betaSquared = MathUtils.square(gameInfo.getBeta());
        double tauSquared = MathUtils.square(gameInfo.getDynamicsFactor());

        int totalPlayers = userTeam.size() + otherTeam.size();

        double userMeanSum = 0;
        for (Rank r : userTeam) {
            userMeanSum += r.getMean();
        }
        double otherTeamMeanSum = 0;
        for (Rank r : otherTeam) {
            otherTeamMeanSum += r.getMean();
        }

        double totalSumStandardDeviation = 0;
        for (Rank r : userTeam) {
            totalSumStandardDeviation += MathUtils.square(r.getStandardDeviation());
        }
        for (Rank r : otherTeam) {
            totalSumStandardDeviation += MathUtils.square(r.getStandardDeviation());
        }

        double c = Math.sqrt(totalSumStandardDeviation + totalPlayers*betaSquared);

        double winningMean = winner == OTHERTEAM ? otherTeamMeanSum : userMeanSum;
        double losingMean = winner == OTHERTEAM ? userMeanSum : otherTeamMeanSum;

        double meanDelta = winningMean - losingMean;

        double v;
        double w;
        double rankMultiplier;

        v = TruncatedGaussianCorrectionFunctions.vExceedsMargin(meanDelta, drawMargin, c);
        w = TruncatedGaussianCorrectionFunctions.wExceedsMargin(meanDelta, drawMargin, c);

        rankMultiplier = (winner == USERTEAM) ?  1 : -1;

        Rank userRank = userTeam.get(0);

        double meanMultiplier = (MathUtils.square(userRank.getStandardDeviation()) + tauSquared)/c;
        double stdDevMultiplier = (MathUtils.square(userRank.getStandardDeviation()) + tauSquared)/MathUtils.square(c);

        double userMeanDelta = (rankMultiplier*meanMultiplier*v);
        double newMean = userRank.getMean() + userMeanDelta;

        double newStdDev =
                Math.sqrt((MathUtils.square(userRank.getStandardDeviation()) + tauSquared)*(1 - w*stdDevMultiplier));

        return new Rank(newMean, newStdDev);
    }

}
