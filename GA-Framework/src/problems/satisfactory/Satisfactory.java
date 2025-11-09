package problems.satisfactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import problems.Evaluator;
import problems.satisfactory.Recipe.ItemFlow;
import solutions.Solution;

public class Satisfactory implements Evaluator<Double>
{
    // Auxiliary class for reading json objects with GSON
    private static class SatisfactoryJson
    {
        List<Recipe> recipes;
        List<ItemFlow> available_inputs;
        List<ItemFlow> desired_outputs;
    }

    private List<Recipe> recipes;
    private List<ItemFlow> availableInputs;
    private List<ItemFlow> desiredOutputs;
    private double penaltyConstant = 1000.0;

    public Satisfactory(String filename) throws IOException
    {
        readInput(filename);
    }

    private void readInput(String filename) throws IOException
    {
        //Implementation with Gson to read JSON file
        Gson gson = new Gson();
        Map<String, Object> jsonData = gson.fromJson(new FileReader(filename),
                new TypeToken<Map<String, Object>>() {}.getType());

        String jsonString = gson.toJson(jsonData);
        SatisfactoryJson parsed = gson.fromJson(jsonString, SatisfactoryJson.class);

        this.recipes = parsed.recipes;
        this.availableInputs = parsed.available_inputs;
        this.desiredOutputs = parsed.desired_outputs;

        // Alternative readInputs method, without utilizing Gson library
        // String jsonContent = new String(Files.readAllBytes(Paths.get(filename)));
        // JSONObject root = new JSONObject(jsonContent);

        // // Recipes
        // recipes = new ArrayList<>();
        // JSONArray recipesArray = root.getJSONArray("recipes");
        // for (int i = 0; i < recipesArray.length(); i++)
        // {
        //     JSONObject r = recipesArray.getJSONObject(i);
        //     String name = r.getString("name");
        //     List<ItemFlow> inputs = parseItemList(r.getJSONArray("inputs"));
        //     List<ItemFlow> outputs = parseItemList(r.getJSONArray("outputs"));
        //     recipes.add(new Recipe(name, inputs, outputs));
        // }

        // availableInputs = parseItemList(root.getJSONArray("available_inputs"));
        // desiredOutputs = parseItemList(root.getJSONArray("desired_outputs"));
    }

    // Auxiliary method to alternative readInputs method
    // private List<ItemFlow> parseItemList(JSONArray arr)
    // {
    //     List<ItemFlow> items = new ArrayList<>();
    //     for (int i = 0; i < arr.length(); i++) {
    //         JSONObject obj = arr.getJSONObject(i);
    //         items.add(new ItemFlow(
    //             obj.getString("name"),
    //             obj.getDouble("quantity_per_min")
    //         ));
    //     }
    //     return items;
    // }

    //#region Evaluator methods

    @Override
    public Double evaluate(Solution<Double> sol) {
        double cost = computeTotalCost(sol);    // C(x)
        double penalty = computePenalty(sol);   // P(x)
        return cost + penalty;
    }

    @Override
    public boolean isFeasible(Solution<Double> sol) {
        return computePenalty(sol) == 0.0;
    }

    @Override
    public Integer getDomainSize() {
        return recipes.size();
    }

    // The next region of methods may not be utilized in the GA, but are implemented because of the Evaluator interface
    //#region Evaluator Interface (Unused methods)

    @Override
    public Double evaluateInsertionCost(Double elem, Solution<Double> sol)
    {
        // delta is utilized to measure how much the cost variates if the production is a little increased
        double delta = 0.1;
        Solution<Double> clone = new Solution<>(sol);
        clone.add(elem + delta);
        double oldCost = evaluate(sol);
        double newCost = evaluate(clone);
        return newCost - oldCost;

    }

    @Override
    public Double evaluateRemovalCost(Double elem, Solution<Double> sol)
    {
        // delta is utilized to measure how much the cost variates if the production is a little decreased
        double delta = 0.1;
        Solution<Double> clone = new Solution<>(sol);
        clone.remove(elem);
        clone.add(elem - delta);
        double oldCost = evaluate(sol);
        double newCost = evaluate(clone);
        return newCost - oldCost;
    }

    @Override
    public Double evaluateExchangeCost(Double elemIn, Double elemOut, Solution<Double> sol)
    {
        double oldCost = evaluate(sol);
        Solution<Double> clone = new Solution<>(sol);
        int idxOut = clone.indexOf(elemOut);
        if (idxOut >= 0) clone.set(idxOut, elemIn);
        double newCost = evaluate(clone);
        return newCost - oldCost;
    }

    //#endregion

    //#endregion

    //#region Methods

    public double computeTotalCost(Solution<Double> sol)
    {
        int totalMachines = 0;

        for (int i = 0; i < recipes.size(); i++) {
            double recipePercentage = sol.get(i);
            // recipePercentage represents the [0, 1] value of usage of the current recipe.
            // If recipePercentage = 0.0 → doesn't use the recipe (0 machines)
            // If 0 < recipePercentage < 1.0 → uses 1 machine
            // If recipePercentage = 1.3 → uses 2 machines, and so on.
            int machinesUsed = (int) Math.ceil(recipePercentage);
            totalMachines += machinesUsed;
        }

        return totalMachines;
    }

    public double computePenalty(Solution<Double> sol)
    {
        Map<String, Double> finalProduction = new HashMap<>();

        // Gets total production and consumption of all recipes
        for (int i = 0; i < recipes.size(); i++) {
            double usage = sol.get(i);
            Recipe r = recipes.get(i);

            for (ItemFlow in : r.getInputs()) {
                finalProduction.merge(in.name, -usage * in.quantityPerMinute, Double::sum);
            }
            for (ItemFlow out : r.getOutputs()) {
                finalProduction.merge(out.name, usage * out.quantityPerMinute, Double::sum);
            }
        }

        double penalty = 0.0;
        // Calculates penalty considering available and utilized resources
        for (ItemFlow available : availableInputs) {
            double net = finalProduction.getOrDefault(available.name, 0.0);
            if (net < -available.quantityPerMinute) {
                penalty += Math.abs(net + available.quantityPerMinute);
            }
        }

        // Applies calculated penalty
        for (ItemFlow desired : desiredOutputs) {
            double produced = finalProduction.getOrDefault(desired.name, 0.0);
            if (produced < desired.quantityPerMinute) {
                penalty += (desired.quantityPerMinute - produced);
            }
        }

        return penalty;
    }

    //#region Gets/Sets

    public double getPenaltyConstant()
    {
        return this.penaltyConstant;
    }

    public void setPenaltyConstant(double newPenalty)
    {
        this.penaltyConstant = newPenalty;
    }

    public List<Recipe> getRecipes()
    {
        return recipes;
    }

    public List<Recipe.ItemFlow> getAvailableInputs()
    {
        return availableInputs;
    }

    public List<Recipe.ItemFlow> getDesiredOutputs()
    {
        return desiredOutputs;
    }

    //#endregion

    //#endregion
}
