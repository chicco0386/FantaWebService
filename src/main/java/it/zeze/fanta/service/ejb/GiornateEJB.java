package it.zeze.fanta.service.ejb;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
import it.zeze.fanta.service.bean.SelectItem;
import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.GiornateRemote;
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

	@EJB(name = "DBManager")
	private DBManager dbManager;

	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		log.info("unmarshallAndSaveFromHtmlFile, entrato");
		String rootHTMLFiles = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_ROOT);
		String fileNameCalendario = ConfigurationUtil.getValue(Constants.CONF_KEY_HTML_FILE_CALENDARIO);
		String fileHTMLPath = rootHTMLFiles + fileNameCalendario;
		try {
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
		} catch (IOException e) {
			log.error(e);
		} catch (XPatherException e) {
			log.error(e);
		} catch (ParseException e) {
			log.error(e);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdGiornata(String dataString) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Giornate getGiornata(String dataString) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
	
	public List<Giornate> getGiornateAll(){
		List<Giornate> toReturn = new ArrayList<Giornate>();
		String qryString = "SELECT g FROM Giornate g";
		Query query = dbManager.getEm().createQuery(qryString);
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
}
