package it.zeze.fanta.service.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.SquadreInterface;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fantaformazioneweb.entity.Squadre;

@Path("/squadreRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class SquadreRESTImpl implements SquadreInterface {

	private static final Logger logger = LogManager.getLogger(SquadreRESTImpl.class);
	
	@EJB(name = "DBManager")
	private DBManager dbManager;
	
	@EJB(name = "SquadreEJB")
	private SquadreLocal squadreEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initMappaSquadre() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Squadre getSquadraFromMapByNome(String nomeSquadraToSearch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Squadre getSquadraByNome(String nomeSquadraToSearch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Squadre getSquadraById(int idSquadra) {
		// TODO Auto-generated method stub
		return null;
	}

}
