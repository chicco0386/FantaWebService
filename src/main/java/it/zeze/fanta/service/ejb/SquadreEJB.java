package it.zeze.fanta.service.ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fanta.service.definition.ejb.SquadreRemote;
import it.zeze.fantaformazioneweb.entity.Squadre;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;

@Stateless
@LocalBean
public class SquadreEJB implements SquadreLocal, SquadreRemote {

	private static final Logger log = LogManager.getLogger(SquadreEJB.class);

	private static final String QUERY_GET_SQUADRA_BY_NAME = "select squadre from Squadre squadre where squadre.nome=:nomeSquadra";
	private static final String QUERY_GET_SQUADRA_BY_ID = "select squadre from Squadre squadre where squadre.id=:id";

	@EJB(name = "DBManager")
	private DBManager dbManager;

	private Map<Integer, String> mapSquadre = new HashMap<Integer, String>();

	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		log.info("unmarshallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String nomeFileSquadre = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_SQUADRE);
		String pathCompletoFileSquadre = rootHTMLFiles + nomeFileSquadre;
		log.debug("Leggo il file HTML [" + pathCompletoFileSquadre + "]");
		List<TagNode> listNodeSquadre;
		try {
			listNodeSquadre = HtmlCleanerUtil.getListOfElementsByXPathFromFile(pathCompletoFileSquadre, "//div[@class='content']/table/tbody/tr/td[@class='a-left']/a");
			if (listNodeSquadre == null || listNodeSquadre.isEmpty()) {
				// Leggo squadre nuovo HTML
				listNodeSquadre = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromFile(pathCompletoFileSquadre, "//table/tbody/tr/td/a/span[contains(@class,'nteam')][1]");
			}
			TagNode currentNodeSquadra;
			String nomeSquadra;
			Squadre foundSquadra = null;
			for (int i = 0; i < listNodeSquadre.size(); i++) {
				currentNodeSquadra = listNodeSquadre.get(i);
				nomeSquadra = currentNodeSquadra.getText().toString().trim();
				if (!nomeSquadra.isEmpty()) {
					foundSquadra = getSquadraByNome(nomeSquadra);
					if (foundSquadra != null && foundSquadra.getId() > 0) {
						log.info("Squadra [" + nomeSquadra + "] gia' inserita");
					} else {
						Squadre squadraToInsert = new Squadre();
						squadraToInsert.setNome(nomeSquadra.toUpperCase());
						dbManager.getEm().persist(squadraToInsert);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("unmarshallAndSaveFromHtmlFile, uscito");
	}

	@Override
	public void initMappaSquadre() {
		mapSquadre.clear();
		List<Squadre> result = getSquadreAll();
		for (int i = 0; i < result.size(); i++) {
			mapSquadre.put(result.get(i).getId(), result.get(i).getNome());
		}
	}

	@Override
	public Squadre getSquadraFromMapByNome(String nomeSquadraToSearch) {
		Squadre squadraToReturn = new Squadre();
		if (mapSquadre == null || mapSquadre.isEmpty()) {
			initMappaSquadre();
		}
		Iterator<Entry<Integer, String>> it = mapSquadre.entrySet().iterator();
		boolean trovato = false;
		Entry<Integer, String> currentEntity;
		while (it.hasNext() && !trovato) {
			currentEntity = it.next();
			if (currentEntity.getValue().equalsIgnoreCase(nomeSquadraToSearch.trim())) {
				trovato = true;
				squadraToReturn.setId(currentEntity.getKey());
				squadraToReturn.setNome(currentEntity.getValue());
			}
		}
		// Ricerco per LIKE per nuovo HTML
		if (!trovato){
			it = mapSquadre.entrySet().iterator();
			while (it.hasNext() && !trovato) {
				currentEntity = it.next();
				if (currentEntity.getValue().toLowerCase().startsWith(nomeSquadraToSearch.trim().toLowerCase())) {
					trovato = true;
					squadraToReturn.setId(currentEntity.getKey());
					squadraToReturn.setNome(currentEntity.getValue());
				}
			}
		}
		return squadraToReturn;
	}

	@Override
	public Squadre getSquadraByNome(String nomeSquadraToSearch) {
		nomeSquadraToSearch = nomeSquadraToSearch.toUpperCase();
		EntityManager em = dbManager.getEm();
		Query query = em.createQuery(QUERY_GET_SQUADRA_BY_NAME);
		query.setParameter("nomeSquadra", nomeSquadraToSearch.trim().toUpperCase());
		try {
			return (Squadre) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Squadre getSquadraById(int idSquadra) {
		EntityManager em = dbManager.getEm();
		Query query = em.createQuery(QUERY_GET_SQUADRA_BY_ID);
		query.setParameter("id", idSquadra);
		try {
			return (Squadre) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<Squadre> getSquadreAll() {
		List<Squadre> toReturn = new ArrayList<Squadre>();
		String qryString = "SELECT s FROM Squadre s";
		Query query = dbManager.getEm().createQuery(qryString);
		toReturn = query.getResultList();
		return toReturn;
	}

}
