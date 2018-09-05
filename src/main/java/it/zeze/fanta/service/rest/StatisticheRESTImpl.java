package it.zeze.fanta.service.rest;

import it.zeze.fanta.ejb.util.JNDIUtils;
import it.zeze.fanta.service.definition.ejb.proxy.seam.StatisticheSeamRemote;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.Statistiche;
import it.zeze.fantaformazioneweb.entity.wrapper.StatisticheWrap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/statisticheRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StatisticheRESTImpl implements StatisticheSeamRemote {

	private static final Logger logger = LogManager.getLogger(StatisticheRESTImpl.class);

	private StatisticheSeamRemote statisticheEJB;

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
	public StatisticheWrap getStatisticheIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		return statisticheEJB.getStatisticheIdGiocatoreIdGiornata(idGiocatore, idGiornata);
	}

	@Override
	public List<StatisticheWrap> initResultList(Giornate giornate, Giocatori giocatori, String orderColumn, String orderDir) {
		return null;
	}

	@Override
	public List<StatisticheWrap> resetResumeStatistiche(List<Statistiche> resultList, Giornate giornate, Giocatori giocatori, String orderColumn, String orderDir) {
		return null;
	}

	@Override
	@GET
	@Path("/downloadFromSite")
	public void downloadFromSite() {
		statisticheEJB.downloadFromSite();
	}

}
