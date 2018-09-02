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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.FormazioniGazzettaLocal;
import it.zeze.fanta.service.definition.ejb.FormazioniGazzettaRemote;
import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniGazzetta;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniGazzettaId;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;
import it.zeze.util.FileFormazioneGazzettaComparator;

@Stateless
@LocalBean
public class FormazioniGazzettaEJB implements FormazioniGazzettaLocal, FormazioniGazzettaRemote {

	private static final Logger log = LogManager.getLogger(FormazioniGazzettaEJB.class);
	
	private static final String SELECT_BY_ID_GIOCATORE_ID_GIORNATA = "select probabiliFormazioniGazzetta from ProbabiliFormazioniGazzetta probabiliFormazioniGazzetta where probabiliFormazioniGazzetta.id.idGiocatore=:idGiocatore and probabiliFormazioniGazzetta.id.idGiornata=:idGiornata";
	private static final String SELECT_COUNT_ID_GIORNATA = "select count(probabiliFormazioniGazzetta.id.idGiornata) from ProbabiliFormazioniGazzetta probabiliFormazioniGazzetta where probabiliFormazioniGazzetta.id.idGiornata=:idGiornata";

	private static final String DELETE_BY_ID_GIORNATA = "delete from ProbabiliFormazioniGazzetta probabiliFormazioniGazzetta where probabiliFormazioniGazzetta.id.idGiornata=:idGiornata";

	@EJB(name = "DBManager")
	private DBManager dbManager;

	@EJB(name = "SquadreEJB")
	private SquadreLocal squadreEJB;

	@EJB(name = "GiornateEJB")
	private GiornateLocal giornateEJB;

	@EJB(name = "GiocatoriEJB")
	private GiocatoriLocal giocatoriEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		log.info("unmashallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String nomeFileFormazioneGazzetta = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_PROB_FORMAZIONI_GAZZETTA);
		String filtroFile = StringUtils.substringBefore(nomeFileFormazioneGazzetta, "{giornata}");
		Collection<File> collFiles = FileUtils.listFiles(new File(rootHTMLFiles), FileFilterUtils.prefixFileFilter(filtroFile), FileFilterUtils.falseFileFilter());
		List<File> listFile = new ArrayList<File>(collFiles);
		Collections.sort(listFile, new FileFormazioneGazzettaComparator());
		Iterator<File> itFile = listFile.iterator();
		File currentFile;
		while (itFile.hasNext()) {
			currentFile = itFile.next();
			String currentGiornataFile;
			try {
				currentGiornataFile = HtmlCleanerUtil.getListOfElementsByAttributeFromFile(currentFile.getAbsolutePath(), "class", "num-day").get(0).getText().toString();
				String currentGiornata = StringUtils.substringBefore(currentGiornataFile.trim().toLowerCase(), "^");
				String currentDataGiornata = StringUtils.substringAfter(currentGiornataFile.trim(), "^ GIORNATA - ").trim();
				log.info("Probabili formazioni FG per la " + "[" + currentGiornata + "]a giornata data giornata [" + currentDataGiornata + "]");
				unmarshallAndSaveSingleHtmlFile(currentFile, currentDataGiornata);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPatherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				// Questo perche' nel nuovo HTML NON trova
				// currentFile.getAbsolutePath(), "class", "num-day").get(0)
				// Nuovo HTML gazzetta 2014
				List<TagNode> listNode;
				try {
					listNode = HtmlCleanerUtil.getListOfElementsByXPathFromFile(currentFile.getAbsolutePath(), "//*[@id='calcio']/section[1]/section[3]/div[1]/div/div[1]/h3");
					
					String currentGiornataNew = listNode.get(0).getText().toString();
					// 1a giornata
					currentGiornataNew = StringUtils.substringBefore(currentGiornataNew.trim().toLowerCase(), "a giornata".toLowerCase());
					log.info("Probabili formazioni FG NEW 2014 per la " + "[" + currentGiornataNew + "]a giornata stagione [" + stagione + "]");
					unmarshallAndSaveSingleHtmlFileNew2014_2015(currentFile, currentGiornataNew, stagione);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (XPatherException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		log.info("unmashallAndSaveFromHtmlFile, uscito");
	}

	private void unmarshallAndSaveSingleHtmlFile(File fileToElaborate, String currentDataGiornata) {
		String path = fileToElaborate.getAbsolutePath();
		log.info("unmarshallAndSaveFromHtmlFile, entrato per elaborare il file [" + path + "]");
		try {
			List<TagNode> listMatchsIn = HtmlCleanerUtil.getListOfElementsByXPathFromFile(path, "//ul[@class='left campo']");
			List<TagNode> listMatchsOut = HtmlCleanerUtil.getListOfElementsByXPathFromFile(path, "//ul[@class='right campo']");
			TagNode tagNomeSquadra;
			String nomeSquadra;
			TagNode currentMatch;
			List<TagNode> listPlayersName;
			TagNode currentGiocatoreNode;
			String currentNomeGiocatore;
			// Cancello vecchie formazioni
			int idGiornata = giornateEJB.getIdGiornata(currentDataGiornata);
			deleteByIdGiornata(idGiornata);
			Giornate currentGiornata = giornateEJB.getGiornata(currentDataGiornata);
			String stagione;
			if (currentGiornata != null) {
				stagione = currentGiornata.getStagione();
			} else {
				throw new Exception("Impossibile recuperare la stagione per [" + currentDataGiornata + "]");
			}

			boolean noLike = false;
			// Squadre casa
			for (int i = 0; i < listMatchsIn.size(); i++) {
				currentMatch = listMatchsIn.get(i);
				tagNomeSquadra = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(currentMatch, "class", "title").get(0);
				tagNomeSquadra = tagNomeSquadra.findElementByName("span", false);
				nomeSquadra = tagNomeSquadra.getText().toString();
				listPlayersName = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(currentMatch, "class", "col2");
				Iterator<TagNode> itPlayer = listPlayersName.iterator();
				while (itPlayer.hasNext()) {
					currentGiocatoreNode = itPlayer.next();
					currentNomeGiocatore = currentGiocatoreNode.getText().toString();
					log.info("------ currentGiocatore IN: " + currentNomeGiocatore);
					Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(currentNomeGiocatore, nomeSquadra, stagione, noLike);
					if (giocatoriFormazione != null) {
						if (!HtmlCleanerUtil.nodeContainsAttribute(currentGiocatoreNode.getParent(), "class", "bottom")) {
							ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, true, false);
							ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
							instance.setId(instanceId);
							dbManager.persist(instance);
						} else {
							ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, false, true);
							ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
							instance.setId(instanceId);
							dbManager.persist(instance);
						}
					} else {
						log.error("Giocatore [" + currentNomeGiocatore + "] squadra [" + nomeSquadra + "] NON trovato");
					}
				}
			}
			// Squadre fuori casa
			for (int i = 0; i < listMatchsOut.size(); i++) {
				currentMatch = listMatchsOut.get(i);
				tagNomeSquadra = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(currentMatch, "class", "title").get(0);
				tagNomeSquadra = tagNomeSquadra.findElementByName("span", false);
				nomeSquadra = tagNomeSquadra.getText().toString();
				listPlayersName = HtmlCleanerUtil.getListOfElementsByAttributeFromElement(currentMatch, "class", "col2");
				Iterator<TagNode> itPlayer = listPlayersName.iterator();
				while (itPlayer.hasNext()) {
					currentGiocatoreNode = itPlayer.next();
					currentNomeGiocatore = currentGiocatoreNode.getText().toString();
					log.info("------ currentGiocatore OUT: " + currentNomeGiocatore);
					Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(currentNomeGiocatore, nomeSquadra, stagione, noLike);
					if (giocatoriFormazione != null) {
						if (!HtmlCleanerUtil.nodeContainsAttribute(currentGiocatoreNode.getParent(), "class", "bottom")) {
							ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, true, false);
							ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
							instance.setId(instanceId);
							dbManager.persist(instance);
						} else {
							ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, false, true);
							ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
							instance.setId(instanceId);
							dbManager.persist(instance);
						}
					} else {
						log.error("Giocatore [" + currentNomeGiocatore + "] squadra [" + nomeSquadra + "] NON trovato");
					}
				}
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
	}
	
	private void unmarshallAndSaveSingleHtmlFileNew2014_2015(File fileToElaborate, String giornata, String stagione) {
		String path = fileToElaborate.getAbsolutePath();
		log.info("unmarshallAndSaveSingleHtmlFileNew2014_2015, entrato per elaborare il file [" + path + "]");
		try {
			List<TagNode> listPlayersInNode = HtmlCleanerUtil.getListOfElementsByXPathFromFile(path, "//div[@class='MXXX-central-articles-main-column']");
			// Partite
			List<TagNode> listPlayersNameInNodeToClean = HtmlCleanerUtil.getListOfElementsByXPathFromElement(listPlayersInNode.get(0), "//div[@class='probabiliFormazioni']/div");
			List<TagNode> listPlayersNameInNode = new ArrayList<TagNode>();
			for (TagNode currentNode : listPlayersNameInNodeToClean){
				if (HtmlCleanerUtil.nodeContainsAttribute(currentNode, "class", "matchFieldContainer")){
					listPlayersNameInNode.add(currentNode);
				}
			}

			// Nomi squadre
			TagNode currentPartitaNode;
			List<TagNode> currentPartita;
			TagNode particaCasaNode;
			TagNode particaFuoriCasaNode;
			String currentNomeSquadraCasa;
			String currentNomeSquadraFuori;
			String currentNomeGiocatore;

			boolean noLike = false;
			// Cancello vecchie formazioni
			int idGiornata = giornateEJB.getIdGiornata(Integer.parseInt(giornata), stagione);
			deleteByIdGiornata(idGiornata);
			for (int i = 0; i < listPlayersNameInNode.size(); i++) {
				currentPartitaNode = listPlayersNameInNode.get(i);
				currentPartita = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPartitaNode, "//span[@class='teamName']/a");
				particaCasaNode = currentPartita.get(0);
				particaFuoriCasaNode = currentPartita.get(1);
				log.info("> Partita [" + (i + 1) + "]");
				// CASA
				currentNomeSquadraCasa = particaCasaNode.getText().toString().trim();
				log.info("-> Casa [" + currentNomeSquadraCasa + "]");
				// Giocatori Titolari
				log.info("--> Titolari");
				List<TagNode> listTitolari = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPartitaNode, "//li[@class='team-players-inner']");
				List<TagNode> listPlayersCasaTit = HtmlCleanerUtil.getListOfElementsByXPathFromElement(listTitolari.get(0), "//li/span[@class='team-player']");
				for (TagNode currentTag : listPlayersCasaTit) {
					if (currentTag.getParent().getAttributeByName("class") == null) {
						currentNomeGiocatore = currentTag.getText().toString().trim();
						if (currentNomeGiocatore != null && !StringUtils.deleteWhitespace(currentNomeGiocatore).isEmpty()) {
							Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(currentNomeGiocatore, currentNomeSquadraCasa, stagione, noLike);
							if (giocatoriFormazione != null) {
								ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, true, false);
								ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
								instance.setId(instanceId);
								dbManager.persist(instance);
							} else {
								log.error("Giocatore [" + currentNomeGiocatore + "] squadra [" + currentNomeSquadraCasa + "] NON trovato");
							}
						}
					}
				}
				// Giocatori Panchina
				log.info("--> Panchina");
				List<TagNode> listPanchina = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPartitaNode, "//div[@class='matchDetails']/div[@class='homeDetails']/p[1]");
				String panchinaToSplit = listPanchina.get(0).getText().toString();
				List<String> listGiocatoriPanchina = getGiocatoriPanchinaNew(panchinaToSplit);
				for (String current : listGiocatoriPanchina) {
					currentNomeGiocatore = current.trim();
					if (currentNomeGiocatore != null && !StringUtils.deleteWhitespace(currentNomeGiocatore).isEmpty()) {
						Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(currentNomeGiocatore, currentNomeSquadraCasa, stagione, noLike);
						if (giocatoriFormazione != null) {
							ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, false, true);
							ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
							instance.setId(instanceId);
							dbManager.persist(instance);
						} else {
							log.error("Giocatore [" + currentNomeGiocatore + "] squadra [" + currentNomeSquadraCasa + "] NON trovato");
						}
					}
				}
				// FUORI CASA
				currentNomeSquadraFuori = particaFuoriCasaNode.getText().toString();
				log.info("-> Fuori casa [" + currentNomeSquadraFuori + "]");
				// Giocatori Titolari
				log.info("--> Titolari");
				listPlayersCasaTit = HtmlCleanerUtil.getListOfElementsByXPathFromElement(listTitolari.get(1), "//li/span[@class='team-player']");
				for (TagNode currentTag : listPlayersCasaTit) {
					if (currentTag.getParent().getAttributeByName("class") == null) {
						currentNomeGiocatore = currentTag.getText().toString().trim();
						if (currentNomeGiocatore != null && !StringUtils.deleteWhitespace(currentNomeGiocatore).isEmpty()) {
							Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(currentNomeGiocatore, currentNomeSquadraFuori, stagione, noLike);
							if (giocatoriFormazione != null) {
								ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, true, false);
								ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
								instance.setId(instanceId);
								dbManager.persist(instance);
							} else {
								log.error("Giocatore [" + currentNomeGiocatore + "] squadra [" + currentNomeSquadraFuori + "] NON trovato");
							}
						}
					}
				}
				// Giocatori Panchina
				log.info("--> Panchina");
				listPanchina = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentPartitaNode, "//div[@class='matchDetails']/div[@class='awayDetails']/p[1]");
				panchinaToSplit = listPanchina.get(0).getText().toString();
				listGiocatoriPanchina = getGiocatoriPanchinaNew(panchinaToSplit);
				for (String current : listGiocatoriPanchina) {
					currentNomeGiocatore = current.trim();
					if (currentNomeGiocatore != null && !StringUtils.deleteWhitespace(currentNomeGiocatore).isEmpty()) {
						Giocatori giocatoriFormazione = giocatoriEJB.getGiocatoreByNomeSquadra(currentNomeGiocatore, currentNomeSquadraFuori, stagione, noLike);
						if (giocatoriFormazione != null) {
							ProbabiliFormazioniGazzettaId instanceId = new ProbabiliFormazioniGazzettaId(giocatoriFormazione.getId(), idGiornata, false, true);
							ProbabiliFormazioniGazzetta instance = new ProbabiliFormazioniGazzetta();
							instance.setId(instanceId);
							dbManager.persist(instance);
						} else {
							log.error("Giocatore [" + currentNomeGiocatore + "] squadra [" + currentNomeSquadraFuori + "] NON trovato");
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int deleteByIdGiornata(int idGiornata) {
		int rowDeleted = 0;
		Query query = dbManager.getEm().createQuery(DELETE_BY_ID_GIORNATA);
		query.setParameter("idGiornata", idGiornata);
		rowDeleted = (Integer) query.executeUpdate();
		log.info("Cancellate [" + rowDeleted + "] righe per la giornata [" + idGiornata + "]");
		return rowDeleted;
	}
	
	private static List<String> getGiocatoriPanchinaNew(String stringPanchinaHTMLToSplit){
		List<String> toReturn = new ArrayList<String>();
		System.out.println(stringPanchinaHTMLToSplit);
		// Rimuovo Panchina: 
		stringPanchinaHTMLToSplit = stringPanchinaHTMLToSplit.replace("Panchina: ", "");
		String[] arrayPanchina = stringPanchinaHTMLToSplit.split(",");
		for (String currentPanchina : arrayPanchina){
			currentPanchina = currentPanchina.trim().replaceAll("\\A(\\d*){3}\\s*", "");
			currentPanchina = currentPanchina.replace("-", "");
			currentPanchina = currentPanchina.replace("?", "'");
			currentPanchina = currentPanchina.trim().replaceAll("\\A(.){1}", "");
			toReturn.add(currentPanchina.trim());
		}
		return toReturn;	
	}
	
	@Override
	public ProbabiliFormazioniGazzetta selectByIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		ProbabiliFormazioniGazzetta toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_GIOCATORE_ID_GIORNATA);
		query.setParameter("idGiocatore", idGiocatore);
		query.setParameter("idGiornata", idGiornata);
		try {
			toReturn = (ProbabiliFormazioniGazzetta) query.getSingleResult();
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
