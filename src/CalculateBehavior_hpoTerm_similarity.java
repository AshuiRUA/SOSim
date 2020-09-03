import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public interface CalculateBehavior_hpoTerm_similarity {
    HashMap<String,Double> calculate(HPO_term hpo_term1, HPO_term hpo_term2, TreeMap<String,HPO_term> HPO_terms, int num_diseases);
}
