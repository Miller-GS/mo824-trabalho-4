package problems.satisfactory.solvers;
 
import java.io.IOException;
import solutions.Solution;
 
public class GA_Satisfactory_AdaptiveMutation extends GA_Satisfactory
{
    private int generationsWithoutImprovementsCounter;
    private final int generationsWithoutImprovementMax = 70;    // Determines max acceptable number of generations withut improvements
    private final double maxMR = 0.5;
    private final double minMR = 0.001;                         // Max and Min acceptable mutation rates
 
    public GA_Satisfactory_AdaptiveMutation(Integer generations, Integer popSize, Double mutationRate, String filename, Long timeoutInSeconds) throws IOException
    {
        super(generations, popSize, mutationRate, filename, timeoutInSeconds);
        this.generationsWithoutImprovementsCounter = 0;
    }
 
    @Override
    public Solution<Double> solve()
    {
        Population population = initializePopulation();
        bestChromosome = getBestChromosome(population);
        bestSol = decode(bestChromosome);
 
        long startTime = System.currentTimeMillis();
        for (currentGeneration = 1; currentGeneration <= generations; currentGeneration++)
        {
            Population parents = selectParents(population);
            Population offsprings = crossover(parents);
            Population mutants = mutate(offsprings);
            Population newPopulation = selectPopulation(mutants);
            population = newPopulation;
            bestChromosome = getBestChromosome(population);
            Solution<Double> bestSolCurrentGen = decode(bestChromosome);
 
            // Adaptation change: if the algorithm spends too many generations without any improvements, we increase mutation rate;
            // This will force the population to change and escape the most common places if it stagnates, allowing for better exploration.
            // On the other hand, if the population is improving fast, the mutation rate is gradually decreased, also allowing for better
            // exploration
            if (bestSolCurrentGen.cost < bestSol.cost)
            {
                if (ObjFunction.isFeasible(bestSolCurrentGen)) {
                    bestSol = bestSolCurrentGen;
                    if (verbose)
					    logger.info(logPrefix + "(Gen. " + currentGeneration + ") BestSol = " + bestSol);
                }
                
                generationsWithoutImprovementsCounter = 0;
                double previousMutationRate = mutationRate;
                mutationRate = Math.max(mutationRate * 0.9, minMR);
                if (previousMutationRate != mutationRate && verbose)
                    System.out.println("[DEC_MR] Mutation rate decreased to " + mutationRate);
            }
            else
            {
                generationsWithoutImprovementsCounter++;
                if (generationsWithoutImprovementsCounter >= generationsWithoutImprovementMax)
                {
                    double previousMutationRate = mutationRate;
                    mutationRate = Math.min(mutationRate * 1.5, maxMR);
                    generationsWithoutImprovementsCounter = 0;
                    if (previousMutationRate != mutationRate && verbose)
                        System.out.println("[INC_MR] Mutation rate increased to " + mutationRate);
                }
            }
 
            long currentTime = System.currentTimeMillis();
            if (timeoutInSeconds != null && (currentTime - startTime) >= timeoutInSeconds * 1000)
            {
                System.out.println("[TIME] Timeout after " + timeoutInSeconds + "s");
                break;
            }
        }

        if (!ObjFunction.isFeasible(bestSol)) {
            throw new RuntimeException("No feasible solution found.");
        }
 
        return bestSol;
    }
}