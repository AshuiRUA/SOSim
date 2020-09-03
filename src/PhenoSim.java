import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class PhenoSim implements CalculateBehavior_hpoTerm_similarity {
    @Override
    public HashMap<String,Double> calculate(HPO_term hpo_term1, HPO_term hpo_term2, TreeMap<String, HPO_term> HPO_terms, int num_diseases)
    {
        HashMap<String,Double> results = new HashMap<>();
        if ( ! hpo_term1.ancestors.contains(hpo_term2.getId()) && ! hpo_term2.ancestors.contains(hpo_term1.getId()) && !hpo_term1.getId().equals(hpo_term2.getId()))
        {
            // 这两个HPO_term互不为祖先，不是reachable的
            results.put("PhenoSim", 0.0);
        }
        else
        {
            results.put("PhenoSim", Math.min(hpo_term1.getIC(), hpo_term2.getIC()));
        }
        return results;
    }
}