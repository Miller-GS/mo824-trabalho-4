package problems.satisfactory.solvers;

import java.io.IOException;
import metaheuristics.ga.AbstractGA;
import problems.satisfactory.Satisfactory;
import solutions.Solution;

public class GA_Satisfactory extends AbstractGA<Double, Double>
{
    private Satisfactory satisfactoryProblem;

    public GA_Satisfactory(int gens, int popSize, double mutRate, String filename, long timeout) throws IOException
    {
        super(new Satisfactory(filename), gens, popSize, mutRate, timeout);
    }

    @Override
    public Solution<Double> createEmptySol()
    {
        return new Solution<>();
    }

    @Override
    protected Solution<Double> decode(Chromosome chromosome)
    {
        Solution<Double> sol = new Solution<>();
        sol.addAll(chromosome);
        sol.cost = satisfactoryProblem.evaluate(sol);
        return sol;
    }

    @Override
    protected Chromosome generateRandomChromosome()
    {
        Chromosome c = new Chromosome();
        for (int i = 0; i < chromosomeSize; i++)
            c.add(rng.nextDouble()); // [0, 1]
        return c;
    }

    @Override
    protected void mutateGene(Chromosome chromosome, Integer locus)
    {
        double val = chromosome.get(locus);
        double mutated = val + rng.nextGaussian() * 0.1;
        chromosome.set(locus, Math.max(0.0, Math.min(1.0, mutated)));
    }

    @Override
    protected Double fitness(Chromosome chromosome)
    {
        // Incorporating the suggestion we've been given, we have the formulas below:
        // f(x, g) = C(x) + (K(g) * P(x));
        // K(g) = K0 * (1 + α * (g / G))
        // where C(x) is the cost function, P(x) is the "unfeasibility rate"
        // and K0 is an initial value for the penalty constant, g is the current generation, G is the max number of generations and α is a scaling factor

        Solution<Double> sol = decode(chromosome);
        double cost = satisfactoryProblem.computeTotalCost(sol);
        double penalty = satisfactoryProblem.computePenalty(sol);

        // Control dynamic penalty
        double basePenalty = 3.0;      // K0, may be adjusted if needed
        // Considerations about basePenalty: considering the scale of solutions cost (that represents number of machines utilized, which may be 10 - 100), this may be small
        // If needed, we can reconsider this value and increase it to something like 5.0 or 10.0 (if cost starts scaling too much and too fast)

        double growthRate = 9.0;        // α, may be adjusted if needed
        // Considerations about growthRate: this value controls how fast the penalty grows. It follows the formula: 1 + α, meaning if this value is 9,
        // then at max generation the penalty will be 10 times the base penalty.

        double generationFactor = 1.0 + growthRate * (currentGeneration / (double) generations);
        double currentPenalty = basePenalty * generationFactor;
        double total = cost + currentPenalty * penalty;
        return -total;
    }

    public static void main(String[] args) throws IOException
    {
        GA_Satisfactory ga = new GA_Satisfactory(500, 50, 0.05, "instances/example.json", 60L);
        Solution<Double> best = ga.solve();
        System.out.println("Best solution: " + best);
    }
}
