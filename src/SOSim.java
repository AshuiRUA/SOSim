import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class SOSim {
    public static void main(String[] args) throws Exception {
        String path_oboFile = "data/source_data/hp.obo";
        String path_DiseaseAnnotationFile = "data/source_data/DiseaseAnnotation.txt";
        String path_GeneAnnotationFile = "data/source_data/gene_phenotype_disease.txt";
        String path_hpo = "data/hpo.ser";
        //参数
        boolean restricted_geneAnnotation = false;
        ///////////////////////////////////////////////////////////
        HPO hpo = new HPO(path_oboFile, path_DiseaseAnnotationFile, path_GeneAnnotationFile, restricted_geneAnnotation);

        String path_dataset_Masino = "data/dataset/dataset_Masino.txt";
        HashMap<String, String> dataset_Masino = load_dataset(path_dataset_Masino);

        // 参数
        boolean unsymmetrical = true;
        boolean allGenes = true;
        //////////////////////////////////////////////
        if (allGenes)
        {
            hpo.calculate_sim_Diseases_Genes(dataset_Masino.keySet(), hpo.Genes.keySet(), Calculation_type.All, unsymmetrical);
        }
        else
        {
            hpo.calculate_sim_Diseases_Genes(dataset_Masino, Calculation_type.All, unsymmetrical);
        }
        String path_outputFile = "data/result/Masino_geneAnnotationUnrestricted_CSIsrootNot1_allGenes.txt";
        hpo.output_dataset_ranking(dataset_Masino, path_outputFile);
    }

    private void output_HPO_object(HPO hpo, String path) throws Exception
    {
        // 先判断文件是否存在
        File file_temp = new File(path);
        if (file_temp.exists())
        {
            throw new Exception(path+"文件已存在");
        }
        // 不存在的话，就输出序列化后的hpo
        try
        {
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(hpo);
            out.close();
            fileOut.close();
            System.out.println("序列化后的HPO储存在"+path);
        }catch(IOException i)
        {
            i.printStackTrace();
        }
    }

    private HPO read_HPO_object(String path_hpo)
    {
        try
        {
            FileInputStream fileIn = new FileInputStream(path_hpo);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            HPO result = (HPO) in.readObject();
            in.close();
            fileIn.close();
            return result;
        }catch(IOException i)
        {
            i.printStackTrace();
            return null;
        }catch(ClassNotFoundException c)
        {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> load_dataset(String path) throws FileNotFoundException
    {
        HashMap<String, String> results = new HashMap<>();
        File file = new File(path);
        Scanner sc = new Scanner(file);
        boolean is_first = true;
        String[] line_array = new String[2];
        while (sc.hasNext())
        {
            String line = sc.nextLine();
            if (is_first)
            {
                is_first = false;
                continue;
            }
            line_array = line.split("\t");
            results.put(line_array[0], line_array[1]);
        }
        sc.close();
        return results;
    }
}
