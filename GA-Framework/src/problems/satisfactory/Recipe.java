package problems.satisfactory;

import java.util.List;

public class Recipe
{
    public static class ItemFlow
    {
        public String name;
        public double quantityPerMinute;

        @Override
        public String toString()
        {
            return name + " (" + quantityPerMinute + "/min)";
        }
    }

    private String name;
    private List<ItemFlow> inputs;
    private List<ItemFlow> outputs;

    public Recipe(String name, List<ItemFlow> inputs, List<ItemFlow> outputs)
    {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String getName()
    {
        return name;
    }

    public List<ItemFlow> getInputs()
    {
        return inputs;
    }

    public List<ItemFlow> getOutputs()
    {
        return outputs;
    }

    @Override
    public String toString()
    {
        return "Recipe{" +
                "name='" + name + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }
}
