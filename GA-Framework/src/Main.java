
import problems.qbf.solvers.GA_QBF_SC;
import solutions.Solution;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        // Setup logger to write to file
        setupLogger();
        
        String[] instances = listInstances();
        InstanceParameters[] parameters = listParameters();

        logger.info("Starting GRASP QBF-SC solver execution");
        logger.info("Number of instances: " + instances.length);
        logger.info("Number of parameter configurations: " + parameters.length);

        for (String instance : instances) {
            for (InstanceParameters param : parameters) {
                try {
                    GA_QBF_SC solver = param.createSolver(instance, logger);
                    long startTime = System.currentTimeMillis();

                    logger.info("Solving instance " + instance + " with parameters: " + param);
                    
                    Solution<Integer> bestSol = solver.solve();
                    long endTime = System.currentTimeMillis();
                    long executionTime = endTime - startTime;
                    
                    logger.info("maxVal = " + bestSol);
                    logger.info("Solution found in " + executionTime + " ms");
                    logger.info("Instance " + instance + " completed successfully\n");
                    
                } catch (Exception e) {
                    logger.severe("Error solving instance " + instance + " with parameters: " + e.getMessage());
                    logger.severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                }
            }
        }
        
        logger.info("GRASP QBF-SC solver execution completed");
    }
    
    private static void setupLogger() {
        try {
            // Create timestamp for unique log file
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());
            String logFileName = "results/ga_qbf_sc_" + timestamp + ".log";
            
            // Create results directory if it doesn't exist
            java.io.File resultsDir = new java.io.File("results");
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }
            
            // Create file handler
            FileHandler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            
            // Configure logger
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);
            
            // Also log to console for immediate feedback
            logger.setUseParentHandlers(true);
            
            logger.info("Logger initialized. Output will be written to: " + logFileName);
            
        } catch (IOException e) {
            System.err.println("Failed to setup logger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    protected static String[] listInstances() {
        int nInstances = 1;
        String[] instances = new String[nInstances];
        for (int i = 0; i < nInstances; i++) {
            instances[i] = "GA-Framework/instances/qbf-sc/instance_" + i + ".txt";
        }
        return instances;
    }

    protected static InstanceParameters[] listParameters() {
        Integer maxGenerations = Integer.MAX_VALUE; // Run until timeout
        Long timeoutInSeconds = 60L * 30L; // 30 minutes
        Integer population1 = 100;
        Integer population2 = 1000;
        Double mutationRate1 = 1.0 / 100.0;
        Double mutationRate2 = 10.0 / 100.0;

        return new InstanceParameters[] {
            // PADRÃO: população 100, mutação 1%, construção aleatória
            new InstanceParameters(maxGenerations, population1, mutationRate1, timeoutInSeconds),
            // PADRÃO + POP: população 1000, mutação 1%, construção aleatória
            new InstanceParameters(maxGenerations, population2, mutationRate1, timeoutInSeconds),
            // PADRÃO + MUT: população 100, mutação 10%, construção aleatória
            new InstanceParameters(maxGenerations, population1, mutationRate2, timeoutInSeconds),
            // PADRÃO + EVOL1: população 100, mutação 1%, construção alternativa 1
            // @TODO: Implementar construção alternativa 1
            new InstanceParameters(maxGenerations, population1, mutationRate1, timeoutInSeconds),
            // PADRÃO + EVOL2: população 100, mutação 1%, construção alternativa 2
            new InstanceParameters(maxGenerations, population1, mutationRate1, timeoutInSeconds),
        };
    }
}

class InstanceParameters {
    protected Integer maxGenerations;
    protected Integer populationSize;
    protected Double mutationRate;
    protected Long timeoutInSeconds;

    public InstanceParameters(Integer maxGenerations, Integer populationSize, Double mutationRate, Long timeoutInSeconds) {
        this.maxGenerations = maxGenerations;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public GA_QBF_SC createSolver(String filename, Logger logger) throws Exception {
        GA_QBF_SC solver = new GA_QBF_SC(maxGenerations, populationSize, mutationRate, filename, timeoutInSeconds);
        solver.setLogger(logger);
        return solver;
    }

    @Override
    public String toString() {
        return "maxGenerations=" + maxGenerations + ", populationSize=" + populationSize + ", mutationRate=" + mutationRate + ", timeoutInSeconds=" + timeoutInSeconds;
    }
}