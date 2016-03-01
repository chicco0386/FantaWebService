package it.zeze.fanta.service.ejb;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fanta.service.definition.ejb.StatisticheLocal;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.Statistiche;
import it.zeze.fantaformazioneweb.entity.StatisticheId;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;

@Stateless
@LocalBean
public class StatisticheEJB implements StatisticheLocal {
	
	private static final Logger log = LogManager.getLogger(StatisticheEJB.class);
	
	private static final String SELECT_BY_ID_GIOCATORE_ID_GIORNATE = "select statistiche from Statistiche statistiche where statistiche.id.idGiocatore=:idGiocatore and statistiche.id.idGiornata=:idGiornata";
	private static final String SELECT_BY_ID_GIOCATORE_STAGIONE = "select statistiche from Statistiche statistiche, Giornate gior where statistiche.id.idGiocatore=:idGiocatore and statistiche.id.idGiornata=gior.id AND gior.stagione = :stagione";
	private static final String SELECT_BY_ID_GIOCATORE = "select statistiche from Statistiche statistiche where statistiche.id.idGiocatore=:idGiocatore";
	private static final String SELECT_COUNT_BY_ID_GIORNATA = "select count(statistiche.id.idGiornata) from Statistiche statistiche where statistiche.id.idGiornata=:idGiornata";
	
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
		log.info("unmarshallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String nomeFileSquadre = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_STATISTICHE_G);
		// Per tutte le giornate presenti su DB controllo se esiste il relativo
		// file e lo elaboro
		String pathCompletoFileSquadre;
		List<Giornate> listaGiornate = giornateEJB.getGiornateAll();
		Giornate currentGiornata;
		File currentFileGiornata;
		for (int i = 0; i < listaGiornate.size(); i++) {
			currentGiornata = listaGiornate.get(i);
			pathCompletoFileSquadre = rootHTMLFiles + createNomeFileGiornata(nomeFileSquadre, String.valueOf(currentGiornata.getNumeroGiornata()));
			currentFileGiornata = new File(pathCompletoFileSquadre);
			if (currentFileGiornata.exists()) {
				try {
					unmarshallAndSaveFromHtmlFile(currentFileGiornata, String.valueOf(currentGiornata.getNumeroGiornata()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XPatherException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		log.info("unmarshallAndSaveFromHtmlFile, uscito");
	}
	
	private String createNomeFileGiornata(String nomeFile, String numGiornata) {
		String fileGiornataToReturn = "";
		fileGiornataToReturn = StringUtils.replace(nomeFile, Constants.STRING_TO_REPLACE_NOME_FILE_GIORNATE, numGiornata);
		return fileGiornataToReturn;
	}

	private void unmarshallAndSaveFromHtmlFile(File fileGiornata, String numGiornata) throws IOException, XPatherException {
		List<TagNode> listaStagioni = HtmlCleanerUtil.getListOfElementsByXPathFromFile(fileGiornata.getAbsolutePath(), "//div[@class='redazione']/ul/li[@class='here']");
		String currentStagioneFile = listaStagioni.get(0).getText().toString().trim();
		String currentStagione = StringUtils.substringAfter(currentStagioneFile.trim(), "Stag. ");
		currentStagione = giornateEJB.getStagione(currentStagione);
		log.info("Elaboro statistiche della giornata [" + numGiornata + "] della stagione [" + currentStagione + "]");
		List<TagNode> listaTabelleVotiPerSquadra = HtmlCleanerUtil.getListOfElementsByXPathFromFile(fileGiornata.getAbsolutePath(), "//div[@id='allvotes']");
		TagNode currentNodeSquadra;
		String currentNomeSquadra;
		List<TagNode> listaGiocatori;
		TagNode currentNodeGiocatore;
		TagNode currentNodeGiocatoreNome;
		String currentGiocatoreNome;
		TagNode currentNodeGiocatoreRuolo;
		String currentGiocatoreRuolo;
		List<TagNode> listaColonneGiocatore;
		TagNode currentColonnaGiocatore;
		int currentEspulso;
		int currentAmmonito;
		int currentGoalFatto;
		int currentGoalSuRigore;
		int currentGoalSubito;
		int currentRigoreParato;
		int currentRigoreSbagliato;
		int currentAutorete;
		int currentAssist;
		List<TagNode> listaNodeMedieVoti;
		TagNode currentNodeMediaVoto;
		BigDecimal currentMediaVoto;
		List<TagNode> listaNodeMedieFantaVoti;
		TagNode currentNodeMediaFantaVoto;
		BigDecimal currentMediaFantaVoto;
		boolean noLike = true;
		int idGiornata = giornateEJB.getIdGiornata(Integer.valueOf(numGiornata), currentStagione);
		if (!statGiaInserite(idGiornata)) {
			Giocatori currentGiocatoreDB;
			for (int i = 0; i < listaTabelleVotiPerSquadra.size(); i++) {
				currentNodeSquadra = listaTabelleVotiPerSquadra.get(i);
				// Recupero il nome della squadra
				currentNodeSquadra = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentNodeSquadra, "/div[@id]").get(0);
				currentNomeSquadra = currentNodeSquadra.getAttributeByName("id");

				listaGiocatori = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentNodeSquadra, "/table/tbody/tr[@class='P']");
				int numColonnaPartenza = 2;
				for (int y = 0; y < listaGiocatori.size(); y++) {
					numColonnaPartenza = 2;
					currentEspulso = 0;
					currentAmmonito = 0;
					currentMediaVoto = new BigDecimal(0);
					currentMediaFantaVoto = new BigDecimal(0);
					currentNodeGiocatore = listaGiocatori.get(y);
					currentNodeGiocatoreNome = currentNodeGiocatore.findElementByAttValue("class", "n", false, false);
					currentNodeGiocatoreNome = currentNodeGiocatoreNome.findElementByName("a", false);
					currentGiocatoreNome = currentNodeGiocatoreNome.getText().toString();
					currentNodeGiocatoreRuolo = currentNodeGiocatore.findElementByAttValue("class", "r", false, false);
					currentGiocatoreRuolo = currentNodeGiocatoreRuolo.getText().toString();
					// Salva solo i giocatori non gli allenatori (ruolo = M)
					if (!currentGiocatoreRuolo.equalsIgnoreCase("M")) {
						listaColonneGiocatore = currentNodeGiocatore.getElementListByName("td", true);
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						currentMediaVoto = new BigDecimal(currentColonnaGiocatore.getText().toString().replace(",", "."));
						if (HtmlCleanerUtil.nodeContainsAttribute(currentColonnaGiocatore, "class", "vesp")) {
							currentEspulso = currentEspulso + 1;
						} else if (HtmlCleanerUtil.nodeContainsAttribute(currentColonnaGiocatore, "class", "vamm")) {
							currentAmmonito = currentAmmonito + 1;
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentGoalFatto = 0;
						} else {
							currentGoalFatto = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentGoalSuRigore = 0;
						} else {
							currentGoalSuRigore = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentGoalSubito = 0;
						} else {
							currentGoalSubito = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentRigoreParato = 0;
						} else {
							currentRigoreParato = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentRigoreSbagliato = 0;
						} else {
							currentRigoreSbagliato = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentAutorete = 0;
						} else {
							currentAutorete = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}
						currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
						if (currentColonnaGiocatore.getText().toString().contains("-")) {
							currentAssist = 0;
						} else {
							currentAssist = Integer.parseInt(currentColonnaGiocatore.getText().toString());
						}

						// Recupero il giocatore relativo su DB
						currentGiocatoreDB = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(currentGiocatoreNome, currentNomeSquadra, currentGiocatoreRuolo, currentStagione, noLike);
						if (currentGiocatoreDB == null) {
							log.warn("Giocatore [" + currentGiocatoreNome + "] [" + currentNomeSquadra + "] [" + currentGiocatoreRuolo + "] NON presente nel DB. Procedo con il suo inserimento");
							giocatoriEJB.insertOrUpdateGiocatore(currentNomeSquadra, currentGiocatoreNome, currentGiocatoreRuolo, currentStagione, noLike);
							currentGiocatoreDB = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(currentGiocatoreNome, currentNomeSquadra, currentGiocatoreRuolo, currentStagione, noLike);
						}
						log.info("Inserisco statistiche giocatore [" + currentGiocatoreNome + "] [" + currentNomeSquadra + "] [" + currentGiocatoreRuolo + "]");
						StatisticheId statisticheId = new StatisticheId();
						statisticheId.setIdGiocatore(currentGiocatoreDB.getId());
						statisticheId.setIdGiornata(idGiornata);
						statisticheId.setAmmonizioni(currentAmmonito);
						statisticheId.setEspulsioni(currentEspulso);
						statisticheId.setAssist(currentAssist);
						statisticheId.setAutoreti(currentAutorete);
						statisticheId.setGoalFatti(currentGoalFatto);
						statisticheId.setGoalRigore(currentGoalSuRigore);
						statisticheId.setGoalSubiti(currentGoalSubito);
						statisticheId.setMediaVoto(currentMediaVoto);
						currentMediaFantaVoto = calcolaFantaVoto(currentMediaVoto, currentAmmonito, currentEspulso, currentAssist, currentAutorete, currentGoalFatto, currentGoalSuRigore, currentRigoreSbagliato, currentGoalSubito, currentRigoreParato);
						statisticheId.setMediaVotoFm(currentMediaFantaVoto);
						statisticheId.setRigoriParati(currentRigoreParato);
						statisticheId.setRigoriSbagliati(currentRigoreSbagliato);

						Statistiche statisticheToInsert = new Statistiche();
						statisticheToInsert.setId(statisticheId);
						dbManager.persist(statisticheToInsert);
					}

				}
			}
		} else {
			log.info("Statistiche della giornata [" + numGiornata + "] gia' inserite");
		}
	}

	private BigDecimal calcolaFantaVoto(BigDecimal mediaVoto, int ammonito, int espulso, int assist, int autoreti, int goalFatti, int rigoreFatto, int rigoreSbagliato, int rigoreSubito, int rigoreParato) {
		BigDecimal fantaVoto = mediaVoto;
		if (ammonito > 0) {
			fantaVoto = fantaVoto.subtract(new BigDecimal(0.5));
		}
		if (espulso > 0) {
			fantaVoto = fantaVoto.subtract(BigDecimal.ONE);
		}
		fantaVoto = fantaVoto.add(new BigDecimal(assist));
		if (autoreti > 0) {
			fantaVoto = fantaVoto.subtract(new BigDecimal(autoreti * 2));
		}
		if (goalFatti > 0) {
			fantaVoto = fantaVoto.add(new BigDecimal(goalFatti * 3));
		}
		if (rigoreFatto > 0) {
			fantaVoto = fantaVoto.add(new BigDecimal(rigoreFatto * 3));
		}
		if (rigoreSbagliato > 0) {
			fantaVoto = fantaVoto.subtract(new BigDecimal(rigoreSbagliato * 3));
		}
		if (rigoreSubito > 0) {
			fantaVoto = fantaVoto.subtract(new BigDecimal(rigoreSubito * 1));
		}
		if (rigoreParato > 0) {
			fantaVoto = fantaVoto.add(new BigDecimal(rigoreParato * 3));
		}
		return fantaVoto;
	}

	private Statistiche getStatisticheIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		Statistiche toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_GIOCATORE_ID_GIORNATE);
		query.setParameter("idGiocatore", idGiocatore);
		if (idGiornata > 1) {
			query.setParameter("idGiornata", idGiornata - 1);
		} else if (idGiornata == 1) {
			query.setParameter("idGiornata", idGiornata);
		}
		try {
			toReturn = (Statistiche) query.getSingleResult();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con idGiocatore [" + idGiocatore + "] e idGiornata [" + idGiornata + "]");
		}
		return toReturn;
	}

	private List<Statistiche> getStatisticheIdGiocatore(int idGiocatore) {
		List<Statistiche> toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_GIOCATORE);
		query.setParameter("idGiocatore", idGiocatore);
		try {
			toReturn = (List<Statistiche>) query.getResultList();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con idGiocatore [" + idGiocatore + "]");
		}
		return toReturn;
	}

	private List<Statistiche> getStatisticheIdGiocatoreAndStagione(int idGiocatore, String stagione) {
		List<Statistiche> toReturn = null;
		String stagioneParse = giornateEJB.getStagione(stagione);
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_GIOCATORE_STAGIONE);
		query.setParameter("idGiocatore", idGiocatore);
		query.setParameter("stagione", stagioneParse);
		try {
			toReturn = (List<Statistiche>) query.getResultList();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con idGiocatore [" + idGiocatore + "] e stagione [" + stagioneParse + "]");
		}
		return toReturn;
	}

	private boolean statGiaInserite(int idGiornata) {
		boolean giaInserita = false;
		Query query = dbManager.getEm().createQuery(SELECT_COUNT_BY_ID_GIORNATA);
		query.setParameter("idGiornata", idGiornata);
		long count = (Long) query.getSingleResult();
		if (count > 0) {
			giaInserita = true;
		}
		return giaInserita;
	}
}
