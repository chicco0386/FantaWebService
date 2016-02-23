package it.zeze.fanta.service.ejb;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.GiornateRemote;
import it.zeze.fanta.service.rest.CalendarioRESTImpl;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.html.cleaner.HtmlCleanerUtil;
import it.zeze.util.ConfigurationUtil;
import it.zeze.util.Constants;

@Stateless
@LocalBean
public class GiornateEJB implements GiornateLocal, GiornateRemote {
	
	private static final Logger log = LogManager.getLogger(CalendarioRESTImpl.class);
	
	@EJB(name = "CalendarioEJB")
	private CalendarioLocal calendarioEJB;
	
	@PersistenceContext(unitName = "FantaFormazioneService")
	EntityManager em;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("unmarshallAndSaveFromHtmlFile, uscito");
	}

	@Override
	public String getStagione(String stagioneInput) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdGiornata(int numeroGiornata, String stagione) {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Giornate> getGiornateByStagione(String stagione) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private int salvaGiornate(TagNode calendarNode, int numeroGiornata, String nomeStagione) throws IOException, XPatherException, ParseException {
		int idGiornataInseritoToReturn = -1;
//		String divisoreData = "/";
//		String patternData = "dd" + divisoreData + "MM" + divisoreData + "yyyy";
//		List<TagNode> listNodeGiornate = HtmlCleanerUtil.getListOfElementsByXPathFromElement(calendarNode, "/thead/tr/th/h3[@class='ra']");
//		String currentStringGiornata;
//		int indexOf;
//		String currentStringData;
//		Date currentDateParsed;
//		for (int i = 0; i < listNodeGiornate.size(); i++) {
//			currentStringGiornata = listNodeGiornate.get(i).getText().toString();
//			indexOf = StringUtils.indexOf(currentStringGiornata, divisoreData);
//			currentStringData = StringUtils.substring(currentStringGiornata, indexOf - 2, indexOf + (patternData.length() - 2));
//			currentDateParsed = DateUtil.getDateWithPatternFromString(currentStringData, patternData);
//			giornateHome.clearInstance();
//			giornateHome.getInstance().setNumeroGiornata(numeroGiornata);
//			giornateHome.getInstance().setStagione(nomeStagione);
//			giornateHome.getInstance().setData(currentDateParsed);
//			giornateHome.persist();
//			idGiornataInseritoToReturn = giornateHome.getInstance().getId();
//		}
		return idGiornataInseritoToReturn;
	}
}
