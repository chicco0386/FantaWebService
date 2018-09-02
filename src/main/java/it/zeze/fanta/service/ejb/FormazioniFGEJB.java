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
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

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
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		log.info("unmashallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String nomeFileFormazioneFG = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_PROB_FORMAZIONI_FG);
		String filtroFile = StringUtils.substringBefore(nomeFileFormazioneFG, "{giornata}");
		Collection<File> collFiles = FileUtils.listFiles(new File(rootHTMLFiles), FileFilterUtils.prefixFileFilter(filtroFile), FileFilterUtils.falseFileFilter());
		List<File> listFile = new ArrayList<File>(collFiles);
		Collections.sort(listFile, new FileFormazioneFGComparator());
		Iterator<File> itFile = listFile.iterator();
		File currentFile;
		while (itFile.hasNext()) {
			currentFile = itFile.next();
			unmarshallAndSaveSingleHtmlFile(currentFile, stagione);
		}
		log.info("unmashallAndSaveFromHtmlFile, uscito");
	}
	
	private void unmarshallAndSaveSingleHtmlFile(File fileToElaborate, String stagione) {
		log.info("unmarshallAndSaveSingleHtmlFile, entrato per elaborare il file [" + fileToElaborate.getAbsolutePath() + "]");
		List<TagNode> listMatchsNode;
		try {
			listMatchsNode = HtmlCleanerUtil.getListOfElementsByAttributeFromFile(fileToElaborate.getAbsolutePath(), "class", "score-probabili");
			if (listMatchsNode != null && !listMatchsNode.isEmpty()) {
				// Vecchio HTML
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
						salvaGiocatoreFormazione(idGiornata, currentGiocatoreNome, squadraIn, currentGiocatoreRuolo, currentStagione, true);
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
						salvaGiocatoreFormazione(idGiornata, currentGiocatoreNome, squadraIn, currentGiocatoreRuolo, currentStagione, false);
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
						salvaGiocatoreFormazione(idGiornata, currentGiocatoreNome, squadraOut, currentGiocatoreRuolo, currentStagione, true);
					}
					// Prendo i giocatori fuori casa pachina
					listPlayersNameOutNode = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPlayerNode, "//div[@class='out']//div[@class='namesub']");
					for (int y = 0; y < listPlayersNameOutNode.size(); y++) {
						currentSinglePlayerNode = listPlayersNameOutNode.get(y);
						currentPlayerNomeNode = currentSinglePlayerNode.findElementByName("a", false);
						currentGiocatoreNome = currentPlayerNomeNode.getText().toString();
						log.info("----  giocatore fuori panc " + currentGiocatoreNome);
						currentPlayerRuoloNode = currentSinglePlayerNode.findElementByName("span", false);
						currentGiocatoreRuolo = currentPlayerRuoloNode.getText().toString();
						salvaGiocatoreFormazione(idGiornata, currentGiocatoreNome, squadraOut, currentGiocatoreRuolo, currentStagione, false);
					}
				}
			} else {
				// Nuovo HTML
				unmarshallAndSaveSingleHtmlFileNEW(fileToElaborate, stagione);
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
	
	private void unmarshallAndSaveSingleHtmlFileNEW(File fileToElaborate, String stagione) {
		log.info("unmarshallAndSaveSingleHtmlFileNEW, entrato per elaborare il file [" + fileToElaborate.getAbsolutePath() + "]");
		try {
			String currentGiornata = HtmlCleanerUtil.getAttributeValueFromFile(fileToElaborate.getAbsolutePath(), "id", "id_giornata", "value");
			int idGiornata = giornateEJB.getIdGiornata(Integer.valueOf(currentGiornata), stagione);
			deleteByIdGiornata(idGiornata);
			List<TagNode> listRootTagSquadre = HtmlCleanerUtil.getListOfElementsByAttributeFromFile(fileToElaborate.getAbsolutePath(), "id", "sqtab");
			if (listRootTagSquadre != null && !listRootTagSquadre.isEmpty()) {
				TagNode rootTag = listRootTagSquadre.get(0);
				List<TagNode> listPartite = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(rootTag, "//div[contains(@class,'tab-pane')]");
				// TagNode currentPartita = null;
				for (int i = 0; i < listPartite.size(); i++) {
					TagNode currentPartita = null;
					currentPartita = listPartite.get(i);
					unmarshallAndSaveSingleHtmlFileNEWPartita(currentPartita, stagione, idGiornata);
				}
			} else {
				log.info("Nessun rootTag contenente le partite!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("unmarshallAndSaveSingleHtmlFileNEW, uscito");
	}

	private void unmarshallAndSaveSingleHtmlFileNEWPartita(TagNode nodePartita, String stagione, int idGiornata) throws TransformerFactoryConfigurationError, Exception {
		try {
			if (nodePartita != null) {
				List<TagNode> listSquadreHome = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(nodePartita, "itemprop", "homeTeam");
				String nomeSquadra = listSquadreHome.get(0).getElementsByName("h3", true)[0].getText().toString();
				log.info("Squadra CASA [" + nomeSquadra + "]");
				// Recupero lista giocatori
				List<TagNode> listaGiocatori = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(nodePartita, "//div[contains(@class,'probbar')]");
				TagNode titolariCasa = listaGiocatori.get(0);
				List<TagNode> listTitolariCasa = unmarshallAndSaveGiocatoriCasaNEW(titolariCasa, true);
				log.info("Giocatori TITOLARI CASA [" + listTitolariCasa.size() + "]");
				String giocatoreNome = null;
				String giocatoreRuolo = null;
				for (TagNode current : listTitolariCasa) {
					giocatoreNome = getNomeGiocatore(current);
					giocatoreRuolo = getRuoloGiocatore(current);
					salvaGiocatoreFormazione(idGiornata, giocatoreNome, nomeSquadra, giocatoreRuolo, stagione, true);
					log.info(giocatoreNome + " - " + giocatoreRuolo);
				}
				TagNode panchinaCasa = listaGiocatori.get(2);
				List<TagNode> listPanchinaCasa = unmarshallAndSaveGiocatoriCasaNEW(panchinaCasa, false);
				log.info("Giocatori PANCHINA CASA [" + listPanchinaCasa.size() + "]");
				for (TagNode current : listPanchinaCasa) {
					giocatoreNome = getNomeGiocatore(current);
					giocatoreRuolo = getRuoloGiocatore(current);
					salvaGiocatoreFormazione(idGiornata, giocatoreNome, nomeSquadra, giocatoreRuolo, stagione, false);
					log.info(getNomeGiocatore(current) + " - " + getRuoloGiocatore(current));
				}

				List<TagNode> listSquadreFuori = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(nodePartita, "itemprop", "awayTeam");
				nomeSquadra = listSquadreFuori.get(0).getElementsByName("h3", true)[0].getText().toString();
				log.info("Squadra FUORI [" + nomeSquadra + "]");
				TagNode titolariFuori = listaGiocatori.get(1);
				List<TagNode> listTitolariFuori = unmarshallAndSaveGiocatoriFuoriNEW(titolariFuori, true);
				log.info("Giocatori TITOLARI FUORI [" + listTitolariFuori.size() + "]");
				for (TagNode current : listTitolariFuori) {
					giocatoreNome = getNomeGiocatore(current);
					giocatoreRuolo = getRuoloGiocatore(current);
					salvaGiocatoreFormazione(idGiornata, giocatoreNome, nomeSquadra, giocatoreRuolo, stagione, true);
					log.info(getNomeGiocatore(current) + " - " + getRuoloGiocatore(current));
				}
				TagNode panchinaFuori = listaGiocatori.get(3);
				List<TagNode> listPanchinaFuori = unmarshallAndSaveGiocatoriFuoriNEW(panchinaFuori, false);
				log.info("Giocatori PANCHINA FUORI [" + listPanchinaFuori.size() + "]");
				for (TagNode current : listPanchinaFuori) {
					giocatoreNome = getNomeGiocatore(current);
					giocatoreRuolo = getRuoloGiocatore(current);
					salvaGiocatoreFormazione(idGiornata, giocatoreNome, nomeSquadra, giocatoreRuolo, stagione, false);
					log.info(getNomeGiocatore(current) + " - " + getRuoloGiocatore(current));
				}

			} else {
				log.info("Nessun nodePartita!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<TagNode> unmarshallAndSaveGiocatoriCasaNEW(TagNode nodeGiocatoriCasa, boolean titolari) throws IOException, XPatherException {
		List<TagNode> listGiocatoriCasa = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(nodeGiocatoriCasa, "class", "pgroup lf");
		if (titolari && listGiocatoriCasa.size() > 11){
			listGiocatoriCasa = listGiocatoriCasa.subList(0, 11);
		}
		return listGiocatoriCasa;
	}

	private List<TagNode> unmarshallAndSaveGiocatoriFuoriNEW(TagNode nodeGiocatoriCasa, boolean titolari) throws IOException, XPatherException {
		List<TagNode> listGiocatoriFuori = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(nodeGiocatoriCasa, "class", "pgroup rt");
		if (titolari && listGiocatoriFuori.size() > 11){
			listGiocatoriFuori = listGiocatoriFuori.subList(0, 11);
		}
		return listGiocatoriFuori;
	}
	
	private String getRuoloGiocatore(TagNode nodeGiocatore) throws XPathExpressionException, IOException, XPatherException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		String toReturn = null;
		List<TagNode> list = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(nodeGiocatore, "//span[contains(@class,'role')]");
		if (list != null && !list.isEmpty()) {
			TagNode node = list.get(0);
			toReturn = node.getText().toString().trim();
		}
		return toReturn;
	}

	private String getNomeGiocatore(TagNode nodeGiocatore) throws XPathExpressionException, IOException, XPatherException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		String toReturn = null;
		List<TagNode> list = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(nodeGiocatore, "//div[contains(@class,'pname2')]/a");
		if (list != null && !list.isEmpty()) {
			TagNode node = list.get(0);
			toReturn = node.getText().toString().trim();
		}
		return toReturn;
	}
	
	private void salvaGiocatoreFormazione(int idGiornata, String giocatoreNome, String squadra, String giocatoreRuolo, String stagione, boolean titolare) {
		Giocatori giocatoriFormazione = getGiocatoreFormazione(giocatoreNome, squadra, giocatoreRuolo, stagione);
		ProbabiliFormazioniFgId instanceId = new ProbabiliFormazioniFgId(giocatoriFormazione.getId(), idGiornata, titolare, !titolare);
		ProbabiliFormazioniFg instance = new ProbabiliFormazioniFg();
		instance.setId(instanceId);
		dbManager.persist(instance);
	}
	
	private int deleteByIdGiornata(int idGiornata) {
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
	
	@Override
	public ProbabiliFormazioniFg selectByIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		ProbabiliFormazioniFg toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_GIOCATORE_ID_GIORNATA);
		query.setParameter("idGiocatore", idGiocatore);
		query.setParameter("idGiornata", idGiornata);
		try {
			toReturn = (ProbabiliFormazioniFg) query.getSingleResult();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con idGiocatore [" + idGiocatore + "] e idGiornata [" + idGiornata + "]");
		}
		return toReturn;
	}

	private boolean probFormGiaInserita(int idGiornata) {
		boolean giaInserita = false;
		Query query = dbManager.getEm().createQuery(SELECT_COUNT_ID_GIORNATA);
		query.setParameter("idGiornata", idGiornata);
		long count = (Long) query.getSingleResult();
		if (count > 0) {
			giaInserita = true;
		}
		return giaInserita;
	}
	
}
