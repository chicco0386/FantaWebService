package it.zeze.fanta.service.ejb;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

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
import it.zeze.fantaformazioneweb.entity.Squadre;
import it.zeze.html.cleaner.HtmlCleanerUtil;

@Stateless
@LocalBean
public class CalendarioEJB implements CalendarioLocal, CalendarioRemote {
	
	private static final Logger log = LogManager.getLogger(CalendarioEJB.class);
	
	private static final String GET_CALENDARIO_BY_ID_GIOR_ID_SQUADRA = "select calendario from Calendario calendario where calendario.id.idGiornata = :idGiornata AND (calendario.id.idSquadraCasa = :idSquadra OR calendario.id.idSquadraFuoriCasa = :idSquadra)";
	
	@EJB(name = "DBManager")
	private DBManager dbManager;
	
	@EJB(name="SquadreEJB")
	private SquadreLocal squadreEJB;
	
	@EJB(name="GiornateEJB")
	private GiornateLocal giornateEJB;

	@Override
	public void inizializzaCalendario(String stagione) {
		squadreEJB.unmarshallAndSaveFromHtmlFile();
		giornateEJB.unmarshallAndSaveFromHtmlFile(stagione);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("unmarshallAndSaveFromHtmlFile, uscito");
	}

	@Override
	public Calendario getCalendarioByIdGiornataIdSquadra(int idGiornata, int idSquadra) {
		Calendario toReturn = null;
		EntityManager em = dbManager.getEm();
		Query query = em.createQuery(GET_CALENDARIO_BY_ID_GIOR_ID_SQUADRA);
		query.setParameter("idGiornata", idGiornata);
		query.setParameter("idSquadra", idSquadra);
		try {
			toReturn = (Calendario) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		return toReturn;
	}

	@Override
	public String getNomeSquadraAvversaria(int idGiornata, int idSquadra) {
		String nomeSquadraAvv = "";
		try {
			Calendario calendarioReturn = getCalendarioByIdGiornataIdSquadra(idGiornata, idSquadra);
			if (calendarioReturn != null) {
				int idSquadraToSearch = 0;
				if (calendarioReturn.getId().getIdSquadraCasa() != idSquadra) {
					idSquadraToSearch = calendarioReturn.getId().getIdSquadraCasa();
				} else if (calendarioReturn.getId().getIdSquadraFuoriCasa() != idSquadra) {
					idSquadraToSearch = calendarioReturn.getId().getIdSquadraFuoriCasa();
				}
				if (idSquadraToSearch > 0) {
					Squadre squadra = squadreEJB.getSquadraById(idSquadraToSearch);
					nomeSquadraAvv = squadra.getNome();
				} else {
					log.warn("idSquadra NON trovato");
				}
			}
		} catch (NoResultException e) {
			return null;
		}
		return nomeSquadraAvv;
	}

	@Override
	public boolean isSquadraFuoriCasa(int idGiornata, int idSquadra) {
		boolean isCasa = true;
		Calendario calendarioReturn = getCalendarioByIdGiornataIdSquadra(idGiornata, idSquadra);
		if (calendarioReturn != null) {
			if (calendarioReturn.getId().getIdSquadraCasa() == idSquadra) {
				isCasa = true;
			} else if (calendarioReturn.getId().getIdSquadraFuoriCasa() == idSquadra) {
				isCasa = false;
			}
		}
		return isCasa;
	}

}
