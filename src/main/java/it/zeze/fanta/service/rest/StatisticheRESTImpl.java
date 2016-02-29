package it.zeze.fanta.service.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.StatisticheInterface;
import it.zeze.fanta.service.ejb.StatisticheEJB;

@Path("/statisticheRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class StatisticheRESTImpl implements StatisticheInterface {

	private static final Logger logger = LogManager.getLogger(StatisticheRESTImpl.class);

	@EJB(name = "StatisticheEJB")
	private StatisticheEJB statisticheEJB;

	@Override
	@GET
	@Path("/unmarshallAndSaveFromHtmlFile")
	public void unmarshallAndSaveFromHtmlFile() {
		statisticheEJB.unmarshallAndSaveFromHtmlFile();
	}

}
