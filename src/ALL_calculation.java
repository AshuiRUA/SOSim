import java.util.HashMap;
import java.util.TreeMap;

public class ALL_calculation implements CalculateBehavior_hpoTerm_similarity {
    CalculateBehavior_hpoTerm_similarity Resnik_Lin_Schlicker_calculation = new Resnik_Lin_Schlicker();
    CalculateBehavior_hpoTerm_similarity PhenoSim_calculation = new PhenoSim();
    CalculateBehavior_hpoTerm_similarity SOSim_calculation = new SOSim_calculate_CAIsRoot_not1();
    @Override
    public HashMap<String,Double> calculate(HPO_term hpo_term1, HPO_term hpo_term2, TreeMap<String, HPO_term> HPO_terms, int num_diseases) {
        HashMap<String,Double> results = new HashMap<>();
        HashMap_addALL(results, Resnik_Lin_Schlicker_calculation.calculate(hpo_term1, hpo_term2, HPO_terms, num_diseases));
        HashMap_addALL(results, PhenoSim_calculation.calculate(hpo_term1, hpo_term2, HPO_terms, num_diseases));
        HashMap_addALL(results, SOSim_calculation.calculate(hpo_term1, hpo_term2, HPO_terms, num_diseases));
        return results;
    }

    private void HashMap_addALL(HashMap<String, Double>a, HashMap<String, Double>b)
    {
        for (String str : b.keySet())
        {
            a.put(str, b.get(str));
        }
    }
}
