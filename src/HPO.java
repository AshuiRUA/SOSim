import java.io.*;
import java.util.*;


public class HPO implements java.io.Serializable{
    public TreeMap<String, HPO_term> HPO_terms = new TreeMap<>();
    public HashMap<String, Disease> Diseases = new HashMap<>();
    public HashMap<String, Gene> Genes = new HashMap<>();
    public HashMap<String, Patient> Patients = new HashMap<>();
    int num_diseases = 0;
    public CalculateBehavior_hpoTerm_similarity calculation;

    HPO(String path_obo, String path_diseaseAnnotation, String path_geneAnnotation, boolean restricted_geneAnnotation) throws Exception
    {
        System.out.println("开始构造HPO");
        // 从obo中读取HPO_terms
        import_obo_file(path_obo);
        // 检查哪些HPO term没有父母
        exam_hpoTerm_without_parent();
        // 为HPO_terms中每个HPO_term找到ancestors
        find_ancestor_for_each_HPOTerm();
        // 检查哪些HPO term没有祖先，谁的祖先没有HP:0000001
        exam_hpoTerm_without_ancestor();
        // 从DiseaseAnnotation文件中导入Diseases
        import_DiseaseAnnotation_file(path_diseaseAnnotation);
        // 每个HPO_term根据自己的disease，初始化自己的D
        D_init_all_hpoTerm();
        // 统计所有的疾病数量
        num_diseases_init();
        // 每个HPO_term计算出自己的IC
        IC_init_all_hpoTerm();
        // 找出每个HPO_term的MIMIA
        MIMIA_init_all_hpoTerm();
        // 从gene_phenotype_disease.txt中导入Genes
        if (restricted_geneAnnotation)
        {
            import_GeneAnnotation_file_restricted(path_geneAnnotation);
        }
        else
        {
            import_GeneAnnotation_file(path_geneAnnotation);
        }
        System.out.println("HPO 构造完成");
    }

    private void import_obo_file(String path_obo) throws Exception
    {
        // 读取obo文件，并绑定一个Scanner对象
        File file_obo = new File(path_obo);
        Scanner sc = new Scanner(file_obo);
        // 从obo文件中读取内容，需要的变量
        boolean first_Term = true;
        boolean is_obsolete = false;
        String id = "null";
        HashSet<String> parents = new HashSet<>();
        // 开始读取
        while (sc.hasNext())
        {
            String line = sc.nextLine();
            // 遇到"[Term]"
            if (line.startsWith("[Term]") && first_Term)
            {
                // 如果是第一次遇到“[Term]”，不创建HPO_term对象，把first_Term取false
                first_Term = false;
                continue;
            }
            else if (line.startsWith("[Term]") && !first_Term && !is_obsolete)
            {
                // 如果不是第一次遇到"[Term]"，创建HPO_term对象
                if (!id.equals("null"))
                {
                    HPO_term temp = new HPO_term(id);
                    temp.setParents(parents);
                    if (this.HPO_terms.containsKey(id))
                    {
                        throw new Exception("重复创建id相同的HPO term");
                    }
                    this.HPO_terms.put(id, temp);
                    // 重新初始化id和parents
                    id = "null";
                    parents = new HashSet<>();
                }
                else
                {
                    throw new Exception("没有id就要创建HPO_term");
                }
            }
            else if (line.startsWith("[Term]") && !first_Term && is_obsolete)
            {
                // 如果是一个废弃的term就不加入HPO_terms，初始化id和parents
                is_obsolete = false;
                id = "null";
                parents = new HashSet<>();
            }
            else if (line.startsWith("id: "))
            {
                id = line.substring(4, 14);
            }
            else if (line.startsWith("is_obsolete: true"))
            {
                is_obsolete = true;
            }
            else if (line.startsWith("is_a: "))
            {
                String id_parent = line.substring(6, 16);
                parents.add(id_parent);
            }
        }
        // 把最后的 HPO term创建，加入HPO_terms
        if (!id.equals("null"))
        {
            HPO_term temp = new HPO_term(id);
            temp.setParents(parents);
            if (this.HPO_terms.containsKey(id))
            {
                throw new Exception("重复创建id相同的HPO term");
            }
            this.HPO_terms.put(id, temp);
        }
        else
        {
            throw new Exception("没有id就要创建HPO_term");
        }
        sc.close();
    }

    private void import_DiseaseAnnotation_file(String path_diseaseAnnotation) throws FileNotFoundException
    {
        // 打开文件，绑定Scanner
        File file = new File(path_diseaseAnnotation);
        Scanner sc = new Scanner(file);
        // 读取文件需要的变量
        boolean is_first_line = true;
        String[] line_array;
        String id_disease;
        String id_hpoTerm;
        // 开始读取文件
        while (sc.hasNext())
        {
            String line = sc.nextLine();
            if (is_first_line)
            {
                // 跳过第一行的表头
                is_first_line = false;
                continue;
            }
            line_array = line.split("\t");
            id_disease = line_array[0];
            id_hpoTerm = line_array[1];
            // 根据疾病是否在this.Diseases中存在，创建Disease或者向对应的Disease中添加id_HPOTerm
            Disease disease_temp;
            if (Diseases.containsKey(id_disease))
            {
                disease_temp = Diseases.get(id_disease);
                disease_temp.add_hpoTerm(id_hpoTerm);
            }
            else
            {
                disease_temp = new Disease(id_disease);
                disease_temp.add_hpoTerm(id_hpoTerm);
                Diseases.put(id_disease, disease_temp);
            }
            // 根据HPO term标注疾病的记录，把相应的HPO_term和其祖先的diseases中加上此疾病
            add_idDisease_to_hpoTermAndItsAncestors(id_hpoTerm, id_disease);
        }
        sc.close();
    }

    private void import_GeneAnnotation_file_restricted(String path_geneAnnotation) throws FileNotFoundException
    {
        // GeneAnnotation文件导入Genes，是受到已经导入的Disease控制的
        File file = new File(path_geneAnnotation);
        Scanner sc = new Scanner(file);
        // 读取文件需要的数据
        boolean is_firstLine = true;
        String[] line_array;
        String name_gene;
        String id_hpoTerm;
        String id_disease;
        // 开始读取
        while (sc.hasNext())
        {
            String line = sc.nextLine();
            if (is_firstLine)
            {
                is_firstLine = false;
                continue;
            }
            line_array = line.split("\t");
            name_gene = line_array[0];
            id_hpoTerm = line_array[1];
            id_disease = line_array[2];
            if (Diseases.containsKey(id_disease)
                    && Diseases.get(id_disease).hpo_terms.contains(id_hpoTerm)
                    && Genes.containsKey(name_gene))
            {
                // Genes中已经有这个name_gene
                Genes.get(name_gene).add_hpoTerm(id_hpoTerm);
            }
            else if (Diseases.containsKey(id_disease)
                    && Diseases.get(id_disease).hpo_terms.contains(id_hpoTerm))
            {
                // Genes中没有这个name_gene
                Gene gene_temp = new Gene(name_gene);
                gene_temp.add_hpoTerm(id_hpoTerm);
                Genes.put(name_gene, gene_temp);
            }
        }
        sc.close();
    }

    private void import_GeneAnnotation_file(String path_geneAnnotation) throws FileNotFoundException
    {
        // GeneAnnotation文件导入Genes，是受到已经导入的Disease控制的
        File file = new File(path_geneAnnotation);
        Scanner sc = new Scanner(file);
        // 读取文件需要的数据
        boolean is_firstLine = true;
        String[] line_array;
        String name_gene;
        String id_hpoTerm;
        String id_disease;
        // 开始读取
        while (sc.hasNext())
        {
            String line = sc.nextLine();
            if (is_firstLine)
            {
                is_firstLine = false;
                continue;
            }
            line_array = line.split("\t");
            name_gene = line_array[0];
            id_hpoTerm = line_array[1];
            id_disease = line_array[2];
            if (HPO_terms.containsKey(id_hpoTerm)
                    && Genes.containsKey(name_gene))
            {
                // Genes中已经有这个name_gene
                Genes.get(name_gene).add_hpoTerm(id_hpoTerm);
            }
            else if (HPO_terms.containsKey(id_hpoTerm))
            {
                // Genes中没有这个name_gene
                Gene gene_temp = new Gene(name_gene);
                gene_temp.add_hpoTerm(id_hpoTerm);
                Genes.put(name_gene, gene_temp);
            }
        }
        sc.close();
    }

    private void find_ancestor_for_each_HPOTerm()
    {
        for (String id : this.HPO_terms.keySet())
        {
            HPO_term hpo_term = this.HPO_terms.get(id);
            hpo_term.setAncestors(find_ancestors(id));
        }
        System.out.println("为每个HPO term寻找祖先");
    }

    private HashSet<String> find_ancestors(String id_hpoTerm)
    {
        HPO_term hpo_term = this.HPO_terms.get(id_hpoTerm);
        HashSet<String> ancestors = new HashSet<>(hpo_term.parents);
        for (String id_parent : hpo_term.parents)
        {
            ancestors.addAll(find_ancestors(id_parent));
        }
        return ancestors;
    }

    private void add_idDisease_to_hpoTermAndItsAncestors(String id_hpoTerm, String id_disease)
    {
        HPO_term hpoTerm_temp = this.HPO_terms.get(id_hpoTerm);
        hpoTerm_temp.add_disease(id_disease);
        for (String id_hpoTermAncestor : hpoTerm_temp.ancestors)
        {
            HPO_term hpoTerm_ancestor = this.HPO_terms.get(id_hpoTermAncestor);
            hpoTerm_ancestor.add_disease(id_disease);
        }
    }

    private void D_init_all_hpoTerm() throws Exception
    {
        for (String id_hpoTerm : HPO_terms.keySet())
        {
            HPO_term hpoTerm_temp = HPO_terms.get(id_hpoTerm);
            hpoTerm_temp.D_init();
            if (hpoTerm_temp.getD() == 0)
            {
                //System.out.println(hpoTerm_temp.getId()+"的D==0");
            }
        }
    }

    private void exam_hpoTerm_without_parent()
    {
        for (String id_hpoTerm: this.HPO_terms.keySet())
        {
            HPO_term hpo_term = this.HPO_terms.get(id_hpoTerm);
            if (hpo_term.parents.isEmpty())
            {
                System.out.println(hpo_term.getId() + "没有父母");
            }
        }
    }

    private void exam_hpoTerm_without_ancestor()
    {
        for (String id_hpoTerm: this.HPO_terms.keySet())
        {
            HPO_term hpo_term = this.HPO_terms.get(id_hpoTerm);
            if (hpo_term.ancestors.isEmpty())
            {
                System.out.println(hpo_term.getId() + "没有祖先");
            }
            if (!hpo_term.ancestors.contains("HP:0000001"))
            {
                System.out.println(hpo_term.getId()+"的祖先没有HP:0000001");
            }
        }
    }

    private void print_HPO()
    {
        for (String id : this.HPO_terms.keySet())
        {
            HPO_term hpo_term = this.HPO_terms.get(id);
            System.out.println(hpo_term.getId());
            System.out.println(hpo_term.parents);
        }
    }

    private void num_diseases_init() throws Exception
    {
        int D_HP0000002 = this.HPO_terms.get("HP:0000001").D;
        if (D_HP0000002 != Diseases.size())
        {
            System.out.println("D_HP0000001="+D_HP0000002+"\tDisease.size()="+Diseases.size());
            throw new Exception("D_HP0000001 != Diseases.size()");
        }
        else
        {
            num_diseases = D_HP0000002;
        }
    }

    private void IC_init_all_hpoTerm()
    {
        for (String id_hpoTerm : HPO_terms.keySet())
        {
            HPO_terms.get(id_hpoTerm).IC_init(num_diseases);
        }
    }

    private void MIMIA_init_all_hpoTerm() throws Exception
    {
        for (String id_hpoTerm : HPO_terms.keySet())
        {
            String id_MIMIA = "null";
            double max_IC = -1;
            HPO_term hpoTerm_temp = HPO_terms.get(id_hpoTerm);
            if (hpoTerm_temp.ancestors.isEmpty())
            {
                // 没有祖先，说明是HP:0000001
                hpoTerm_temp.setId_MIMIA("no MIMIA");
            }
            else
            {
                // 有祖先就挨个检查IC，找到MIMIA
                for (String id_hpoTerm_ancestor : hpoTerm_temp.getAncestors())
                {
                    HPO_term hpoTerm_temp_ancestor = HPO_terms.get(id_hpoTerm_ancestor);
                    if (hpoTerm_temp_ancestor.getIC() > max_IC
                            && Double.isFinite(hpoTerm_temp_ancestor.getIC())
                            && hpoTerm_temp_ancestor.getIC() < hpoTerm_temp.getIC())
                    {
                        id_MIMIA = hpoTerm_temp_ancestor.getId();
                        max_IC = hpoTerm_temp_ancestor.getIC();
                    }
                }
                hpoTerm_temp.setId_MIMIA(id_MIMIA);
            }
            if (hpoTerm_temp.getId_MIMIA().equals("null"))
            {
                throw new Exception(hpoTerm_temp.getId()+"没找到MIMIA");
            }
        }
    }

    // 为每个疾病，计算它和所有基因的相似度
    public void calculate_sim_Diseases_Genes(Set<String> list_id_disease, Set<String> list_name_gene,
                                                 Calculation_type type, int limit_disease, boolean unsymmetrical) throws Exception
    {
        if (limit_disease < 0){throw new Exception("limit < 0");}
        int count = 0;
        for (String id_disease : list_id_disease)
        {
            if ( ! Diseases.keySet().contains(id_disease))
            {
                System.out.println("HPO.Diseases不包含"+id_disease);
                continue;
            }
            //////////////////////////////////////////////////////////////
            System.out.format("%s和基因们的相似度计算中\n",id_disease);
            //////////////////////////////////////////////////////////////
            for (String name_gene : list_name_gene)
            {
                if (! Genes.keySet().contains(name_gene))
                {
                    continue;
                }
                // 保证了调用sim_disease_gene的id_disease和name_gene，都是Disease和Gene里有的
                sim_disease_gene(Diseases.get(id_disease), name_gene, type, unsymmetrical);
            }
            ++count;
            if (count == limit_disease)break;
        }
    }

    // 为每个疾病，计算它和所有基因的相似度
    public void calculate_sim_Diseases_Genes(Set<String> list_id_disease, Set<String> list_name_gene,
                                                 Calculation_type type, boolean unsymmetrical)
    {
        for (String id_disease : list_id_disease)
        {
            if ( ! Diseases.keySet().contains(id_disease))
            {
                System.out.println("HPO.Diseases不包含"+id_disease);
                continue;
            }
            //////////////////////////////////////////////////////////////
            System.out.format("%s和基因们的相似度计算中\n",id_disease);
            //////////////////////////////////////////////////////////////
            for (String name_gene : list_name_gene)
            {
                if (! Genes.keySet().contains(name_gene))
                {
                    continue;
                }
                // 保证了调用sim_disease_gene的id_disease和name_gene，都是Disease和Gene里有的
                sim_disease_gene(Diseases.get(id_disease), name_gene, type, unsymmetrical);
            }
        }
    }

    // 为每个疾病，计算它和所有基因的相似度
    public void calculate_sim_Diseases_Genes(HashMap<String, String> dataset, Calculation_type type,
                                             int limit_disease, boolean unsymmetrical) throws Exception
    {
        // 先把HashMap格式dataset，转化成两个Set
        Set<String> list_id_disease = dataset.keySet();
        Set<String> list_name_gene = new HashSet<>();
        for (String id_disease : list_id_disease)
        {
            list_name_gene.add(dataset.get(id_disease));
        }
        // 挨个计算Disease-Gene相似度
        if (limit_disease < 0){throw new Exception("limit < 0");}
        int count = 0;
        for (String id_disease : list_id_disease)
        {
            if ( ! Diseases.keySet().contains(id_disease))
            {
                System.out.println("HPO.Diseases不包含"+id_disease);
                continue;
            }
            //////////////////////////////////////////////////////////////
            System.out.format("%s和基因们的相似度计算中\n",id_disease);
            //////////////////////////////////////////////////////////////
            for (String name_gene : list_name_gene)
            {
                if (! Genes.keySet().contains(name_gene))
                {
                    continue;
                }
                // 保证了调用sim_disease_gene的id_disease和name_gene，都是Disease和Gene里有的
                sim_disease_gene(Diseases.get(id_disease), name_gene, type, unsymmetrical);
            }
            ++count;
            if (count == limit_disease)break;
        }
    }

    // 为每个疾病，计算它和所有基因的相似度
    public void calculate_sim_Diseases_Genes(HashMap<String, String> dataset, Calculation_type type, boolean unsymmetrical)
    {
        // 先把HashMap格式dataset，转化成两个Set
        Set<String> list_id_disease = dataset.keySet();
        Set<String> list_name_gene = new HashSet<>();
        for (String id_disease : list_id_disease)
        {
            list_name_gene.add(dataset.get(id_disease));
        }
        // 挨个计算Disease-Gene相似度
        for (String id_disease : list_id_disease)
        {
            if ( ! Diseases.keySet().contains(id_disease))
            {
                System.out.println("HPO.Diseases不包含"+id_disease);
                continue;
            }
            //////////////////////////////////////////////////////////////
            System.out.format("%s和基因们的相似度计算中\n",id_disease);
            //////////////////////////////////////////////////////////////
            for (String name_gene : list_name_gene)
            {
                if (! Genes.keySet().contains(name_gene))
                {
                    continue;
                }
                // 保证了调用sim_disease_gene的id_disease和name_gene，都是Disease和Gene里有的
                //System.out.format("%s-%s相似度计算中\n",id_disease, name_gene);
                sim_disease_gene(Diseases.get(id_disease), name_gene, type, unsymmetrical);
            }
            //////////////////////////////////////////////////////////
//            System.out.println(Diseases.get(id_disease).sim_gene_simType_simResult);
            /////////////////////////////////////////////////////////////
        }

    }

    // 计算一个疾病到一个基因的相似度
    public HashMap<String, Double> sim_disease_gene(Disease disease, String name_gene, Calculation_type type, boolean unsymmetrical)
    {
        ///////////////////////////////////////////////////////////
//        System.out.format("%s-%s相似度计算\n", id_disease, name_gene);
        ////////////////////////////////////////////////////////////
        HashMap<String, Double> results = new HashMap<>();
        List<HashMap<String,Double>> sim_disease_gene = new ArrayList<>();
        if (unsymmetrical)
        {
            // 计算一个疾病到一个基因的相似度
            // 计算一个疾病到一个基因的相似度
            // 开始挨个计算HPO_term-Gene相似度
            for (String id_hpoTerm_disease : disease.hpo_terms)
            {
                // 计算疾病的一个HPO_term和基因的所有HPO_term的相似度矩阵
                HashMap<String, Double> sims_hpoTerm_gene = sim_hpoTerm_gene(id_hpoTerm_disease, name_gene, type);
                sim_disease_gene.add(sims_hpoTerm_gene);
            }
            // 针对每种相似度求法，对Disease的所有的HPO_term-Gene的相似度们求平均值，就是Disease-Gene相似度。
            for (String sim_type : sim_disease_gene.get(0).keySet())
            {
                ArrayList<Double> temp = new ArrayList();
                for (int index = 0; index < disease.hpo_terms.size(); ++index)
                {
                    temp.add(sim_disease_gene.get(index).get(sim_type));
                }
                results.put(sim_type, temp.stream().mapToDouble(x->x).summaryStatistics().getAverage());
            }
            // 把计算的结果放入Disease.sim_gene_simType_SimResult里
            if (! disease.sim_gene_simType_simResult.containsKey(name_gene))
            {
                disease.sim_gene_simType_simResult.put(name_gene, results);
            }
            else
            {
                for (String sim_type : results.keySet())
                {
                    if( ! disease.sim_gene_simType_simResult.get(name_gene).containsKey(sim_type))
                    {
                        disease.sim_gene_simType_simResult.get(name_gene).put(sim_type, results.get(sim_type));
                    }
                }
            }
        }
        else
        {
            // 计算一个疾病到一个基因的相似度
            // 计算一个疾病到一个基因的相似度
            // 开始挨个计算HPO_term-Gene相似度
            for (String id_hpoTerm_disease : disease.hpo_terms)
            {
                // 计算疾病的一个HPO_term和基因的所有HPO_term的相似度矩阵
                HashMap<String, Double> sims_hpoTerm_gene = sim_hpoTerm_gene(id_hpoTerm_disease, name_gene, type);
                sim_disease_gene.add(sims_hpoTerm_gene);
            }
            // 针对每种相似度求法，对Disease的所有的HPO_term-Gene的相似度们求平均值，就是Disease-Gene相似度。
            for (String sim_type : sim_disease_gene.get(0).keySet())
            {
                ArrayList<Double> temp = new ArrayList();
                for (int index = 0; index < disease.hpo_terms.size(); ++index)
                {
                    temp.add(sim_disease_gene.get(index).get(sim_type));
                }
                results.put(sim_type, temp.stream().mapToDouble(x->x).summaryStatistics().getAverage());
            }
            // 计算从基因到疾病的相似度
            HashMap<String, Double> sim_gene_disease = sim_gene_disease(Genes.get(name_gene), disease.id, type);
            HashMap<String, Double> results_temp = new HashMap<>();
            // 把从疾病到基因的相似度和从基因到疾病的相似度，做平均
            for (String sim_type : results.keySet())
            {
                double temp = (results.get(sim_type) + sim_gene_disease.get(sim_type)) / 2;
                results_temp.put(sim_type, temp);
            }
            results = results_temp;
            // 把计算的结果放入Disease.sim_gene_simType_SimResult里
            if (! disease.sim_gene_simType_simResult.containsKey(name_gene))
            {
                disease.sim_gene_simType_simResult.put(name_gene, results);
            }
            else
            {
                for (String sim_type : results.keySet())
                {
                    if( ! disease.sim_gene_simType_simResult.get(name_gene).containsKey(sim_type))
                    {
                        disease.sim_gene_simType_simResult.get(name_gene).put(sim_type, results.get(sim_type));
                    }
                }
            }
        }
        return results;

    }

    // 计算一个基因到一个疾病的相似度，返回的map元素是“相似度类型-相似度结果”
    public HashMap<String, Double> sim_gene_disease(Gene gene, String id_disease, Calculation_type type)
    {
        ///////////////////////////////////////////////////////////
//        System.out.format("%s-%s相似度计算\n", id_disease, name_gene);
        ////////////////////////////////////////////////////////////
        HashMap<String, Double> results = new HashMap<>();
        List<HashMap<String,Double>> sim_disease_disease = new ArrayList<>();
        // 开始挨个计算HPO_term-Disease相似度
        for (String id_hpoTerm_disease : gene.hpo_terms)
        {
            // 计算疾病的一个HPO_term和疾病的所有HPO_term的相似度矩阵
            HashMap<String, Double> sims_hpoTerm_disease = sim_hpoTerm_disease(id_hpoTerm_disease, id_disease, type);
            sim_disease_disease.add(sims_hpoTerm_disease);
        }
        // 针对每种相似度求法，对Disease的所有的HPO_term-Gene的相似度们求平均值，就是Disease-Gene相似度。
        for (String sim_type : sim_disease_disease.get(0).keySet())
        {
            ArrayList<Double> temp = new ArrayList();
            for (int index = 0; index < gene.hpo_terms.size(); ++index)
            {
                temp.add(sim_disease_disease.get(index).get(sim_type));
            }
            results.put(sim_type, temp.stream().mapToDouble(x->x).summaryStatistics().getAverage());
        }
        return results;
    }

    // 计算一个HPO term和一个基因的相似度
    public HashMap<String, Double> sim_hpoTerm_gene(String id_hpoTerm, String name_gene, Calculation_type type)
    {
        HashMap<String, Double> results = new HashMap<>();
        Gene gene = Genes.get(name_gene);
        List<HashMap<String, Double>> sim_hpoTerm_gene = new ArrayList<>();
        for (String id_hpoTerm_gene : gene.hpo_terms)
        {
            HashMap<String, Double> sims_hpoTermDisease_hpoTermGene = calculate_hpoTerm_similarity(HPO_terms.get(id_hpoTerm), HPO_terms.get(id_hpoTerm_gene), type);
            sim_hpoTerm_gene.add(sims_hpoTermDisease_hpoTermGene);
        }
        // 对每一列sim_hpoTerm_gene求MAX，得到结果
        for (String sim_type : sim_hpoTerm_gene.get(0).keySet())
        {
            ArrayList<Double> temp = new ArrayList<>();
            double max = -1000000;
            for (int index = 0; index < gene.hpo_terms.size(); ++index)
            {
                temp.add(sim_hpoTerm_gene.get(index).get(sim_type));
            }
            results.put(sim_type, Collections.max(temp));
        }
        return results;
    }

    // 计算一个HPO term和一个疾病的相似度
    public HashMap<String, Double> sim_hpoTerm_disease(String id_hpoTerm, String id_disease, Calculation_type type)
    {
        HashMap<String, Double> results = new HashMap<>();
        Disease disease;
        if (Diseases.containsKey(id_disease))
        {
            disease = Diseases.get(id_disease);
        }
        else
        {
            disease = Patients.get(id_disease);
        }
        List<HashMap<String, Double>> sim_hpoTerm_disease = new ArrayList<>();
        // 计算给定HPO term和给定疾病的所有HPO term的相似度
        for (String id_hpoTerm_gene : disease.hpo_terms)
        {
            HashMap<String, Double> sims_hpoTermDisease_hpoTermGene = calculate_hpoTerm_similarity(HPO_terms.get(id_hpoTerm), HPO_terms.get(id_hpoTerm_gene), type);
            sim_hpoTerm_disease.add(sims_hpoTermDisease_hpoTermGene);
        }
        // 对每一列sim_hpoTerm_gene求MAX，得到结果
        for (String sim_type : sim_hpoTerm_disease.get(0).keySet())
        {
            ArrayList<Double> temp = new ArrayList<>();
            double max = -1000000;
            for (int index = 0; index < disease.hpo_terms.size(); ++index)
            {
                temp.add(sim_hpoTerm_disease.get(index).get(sim_type));
            }
            results.put(sim_type, Collections.max(temp));
        }
        return results;
    }

    // 计算两个HPO term的相似度
    public HashMap<String, Double> calculate_hpoTerm_similarity(HPO_term hpoTerm1, HPO_term hpoTerm2, Calculation_type type)
    {
        HashMap<String, Double> results = new HashMap<>();
        switch (type) {
            case Resnik -> calculation = new Resnik();
            case Lin -> calculation = new Lin();
            case Schlicker -> calculation = new Schlicker();
            case PhenoSim -> calculation = new PhenoSim();
            case SOSim -> calculation = new SOSim_calculate_CAIsRoot_not1();
            case Resnik_Lin_Schlicker -> calculation = new Resnik_Lin_Schlicker();
            case All -> calculation = new ALL_calculation();
        }

        results = calculation.calculate(hpoTerm1, hpoTerm2, HPO_terms, num_diseases);
        return results;
    }

    // 根据疾病-基因对，输出基因的排名
    public void output_dataset_ranking(HashMap<String, String> dataset, String output_path) throws FileNotFoundException
    {
        File file_output = new File(output_path);
        if (file_output.exists())
        {
            System.out.println(file_output+"已经存在");
        }
        else
        {
            try(PrintWriter output = new PrintWriter(file_output))
            {
                for (String id_disease : dataset.keySet())
                {
                    if (Diseases.keySet().contains(id_disease))
                    {
                        Disease disease = Diseases.get(id_disease);
                        String name_gene = dataset.get(id_disease);
                        ////////////////////////////////////////////////////
                        Gene gene = Genes.get(name_gene);
                        /////////////////////////////////////////////////////
                        output.format("%s\t%s\t", id_disease, name_gene);
                        output.println(disease.gene_sim_ranking_allSimType(name_gene));
                    }
                    else
                    {
                        String name_gene = dataset.get(id_disease);
                        output.format("%s-%s\n", id_disease, name_gene);
                        output.format("%s不在HPO标注过的疾病中\n",id_disease);
                    }

                }
            }
        }

    }

    // 根据“疾病-表型频率”文件，创建hpo.Patients
    public void generatePatient_useFrequencyFile(String path_frequencyFile, int num_disease_perDisease, boolean add_noise) throws Exception
    {
        File file_frequency = new File(path_frequencyFile);
        Scanner sc = new Scanner(file_frequency);
        // 遍历文件需要的变量
        boolean is_first = true;
        String id_disease = new String();
        String name_gene = new String();
        String[] line_array = new String[2];
        HashMap<String, Double> map_idHPOTerm_frequency = new HashMap();
        // 开始遍历文件
        while (sc.hasNext())
        {
            String line = sc.nextLine().strip();
            if (line.equals("[Disease]") && is_first)
            {
                if (sc.hasNext())
                {
                    line_array = sc.nextLine().strip().split("\t");
                    id_disease = line_array[0];
                    name_gene = line_array[1];
                }
                else
                { throw new Exception("[Disease]的下一行为空");}
                is_first = false;
            }
            else if (line.equals("[Disease]"))
            {
                // 生成一定数量的病人，然后
                Patients.putAll(generate_Patient(id_disease, name_gene, map_idHPOTerm_frequency,num_disease_perDisease,add_noise));
                // 重新初始化变量
                map_idHPOTerm_frequency = new HashMap();
                if (sc.hasNext())
                {
                    line_array = sc.nextLine().strip().split("\t");
                    id_disease = line_array[0];
                    name_gene = line_array[1];
                }
                else
                { throw new Exception("[Disease]的下一行为空");}
            }
            else
            {
                line_array = line.strip().split("\t");
                map_idHPOTerm_frequency.put(line_array[0], Double.valueOf(line_array[1]));
            }

        }
        Patients.putAll(generate_Patient(id_disease, name_gene, map_idHPOTerm_frequency,num_disease_perDisease,add_noise));
    }

    // 创建指定数量的Patient对象
    private HashMap<String, Patient> generate_Patient(String id_disease, String name_gene,
                                                      HashMap<String, Double> map_idHPOTerm_frequency,
                                                      int num_patient_perDisease, boolean add_noise)
    {
        HashMap<String, Patient> result = new HashMap<>();
        if (Diseases.containsKey(id_disease) && Genes.containsKey(name_gene))
        {
            Disease disease = Diseases.get(id_disease);
            for (int i=0; i<num_patient_perDisease; ++i)
            {
                String id_patient = null;
                id_patient = String.format(id_disease+"_%d", i+1);
                Patient patient_temp = new Patient(id_patient, disease, name_gene, map_idHPOTerm_frequency);
                if (add_noise)
                {
                    patient_temp.add_noise(this);
                }
                result.put(id_patient, patient_temp);
            }
        }
        return result;
    }

    // 为每个病人，计算它和所有基因的相似度
    public void calculate_sim_Patients_Genes(Set<String> list_id_patient, Set<String> list_name_gene,
                                             Calculation_type type, boolean unsymmetrical)
    {
        int count = 0;
        int size = list_id_patient.size();
        for (String id_patient : list_id_patient)
        {
            ++count;
            if ( ! Patients.keySet().contains(id_patient))
            {
                System.out.println("HPO.Diseases不包含"+id_patient);
                continue;
            }
            //////////////////////////////////////////////////////////////
            System.out.format("[%d/%d]  %s和基因们的相似度计算中\n",count, size, id_patient);
            //////////////////////////////////////////////////////////////
            for (String name_gene : list_name_gene)
            {
                if (! Genes.keySet().contains(name_gene))
                {
                    continue;
                }
                // 保证了调用sim_disease_gene的id_disease和name_gene，都是Disease和Gene里有的
                sim_disease_gene(Patients.get(id_patient), name_gene, type, unsymmetrical);
            }
        }
    }

    // 为每个病人，计算它和所有基因的相似度
    public void calculate_sim_Patients_Genes(Set<String> list_id_patient, Set<String> list_name_gene,
                                             Calculation_type type, int limit, boolean unsymmetrical) throws Exception
    {
        if (limit < 0){throw new Exception("limit < 0");}
        int count = 0;
        for (String id_patient : list_id_patient)
        {
            if ( ! Patients.keySet().contains(id_patient))
            {
                System.out.println("HPO.Diseases不包含"+id_patient);
                continue;
            }
            //////////////////////////////////////////////////////////////
            System.out.format("%s和基因们的相似度计算中\n",id_patient);
            //////////////////////////////////////////////////////////////
            for (String name_gene : list_name_gene)
            {
                if (! Genes.keySet().contains(name_gene))
                {
                    continue;
                }
                // 保证了调用sim_disease_gene的id_disease和name_gene，都是Disease和Gene里有的
                sim_disease_gene(Patients.get(id_patient), name_gene, type, unsymmetrical);
            }
            ++count;
            if (count == limit)break;
        }
    }

    // 根据病人-基因对，输出基因的排名
    public void output_patientGene_ranking(String path_outputFile) throws FileNotFoundException {
        File file_output = new File(path_outputFile);
        if (file_output.exists())
        {
            System.out.println(file_output+"已经存在");
        }
        else
        {
            try(PrintWriter output = new PrintWriter(file_output))
            {
                for (String id_patient : Patients.keySet())
                {
                    Patient patient = Patients.get(id_patient);
                    output.format("%s\t%s\t", patient.id, patient.name_causativeGene);
                    output.println(patient.gene_sim_ranking_allSimType(patient.name_causativeGene));
                }
            }
        }
    }
}
