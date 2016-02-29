package it.zeze.fanta.service.ejb;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fanta.service.definition.ejb.CalendarioRemote;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fantaformazioneweb.entity.Calendario;
import it.zeze.fantaformazioneweb.entity.CalendarioId;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.html.cleaner.HtmlCleanerUtil;

@Stateless
@LocalBean
public class CalendarioEJB implements CalendarioLocal, CalendarioRemote{
	
	private static final Logger log = LogManager.getLogger(CalendarioEJB.class);
	
	@EJB(name = "DBManager")
	private DBManager dbManager;
	
	@EJB(name="SquadreEJB")
	private SquadreLocal squadreEJB;
	
	@EJB(name="GiornateEJB")
	private GiornateLocal giornateEJB;

	@Override
	public void inizializzaCalendario() {
		squadreEJB.unmarshallAndSaveFromHtmlFile();
		giornateEJB.unmarshallAndSaveFromHtmlFile();
	}

	@Override
	public Giornate getGiornate() {
		return (Giornate) dbManager.getEm().createQuery("SELECT g FROM Giornate g").getResultList().get(0);
	}

	@Override
	public void unmarshallAndSaveFromNodeCalendario(int idGiornata, TagNode calendarNode) {
		log.info("unmarshallAndSaveFromHtmlFile, entrato");
		try {
			squadreEJB.initMappaSquadre();
			List<TagNode> listNodeGiornate = HtmlCleanerUtil.getListOfElementsByXPathFromElement(calendarNode, "/tbody/tr/td");
			TagNode currentNodePartita;
			List<TagNode> listSquadrePartita;
			String squadraCasa;
			int idSquadraCasa;
			String squadraFuori;
			int idSquadraFuori;
			for (int i = 0; i < listNodeGiornate.size(); i++) {
				currentNodePartita = listNodeGiornate.get(i);
				listSquadrePartita = HtmlCleanerUtil.getListOfElementsByXPathFromElement(currentNodePartita, "/a");
				if (!listSquadrePartita.isEmpty()) {
					squadraCasa = listSquadrePartita.get(0).getText().toString();
					squadraFuori = listSquadrePartita.get(1).getText().toString();
					idSquadraCasa = squadreEJB.getSquadraFromMapByNome(squadraCasa).getId();
					idSquadraFuori = squadreEJB.getSquadraFromMapByNome(squadraFuori).getId();
					Calendario calendarioToInsert = new Calendario();
					calendarioToInsert.setId(new CalendarioId(idGiornata, idSquadraCasa, idSquadraFuori));
					dbManager.persist(calendarioToInsert);
				}
			}
		} catch (IOException e) {
			log.error(e);
		} catch (XPatherException e) {
			log.error(e);
		}
		log.info("unmarshallAndSaveFromHtmlFile, uscito");
		
	}

	@Override
	public Calendario getCalendarioByIdGiornataIdSquadra(int idGiornata, int idSquadra) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNomeSquadraAvversaria(int idGiornata, int idSquadra) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSquadraFuoriCasa(int idGiornata, int idSquadra) {
		// TODO Auto-generated method stub
		return false;
	}

}
