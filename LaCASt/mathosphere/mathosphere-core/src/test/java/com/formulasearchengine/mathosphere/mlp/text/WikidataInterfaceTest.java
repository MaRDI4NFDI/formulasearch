package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Moritz on 12.12.2015.
 */
public class WikidataInterfaceTest {

    String[] qIds = new String[] {"Q7913892",
            "Q12503",
            "Q3176558",
            "Q36161",
            "Q739925",
            "Q49008",
            "Q12503",
            "Q5156597",
            "Q11567",
            "Q1413083",
            "Q50700",
            "Q50701",
            "Q935944",
            "Q50701",
            "Q935944",
            "Q1144319",
            "Q50700",
            "Q3150667",
            "Q2256802",
            "Q729113",
            "Q21199",
            "Q33456",
            "Q44946",
            "Q230883",
            "Q21199",
            "Q21199",
            "Q50700",
            "Q50700",
            "Q50700",
            "Q50700",
            "Q378201",
            "Q302462",
            "Q3913",
            "Q3913",
            "Q3913",
            "Q12916",
            "Q12916",
            "Q11352",
            "Q2303886",
            "Q526719",
            "Q11348",
            "Q1027788",
            "Q12916",
            "Q12916",
            "Q946764",
            "Q19033",
            "Q126017",
            "Q230963",
            "Q2303886",
            "Q168698",
            "Q917476",
            "Q17285",
            "Q1663694",
            "Q1663694",
            "Q1663694",
            "Q1663694",
            "Q5597315",
            "Q5597315",
            "Q2303886",
            "Q46276",
            "Q2140940",
            "Q36253",
            "Q1096885",
            "Q189569",
            "Q3176558",
            "Q188889",
            "Q188889",
            "Q13824",
            "Q2111",
            "Q174102",
            "Q1440227",
            "Q167",
            "Q1515261",
            "Q1128317",
            "Q111059",
            "Q111059",
            "Q43260",
            "Q3150667",
            "Q43260",
            "Q11567",
            "Q2095069",
            "Q21199",
            "Q21199",
            "Q2303886",
            "Q2303886",
            "Q1137759",
            "Q193796",
            "Q12916",
            "Q6520159",
            "Q11471",
            "Q167",
            "Q12916",
            "Q12916",
            "Q21199",
            "Q21199",
            "Q3686031",
            "Q11471",
            "Q9492",
            "Q12916",
            "Q4440864",
            "Q12916",
            "Q18373",
            "Q2111",
            "Q1289248",
            "Q876346",
            "Q1289248",
            "Q464794",
            "Q193794",
            "Q192826",
            "Q11471",
            "Q929043",
            "Q2518235",
            "Q782566",
            "Q1074380",
            "Q1413083",
            "Q1413083",
            "Q1008943",
            "Q1256787",
            "Q13471665",
            "Q1289248",
            "Q2337858",
            "Q11348",
            "Q11348",
            "Q11348",
            "Q11471",
            "Q2918589",
            "Q1045555",
            "Q21199",
            "Q82580",
            "Q18848",
            "Q18848",
            "Q1952404",
            "Q11703678",
            "Q11703678",
            "Q2303886",
            "Q1096885",
            "Q4440864",
            "Q2362761",
            "Q11471",
            "Q3176558",
            "Q30006",
            "Q11567",
            "Q3258885",
            "Q131030",
            "Q21406831",
            "Q131030",
            "Q186290",
            "Q1591095",
            "Q11348",
            "Q3150667",
            "Q474715",
            "Q379825",
            "Q379825",
            "Q192704",
            "Q44432",
            "Q44432",
            "Q319913",
            "Q12916",
            "Q12916",
            "Q2627460",
            "Q2627460",
            "Q190109",
            "Q83478",
            "Q18848",
            "Q379825",
            "Q844128",
            "Q2608202",
            "Q29539",
            "Q11465",
            "Q176737",
            "Q176737",
            "Q176737",
            "Q1413083",
            "Q1759756",
            "Q900231",
            "Q39297",
            "Q39297",
            "Q39552",
            "Q39297",
            "Q1948412",
            "Q3554818",
            "Q21199",
            "Q12916",
            "Q168698",
            "Q50701",
            "Q11053",
            "Q12916",
            "Q12916",
            "Q12916",
            "Q12503",
            "Q12503",
            "Q176623",
            "Q10290214",
            "Q10290214",
            "Q505735",
            "Q1057607",
            "Q11471",
            "Q1057607",
            "Q5227327",
            "Q6901742",
            "Q159375",
            "Q2858846",
            "Q1134404",
            "Q12916",
            "Q4440864",
            "Q838611",
            "Q44946",
            "Q173817",
            "Q12916",
            "Q21199",
            "Q12916",
            "Q190056",
            "Q10290214",
            "Q10290214",
            "Q506041",
            "Q2858846"};

    @Test
    public void testGetAliases() throws Exception {
        for (String qid : qIds) {
            Path file = Paths.get(File.createTempFile("temp", Long.toString(System.nanoTime())).getPath());
            List<String> aliases = WikidataInterface.getAliases(qid);
            aliases = aliases.stream().map(a -> "\"" + a + "\"").collect(Collectors.toList());
            Files.write(file, aliases, Charset.forName("UTF-8"));
        }
    }

    @Test
    public void testGetEntities() throws Exception {
        final ArrayList<String> expected = Lists.newArrayList("Q12916");
        Assert.assertEquals(expected.get(0), WikidataInterface.getEntities("real number").get(0));
    }
}