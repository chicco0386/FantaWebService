package it.zeze.fanta.service.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import it.zeze.fanta.ejb.util.JNDIUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.StatisticheInterface;
import it.zeze.fanta.service.definition.ejb.StatisticheLocal;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.Statistiche;

@Path("/statisticheRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StatisticheRESTImpl implements StatisticheInterface {

	private static final Logger logger = LogManager.getLogger(StatisticheRESTImpl.class);

	private StatisticheLocal statisticheEJB;

	{
		try {
			statisticheEJB = JNDIUtils.getStatisticheEJB();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Override
	@GET
	@Path("/unmarshallAndSaveFromHtmlFile")
	public void unmarshallAndSaveFromHtmlFile(@QueryParam("stagione") String stagione) {
		statisticheEJB.unmarshallAndSaveFromHtmlFile(stagione);
	}

	@Override
	public Statistiche getStatisticheIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		return statisticheEJB.getStatisticheIdGiocatoreIdGiornata(idGiocatore, idGiornata);
	}

	@Override
	public List<Statistiche> initResultList(Giornate giornate, Giocatori giocatori, String orderColumn, String orderDir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Statistiche> resetResumeStatistiche(List<Statistiche> resultList, Giornate giornate, Giocatori giocatori, String orderColumn, String orderDir) {
		// TODO Auto-generated method stub
		return null;
	}

}
