package com.ontology.mavenproject1;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.iri.impl.Main;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.reasoner.ReasonerOntologia;
import com.xml.ProducaoRebanhoData;
/**
 *
 * @author Pedro Ivo
 */
@Path("producao_rebanho_data")
public class ProducaoRebanhoDataResource {
    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public List<ProducaoRebanhoData> QueryControle(){
        //Variáveis
        List<ProducaoRebanhoData> producoes = new ArrayList<>();
        Model model;
        Query query;
        String queryString;
        //Carregando arquivo .owl
        FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
        //model = FileManager.get().loadModel(
        //        "C:\\Users\\Pedro Ivo\\Documents\\NetBeansProjects\\visualizacao_semantica_estrutural\\files\\ontology\\vaquinha_importacao.owl");
        
        ReasonerOntologia ro = new ReasonerOntologia();
        model = ro.getInfModel();
        
        //Montando a string de query em sparql
        //Query para obter as fazendas relacionadas com todos os produtores cadastrados
//Usando normal
//        queryString = "PREFIX ont: <http://www.semanticweb.org/pedroivo/ontologies/2016/6/vaquinha.owl#>\n" +
//"SELECT ?maior_controle (SUM(?total_leite) as ?prod_leite) (COUNT(?total_leite) as ?total_controles)\n" +
//"WHERE{\n" +
//"{\n" +
//"SELECT ?nome_rebanho ?nome_vaca (MAX(?data) as ?maior_controle) (MAX(?leite) as ?total_leite)\n" +
//"  WHERE{  ?rebanho ont:hasAnimal ?vaca.   ?vaca ont:hasProduction ?prod. ?prod ont:hasDairyControl ?controle.   ?controle ont:DataControleLeiteiro ?data. ?controle ont:ValorAcumuladoLeite ?leite. ?rebanho ont:NomeRebanho ?nome_rebanho.\n" +
//"?vaca ont:NomeVaca ?nome_vaca. FILTER regex(?nome_rebanho , \"herd1964\")}\n" +
//"GROUP BY ?nome_rebanho ?nome_vaca\n" +
//"}\n" +
//"}GROUP BY ?maior_controle ORDER BY ?maior_controle";

//Usando property chain
        queryString = "PREFIX ont: <http://www.semanticweb.org/pedroivo/ontologies/2016/6/vaquinha.owl#>\n" +
"SELECT ?maior_controle (SUM(?total_leite) as ?prod_leite) (COUNT(?total_leite) as ?total_controles)\n" +
"WHERE{\n" +
"{\n" +
"SELECT ?nome_rebanho ?nome_vaca (MAX(?data) as ?maior_controle) (MAX(?leite) as ?total_leite)\n" +
"  WHERE{  ?rebanho ont:hasAnimal ?vaca.    ?vaca ont:cowHasDairyControl ?controle.  ?controle ont:DataControleLeiteiro ?data. ?controle ont:ValorAcumuladoLeite ?leite. ?rebanho ont:NomeRebanho ?nome_rebanho.\n" +
"?vaca ont:NomeVaca ?nome_vaca. FILTER regex(?nome_rebanho , \"herd1964\")}\n" +
"  GROUP BY ?nome_rebanho ?nome_vaca\n" +
"}\n" +
"}GROUP BY ?maior_controle ORDER BY ?maior_controle";
        
        //Exibe todos os resultados obtidos com a query
        //Criação e execução da query para explorar os resultados em seguida
        query = QueryFactory.create(queryString);
        try(QueryExecution qexec = QueryExecutionFactory.create(query,model)){
            ResultSet results = qexec.execSelect();
            while( results.hasNext() ){
                QuerySolution soln = results.nextSolution();
                producoes.add(new ProducaoRebanhoData(
                                    (Float)soln.getLiteral("prod_leite").getValue(),
                                    ((XSDDateTime)soln.getLiteral("maior_controle").getValue()).asCalendar(),
                                    (Integer)soln.getLiteral("total_controles").getValue()
                ));
                
            }
        }
        
        return producoes;
    }
}
