package it.zeze.fanta.service.ejb;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.bean.SelectItem;
import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.GiornateRemote;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fantaformazioneweb.entity.Calendario;
import it.zeze.fantaformazioneweb.entity.CalendarioId;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;
import it.zeze.util.DateUtil;

@Stateless
@LocalBean
public class GiornateEJB implements GiornateLocal, GiornateRemote {
	
	private static final String SELECT_BY_NUMERO_GIORNATA_STAGIONE = "SELECT g.id FROM Giornate g WHERE g.numeroGiornata = :numeroGiornata AND g.stagione = :stagione";
	private static final String SELECT_BY_DATA = "SELECT g.id FROM Giornate g WHERE g.data = :data";
	private static final String SELECT_BY_ID = "SELECT g FROM Giornate g WHERE g.id = :idGiornata";
	private static final String ORDER_BY_ID = "SELECT g FROM Giornate g ORDER BY g.id DESC";
	private static final String SELECT_BY_STAGIONE = "SELECT g FROM Giornate g WHERE g.stagione = :stagione ORDER BY g.id DESC";
	private final static String SELECT_STAGIONI = "select g.stagione from Giornate g group by g.stagione order by g.stagione desc";

	private static final Logger log = LogManager.getLogger(GiornateEJB.class);

	@EJB(name = "CalendarioEJB")
	private CalendarioLocal calendarioEJB;
	
	@EJB(name = "SquadreEJB")
	private SquadreLocal squadreEJB;

	@EJB(name = "DBManager")
	private DBManager dbManager;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		log.info("unmarshallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String fileNameCalendario = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_CALENDARIO);
		String fileHTMLPath = rootHTMLFiles + fileNameCalendario;
		try {
			File fileCalendario = new File(fileHTMLPath);
			if (fileCalendario.exists()) {
				String nomeStagione = null;
				List<TagNode> listNomeStagione = HtmlCleanerUtil.getListOfElementsByXPathFromFile(fileHTMLPath, "//div[@id='stats']/h1");
				if (listNomeStagione == null || listNomeStagione.isEmpty()) {
					throw new ParseException("Nome stagione NON trovato", 0);
				} else {
					nomeStagione = listNomeStagione.get(0).getText().toString();
					nomeStagione = StringUtils.substringAfter(nomeStagione.toLowerCase(), "Calendario Serie A ".toLowerCase());
				}

				List<TagNode> listNodeGiornate = HtmlCleanerUtil.getListOfElementsByXPathFromFile(fileHTMLPath, "//div[@id='stats']/div[@class='calendar']/table");
				TagNode currentNodeGiornata;
				int currentIdGiornata;
				int currentNumeroGiornata;
				log.info("Salvo giornate per la stagione [" + nomeStagione + "]");
				for (int i = 0; i < listNodeGiornate.size(); i++) {
					currentNodeGiornata = listNodeGiornate.get(i);
					currentNumeroGiornata = i + 1;
					currentIdGiornata = getIdGiornata(currentNumeroGiornata, nomeStagione);
					if (currentIdGiornata != -1) {
						log.info("Stagione [" + currentNumeroGiornata + "] stagione [" + nomeStagione + "] gia' inserita");
					} else {
						currentIdGiornata = salvaGiornate(currentNodeGiornata, i + 1, nomeStagione);
					}
					log.info("currentIdGiornata " + currentIdGiornata);
					calendarioEJB.unmarshallAndSaveFromNodeCalendario(currentIdGiornata, currentNodeGiornata);
				}
			} else {
				// Nuovo HTML
				fileNameCalendario = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_CALENDARIO_NEW);
				String wildCard = StringUtils.replace(fileNameCalendario, Constants.STRING_TO_REPLACE_NOME_FILE_CALENDARIO_NEW, "*");
				Iterator<File> itFile = FileUtils.iterateFiles(new File(rootHTMLFiles), new WildcardFileFilter(wildCard), null);
				File currentGiornataFile;
				String currentNumeroGiornata;
				TagNode currentGiornataTag;
				while (itFile.hasNext()) {
					currentGiornataFile = itFile.next();
					currentNumeroGiornata = StringUtils.substringBetween(currentGiornataFile.getName(), "_", "_");
					currentGiornataTag = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromFile(currentGiornataFile.getAbsolutePath(), "//div[@id='artContainer']").get(0);

					salvaGiornataNew(currentGiornataTag, Integer.valueOf(currentNumeroGiornata), stagione);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
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
	public String getStagione(String stagioneInput) {
		String toReturn = stagioneInput;
		if (stagioneInput.length() > 7) {
			String temp = stagioneInput;
			temp = StringUtils.deleteWhitespace(stagioneInput);
			String toReplace = StringUtils.substringAfterLast(temp, "/");
			if (toReplace.length() > 2) {
				temp = StringUtils.substring(toReplace, 2);
			}
			toReturn = StringUtils.replace(toReturn, toReplace, temp);
		}
		return toReturn;
	}

	@Override
	public int getIdGiornata(int numeroGiornata, String stagione) {
		int idGiornata = -1;
		Query query = dbManager.getEm().createQuery(SELECT_BY_NUMERO_GIORNATA_STAGIONE);
		query.setParameter("numeroGiornata", numeroGiornata);
		query.setParameter("stagione", stagione);
		try {
			idGiornata = (Integer) query.getSingleResult();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con numeroGiornata [" + numeroGiornata + "] e stagione [" + stagione + "]");
		}
		return idGiornata;
	}

	@Override
	public Giornate getGiornataById(int idGiornata) {
		Giornate toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID);
		query.setParameter("idGiornata", idGiornata);
		try {
			toReturn = (Giornate) query.getSingleResult();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con idGiornata [" + idGiornata + "]");
		}
		return toReturn;
	}

	@Override
	public int getIdGiornata(String dataString) {
		int idGiornata = -1;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date data = formatter.parse(dataString);
			Query query = dbManager.getEm().createQuery(SELECT_BY_DATA);
			query.setParameter("data", data);

			idGiornata = (Integer) query.getSingleResult();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato con data [" + dataString + "]");
		} catch (ParseException e) {
			log.error("Errore parsing [" + dataString + "]", e);
		}
		return idGiornata;
	}

	@Override
	public Giornate getGiornata(String dataString) {
		Giornate toReturn = null;
		int idGiornata = getIdGiornata(dataString);
		if (idGiornata != -1) {
			toReturn = getGiornataById(idGiornata);
		}
		return toReturn;
	}

	@Override
	public Giornate getLastGiornata() {
		Giornate toReturn = null;
		Query query = dbManager.getEm().createQuery(ORDER_BY_ID);
		try {
			List<Giornate> resultList = (List<Giornate>) query.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				toReturn = resultList.get(0);
			}
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato per giornate");
		}
		return toReturn;
	}

	@Override
	public List<Giornate> getGiornateByStagione(String stagione) {
		List<Giornate> toReturn = null;
		stagione = getStagione(stagione);
		Query query = dbManager.getEm().createQuery(SELECT_BY_STAGIONE);
		query.setParameter("stagione", stagione);
		try {
			toReturn = (List<Giornate>) query.getResultList();
		} catch (NoResultException e) {
			log.error("Nessun risultato tovato per giornate");
		}
		return toReturn;
	}

	private int salvaGiornate(TagNode calendarNode, int numeroGiornata, String nomeStagione) throws IOException, XPatherException, ParseException {
		int idGiornataInseritoToReturn = -1;
		String divisoreData = "/";
		String patternData = "dd" + divisoreData + "MM" + divisoreData + "yyyy";
		List<TagNode> listNodeGiornate = HtmlCleanerUtil.getListOfElementsByXPathFromElement(calendarNode, "/thead/tr/th/h3[@class='ra']");
		String currentStringGiornata;
		int indexOf;
		String currentStringData;
		Date currentDateParsed;
		for (int i = 0; i < listNodeGiornate.size(); i++) {
			currentStringGiornata = listNodeGiornate.get(i).getText().toString();
			indexOf = StringUtils.indexOf(currentStringGiornata, divisoreData);
			currentStringData = StringUtils.substring(currentStringGiornata, indexOf - 2, indexOf + (patternData.length() - 2));
			currentDateParsed = DateUtil.getDateWithPatternFromString(currentStringData, patternData);
			Giornate toInsert = new Giornate();
			toInsert.setNumeroGiornata(numeroGiornata);
			toInsert.setStagione(nomeStagione);
			toInsert.setData(currentDateParsed);
			dbManager.persist(toInsert);
			toInsert = getGiornate(numeroGiornata, nomeStagione);
			idGiornataInseritoToReturn = toInsert.getId();
		}
		return idGiornataInseritoToReturn;
	}
	
	public Giornate getGiornate(int numeroGiornata, String stagione){
		Giornate toReturn = null;
		String qryString = "SELECT g FROM Giornate g WHERE g.numeroGiornata = :numeroGiornata AND g.stagione = :stagione";
		Query query = dbManager.getEm().createQuery(qryString);
		query.setParameter("numeroGiornata", numeroGiornata);
		query.setParameter("stagione", stagione);
		toReturn = (Giornate) query.getSingleResult();
		return toReturn;
	}
	
	public List<Giornate> getGiornateAll(String stagione){
		List<Giornate> toReturn = new ArrayList<Giornate>();
		String qryString = "SELECT g FROM Giornate g WHERE g.stagione = :stagione order by g.numeroGiornata";
		Query query = dbManager.getEm().createQuery(qryString);
		query.setParameter("stagione", stagione);
		toReturn = (List<Giornate>) query.getResultList();
		return toReturn;
	}

	@Override
	public List<SelectItem> getStagioniAll() {
		List<SelectItem> toReturn = new ArrayList<SelectItem>();
		Query query = dbManager.getEm().createQuery(SELECT_STAGIONI);
		List<String> resultList = query.getResultList();
		String currentStagione;
		for (int i = 0; i < resultList.size(); i++) {
			currentStagione = resultList.get(i);
			toReturn.add(new SelectItem(currentStagione, currentStagione));
		}
		return toReturn;
	}
	
	private int salvaGiornataNew(TagNode calendarNode, int numeroGiornata, String nomeStagione) throws IOException, XPatherException, ParseException, XPathExpressionException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		int idGiornataInseritoToReturn = -1;
		List<TagNode> listDivGiornateNode = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(calendarNode, "//div[contains(@class,'row')][2]/div");
		List<TagNode> currentListDate;
		List<TagNode> currentListSquadre;
		List<TagNode> currentListsquadraCasa = null;
		List<TagNode> currentListsquadraFuori = null;
		String dataPartita;
		String patternData = "dd MMM";
		Date dataParsed = null;
		String squadraCasa;
		int idSquadraCasa;
		String squadraFuori;
		int idSquadraFuori;
		for (TagNode currentDiv : listDivGiornateNode) {
			currentListDate = null;
			currentListSquadre = null;

			currentListDate = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentDiv, "//div/span");
			currentListSquadre = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(currentDiv, "//div[contains(@class,'tablefk')]");
			if (currentListDate != null && !currentListDate.isEmpty()) {
				// Data giornata
				dataPartita = currentListDate.get(0).getText().toString();

				dataParsed = DateUtil.getDateWithPatternFromString(dataPartita, patternData, Locale.ITALIAN);
				dataParsed = DateUtil.getDateAnnoStagione(dataParsed, nomeStagione);
				log.info("Data [" + dataPartita + "] = " + DateUtil.getDateAsString(dataParsed, "dd/MM/YYYY"));
			}
			if (currentListSquadre != null && !currentListSquadre.isEmpty()) {
				// Partita con squadre
				currentListsquadraCasa = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(currentListSquadre.get(0), "//div[contains(@class,'ui-match-up')]//div[contains(@class,'left')]//h3[contains(@class,'team-name')]");
				currentListsquadraFuori = HtmlCleanerUtil.getListOfElementsByXPathSpecialFromElement(currentListSquadre.get(0), "//div[contains(@class,'ui-match-up')]//div[contains(@class,'right')]//h3[contains(@class,'team-name')]");
				for (int i = 0; i < currentListsquadraCasa.size(); i++) {
					log.info(currentListsquadraCasa.get(i).getText() + " - " + currentListsquadraFuori.get(i).getText());
				}
			}

			log.info("Salvo giornate per la stagione [" + nomeStagione + "]");
			idGiornataInseritoToReturn = getIdGiornata(numeroGiornata, nomeStagione);
			if (idGiornataInseritoToReturn != -1) {
				log.info("Stagione [" + numeroGiornata + "] stagione [" + nomeStagione + "] gia' inserita");
			} else {
				Giornate toInsert = new Giornate();
				toInsert.setNumeroGiornata(numeroGiornata);
				toInsert.setStagione(nomeStagione);
				toInsert.setData(dataParsed);
				dbManager.persist(toInsert);
				toInsert = getGiornate(numeroGiornata, nomeStagione);
				idGiornataInseritoToReturn = toInsert.getId();
			}
			log.info("currentIdGiornata " + idGiornataInseritoToReturn);
			for (int i = 0; i < currentListsquadraCasa.size(); i++) {
				squadraCasa = currentListsquadraCasa.get(i).getText().toString();
				squadraFuori = currentListsquadraFuori.get(i).getText().toString();
				idSquadraCasa = squadreEJB.getSquadraFromMapByNome(squadraCasa).getId();
				idSquadraFuori = squadreEJB.getSquadraFromMapByNome(squadraFuori).getId();
				Calendario calendarioToInsert = new Calendario();
				calendarioToInsert.setId(new CalendarioId(idGiornataInseritoToReturn, idSquadraCasa, idSquadraFuori));
				dbManager.persist(calendarioToInsert);
			}
		}
		return idGiornataInseritoToReturn;
	}
}
