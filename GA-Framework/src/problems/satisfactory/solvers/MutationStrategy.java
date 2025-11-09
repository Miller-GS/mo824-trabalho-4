package problems.satisfactory.solvers;

import java.util.Random;

public class MutationStrategy
{
    private static final Random rng = new Random();
    private double sigma = 0.1; // gaussian's standard deviation

    public enum Type
    {
        GAUSSIAN,
        INVERSION,
        GAUSSIAN_INVERSION
    }

    public double mutateValue(double gene, Type type)
    {
        switch (type)
        {
            case GAUSSIAN ->
            {
                gene += rng.nextGaussian() * sigma;
                gene = Math.max(0.0, Math.min(1.0, gene));
            }
            case INVERSION -> gene = 1.0 - gene;
            case GAUSSIAN_INVERSION ->
            {
                if (rng.nextBoolean()) {
                    gene += rng.nextGaussian() * sigma;
                    gene = Math.max(0.0, Math.min(1.0, gene));
                } else {
                    gene = 1.0 - gene;
                }
            }
        }

        return gene;
    }

    public void setSigma(double sigma)
    {
        this.sigma = sigma;
    }
}
