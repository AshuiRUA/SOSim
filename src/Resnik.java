import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Resnik implements CalculateBehavior_hpoTerm_similarity {

    @Override
    public HashMap<String,Double> calculate(HPO_term hpo_term1, HPO_term hpo_term2, TreeMap<String, HPO_term> HPO_terms, int num_diseases)
    {
        HashMap<String,Double> results = new HashMap<>();
        // 找到连个HPO_term的MICA
        double max_IC = -1;
        for (String id_ancestor_1 : hpo_term1.ancestors)
        {
            if (hpo_term2.ancestors.contains(id_ancestor_1))
            {
                HPO_term hpo_term_temp = HPO_terms.get(id_ancestor_1);
                double IC_temp = hpo_term_temp.getIC();
                if ( IC_temp > max_IC && Double.isFinite(IC_temp))
                {
                    max_IC = hpo_term_temp.IC;
                }
            }
        }
        results.put("Resnik", max_IC);
        return results;
    }
}
