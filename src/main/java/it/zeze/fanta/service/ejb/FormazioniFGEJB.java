package it.zeze.fanta.service.ejb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.FormazioniFGLocal;
import it.zeze.fanta.service.definition.ejb.FormazioniFGRemote;
import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniFg;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniFgId;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;
import it.zeze.util.FileFormazioneFGComparator;

@Stateless
@LocalBean
public class FormazioniFGEJB implements FormazioniFGLocal, FormazioniFGRemote {
	
	private static final Logger log = LogManager.getLogger(FormazioniFGEJB.class);
	
	private static final String SELECT_BY_ID_GIOCATORE_ID_GIORNATA = "select probabiliFormazioniFg from ProbabiliFormazioniFg probabiliFormazioniFg where probabiliFormazioniFg.id.idGiocatore=:idGiocatore and probabiliFormazioniFg.id.idGiornata=:idGiornata";
	private static final String SELECT_COUNT_ID_GIORNATA = "select count(probabiliFormazioniFg.id.idGiornata) from ProbabiliFormazioniFg probabiliFormazioniFg where probabiliFormazioniFg.id.idGiornata=:idGiornata";

	private static final String DELETE_BY_ID_GIORNATA = "delete from ProbabiliFormazioniFg probabiliFormazioniFg where probabiliFormazioniFg.id.idGiornata=:idGiornata";
		
	@EJB(name = "DBManager")
	private DBManager dbManager;
	
	@EJB(name="SquadreEJB")
	private SquadreLocal squadreEJB;
	
	@EJB(name="GiornateEJB")
	private GiornateLocal giornateEJB;
	
	@EJB(name="GiocatoriEJB")
	private GiocatoriLocal giocatoriEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		log.info("unmashallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String nomeFileFormazioneFG = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_PROB_FORMAZIONI_FG);
		String filtroFile = StringUtils.substringBefore(nomeFileFormazioneFG, "{giornata}");
		filtroFile = StringUtils.substringAfter(filtroFile, "/");
		Collection<File> collFiles = FileUtils.listFiles(new File(rootHTMLFiles), FileFilterUtils.prefixFileFilter(filtroFile), FileFilterUtils.falseFileFilter());
		List<File> listFile = new ArrayList<File>(collFiles);
		Collections.sort(listFile, new FileFormazioneFGComparator());
		Iterator<File> itFile = listFile.iterator();
		File currentFile;
		while (itFile.hasNext()) {
			currentFile = itFile.next();
			unmarshallAndSaveSingleHtmlFile(currentFile);
		}
		log.info("unmashallAndSaveFromHtmlFile, uscito");		
	}
	
	private void unmarshallAndSaveSingleHtmlFile(File fileToElaborate) {
		log.info("unmarshallAndSaveSingleHtmlFile, entrato per elaborare il file [" + fileToElaborate.getAbsolutePath() + "]");
		List<TagNode> listMatchsNode;
		try {
			listMatchsNode = HtmlCleanerUtil.getListOfElementsByAttributeFromFile(fileToElaborate.getAbsolutePath(), "class", "score-probabili");
			List<TagNode> listPlayersNode = HtmlCleanerUtil.getListOfElementsByAttributeFromFile(fileToElaborate.getAbsolutePath(), "class", "player");
			String currentGiornataFromFile = HtmlCleanerUtil.getListOfElementsByAttributeFromFile(fileToElaborate.getAbsolutePath(), "id", "ContentPlaceHolderElle_Labelgiornata").get(0).getText().toString();
			String currentGiornata = StringUtils.remove(currentGiornataFromFile.trim().toLowerCase(), " giornata");
			String currentStagione = HtmlCleanerUtil.getListOfElementsByXPathFromFile(fileToElaborate.getAbsolutePath(), "//div[@id='article']/h2").get(0).getText().toString();
			currentStagione = StringUtils.substringAfter(currentStagione.trim(), "Probabili Formazioni Serie A - ").trim();
			currentStagione = StringUtils.substringBefore(currentStagione, currentGiornataFromFile);
			currentStagione = giornateEJB.getStagione(currentStagione.trim());
			log.info("Probabili formazioni FG per la " + "[" + currentGiornata + "]a giornata e stagione [" + currentStagione + "]");
			TagNode currentMatchNode = null;
			String squadraIn = "";
			String squadraOut = "";
			List<TagNode> listPlayersNameInNode = null;
			List<TagNode> listPlayersNameOutNode = null;
			TagNode currentPlayerNode = null;
			TagNode currentSinglePlayerNode = null;
			TagNode currentPlayerNomeNode;
			TagNode currentPlayerRuoloNode;
			String currentGiocatoreNome;
			String currentGiocatoreRuolo;
			// Cancello vecchie formazioni
			int idGiornata = giornateEJB.getIdGiornata(Integer.valueOf(currentGiornata), currentStagione);
			deleteByIdGiornata(idGiornata);
			for (int i = 0; i < listMatchsNode.size(); i++) {
				currentMatchNode = listMatchsNode.get(i);
				squadraIn = currentMatchNode.findElementByAttValue("class", "team-in-p", false, true).getText().toString();
				squadraOut = currentMatchNode.findElementByAttValue("class", "team-out-p", false, true).getText().toString();

				currentPlayerNode = listPlayersNode.get(i);
				// Prendo i giocatori in casa titolari
				listPlayersNameInNode = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPlayerNode, "//div[@class='in']//div[@class='name']");
				for (int y = 0; y < listPlayersNameInNode.size(); y++) {
					currentSinglePlayerNode = listPlayersNameInNode.get(y);
					currentPlayerNomeNode = currentSinglePlayerNode.findElementByName("a", false);
					currentGiocatoreNome = currentPlayerNomeNode.getText().toString();
					log.info("----  giocatore casa tit " + currentGiocatoreNome);
					currentPlayerRuoloNode = currentSinglePlayerNode.findElementByName("span", false);
					currentGiocatoreRuolo = currentPlayerRuoloNode.getText().toString();
					Giocatori giocatoriFormazione = getGiocatoreFormazione(currentGiocatoreNome, squadraIn, currentGiocatoreRuolo, currentStagione);
					
					ProbabiliFormazioniFgId instanceId = new ProbabiliFormazioniFgId(giocatoriFormazione.getId(), idGiornata, true, false);
					ProbabiliFormazioniFg instance = new ProbabiliFormazioniFg();
					instance.setId(instanceId);
					dbManager.persist(instance);
				}
				// Prendo i giocatori in casa panchina
				listPlayersNameInNode = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPlayerNode, "//div[@class='in']//div[@class='namesub']");
				for (int y = 0; y < listPlayersNameInNode.size(); y++) {
					currentSinglePlayerNode = listPlayersNameInNode.get(y);
					currentPlayerNomeNode = currentSinglePlayerNode.findElementByName("a", false);
					currentGiocatoreNome = currentPlayerNomeNode.getText().toString();
					log.info("----  giocatore casa panc " + currentGiocatoreNome);
					currentPlayerRuoloNode = currentSinglePlayerNode.findElementByName("span", false);
					currentGiocatoreRuolo = currentPlayerRuoloNode.getText().toString();
					Giocatori giocatoriFormazione = getGiocatoreFormazione(currentGiocatoreNome, squadraIn, currentGiocatoreRuolo, currentStagione);
					
					ProbabiliFormazioniFgId instanceId = new ProbabiliFormazioniFgId(giocatoriFormazione.getId(), idGiornata, false, true);
					ProbabiliFormazioniFg instance = new ProbabiliFormazioniFg();
					instance.setId(instanceId);
					dbManager.persist(instance);
				}

				// Prendo i giocatori fuori casa
				listPlayersNameOutNode = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPlayerNode, "//div[@class='out']//div[@class='name']");
				for (int y = 0; y < listPlayersNameOutNode.size(); y++) {
					currentSinglePlayerNode = listPlayersNameOutNode.get(y);
					currentPlayerNomeNode = currentSinglePlayerNode.findElementByName("a", false);
					currentGiocatoreNome = currentPlayerNomeNode.getText().toString();
					log.info("----  giocatore fuori tit " + currentGiocatoreNome);
					currentPlayerRuoloNode = currentSinglePlayerNode.findElementByName("span", false);
					currentGiocatoreRuolo = currentPlayerRuoloNode.getText().toString();
					Giocatori giocatoriFormazione = getGiocatoreFormazione(currentGiocatoreNome, squadraOut, currentGiocatoreRuolo, currentStagione);
					
					ProbabiliFormazioniFgId instanceId = new ProbabiliFormazioniFgId(giocatoriFormazione.getId(), idGiornata, true, false);
					ProbabiliFormazioniFg instance = new ProbabiliFormazioniFg();
					instance.setId(instanceId);
					dbManager.persist(instance);
				}
				// Prendo i giocatori in casa pachina
				listPlayersNameOutNode = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPlayerNode, "//div[@class='out']//div[@class='namesub']");
				for (int y = 0; y < listPlayersNameOutNode.size(); y++) {
					currentSinglePlayerNode = listPlayersNameOutNode.get(y);
					currentPlayerNomeNode = currentSinglePlayerNode.findElementByName("a", false);
					currentGiocatoreNome = currentPlayerNomeNode.getText().toString();
					log.info("----  giocatore fuori panc " + currentGiocatoreNome);
					currentPlayerRuoloNode = currentSinglePlayerNode.findElementByName("span", false);
					currentGiocatoreRuolo = currentPlayerRuoloNode.getText().toString();
					Giocatori giocatoriFormazione = getGiocatoreFormazione(currentGiocatoreNome, squadraOut, currentGiocatoreRuolo, currentStagione);
					
					ProbabiliFormazioniFgId instanceId = new ProbabiliFormazioniFgId(giocatoriFormazione.getId(), idGiornata, false, true);
					ProbabiliFormazioniFg instance = new ProbabiliFormazioniFg();
					instance.setId(instanceId);
					dbManager.persist(instance);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("unmarshallAndSaveSingleHtmlFile, uscito");
	}
	
	public int deleteByIdGiornata(int idGiornata) {
		int rowDeleted = 0;
		Query query = dbManager.getEm().createQuery(DELETE_BY_ID_GIORNATA);
		query.setParameter("idGiornata", idGiornata);
		rowDeleted = (Integer) query.executeUpdate();
		log.info("Cancellate [" + rowDeleted + "] righe per la giornata [" + idGiornata + "]");
		return rowDeleted;
	}
	
	private Giocatori getGiocatoreFormazione(String giocatoreNome, String squadra, String ruolo, String stagione) {
		boolean noLike = false;
		Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(giocatoreNome, squadra, stagione, noLike);
		if (giocatoriFormazione == null) {
			giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(giocatoreNome, squadra, ruolo, stagione, noLike);
			if (giocatoriFormazione == null) {
				log.warn("Giocatore [" + giocatoreNome + "] squadra [" + squadra + "] ruolo [" + ruolo + "] stagione [" + stagione + "] NON trovato. Procedo con il suo inserimento");
				giocatoriEJB.insertOrUpdateGiocatore(squadra, giocatoreNome, ruolo, stagione, noLike);
				giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(giocatoreNome, squadra, ruolo, stagione, noLike);
			}
		}
		return giocatoriFormazione;
	}
	
}
