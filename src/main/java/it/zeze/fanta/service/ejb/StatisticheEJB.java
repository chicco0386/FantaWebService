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
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

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
import it.zeze.fanta.service.definition.ejb.StatisticheRemote;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.Statistiche;
import it.zeze.fantaformazioneweb.entity.StatisticheId;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;

@Stateless
@LocalBean
public class StatisticheEJB implements StatisticheLocal, StatisticheRemote {
	
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
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
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
			pathCompletoFileSquadre = rootHTMLFiles + createNomeFileGiornata(nomeFileSquadre, String.valueOf(currentGiornata.getId()));
			currentFileGiornata = new File(pathCompletoFileSquadre);
			if (currentFileGiornata.exists()) {
				try {
					unmarshallAndSaveFromHtmlFile(currentFileGiornata, String.valueOf(currentGiornata.getId()), stagione);
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
			}
		}
		log.info("unmarshallAndSaveFromHtmlFile, uscito");
	}
	
	private String createNomeFileGiornata(String nomeFile, String numGiornata) {
		String fileGiornataToReturn = "";
		fileGiornataToReturn = StringUtils.replace(nomeFile, Constants.STRING_TO_REPLACE_NOME_FILE_GIORNATE, numGiornata);
		return fileGiornataToReturn;
	}

	private void unmarshallAndSaveFromHtmlFile(File fileGiornata, String numGiornata, String stagione) throws IOException, XPatherException, XPathExpressionException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		stagione = giornateEJB.getStagione(stagione);
		int idGiornata = giornateEJB.getIdGiornata(Integer.valueOf(numGiornata), stagione);
		if (!statGiaInserite(idGiornata)) {
			try {
				// Vecchio HTML
				log.info("Elaboro statistiche della giornata [" + numGiornata + "] della stagione [" + stagione + "]");
				List<TagNode> listaTabelleVotiPerSquadra = HtmlCleanerUtil.getListOfElementsByXPathFromFile(fileGiornata.getAbsolutePath(), "//div[@id='allvotes']");
				if (listaTabelleVotiPerSquadra == null || listaTabelleVotiPerSquadra.isEmpty()){
					throw new Exception("Rilancio per fare parsing con nuovo HTML");
				}
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
				String currentGoalFatto;
				String currentGoalSuRigore;
				String currentGoalSubito;
				String currentRigoreParato;
				String currentRigoreSbagliato;
				String currentAutorete;
				String currentAssist;
				String currentMediaVoto;

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
							currentMediaVoto = currentColonnaGiocatore.getText().toString();
							if (HtmlCleanerUtil.nodeContainsAttribute(currentColonnaGiocatore, "class", "vesp")) {
								currentEspulso = currentEspulso + 1;
							} else if (HtmlCleanerUtil.nodeContainsAttribute(currentColonnaGiocatore, "class", "vamm")) {
								currentAmmonito = currentAmmonito + 1;
							}
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentGoalFatto = currentColonnaGiocatore.getText().toString();
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentGoalSuRigore = currentColonnaGiocatore.getText().toString();
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentGoalSubito = currentColonnaGiocatore.getText().toString();
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentRigoreParato = currentColonnaGiocatore.getText().toString();
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentRigoreSbagliato = currentColonnaGiocatore.getText().toString();
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentAutorete = currentColonnaGiocatore.getText().toString();
							currentColonnaGiocatore = listaColonneGiocatore.get(numColonnaPartenza++);
							currentAssist = currentColonnaGiocatore.getText().toString();

							saveVotoFromHtmlFile(idGiornata, stagione, currentGiocatoreNome, currentNomeSquadra, currentGiocatoreRuolo, currentAmmonito, currentEspulso, currentAssist, currentAutorete, currentGoalFatto, currentGoalSuRigore, currentGoalSubito, currentMediaVoto, currentRigoreParato, currentRigoreSbagliato);
						}

					}
				}

			} catch (Exception e) {
				// Vecchio HTML
				log.info("Nuovo HTML");
				unmarshallAndSaveFromHtmlFileNEW(fileGiornata, numGiornata, stagione);
			}
		} else {
			log.info("Statistiche della giornata [" + numGiornata + "] gia' inserite");
		}
	}
	
	private void unmarshallAndSaveFromHtmlFileNEW(File fileGiornata, String numGiornata, String stagione) throws IOException, XPatherException, XPathExpressionException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		int idGiornata = giornateEJB.getIdGiornata(Integer.valueOf(numGiornata), stagione);
		log.info("Elaboro statistiche della giornata [" + numGiornata + "] della stagione [" + stagione + "]");
		List<TagNode> listTabelleSquadre = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromFile(fileGiornata.getAbsolutePath(), "//div[contains(@class,'tbvoti')]//table");
		for (TagNode currentNodeVotiSquadra : listTabelleSquadre) {
			salvaVotiSquadraHtmlNEW(idGiornata, stagione, currentNodeVotiSquadra);
		}
	}

	private void salvaVotiSquadraHtmlNEW(int idGiornata, String currentStagione, TagNode currentNodeVotiSquadra) throws IOException, XPatherException, XPathExpressionException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		String currentNomeSquadra;
		String currentGiocatoreNome;
		String currentGiocatoreRuolo;
		String currentAssistString;
		String currentAutoreteString;
		String currentGoalFattoString;
		String currentGoalSuRigoreString;
		String currentGoalSubitoString;
		String currentRigoreParatoString;
		String currentRigoreSbagliatoString;
		String mediaVotoString;

		int currentEspulso;
		int currentAmmonito;

		currentNomeSquadra = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentNodeVotiSquadra, "//table//thead//h3").get(0).getText().toString();
		log.info("Squadra [" + currentNomeSquadra + "]");
		List<TagNode> listaGiocatori = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentNodeVotiSquadra, "//tbody/tr");
		TagNode currentNodeVoto;
		List<TagNode> listaControlloCartellini;
		int tdIndexVotiMI;
		for (TagNode currentGiocatore : listaGiocatori) {
			tdIndexVotiMI = 10;
			currentAmmonito = 0;
			currentEspulso = 0;
			currentGiocatoreNome = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[@class='pname']//a").get(0).getText().toString();
			currentGiocatoreRuolo = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(currentGiocatore, "//span[contains(@class,'role')]").get(0).getText().toString();
			currentNodeVoto = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0);
			listaControlloCartellini = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(currentGiocatore, "//span[contains(@class,'trn-rr')]");
			if (listaControlloCartellini == null || !listaControlloCartellini.isEmpty()) {
				currentEspulso = currentEspulso + 1;
			}
			listaControlloCartellini = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(currentGiocatore, "//span[contains(@class,'trn-ry')]");
			if (listaControlloCartellini == null || !listaControlloCartellini.isEmpty()) {
				currentAmmonito = currentAmmonito + 1;
			}
			mediaVotoString = currentNodeVoto.getText().toString();
			currentGoalFattoString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();
			currentGoalSuRigoreString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();
			currentGoalSubitoString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();
			currentRigoreParatoString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();
			currentRigoreSbagliatoString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();
			currentAutoreteString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();
			currentAssistString = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentGiocatore, "//td[" + tdIndexVotiMI++ + "]").get(0).getText().toString();

			if (!currentGiocatoreRuolo.equalsIgnoreCase("ALL")) {
				saveVotoFromHtmlFile(idGiornata, currentStagione, currentGiocatoreNome, currentNomeSquadra, currentGiocatoreRuolo, currentAmmonito, currentEspulso, currentAssistString, currentAutoreteString, currentGoalFattoString, currentGoalSuRigoreString, currentGoalSubitoString, mediaVotoString, currentRigoreParatoString, currentRigoreSbagliatoString);
			}

		}
	}
	
	private void saveVotoFromHtmlFile(int idGiornata, String currentStagione, String currentGiocatoreNome, String currentNomeSquadra, String currentGiocatoreRuolo, int currentAmmonito, int currentEspulso, String currentAssist, String currentAutorete, String currentGoalFatto, String currentGoalSuRigore, String currentGoalSubito, String currentMediaVoto, String currentRigoreParato, String currentRigoreSbagliato) {
		// Recupero il giocatore relativo su DB
		boolean noLike = true;
		Giocatori currentGiocatoreDB = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(currentGiocatoreNome, currentNomeSquadra, currentGiocatoreRuolo, currentStagione, noLike);
		int espulso = currentEspulso;
		int ammonito = currentAmmonito;
		int goalFatto;
		int goalSuRigore;
		int goalSubito;
		int rigoreParato;
		int rigoreSbagliato;
		int autorete;
		int assist;
		BigDecimal mediaVoto;
		mediaVoto = new BigDecimal(currentMediaVoto.replace(",", "."));
		if (currentGoalFatto.contains("-")) {
			goalFatto = 0;
		} else {
			goalFatto = Integer.parseInt(currentGoalFatto);
		}
		if (currentGoalSuRigore.contains("-")) {
			goalSuRigore = 0;
		} else {
			goalSuRigore = Integer.parseInt(currentGoalSuRigore);
		}
		if (currentGoalSubito.contains("-")) {
			goalSubito = 0;
		} else {
			goalSubito = Integer.parseInt(currentGoalSubito);
		}
		if (currentRigoreParato.contains("-")) {
			rigoreParato = 0;
		} else {
			rigoreParato = Integer.parseInt(currentRigoreParato);
		}
		if (currentRigoreSbagliato.contains("-")) {
			rigoreSbagliato = 0;
		} else {
			rigoreSbagliato = Integer.parseInt(currentRigoreSbagliato);
		}
		if (currentAutorete.contains("-")) {
			autorete = 0;
		} else {
			autorete = Integer.parseInt(currentAutorete);
		}
		if (currentAssist.contains("-")) {
			assist = 0;
		} else {
			assist = Integer.parseInt(currentAssist);
		}

		if (currentGiocatoreDB == null) {
			log.warn("Giocatore [" + currentGiocatoreNome + "] [" + currentNomeSquadra + "] [" + currentGiocatoreRuolo + "] NON presente nel DB. Procedo con il suo inserimento");
			giocatoriEJB.insertOrUpdateGiocatore(currentNomeSquadra, currentGiocatoreNome, currentGiocatoreRuolo, currentStagione, noLike);
			currentGiocatoreDB = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(currentGiocatoreNome, currentNomeSquadra, currentGiocatoreRuolo, currentStagione, noLike);
		}
		log.info("Inserisco statistiche giocatore [" + currentGiocatoreNome + "] [" + currentNomeSquadra + "] [" + currentGiocatoreRuolo + "]");
		StatisticheId statisticheId = new StatisticheId();
		statisticheId.setIdGiocatore(currentGiocatoreDB.getId());
		statisticheId.setIdGiornata(idGiornata);
		statisticheId.setAmmonizioni(ammonito);
		statisticheId.setEspulsioni(espulso);
		statisticheId.setAssist(assist);
		statisticheId.setAutoreti(autorete);
		statisticheId.setGoalFatti(goalFatto);
		statisticheId.setGoalRigore(goalSuRigore);
		statisticheId.setGoalSubiti(goalSubito);
		statisticheId.setMediaVoto(mediaVoto);
		BigDecimal currentMediaFantaVoto = calcolaFantaVoto(mediaVoto, ammonito, espulso, assist, autorete, goalFatto, goalSuRigore, rigoreSbagliato, goalSubito, rigoreParato);
		statisticheId.setMediaVotoFm(currentMediaFantaVoto);
		statisticheId.setRigoriParati(rigoreParato);
		statisticheId.setRigoriSbagliati(rigoreSbagliato);

		Statistiche statisticheToInsert = new Statistiche();
		statisticheToInsert.setId(statisticheId);
		dbManager.persist(statisticheToInsert);
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

	@Override
	public Statistiche getStatisticheIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
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
